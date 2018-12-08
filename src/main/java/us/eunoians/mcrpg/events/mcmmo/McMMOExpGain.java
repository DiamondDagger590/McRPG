package us.eunoians.mcrpg.events.mcmmo;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.events.mcmmo.McMMOPlayerExpGainEvent;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.types.Skills;

public class McMMOExpGain implements Listener {

  @EventHandler (priority = EventPriority.NORMAL)
  public void expGain(McMMOPlayerExpGainEvent e){
	Skills skill = e.getSkillGained().getType();
	Player p = e.getMcMMOPlayer().getPlayer();
	if(McRPG.getInstance().getConfig().getBoolean("Configuration.UseLevelPerms") && !(p.hasPermission("mcrpg.*") || p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".*")
		|| p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".level"))){
	  e.setCancelled(true);
	}
	if(e.getSkillGained().getCurrentLevel() >= McRPG.getInstance().getFileManager().getFile(FileManager.Files.getSkillFile(skill)).getInt("MaxLevel")){
	  e.setCancelled(true);
	}
  }
}
