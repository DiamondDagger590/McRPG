package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;

public class PickupEvent implements Listener {

  @EventHandler (priority = EventPriority.HIGH)
  public void pickupEvent(PlayerPickupItemEvent e){
    if(e.getPlayer().getInventory().getItemInMainHand() == null || e.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR){
	  McMMOPlayer mp = PlayerManager.getPlayer(e.getPlayer().getUniqueId());
	  if(mp.isKeepHandEmpty()){
		int slot = e.getPlayer().getInventory().getHeldItemSlot();
		int firstEmpty = -1;
		for(int i = 0; i < 36; i++){
		  if(i == slot){
		    continue;
		  }
		  ItemStack item = e.getPlayer().getInventory().getItem(i);
		  if(item == null || item.getType() == Material.AIR){
			firstEmpty = i;
			break;
		  }
		}
		if(slot != firstEmpty){
	      e.setCancelled(true);
	      e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 5, 1);
	      e.getPlayer().getInventory().setItem(firstEmpty, e.getItem().getItemStack());
	      e.getItem().getItemStack().setAmount(0);
		}
	  }
	}
  }
}