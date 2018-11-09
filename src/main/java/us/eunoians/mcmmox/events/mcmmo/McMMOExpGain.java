package us.eunoians.mcmmox.events.mcmmo;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.events.mcmmo.McMMOPlayerExpGainEvent;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.types.Skills;

public class McMMOExpGain implements Listener {

  @EventHandler
  public void expGain(McMMOPlayerExpGainEvent e){
	Skills skill = e.getSkillGained().getType();
	Player p = e.getMcMMOPlayer().getPlayer();
	if(Mcmmox.getInstance().getConfig().getBoolean("Configuration.UseLevelPerms") && !(p.hasPermission("mcmmo.*") || p.hasPermission("mcmmo." + skill.getName().toLowerCase() + ".*")
		|| p.hasPermission("mcmmo." + skill.getName().toLowerCase() + ".level"))){
	  e.setCancelled(true);
	}
	if(e.getSkillGained().getCurrentLevel() >= Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.getSkillFile(skill)).getInt("MaxLevel")){
	  e.setCancelled(true);
	}
  }
}
