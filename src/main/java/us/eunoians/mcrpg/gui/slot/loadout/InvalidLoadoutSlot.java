package us.eunoians.mcrpg.gui.slot.loadout;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;

/**
 * This slot is a no-op slot used to represent a slot that is not
 * useable when viewing a loadout.
 */
public class InvalidLoadoutSlot extends McRPGSlot {

    private static final ItemBuilder SLOT_ITEM;

    static {
        ItemStack itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize(" "));
        itemStack.setItemMeta(itemMeta);
        SLOT_ITEM = ItemBuilder.from(itemStack);
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer corePlayer, @NotNull ClickType clickType) {
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@Nullable McRPGPlayer mcRPGPlayer) {
        return SLOT_ITEM;
    }
}
