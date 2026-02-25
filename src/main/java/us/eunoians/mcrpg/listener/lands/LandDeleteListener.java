package us.eunoians.mcrpg.listener.lands;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import me.angeschossen.lands.api.events.LandDeleteEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.board.QuestBoardManager;
import us.eunoians.mcrpg.quest.impl.scope.impl.LandQuestScope;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Registered conditionally when the Lands plugin is present. Listens for
 * {@link LandDeleteEvent} (from the Lands API) and delegates to the generic
 * {@link QuestBoardManager#handleScopeEntityRemoval} method.
 */
public class LandDeleteListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLandDelete(@NotNull LandDeleteEvent event) {
        String landName = event.getLand().getName();
        QuestBoardManager boardManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST_BOARD);
        boardManager.handleScopeEntityRemoval(LandQuestScope.LAND_SCOPE_KEY, landName);
    }
}
