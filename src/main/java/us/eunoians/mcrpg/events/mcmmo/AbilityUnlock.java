package us.eunoians.mcrpg.events.mcmmo;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.events.mcmmo.AbilityUnlockEvent;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.types.Skills;

public class AbilityUnlock implements Listener {

  @EventHandler (priority = EventPriority.NORMAL)
  public void abilityUnlock(AbilityUnlockEvent event){
    Skills skill = Skills.fromString(event.getAbilityToUnlock().getGenericAbility().getSkill());
    String abilityName = event.getAbilityToUnlock().getGenericAbility().getName().replace(" ", "").replace("_","").toLowerCase();
    Player p = event.getMcMMOPlayer().getPlayer();
    if(McRPG.getInstance().getFileManager().getFile(FileManager.Files.getSkillFile(skill)).getBoolean("UsePermsForAbility." + abilityName) && !(p.hasPermission("mcmmo.*") || p.hasPermission("mcmmo." + skill.getName().toLowerCase() + ".*")
		|| p.hasPermission("mcmmo." + skill.getName().toLowerCase() + ".unlock.*")|| p.hasPermission("mcmmo." + skill.getName().toLowerCase() + ".unlock." + abilityName))){
      event.setCancelled(true);
	}
  }
}
