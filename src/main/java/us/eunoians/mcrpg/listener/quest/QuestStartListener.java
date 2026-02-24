package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.quest.QuestStartEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Listens for {@link QuestStartEvent} and adds the quest to the Tier 1 active
 * map and player contribution index via {@link QuestManager#trackActiveQuest}.
 * <p>
 * Runs at {@link EventPriority#MONITOR} so external listeners can react first.
 */
public class QuestStartListener implements Listener {

    /**
     * Handles quest start: registers the quest in the active map and indexes
     * all in-scope players for fast lookup.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestStart(@NotNull QuestStartEvent event) {
        QuestManager questManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);
        questManager.trackActiveQuest(event.getQuestInstance());
    }
}
