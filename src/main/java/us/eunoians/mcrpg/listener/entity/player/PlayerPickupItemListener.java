package us.eunoians.mcrpg.listener.entity.player;

import com.diamonddagger590.mccore.player.PlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.setting.DenySlotSetting;
import us.eunoians.mcrpg.setting.PlayerSetting;

import java.util.HashSet;
import java.util.Set;

/**
 * This listener handles all of the {@link DenySlotSetting}s and will deal changing how
 * a player picks up items if the player has a slot denied.
 */
public class PlayerPickupItemListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPickupItem(@NotNull EntityPickupItemEvent event) {
        PlayerManager playerManager = McRPG.getInstance().getPlayerManager();
        if (event.getEntity() instanceof Player player
                && playerManager.getPlayer(player.getUniqueId()).isPresent()
                && playerManager.getPlayer(player.getUniqueId()).get() instanceof McRPGPlayer mcRPGPlayer) {
            Set<Integer> ignoredSlots = new HashSet<>();
            Inventory inventory = player.getInventory();
            // Populate ignored slots
            for (PlayerSetting playerSetting : mcRPGPlayer.getPlayerSettings()) {
                if (playerSetting instanceof DenySlotSetting denySlotSetting) {
                    ignoredSlots.addAll(denySlotSetting.getDeniedSlots(mcRPGPlayer));
                }
            }

            if (!ignoredSlots.isEmpty()) {
                int firstEmpty = -1;
                for (int i = 0; i < 36; i++) {
                    if (ignoredSlots.contains(i)) {
                        continue;
                    }
                    ItemStack item = inventory.getItem(i);
                    if (item == null || item.getType() == Material.AIR) {
                        firstEmpty = i;
                        break;
                    }
                }

                if (!ignoredSlots.contains(firstEmpty) && firstEmpty != -1) {
                    event.setCancelled(true);
                    ItemStack itemToPickup = event.getItem().getItemStack();
                    int amount = itemToPickup.getAmount();
                    Set<Integer> emptySlots = new HashSet<>();
                    // Go through all slots to mark empty item slots and try to add items if possible to existing slots
                    for (int i = 0; i < inventory.getSize(); i++) {
                        if (ignoredSlots.contains(i)) {
                            continue;
                        }
                        //if the amount is no longer positive then we are done with this item
                        if (amount <= 0) {
                            break;
                        }
                        //Get the current item per iteration
                        ItemStack currentItem = inventory.getItem(i);
                        //If the slot is empty
                        if (currentItem == null || currentItem.getType() == Material.AIR) {
                            emptySlots.add(i);
                        }
                        // If the item is similar to the pickupable item
                        else if (currentItem.isSimilar(itemToPickup)) {
                            // If the max current item is a max stack, then skip it
                            if (currentItem.getMaxStackSize() <= currentItem.getAmount()) {
                                continue;
                            }
                            // Otherwise, there is room to put items into that slot
                            else {
                                // If the current item would go over stack limit with the added amount
                                if (currentItem.getAmount() + amount > currentItem.getMaxStackSize()) {
                                    amount -= currentItem.getMaxStackSize() - currentItem.getAmount();
                                    currentItem.setAmount(currentItem.getMaxStackSize());
                                    continue;
                                }
                                // Otherwise we can add the whole amount
                                else {
                                    currentItem.setAmount(currentItem.getAmount() + amount);
                                    amount = 0;
                                    break;
                                }
                            }
                        }
                    }
                    // Go through empty slots
                    for (int i : emptySlots) {
                        // If there's no items left, then break
                        if (amount == 0) {
                            break;
                        }
                        ItemStack copy = itemToPickup.clone();
                        //if the amount is greater than a stack
                        if (amount > copy.getMaxStackSize()) {
                            copy.setAmount(copy.getMaxStackSize());
                            amount -= copy.getMaxStackSize();
                            inventory.setItem(i, copy);
                            continue;
                        }
                        //Otherwise just slap the item in there and break since we dont need to put it anywhere else
                        else {
                            copy.setAmount(amount);
                            inventory.setItem(i, copy);
                            amount = 0;
                            break;
                        }
                    }
                    Item item = event.getItem();
                    if (amount == 0) {
                        item.remove();
                        return;
                    }
                    item.getItemStack().setAmount(amount);

                    // Deal with lodged projectiles
                    if (item instanceof Arrow arrow) {
                        arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                    }
                    else if (item instanceof Trident trident) {
                        trident.setPickupStatus(Trident.PickupStatus.DISALLOWED);
                    }
                }
            }

        }
    }
}
