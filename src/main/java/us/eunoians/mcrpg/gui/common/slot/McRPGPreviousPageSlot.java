package us.eunoians.mcrpg.gui.common.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.gui.slot.pagination.PreviousPageSlot;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * A default implementation of a previous page slot that pulls from a common display configuration.
 */
public class McRPGPreviousPageSlot extends PreviousPageSlot<McRPGPlayer> implements McRPGSlot {

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer corePlayer) {
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        return ItemBuilder.from(localizationManager.getLocalizedSection(corePlayer, LocalizationKey.GUI_COMMON_PREVIOUS_PAGE_BUTTON_DISPLAY_ITEM));
    }
}
