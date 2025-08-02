package us.eunoians.mcrpg.listener.entity.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.task.player.McRPGPlayerLoadTask;

/**
 * Starts the {@link McRPGPlayerLoadTask} to load in the player
 */
public class PlayerJoinListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void handleJoin(@NotNull PlayerJoinEvent playerJoinEvent) {

        McRPG mcRPG = McRPG.getInstance();
        Player player = playerJoinEvent.getPlayer();
        McRPGPlayer mcRPGPlayer = new McRPGPlayer(player, mcRPG);

        new McRPGPlayerLoadTask(mcRPG, mcRPGPlayer).runTask();
    }
}
