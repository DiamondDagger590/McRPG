package us.eunoians.mcrpg.gui.slot.loadout.display;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.display.LoadoutDisplayItemInputGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;

/**
 * This slot is used whenever a player wants to cancel editing the item that they are trying to
 * use to update the display for a {@link Loadout}.
 */
public class LoadoutDisplayCancelItemEditSlot extends McRPGSlot {

    private final Loadout loadout;

    public LoadoutDisplayCancelItemEditSlot(@NotNull Loadout loadout){
        this.loadout = loadout;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).getOpenedGui(player).ifPresent(gui -> {
                if (gui instanceof LoadoutDisplayItemInputGui displayItemInputGui) {
                    displayItemInputGui.cancelSave();
                }
            });
            player.closeInventory();
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@Nullable McRPGPlayer mcRPGPlayer) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        ItemStack itemStack = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<gold>Cancel editing loadout display item."));
        itemMeta.lore(List.of(miniMessage.deserialize("<gold>Click <gray>to cancel editing loadout display item.")));
        itemStack.setItemMeta(itemMeta);
        return ItemBuilder.from(itemStack);
    }
}
