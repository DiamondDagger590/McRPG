package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;

import java.util.HashMap;
import java.util.UUID;

public class PlayerLogoutEvent implements Listener {

    private static HashMap<UUID, BukkitTask> playerLogOutTasks = new HashMap<>();

    public static boolean hasPlayer(UUID uuid) {
        return playerLogOutTasks.containsKey(uuid);
    }

    public static void cancelRemove(UUID uuid) {
        playerLogOutTasks.remove(uuid).cancel();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void logout(PlayerQuitEvent playerQuitEvent) {

        Player player = playerQuitEvent.getPlayer();
        McRPGPlayer mp;

        try {
            mp = PlayerManager.getPlayer(player.getUniqueId());
        }
        catch (McRPGPlayerNotFoundException exception) {
            return;
        }

        if (McRPG.getInstance().getDisplayManager().doesPlayerHaveDisplay(player)) {
            McRPG.getInstance().getDisplayManager().removePlayersDisplay(player);
        }
        if (ShiftToggle.isPlayerCharging(player)) {
            ShiftToggle.removePlayerCharging(player);
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() && PlayerManager.isPlayerStored(player.getUniqueId())) {
                    PlayerManager.removePlayer(player.getUniqueId());
                }
                playerLogOutTasks.remove(player.getUniqueId());
            }
        }.runTaskLater(McRPG.getInstance(), 5 * 1200);

        playerLogOutTasks.put(player.getUniqueId(), task);

    }
}
