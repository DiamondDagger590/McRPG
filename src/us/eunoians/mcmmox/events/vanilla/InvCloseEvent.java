package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import us.eunoians.mcmmox.gui.GUITracker;

public class InvCloseEvent implements Listener {

  @EventHandler (priority = EventPriority.HIGHEST)
  public void invClose(InventoryCloseEvent e){
	Player p = (Player) e.getPlayer();
	if(GUITracker.isPlayerTracked(p)){
	  if(GUITracker.getPlayersGUI(p).isClearData()){
		GUITracker.stopTrackingPlayer(p);
	  }
	}

  }
}
