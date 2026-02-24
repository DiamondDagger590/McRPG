package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.database.Database;
import us.eunoians.mcrpg.McRPG;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.database.table.quest.QuestCompletionLogDAO;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
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

        logCompletionForAllScopePlayers(questInstance);

        QuestManager questManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);
        questManager.retireQuest(questInstance);
    }

    /**
     * Asynchronously logs a completion entry for every player currently in scope
     * of the completed quest. This data is used to enforce repeat mode restrictions.
     */
    private void logCompletionForAllScopePlayers(@NotNull QuestInstance questInstance) {
        questInstance.getQuestScope().ifPresent(scope -> {
            String definitionKey = questInstance.getQuestKey().toString();
            UUID questUUID = questInstance.getQuestUUID();
            long completedAt = McRPG.getInstance().getTimeProvider().now().toEpochMilli();

            Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.DATABASE).getDatabase();
            database.getDatabaseExecutorService().submit(() -> {
                try (Connection connection = database.getConnection()) {
                    for (UUID playerUUID : scope.getCurrentPlayersInScope()) {
                        QuestCompletionLogDAO.logCompletion(connection, playerUUID, definitionKey, questUUID, completedAt);
                    }
                } catch (SQLException e) {
                    us.eunoians.mcrpg.McRPG.getInstance().getLogger().log(Level.SEVERE,
                            "Failed to log quest completion for " + questInstance.getQuestUUID(), e);
                }
            });
        });
    }
}
