package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;

import java.util.HashMap;
import java.util.UUID;

public class PlayerLogoutEvent implements Listener {

  private static HashMap<UUID, BukkitTask> playerLogOutTasks = new HashMap<>();

  @EventHandler
  public void logout(PlayerQuitEvent e){
    Player p = e.getPlayer();
	McMMOPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
	BukkitTask task = Bukkit.getScheduler().runTaskLater(Mcmmox.getInstance(), ()->{
		if(p.isOnline() && PlayerManager.isPlayerStored(p.getUniqueId())){
		  PlayerManager.removePlayer(p.getUniqueId());
		}
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
