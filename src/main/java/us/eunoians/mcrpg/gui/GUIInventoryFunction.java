package us.eunoians.mcrpg.gui;

import org.bukkit.inventory.Inventory;

@FunctionalInterface
public interface GUIInventoryFunction {

  Inventory generateInventory(GUIBuilder builder);
}
