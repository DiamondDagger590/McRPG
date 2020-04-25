package us.eunoians.mcrpg.events.mcrpg;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityUnlockEvent;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.types.Skills;

public class AbilityUnlock implements Listener {

  @EventHandler (priority = EventPriority.NORMAL)
  public void abilityUnlock(AbilityUnlockEvent event){
    Skills skill = event.getAbilityToUnlock().getGenericAbility().getSkill();
    String abilityName = event.getAbilityToUnlock().getGenericAbility().getName().replace(" ", "").replace("_","").toLowerCase();
    Player p = event.getMcRPGPlayer().getPlayer();
    if(McRPG.getInstance().getFileManager().getFile(FileManager.Files.getSkillFile(skill)).getBoolean("UsePermsForAbility." + abilityName) && !(p.hasPermission("mcrpg.*") || p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".*")
		|| p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".unlock.*")|| p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".unlock." + abilityName))){
      event.setCancelled(true);
	}
    if(McRPG.getInstance().getConfig().getBoolean("Configuration.AbilitySpyEnabled") && !(p.hasPermission("mcrpg.*") || p.hasPermission("mcadmin.abilityspy.exempt") || p.hasPermission("mcadmin.*"))){
      for(Player player : Bukkit.getOnlinePlayers()){
        if(player.getUniqueId().equals(p.getUniqueId())){
          continue;
        }
        if(player.hasPermission("mcrpg.*") || player.hasPermission("mcadmin.*") || player.hasPermission("mcadmin.abilityspy")){
          player.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands..Admin.AbilitySpy.Unlock")
                  .replace("%Player%", p.getDisplayName()).replace("%Ability%", event.getAbilityToUnlock().getGenericAbility().getName())));
        }
      }
    }
  }
}
