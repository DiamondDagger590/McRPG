package us.eunoians.mcrpg.listener.entity.player;

import com.diamonddagger590.mccore.player.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.task.McRPGPlayerUnloadTask;

/**
 * This listener will manage unloading player data
 */
public class PlayerLeaveListener implements Listener {

    @EventHandler
    public void handleQuit(PlayerQuitEvent playerQuitEvent) {
        PlayerManager playerManager = McRPG.getInstance().getPlayerManager();
        Player player = playerQuitEvent.getPlayer();

        if (playerManager.getPlayer(player.getUniqueId()).isPresent() && playerManager.getPlayer(player.getUniqueId()).get() instanceof McRPGPlayer mcRPGPlayer) {
            new McRPGPlayerUnloadTask(McRPG.getInstance(), mcRPGPlayer).runTask();
        }
    }
}
