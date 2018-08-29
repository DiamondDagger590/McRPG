package us.eunoians.mcmmox.gui;

import org.bukkit.inventory.ItemStack;

public class GUIItem {

    private ItemStack item;
    private int slot;

    public GUIItem(ItemStack item, int slot) {
        this.item = item;
        this.slot = slot;
    }

    public ItemStack getItemStack() {
        return item;
    }

    public int getSlot() {
        return slot;
    }

}
