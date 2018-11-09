package us.eunoians.mcmmox.events.mcmmo;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.events.mcmmo.AbilityUnlockEvent;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.types.Skills;

public class AbilityUnlock implements Listener {

  @EventHandler
  public void abilityUnlock(AbilityUnlockEvent event){
    Skills skill = Skills.fromString(event.getAbilityToUnlock().getGenericAbility().getSkill());
    String abilityName = event.getAbilityToUnlock().getGenericAbility().getName().replace(" ", "").replace("_","").toLowerCase();
    Player p = event.getMcMMOPlayer().getPlayer();
    if(Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.getSkillFile(skill)).getBoolean("UsePermsForAbility." + abilityName) && !(p.hasPermission("mcmmo.*") || p.hasPermission("mcmmo." + skill.getName().toLowerCase() + ".*")
		|| p.hasPermission("mcmmo." + skill.getName().toLowerCase() + ".unlock.*")|| p.hasPermission("mcmmo." + skill.getName().toLowerCase() + ".unlock." + abilityName))){
      event.setCancelled(true);
	}
  }
}
