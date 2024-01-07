package us.eunoians.mcrpg.gui;

import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.GuiClickFunction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class HomeGui extends Gui {

    private static final ItemStack FILLER_GLASS = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    private static final ItemStack SETTINGS_BUTTON_ITEM = new ItemStack(Material.LEVER);
    private static final GuiClickFunction ON_SETTINGS_CLICK = ((corePlayer, gui, slot) -> {
        Bukkit.broadcastMessage("Settings clicked");
    });
    private static final ItemStack SKILLS_MENU_BUTTON_ITEM = new ItemStack(Material.REDSTONE);
    static {
        ItemMeta meta = FILLER_GLASS.getItemMeta();
        meta.setDisplayName("");
        FILLER_GLASS.setItemMeta(meta);

        meta = SETTINGS_BUTTON_ITEM.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Settings");
        SETTINGS_BUTTON_ITEM.setItemMeta(meta);

        meta = SKILLS_MENU_BUTTON_ITEM.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Skills Menu");
        SKILLS_MENU_BUTTON_ITEM.setItemMeta(meta);
    }

    public HomeGui(@Nullable UUID inventoryCreatorUUID) {
        super(inventoryCreatorUUID, "<purple>Home Gui", 27);
        this.guiFillerFunction = (inventory -> {
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack itemStack = inventory.getItem(i);
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    inventory.setItem(i, FILLER_GLASS.clone());
                }
            }
        });
    }

    private void setupItems() {

    }
}
