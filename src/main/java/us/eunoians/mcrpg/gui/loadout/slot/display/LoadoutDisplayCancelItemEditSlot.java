package us.eunoians.mcrpg.gui.loadout.slot.display;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.display.LoadoutDisplayItemInputGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * This slot is used whenever a player wants to cancel editing the item that they are trying to
 * use to update the display for a {@link Loadout}.
 */
public class LoadoutDisplayCancelItemEditSlot implements McRPGSlot {

    public LoadoutDisplayCancelItemEditSlot(@NotNull Loadout loadout){
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
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedSection(mcRPGPlayer, LocalizationKey.LOADOUT_DISPLAY_ITEM_INPUT_GUI_CANCEL_ITEM_EDIT_DISPLAY_ITEM));
    }
}
