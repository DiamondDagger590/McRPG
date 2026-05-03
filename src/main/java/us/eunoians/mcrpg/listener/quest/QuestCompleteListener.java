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
        questInstance.grantRewards(event.getQuestDefinition().getRewards());

        event.getQuestDefinition().getRewardDistribution().ifPresent(config -> {
            Map<UUID, Long> contributions = contributionAggregator.fromQuest(questInstance);
            Set<UUID> groupMembers = questInstance.getQuestScope()
                    .map(scope -> scope.getCurrentPlayersInScope())
                    .orElse(Set.of());
            distributionService.resolveAndGrant(config, contributions, groupMembers, questInstance);
        });

        logCompletionForAllScopePlayers(questInstance);
        terminator.releaseBoardSlot(questInstance, "COMPLETED");
        terminator.decrementBoardCount(questInstance);

        QuestManager questManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);
        questManager.retireQuest(questInstance);

        terminator.deregisterEphemeralDefinition(questInstance.getQuestKey());
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
            long completedAt = McRPG.getInstance().getTimeProvider().now().toEpochMilli();

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
