package us.eunoians.mcmmox.events.mcmmo;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcmmox.abilities.mining.RemoteTransfer;
import us.eunoians.mcmmox.api.events.mcmmo.AbilityUpgradeEvent;

public class AbilityUpgrade implements Listener {

  @EventHandler (priority = EventPriority.NORMAL)
  public void abilityUpgrade(AbilityUpgradeEvent event){
    if(!event.isCancelled() && event.getAbilityUpgrading() instanceof RemoteTransfer){
	  ((RemoteTransfer) event.getAbilityUpgrading()).updateBlocks();
	  event.getMcMMOPlayer().saveData();
	}
  }
}
