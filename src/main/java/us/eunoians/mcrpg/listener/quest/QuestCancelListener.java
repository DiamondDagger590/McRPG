package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.quest.QuestCancelEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Listens for {@link QuestCancelEvent} and retires the quest from Tier 1 (active)
 * to Tier 2 (cache). This handles both manual cancellations and cancellations
 * triggered by quest expiration.
 * <p>
 * Runs at {@link EventPriority#MONITOR} so external listeners can react first.
 */
public class QuestCancelListener implements Listener {

    /**
     * Handles quest cancellation: moves the quest from the active map to the
     * finished cache and removes it from the player contribution index.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestCancel(@NotNull QuestCancelEvent event) {
        QuestManager questManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);
        questManager.retireQuest(event.getQuestInstance());
    }
}
