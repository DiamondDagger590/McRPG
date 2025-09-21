package us.eunoians.mcrpg.gui.loadout.slot.display;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.display.LoadoutDisplayItemInputGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * This slot will close the {@link LoadoutDisplayItemInputGui} in order to save the display and reopen
 * the previous gui.
 */
public class LoadoutDisplayItemConfirmSlot implements McRPGSlot {

    public LoadoutDisplayItemConfirmSlot() {
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        // We close the inventory because we auto handle the saving on the close event there :>
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(HumanEntity::closeInventory);
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedSection(mcRPGPlayer, LocalizationKey.LOADOUT_DISPLAY_ITEM_INPUT_GUI_CONFIRM_ITEM_EDIT_DISPLAY_ITEM));
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(LoadoutDisplayItemInputGui.class);
    }
}
