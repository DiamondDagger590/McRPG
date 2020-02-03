package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.BrewerInventory;

public class MoveItemEvent implements Listener{
  
  @EventHandler
  public void moveEvent(InventoryMoveItemEvent e){
    if(e.getDestination() instanceof BrewerInventory || e.getInitiator() instanceof BrewerInventory){
      e.setCancelled(true);
    }
  }
}
