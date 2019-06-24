package us.eunoians.mcrpg.events.vanilla;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("Duplicates")
public class DropItemEvent implements Listener {

  @Getter
  private static HashMap<Location, Integer> blockDropsToMultiplier = new HashMap<>();
  @Getter
  private static HashMap<Location, UUID> blocksToRemoteTransfer = new HashMap<>();

  @EventHandler
  public void dropItem(BlockDropItemEvent e) {
    if(PlayerManager.isPlayerFrozen(e.getPlayer().getUniqueId())){
      return;
    }
    Location loc = e.getBlock().getLocation();
    if(blockDropsToMultiplier.containsKey(loc)) {
      for(Item i : e.getItems()) {
        i.getItemStack().setAmount(blockDropsToMultiplier.get(loc));
      }
      blockDropsToMultiplier.remove(loc);
    }
    if(blocksToRemoteTransfer.containsKey(loc)) {
      McRPGPlayer mp = PlayerManager.getPlayer(blocksToRemoteTransfer.get(loc));
      RemoteTransfer transfer = (RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER);
      Chest chest;
      Block block2 = transfer.getLinkedChestLocation().getBlock();
      if(block2.getType() == Material.CHEST) {
        chest = (Chest) block2.getState();
      }
      else {
        mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.RemoteTransfer.ChestMissing")));
        transfer.setLinkedChestLocation(null);
        mp.setLinkedToRemoteTransfer(false);
        mp.saveData();
        return;
      }
      Inventory inv = chest.getBlockInventory();
      //Iterate across dropped materials
      for(Item item : e.getItems()) {
        //If the item needs to be transferred and is toggled for transferring
        //Apply fortune and silk touch
        Material mat = item.getItemStack().getType();
        if((transfer.getItemsToSync().keySet().contains(mat) && transfer.getItemsToSync().get(mat))){
          //Get the material of the item we are putting in the chest and the amount
          int amount = item.getItemStack().getAmount();
          //if the chest contents are full, check if there are any stacks we can increase before dropping
          for(int i = 0; i < inv.getSize(); i++) {
            //if the amount is no longer positive then we are done with this item
            if(amount <= 0) {
              item.getItemStack().setAmount(0);
              break;
            }
            //Get the current item per iteration
            ItemStack currentItem = inv.getItem(i);
            //If the slot is empty
            if(currentItem == null || currentItem.getType() == Material.AIR) {
              ItemStack newStack = new ItemStack(mat);
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
                item.getItemStack().setAmount(0);
                break;
              }
            }
            else if(currentItem.getType() == mat) {
              if(currentItem.getAmount() == 64) {
                continue;
              }
              else {
                if(currentItem.getAmount() + amount > 64) {
                  amount -= 64 - currentItem.getAmount();
                  currentItem.setAmount(64);
                  continue;
                }
                else {
                  currentItem.setAmount(currentItem.getAmount() + amount);
                  amount = 0;
                  item.getItemStack().setAmount(0);
                  break;
                }
              }
            }
            else {
              continue;
            }
          }
          block2.getState().update();
          //Drop leftovers
          if(amount > 0) {
            item.getItemStack().setAmount(amount);
          }
        }
      }
    }
  }
}
