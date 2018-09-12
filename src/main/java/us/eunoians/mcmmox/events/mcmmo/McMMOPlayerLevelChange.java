package us.eunoians.mcmmox.events.mcmmo;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.events.mcmmo.McMMOPlayerLevelChangeEvent;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;

public class McMMOPlayerLevelChange implements Listener {

  @EventHandler
  public void levelChange(McMMOPlayerLevelChangeEvent e){
    String message = Methods.color(Mcmmox.getInstance().getPluginPrefix() +
		Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.CONFIG).getString("Messages.Players.LevelUp")
			.replaceAll("%levels%", Integer.toString(e.getAmountOfLevelsIncreased())).replaceAll("%skill%", e.getSkillLeveled().getName()));
	if(e.getMcMMOPlayer().isOnline()){
	  Player p = e.getMcMMOPlayer().getPlayer();
	  p.sendMessage(message);
	  World w = p.getWorld();
	  w.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 1);
  	}
  }
}
