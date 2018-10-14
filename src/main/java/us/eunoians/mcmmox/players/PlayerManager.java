package us.eunoians.mcmmox.players;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PlayerManager {

  //Players who are currently logged on
  private static HashMap<UUID, McMMOPlayer> players = new HashMap<>();
  private static ArrayList<UUID> playersFrozen = new ArrayList<UUID>();
  private static Mcmmox plugin;
  private static BukkitTask saveTask;

  public static void addMcMMOPlayer(Player player, boolean freeze){
	if(players.containsKey(player.getUniqueId())){
	  return;
	}
	UUID uuid = player.getUniqueId();
	if(freeze){
	  playersFrozen.add(uuid);
	}
	BukkitTask task = new BukkitRunnable() {
	  public void run(){
		McMMOPlayer mp = new McMMOPlayer(uuid);
		if(mp.isOnline()){
		  players.put(uuid, mp);
		}
		playersFrozen.remove(uuid);
	  }
	}.runTaskAsynchronously(plugin);
  }

  public static boolean isPlayerFrozen(UUID uuid){
	return playersFrozen.contains(uuid);
  }

  public static McMMOPlayer getPlayer(UUID uuid){
	return players.get(uuid);
  }

  public static boolean isPlayerStored(UUID uuid){
	return players.containsKey(uuid);
  }

  public static void removePlayer(UUID uuid){
	players.remove(uuid).saveData();
  }

  public static void startSave(Plugin p){
	plugin = (Mcmmox) p;
	if(saveTask != null){
	  System.out.println(Methods.color(plugin.getPluginPrefix() + "&eRestarting player saving task...."));
	  saveTask.cancel();
	}
	saveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(p, PlayerManager::run, 500, ((Mcmmox) p).getFileManager().getFile(FileManager.Files.CONFIG).getInt("Configuration.SaveInterval") * 1200);
	System.out.println(Methods.color(plugin.getPluginPrefix() + "&aPlayer saving task has been started!"));
	Bukkit.getScheduler().runTaskTimer(Mcmmox.getInstance(), () ->{
	  for(McMMOPlayer mp : players.values()){
	    mp.updateCooldowns();
	}
	}, 0, 20);
  }


  private static void run(){
	players.values().stream().forEach(player -> player.saveData());
  }

  public static void saveAll(){run();}

  public static void shutDownManager(){
    saveAll();
    if(saveTask != null){
      saveTask.cancel();
	}
  }
}
