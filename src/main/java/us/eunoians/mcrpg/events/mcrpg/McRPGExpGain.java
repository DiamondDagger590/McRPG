package us.eunoians.mcrpg.events.mcrpg;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.events.mcrpg.McRPGPlayerExpGainEvent;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.types.Skills;

public class McRPGExpGain implements Listener {

  @EventHandler (priority = EventPriority.NORMAL)
  public void expGain(McRPGPlayerExpGainEvent e){
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
