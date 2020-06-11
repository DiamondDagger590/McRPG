package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("Duplicates")
public class PickupEvent implements Listener {

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void pickupEvent(PlayerPickupItemEvent e) {
    if(PlayerManager.isPlayerFrozen(e.getPlayer().getUniqueId()) || e.getItem().getItemStack().getAmount() < 1){
      return;
    }
    //Disabled Worlds
    if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
         McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(e.getPlayer().getWorld().getName())) {
      return;
    }
    McRPGPlayer mp;
    try{
      mp = PlayerManager.getPlayer(e.getPlayer().getUniqueId());
    }
    catch(McRPGPlayerNotFoundException exception){
      return;
    }
    if(e.getPlayer().getInventory().getItemInMainHand() == null || e.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR || mp.getUnarmedIgnoreSlot() != -1) {
      Set<Integer> ignored = new HashSet<>();
      if(mp.isKeepHandEmpty()){
        ignored.add(e.getPlayer().getInventory().getHeldItemSlot());
      }
      if(mp.getUnarmedIgnoreSlot() != -1){
        ignored.add(mp.getUnarmedIgnoreSlot());
      }
      if(ignored.size() > 0) {
        //int slot = e.getPlayer().getInventory().getHeldItemSlot();
        int firstEmpty = -1;
        for(int i = 0; i < 36; i++) {
          if(ignored.contains(i)) {
            continue;
          }
          ItemStack item = e.getPlayer().getInventory().getItem(i);
          if(item == null || item.getType() == Material.AIR) {
            firstEmpty = i;
            break;
          }
        }
        if(!ignored.contains(firstEmpty) && firstEmpty != -1) {
          e.setCancelled(true);
          FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
          e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.valueOf(soundFile.getString("Sounds.Misc.Pickup.Sound")),
            Float.parseFloat(soundFile.getString("Sounds.Misc.Pickup.Volume")), Float.parseFloat(soundFile.getString("Sounds.Misc.Pickup.Pitch")));
          ItemStack itemToPickup = e.getItem().getItemStack();
          Inventory inv = e.getPlayer().getInventory();
          // e.getPlayer().getInventory().setItem(firstEmpty, e.getItem().getItemStack());
          int amount = itemToPickup.getAmount();
          Material mat = itemToPickup.getType();
          boolean hasMeta = itemToPickup.hasItemMeta();
          ItemMeta meta = hasMeta ? itemToPickup.getItemMeta() : null;
          ArrayList<Integer> emptySlots = new ArrayList<>();
          for(int i = 0; i < inv.getSize(); i++) {
            if(ignored.contains(i)){
              continue;
            }
            //if the amount is no longer positive then we are done with this item
            if(amount <= 0) {
              break;
            }
            //Get the current item per iteration
            ItemStack currentItem = inv.getItem(i);
            //If the slot is empty
            if(currentItem == null || currentItem.getType() == Material.AIR) {
              emptySlots.add(i);
            }
            else if(currentItem.getType() == mat) {
              if(currentItem.getMaxStackSize() < currentItem.getAmount() + 1) {
                continue;
              }
              if(currentItem.getAmount() == 64) {
                continue;
              }
              else {
                if(hasMeta && !currentItem.hasItemMeta() || !hasMeta && currentItem.hasItemMeta()){
                  continue;
                }
                if(hasMeta && currentItem.hasItemMeta() && !meta.equals(currentItem.getItemMeta())){
                  continue;
                }
                if(currentItem.getAmount() + amount > 64) {
                  amount -= 64 - currentItem.getAmount();
                  currentItem.setAmount(64);
                  continue;
                }
                else {
                  currentItem.setAmount(currentItem.getAmount() + amount);
                  amount = 0;
                  break;
                }
              }
            }
            else {
              continue;
            }
          }
          for(int i : emptySlots) {
            if(amount == 0) {
              break;
            }
            ItemStack newStack = new ItemStack(mat);
            if(hasMeta){
              newStack.setItemMeta(meta);
            }
            //if the amount is greater than a stack
            if(amount > 64) {
              newStack.setAmount(64);
              amount -= 64;
              inv.setItem(i, newStack);
              continue;
            }
            //Otherwise just slap the item in there and break since we dont need to put it anywhere else
            else {
              newStack.setAmount(amount);
              inv.setItem(i, newStack);
              amount = 0;
              break;
            }
          }
          if(amount == 0){
            e.getItem().remove();
            return;
          }
          e.getItem().getItemStack().setAmount(amount);
          if(e.getItem() instanceof Arrow){
            Arrow arrow = (Arrow) e.getItem();
            arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
          }
        }
      }
    }
  }
}