package us.eunoians.mcrpg.events.mcrpg;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityAddToLoadoutEvent;

public class LoadoutAdd implements Listener {

  @EventHandler (priority = EventPriority.MONITOR)
  public void loadoutAdd(AbilityAddToLoadoutEvent e){
    if(!e.isCancelled() && e.getAbilityToAdd() instanceof RemoteTransfer){
	  ((RemoteTransfer) e.getAbilityToAdd()).updateBlocks();
	  e.getMcRPGPlayer().saveData();
	}
  }
}
