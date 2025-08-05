package us.eunoians.mcrpg.gui.common.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * This slot is meant to fill space in a GUI where we don't want a blank slot,
 * but we don't want any other UI element there either.
 */
public class McRPGFillerSlot implements McRPGSlot {

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer corePlayer) {
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        return ItemBuilder.from(localizationManager.getLocalizedSection(corePlayer, LocalizationKey.GUI_COMMON_PREVIOUS_FILLER_ITEM_DISPLAY_ITEM));
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer corePlayer, @NotNull ClickType clickType) {
        return true;
    }
}
