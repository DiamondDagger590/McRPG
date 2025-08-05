package us.eunoians.mcrpg.gui.home.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.home.HomeGui;
import us.eunoians.mcrpg.gui.loadout.LoadoutSelectionGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * This slot is used in the {@link HomeGui} to open a new {@link LoadoutSelectionGui} when clicked.
 */
public class HomeLoadoutSlot implements McRPGSlot {

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        if (mcRPGPlayer.getAsBukkitPlayer().isPresent()) {
            LoadoutSelectionGui loadoutSelectionGui = new LoadoutSelectionGui(mcRPGPlayer);
            McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, loadoutSelectionGui);
            mcRPGPlayer.getAsBukkitPlayer().get().openInventory(loadoutSelectionGui.getInventory());
        }
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION).getLocalizedSection(mcRPGPlayer, LocalizationKey.HOME_GUI_LOADOUTS_SLOT_DISPLAY_ITEM));
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(HomeGui.class);
    }
}
