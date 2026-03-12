package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.database.Database;
import us.eunoians.mcrpg.McRPG;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.database.table.board.PlayerBoardStateDAO;
import us.eunoians.mcrpg.database.table.quest.QuestCompletionLogDAO;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.board.distribution.ContributionSnapshot;
import us.eunoians.mcrpg.quest.board.distribution.QuestContributionAggregator;
import us.eunoians.mcrpg.quest.board.distribution.QuestRewardDistributionResolver;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionGranter;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionTypeRegistry;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.source.builtin.BoardPersonalQuestSource;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
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
            Map<UUID, Long> contributions = QuestContributionAggregator.fromQuest(questInstance);
            Set<UUID> groupMembers = questInstance.getQuestScope()
                    .map(scope -> scope.getCurrentPlayersInScope())
                    .orElse(Set.of());
            resolveAndGrantDistribution(config, contributions, groupMembers, questInstance);
        });

        logCompletionForAllScopePlayers(questInstance);
        releaseBoardSlot(questInstance, "COMPLETED");
        decrementBoardCount(questInstance);

        QuestManager questManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);
        questManager.retireQuest(questInstance);

        deregisterEphemeralDefinition(questInstance.getQuestKey());
    }

    private void decrementBoardCount(@NotNull QuestInstance questInstance) {
        if (!questInstance.getQuestSource().getKey().equals(BoardPersonalQuestSource.KEY)) {
            return;
        }
        questInstance.getQuestScope().ifPresent(scope -> {
            for (UUID playerUUID : scope.getCurrentPlayersInScope()) {
                RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.PLAYER)
                        .<McRPGPlayer>getPlayer(playerUUID)
                        .ifPresent(p -> p.asQuestHolder().decrementBoardQuestCount());
            }
        });
    }

    private void deregisterEphemeralDefinition(@NotNull NamespacedKey questKey) {
        if (questKey.getKey().startsWith("gen_")) {
            QuestDefinitionRegistry definitionRegistry = RegistryAccess.registryAccess()
                    .registry(McRPGRegistryKey.QUEST_DEFINITION);
            if (definitionRegistry.deregister(questKey)) {
                McRPG.getInstance().getLogger().fine(
                        "Deregistered ephemeral definition " + questKey);
            }
        }
    }

    /**
     * Asynchronously logs a completion entry for every player currently in scope
     * of the completed quest. This data is used to enforce repeat mode restrictions.
     *
     * @param questInstance the completed quest instance
     */
    /**
     * Shared helper that builds a contribution snapshot, resolves distribution rewards,
     * and grants them via the granter.
     */
    static void resolveAndGrantDistribution(@NotNull RewardDistributionConfig config,
                                            @NotNull Map<UUID, Long> contributions,
                                            @NotNull Set<UUID> groupMembers,
                                            @NotNull QuestInstance quest) {
        ContributionSnapshot snapshot = QuestContributionAggregator.toSnapshot(contributions, groupMembers);
        QuestRarityRegistry rarityRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_RARITY);
        RewardDistributionTypeRegistry typeRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.REWARD_DISTRIBUTION_TYPE);
        Map<UUID, List<QuestRewardType>> resolved = QuestRewardDistributionResolver.resolve(
                config, snapshot, quest.getBoardRarityKey().orElse(null), rarityRegistry, typeRegistry);
        RewardDistributionGranter.grant(resolved, quest.getQuestKey());
    }

    /**
     * Updates the player board state from ACCEPTED to a terminal state so the
     * board quest counter is freed up for new quests.
     */
    private void releaseBoardSlot(@NotNull QuestInstance questInstance, @NotNull String newState) {
        var dbManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE);
        if (dbManager == null) {
            return;
        }
        Database database = dbManager.getDatabase();
        UUID questUUID = questInstance.getQuestUUID();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                PlayerBoardStateDAO.updateStateByQuestInstanceUUID(connection, questUUID, newState);
            } catch (Exception e) {
                McRPG.getInstance().getLogger().log(Level.WARNING,
                        "Failed to release board slot for quest " + questUUID, e);
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
