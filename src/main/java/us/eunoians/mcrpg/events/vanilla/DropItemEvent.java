package us.eunoians.mcrpg.events.vanilla;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("Duplicates")
public class DropItemEvent implements Listener{
  
  @Getter
  private static HashMap<Location, Integer> blockDropsToMultiplier = new HashMap<>();
  @Getter
  private static HashMap<Location, UUID> blocksToRemoteTransfer = new HashMap<>();
  
  @EventHandler
  public void dropItem(BlockDropItemEvent e){
    if(PlayerManager.isPlayerFrozen(e.getPlayer().getUniqueId())){
      return;
    }
    Location loc = e.getBlock().getLocation();
    if(blockDropsToMultiplier.containsKey(loc)){
      for(Item i : e.getItems()){
        i.getItemStack().setAmount(i.getItemStack().getAmount() * blockDropsToMultiplier.get(loc));
      }
      blockDropsToMultiplier.remove(loc);
    }
    if(blocksToRemoteTransfer.containsKey(loc)){
      McRPGPlayer mp;
      try{
        mp = PlayerManager.getPlayer(e.getPlayer().getUniqueId());
      }catch(McRPGPlayerNotFoundException exception){
        return;
      }
      RemoteTransfer transfer = (RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER);
      if(!transfer.isAbilityLinked()){
        return;
      }
      Chest chest;
      Block block2 = transfer.getLinkedChestLocation().getBlock();
      if(block2.getType() == Material.CHEST){
        chest = (Chest) block2.getState();
      }
      else{
        mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.RemoteTransfer.ChestMissing")));
        transfer.setLinkedChestLocation(null);
        mp.setLinkedToRemoteTransfer(false);
        mp.saveData();
        return;
      }
      List<Inventory> inventories = new ArrayList<>();
      Inventory leftSide = chest.getBlockInventory();
      Inventory rightSide;
      boolean isDouble = (chest.getInventory().getHolder() instanceof DoubleChest);
      if(isDouble){
        DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();
        leftSide = doubleChest.getLeftSide().getInventory();
        rightSide = doubleChest.getRightSide().getInventory();
        inventories.add(leftSide);
        inventories.add(rightSide);
      }
      else{
        inventories.add(leftSide);
      }
      //Iterate across dropped materials
      c: for(Item item : e.getItems()){
        Inventory currentInv;
        //If the item needs to be transferred and is toggled for transferring
        //Apply fortune and silk touch
        int amount = item.getItemStack().getAmount();
        Material mat = item.getItemStack().getType();
        a: for(Inventory inv : inventories){
          currentInv = inv;
          if((transfer.getItemsToSync().containsKey(mat) && transfer.getItemsToSync().get(mat))){
            //Get the material of the item we are putting in the chest and the amount
            //if the chest contents are full, check if there are any stacks we can increase before dropping
            b: for(int i = 0; i < currentInv.getSize(); i++){
              //if the amount is no longer positive then we are done with this item
              if(amount <= 0){
                item.getItemStack().setAmount(0);
                break a;
              }
              //Get the current item per iteration
              ItemStack currentItem = currentInv.getItem(i);
              //If the slot is empty
              if(currentItem == null || currentItem.getType() == Material.AIR){
                ItemStack newStack = new ItemStack(mat);
                //if the amount is greater than a stack
                if(amount > 64){
                  newStack.setAmount(64);
                  amount -= 64;
                  currentInv.setItem(i, newStack);
                  continue b;
                }
                //Otherwise just slap the item in there and break since we dont need to put it anywhere else
                else{
                  newStack.setAmount(amount);
                  currentInv.setItem(i, newStack);
                  amount = 0;
                  item.getItemStack().setAmount(0);
                  break a;
                }
              }
              else if(currentItem.getType() == mat){
                if(currentItem.getAmount() == 64){
                  continue b;
                }
                else{
                  if(currentItem.getAmount() + amount > 64){
                    amount -= 64 - currentItem.getAmount();
                    currentItem.setAmount(64);
                    continue b;
                  }
                  else{
                    currentItem.setAmount(currentItem.getAmount() + amount);
                    amount = 0;
                    item.getItemStack().setAmount(0);
                    break a;
                  }
                }
              }
              else{
                continue b;
              }
            }
            block2.getState().update();
            if(isDouble){
              for(int i = -1; i <= 1; i += 2){
                block2.getWorld().getBlockAt(block2.getLocation().add(i, 0, 0)).getState().update();
                block2.getWorld().getBlockAt(block2.getLocation().add(0, 0, i)).getState().update();
              }
            }
            //Drop leftovers
            if(amount > 0){
              item.getItemStack().setAmount(amount);
            }
          }
          else{
            continue c;
          }
        }
      }
    }
  }
}
