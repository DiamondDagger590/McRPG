package us.eunoians.mcmmox.events.mcmmo;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcmmox.abilities.mining.RemoteTransfer;
import us.eunoians.mcmmox.api.events.mcmmo.AbilityAddToLoadoutEvent;

public class LoadoutAdd implements Listener {

  @EventHandler (priority = EventPriority.MONITOR)
  public void loadoutAdd(AbilityAddToLoadoutEvent e){
    if(!e.isCancelled() && e.getAbilityToAdd() instanceof RemoteTransfer){
	  ((RemoteTransfer) e.getAbilityToAdd()).updateBlocks();
	  e.getMcMMOPlayer().saveData();
	}
  }
}
