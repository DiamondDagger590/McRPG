package us.eunoians.mcrpg.gui.loadout.slot.display;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.display.LoadoutDisplayItemInputGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * This slot will open the {@link LoadoutDisplayItemInputGui} whenever clicked to allow
 * users to input an {@link ItemStack} to edit the {@link us.eunoians.mcrpg.loadout.LoadoutDisplay}.
 */
public class LoadoutDisplayItemSlot implements McRPGSlot {

    private final Loadout loadout;

    public LoadoutDisplayItemSlot(@NotNull Loadout loadout) {
        this.loadout = loadout;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            player.closeInventory();
            LoadoutDisplayItemInputGui loadoutDisplayItemInputGui = new LoadoutDisplayItemInputGui(mcRPGPlayer, loadout);
            mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, loadoutDisplayItemInputGui);
            player.openInventory(loadoutDisplayItemInputGui.getInventory());
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.LOADOUT_DISPLAY_HOME_GUI_EDIT_DISPLAY_ITEM_SLOT_DISPLAY_ITEM))
                .setItemStack(loadout.getDisplay().getDisplayItem());
    }
}
