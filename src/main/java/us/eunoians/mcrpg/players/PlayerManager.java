package us.eunoians.mcrpg.players;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.types.TipType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PlayerManager {

  //Players who are currently logged on
  private static HashMap<UUID, McRPGPlayer> players = new HashMap<>();
  private static ArrayList<UUID> playersFrozen = new ArrayList<UUID>();
  private static McRPG plugin;
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
		McRPGPlayer mp = new McRPGPlayer(uuid);
		mp.getUsedTips().add(TipType.LOGIN_TIP);
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

  public static McRPGPlayer getPlayer(UUID uuid){
	return players.get(uuid);
  }

  public static boolean isPlayerStored(UUID uuid){
	return players.containsKey(uuid);
  }

  public static void removePlayer(UUID uuid){
	players.remove(uuid).saveData();
  }

  public static void startSave(Plugin p){
	plugin = (McRPG) p;
	if(saveTask != null){
	  System.out.println(Methods.color(plugin.getPluginPrefix() + "&eRestarting player saving task...."));
	  saveTask.cancel();
	}
	saveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(p, PlayerManager::run, 500, ((McRPG) p).getFileManager().getFile(FileManager.Files.CONFIG).getInt("Configuration.SaveInterval") * 1200);
	System.out.println(Methods.color(plugin.getPluginPrefix() + "&aPlayer saving task has been started!"));
	Bukkit.getScheduler().runTaskTimer(p, () ->{
	  for(McRPGPlayer mp : players.values()){
	    if(isPlayerFrozen(mp.getUuid())){
	      continue;
		}
	    mp.updateCooldowns();
	}
	}, 0, 20);
  }


  private static void run(){
	players.values().forEach(McRPGPlayer::saveData);
  }

  public static void saveAll(){run();}

  public static void shutDownManager(){
    saveAll();
    if(saveTask != null){
      saveTask.cancel();
	}
  }
}
