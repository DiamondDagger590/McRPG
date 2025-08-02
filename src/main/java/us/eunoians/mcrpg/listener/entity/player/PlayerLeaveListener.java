package us.eunoians.mcrpg.listener.entity.player;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.registry.plugin.McRPGPluginHookKey;
import us.eunoians.mcrpg.task.player.McRPGPlayerUnloadTask;

/**
 * This listener will manage unloading player data
 */
public class PlayerLeaveListener implements Listener {

    @EventHandler
    public void handleQuit(PlayerQuitEvent playerQuitEvent) {
        McRPGPlayerManager playerManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
        Player player = playerQuitEvent.getPlayer();

        if (playerManager.getPlayer(player.getUniqueId()).isPresent()) {
            McRPGPlayer mcRPGPlayer = playerManager.getPlayer(player.getUniqueId()).get();
            new McRPGPlayerUnloadTask(McRPG.getInstance(), mcRPGPlayer).runTask();
        }
        McRPG.getInstance().registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(McRPGPluginHookKey.LUNAR_CLIENT)
                .ifPresent(lunarClientHook -> lunarClientHook.clearCooldowns(player.getUniqueId()));
    }
}
