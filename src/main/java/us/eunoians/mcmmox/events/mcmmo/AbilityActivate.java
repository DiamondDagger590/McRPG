package us.eunoians.mcmmox.events.mcmmo;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.events.mcmmo.AbilityActivateEvent;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.types.Skills;

public class AbilityActivate implements Listener {

 @EventHandler
 public void abilityActivateEvent(AbilityActivateEvent e){
   Skills skill = Skills.fromString(e.getAbility().getGenericAbility().getSkill());
   String abilityName = e.getAbility().getGenericAbility().getName().replace(" ", "");
   Player p = e.getUser().getPlayer();
   if(Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.getSkillFile(skill)).getBoolean("UsePermsForAbility." + abilityName) && !(p.hasPermission("mcmmo.*") || p.hasPermission("mcmmo." + skill.getName().toLowerCase() + ".*")
	   || p.hasPermission("mcmmo." + skill.getName().toLowerCase() + ".use.*")|| p.hasPermission("mcmmo." + skill.getName().toLowerCase() + ".unlock." + abilityName))){
	 e.setCancelled(true);
   }
 }
}
