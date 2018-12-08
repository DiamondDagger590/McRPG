package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcrpg.players.McMMOPlayer;
import us.eunoians.mcrpg.players.PlayerManager;

import java.util.ArrayList;

@SuppressWarnings("Duplicates")
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
		if(slot != firstEmpty && firstEmpty != -1){
	      e.setCancelled(true);
	      e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 5, 1);
	      Inventory inv = e.getPlayer().getInventory();
	     // e.getPlayer().getInventory().setItem(firstEmpty, e.getItem().getItemStack());
		  int amount = e.getItem().getItemStack().getAmount();
		  Material mat = e.getItem().getItemStack().getType();
		  ArrayList<Integer> emptySlots = new ArrayList<>();
		  //if the chest contents are full, check if there are any stacks we can increase before dropping
		  for(int i = 0; i < inv.getSize(); i++){
		    if(i == e.getPlayer().getInventory().getHeldItemSlot()){
		      continue;
			}
			//if the amount is no longer positive then we are done with this item
			if(amount <= 0){
			  break;
			}
			//Get the current item per iteration
			ItemStack currentItem = inv.getItem(i);
			//If the slot is empty
			if(currentItem == null || currentItem.getType() == Material.AIR){
			  emptySlots.add(i);
			}
			else if(currentItem.getType() == mat){
			  if(currentItem.getAmount() == 64){
				continue;
			  }
			  else{
				if(currentItem.getAmount() + amount > 64){
				  amount -= 64 - currentItem.getAmount();
				  currentItem.setAmount(64);
				  continue;
				}
				else{
				  currentItem.setAmount(currentItem.getAmount() + amount);
				  amount = 0;
				  break;
				}
			  }
			}
			else{
			  continue;
			}
		  }
		  for(int i : emptySlots){
		    if(amount == 0){
		      break;
			}
			ItemStack newStack = new ItemStack(mat);
			//if the amount is greater than a stack
			if(amount > 64){
			  newStack.setAmount(64);
			  amount -= 64;
			  inv.setItem(i, newStack);
			  continue;
			}
			//Otherwise just slap the item in there and break since we dont need to put it anywhere else
			else{
			  newStack.setAmount(amount);
			  inv.setItem(i, newStack);
			  amount = 0;
			  break;
			}
		  }
	      e.getItem().getItemStack().setAmount(amount);
		}
	  }
	}
  }
}