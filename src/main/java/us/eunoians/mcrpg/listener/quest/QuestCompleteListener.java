package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import us.eunoians.mcrpg.McRPG;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.database.table.quest.QuestCompletionLogDAO;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.board.QuestBoardTerminator;
import us.eunoians.mcrpg.quest.board.distribution.DistributionCompletionService;
import us.eunoians.mcrpg.quest.board.distribution.QuestContributionAggregator;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Listens for {@link QuestCompleteEvent}, grants quest-level rewards, logs the
 * completion for all in-scope players, and retires the quest from Tier 1 (active)
 * to Tier 2 (cache).
 * <p>
 * Runs at {@link EventPriority#MONITOR} so external listeners can react first.
 */
public class QuestCompleteListener implements Listener {

    private final QuestBoardTerminator terminator;
    private final DistributionCompletionService distributionService;
    private final QuestContributionAggregator contributionAggregator;

    public QuestCompleteListener(@NotNull QuestBoardTerminator terminator,
                                 @NotNull DistributionCompletionService distributionService,
                                 @NotNull QuestContributionAggregator contributionAggregator) {
        this.terminator = terminator;
        this.distributionService = distributionService;
        this.contributionAggregator = contributionAggregator;
    }

    /**
     * Handles quest completion: grants the quest-level rewards defined in the quest
     * definition, logs the completion for all in-scope players, and moves the quest
     * from the active map to the finished cache.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestComplete(@NotNull QuestCompleteEvent event) {
        QuestInstance questInstance = event.getQuestInstance();

        // State-critical lifecycle first: completion logging (enforces ONCE/LIMITED repeat modes),
        // board-slot release, retirement, and ephemeral-definition cleanup must all run even if a
        // reward grant later throws. Otherwise a single throwing reward type would leave a ONCE quest
        // unlogged (infinitely repeatable), un-retired (a zombie in the active map), and its board slot
        // held forever.
        logCompletionForAllScopePlayers(questInstance);
        terminator.releaseBoardSlot(questInstance, "COMPLETED");
        terminator.decrementBoardCount(questInstance);

        QuestManager questManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);
        questManager.retireQuest(questInstance);

        terminator.deregisterEphemeralDefinition(questInstance.getQuestKey());

        // Reward granting last, isolated. retireQuest only relocates the instance between tiers, so the
        // scope and contribution data read here are still live. Each grant path already isolates
        // individual reward failures; these guards ensure an unexpected throw cannot undo the lifecycle
        // steps above.
        grantQuestRewards(questInstance, event);
        grantDistributionRewards(questInstance, event);
    }

    /**
     * Grants the quest-level rewards, isolating any failure so it cannot abort completion handling.
     *
     * @param questInstance the completed quest instance
     * @param event         the completion event carrying the quest definition
     */
    private void grantQuestRewards(@NotNull QuestInstance questInstance, @NotNull QuestCompleteEvent event) {
        try {
            questInstance.grantRewards(event.getQuestDefinition().getRewards());
        } catch (RuntimeException e) {
            McRPG.getInstance().getLogger().log(Level.SEVERE,
                    "Failed to grant quest rewards for " + questInstance.getQuestUUID()
                            + "; the quest completion has already been finalized.", e);
        }
    }

    /**
     * Resolves and grants distribution rewards for a scoped quest, isolating any failure so it cannot
     * abort completion handling.
     *
     * @param questInstance the completed quest instance
     * @param event         the completion event carrying the quest definition
     */
    private void grantDistributionRewards(@NotNull QuestInstance questInstance, @NotNull QuestCompleteEvent event) {
        event.getQuestDefinition().getRewardDistribution().ifPresent(config -> {
            try {
                Map<UUID, Long> contributions = contributionAggregator.fromQuest(questInstance);
                Set<UUID> groupMembers = questInstance.getQuestScope()
                        .map(scope -> scope.getCurrentPlayersInScope())
                        .orElse(Set.of());
                distributionService.resolveAndGrant(config, contributions, groupMembers, questInstance);
            } catch (RuntimeException e) {
                McRPG.getInstance().getLogger().log(Level.SEVERE,
                        "Failed to grant distribution rewards for " + questInstance.getQuestUUID()
                                + "; the quest completion has already been finalized.", e);
            }
        });
    }

    private void logCompletionForAllScopePlayers(@NotNull QuestInstance questInstance) {
        questInstance.getQuestScope().ifPresent(scope -> {
            var dbManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.DATABASE);
            if (dbManager == null) {
                return;
            }
            String definitionKey = questInstance.getQuestKey().toString();
            UUID questUUID = questInstance.getQuestUUID();
            var completedAt = McRPG.getInstance().getTimeProvider().now();

            Database database = dbManager.getDatabase();
            database.getDatabaseExecutorService().submit(() -> {
                try (Connection connection = database.getConnection()) {
                    for (UUID playerUUID : scope.getCurrentPlayersInScope()) {
                        QuestCompletionLogDAO.logCompletion(connection, playerUUID, definitionKey, questUUID, completedAt);
                    }
                } catch (SQLException e) {
                    McRPG.getInstance().getLogger().log(Level.SEVERE,
                            "Failed to log quest completion for " + questInstance.getQuestUUID(), e);
                }
            });
        });
    }
}
