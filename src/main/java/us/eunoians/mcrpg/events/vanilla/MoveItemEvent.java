package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.BrewerInventory;
import us.eunoians.mcrpg.types.Skills;

public class MoveItemEvent implements Listener{
  
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void moveEvent(InventoryMoveItemEvent e){
    if(Skills.SORCERY.isEnabled() &&
         (e.getDestination() instanceof BrewerInventory || e.getInitiator() instanceof BrewerInventory || e.getSource() instanceof BrewerInventory)){
      e.setCancelled(true);
    }
  }
}
