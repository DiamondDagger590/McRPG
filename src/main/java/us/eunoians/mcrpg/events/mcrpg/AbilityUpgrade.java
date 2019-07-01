package us.eunoians.mcrpg.events.mcrpg;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityUpgradeEvent;
import us.eunoians.mcrpg.api.util.Methods;

public class AbilityUpgrade implements Listener{

  @EventHandler(priority = EventPriority.NORMAL)
  public void abilityUpgrade(AbilityUpgradeEvent event){
    if(!event.isCancelled() && event.getAbilityUpgrading() instanceof RemoteTransfer){
      ((RemoteTransfer) event.getAbilityUpgrading()).updateBlocks();
      event.getMcRPGPlayer().saveData();
    }
    Player player = event.getMcRPGPlayer().getPlayer();
    if(McRPG.getInstance().getConfig().getBoolean("Configuration.AbilitySpyEnabled") && !(player.hasPermission("mcrpg.*") || player.hasPermission("mcadmin.abilityspy.exempt") || player.hasPermission("mcadmin.*"))){
      for(Player p : Bukkit.getOnlinePlayers()){
        if(player.getUniqueId().equals(p.getUniqueId())){
          continue;
        }
        if(p.hasPermission("mcrpg.*") || p.hasPermission("mcadmin.*") || p.hasPermission("mcadmin.abilityspy")){
          p.sendMessage(Methods.color(event.getMcRPGPlayer().getPlayer(), McRPG.getInstance().getPluginPrefix() +
                  McRPG.getInstance().getLangFile().getString("Messages.Commands.Admin.AbilitySpy.Upgrade")
                          .replace("%Player%", event.getMcRPGPlayer().getPlayer().getDisplayName())
                          .replace("%Ability%", event.getAbilityUpgrading().getGenericAbility().getName())
                          .replace("%Tier%", Integer.toString(event.getNextTier()))));
        }
      }
    }
  }
}
