package us.eunoians.mcrpg.gui.loadout.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.ability.InnateAbilityGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * A nav-bar slot in {@link us.eunoians.mcrpg.gui.loadout.LoadoutGui} that opens
 * {@link InnateAbilityGui}, giving players quick access to their always-active innate abilities
 * without leaving the loadout editing flow.
 */
public class ViewInnateAbilitiesSlot implements McRPGSlot {

    private final McRPGPlayer mcRPGPlayer;
    private final Loadout loadout;

    /**
     * Creates a {@link ViewInnateAbilitiesSlot}.
     *
     * @param mcRPGPlayer The player whose innate abilities will be shown.
     * @param loadout     The loadout currently being edited, used for back navigation.
     */
    public ViewInnateAbilitiesSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.loadout = loadout;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            InnateAbilityGui innateAbilityGui = new InnateAbilityGui(mcRPGPlayer, loadout);
            mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, innateAbilityGui);
            player.openInventory(innateAbilityGui.getInventory());
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        var localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        ItemBuilder itemBuilder = ItemBuilder.from(localizationManager.getLocalizedSection(mcRPGPlayer, LocalizationKey.LOADOUT_GUI_VIEW_INNATE_ABILITIES_SLOT_DISPLAY_ITEM));
        itemBuilder.applyTagReplacements(localizationManager.getPaletteReplacements());
        return itemBuilder;
    }
}
