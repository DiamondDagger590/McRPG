package us.eunoians.mcrpg.events.mcmmo;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.events.mcmmo.AbilityActivateEvent;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.types.Skills;

public class AbilityActivate implements Listener {

  @EventHandler (priority = EventPriority.NORMAL)
 public void abilityActivateEvent(AbilityActivateEvent e){
   Skills skill = Skills.fromString(e.getAbility().getGenericAbility().getSkill());
   String abilityName = e.getAbility().getGenericAbility().getName().replace(" ", "").replace("_", "").toLowerCase();
   Player p = e.getUser().getPlayer();
   if(McRPG.getInstance().getFileManager().getFile(FileManager.Files.getSkillFile(skill)).getBoolean("UsePermsForAbility." + abilityName) && !(p.hasPermission("mcrpg.*") || p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".*")
	   || p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".use.*")|| p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".unlock." + abilityName))){
	 e.setCancelled(true);
   }
 }
}
