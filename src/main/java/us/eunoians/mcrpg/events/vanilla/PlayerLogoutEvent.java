package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;

import java.util.HashMap;
import java.util.UUID;

public class PlayerLogoutEvent implements Listener {

  private static HashMap<UUID, BukkitTask> playerLogOutTasks = new HashMap<>();

  @EventHandler(priority = EventPriority.MONITOR)
  public void logout(PlayerQuitEvent e){
	Player p = e.getPlayer();
	McRPGPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
	if(McRPG.getInstance().getDisplayManager().doesPlayerHaveDisplay(p)){
	  McRPG.getInstance().getDisplayManager().removePlayersDisplay(p);
	}
	if(ShiftToggle.isPlayerCharging(p)){
	  ShiftToggle.removePlayerCharging(p);
	}
	BukkitTask task = Bukkit.getScheduler().runTaskLater(McRPG.getInstance(), () -> {
	  if(!p.isOnline() && PlayerManager.isPlayerStored(p.getUniqueId())){
		PlayerManager.removePlayer(p.getUniqueId());
	  }
	  playerLogOutTasks.remove(p.getUniqueId());
	}, 5 * 1200);
	playerLogOutTasks.put(p.getUniqueId(), task);

  }

  public static boolean hasPlayer(UUID uuid){
	return playerLogOutTasks.containsKey(uuid);
  }

  public static void cancelRemove(UUID uuid){
	playerLogOutTasks.remove(uuid).cancel();
  }
}
