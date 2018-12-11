package us.eunoians.mcrpg.events.mcrpg;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityUnlockEvent;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.types.Skills;

public class AbilityUnlock implements Listener {

  @EventHandler (priority = EventPriority.NORMAL)
  public void abilityUnlock(AbilityUnlockEvent event){
    Skills skill = Skills.fromString(event.getAbilityToUnlock().getGenericAbility().getSkill());
    String abilityName = event.getAbilityToUnlock().getGenericAbility().getName().replace(" ", "").replace("_","").toLowerCase();
    Player p = event.getMcRPGPlayer().getPlayer();
    if(McRPG.getInstance().getFileManager().getFile(FileManager.Files.getSkillFile(skill)).getBoolean("UsePermsForAbility." + abilityName) && !(p.hasPermission("mcrpg.*") || p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".*")
		|| p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".unlock.*")|| p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".unlock." + abilityName))){
      event.setCancelled(true);
	}
  }
}
