package us.eunoians.mcrpg.gui.slot.loadout;

import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

/**
 * This slot is a no-op slot used to represent a slot that is not
 * useable when viewing a loadout.
 */
public class InvalidLoadoutSlot extends Slot {

    private static final ItemStack SLOT_ITEM;

    static {
        SLOT_ITEM = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        ItemMeta itemMeta = SLOT_ITEM.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize(" "));
        SLOT_ITEM.setItemMeta(itemMeta);
    }

    @Override
    public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
        return true;
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        return SLOT_ITEM;
    }
}
