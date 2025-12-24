package us.eunoians.mcrpg.gui.common.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;

/**
 * A default implementation of a next previous gui slot that pulls from a common display configuration.
 * <p>
 * This slot lacks a click handler and expects guis to implement their specific logic for how that
 * button should behave.
 */
public abstract class McRPGPreviousGuiSlot implements McRPGSlot {

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Route route = localizationManager.doesAnyLocaleContainRoute(mcRPGPlayer, getSpecificDisplayItemRoute()) ? getSpecificDisplayItemRoute() : LocalizationKey.GUI_COMMON_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM;
        ItemBuilder itemBuilder = ItemBuilder.from(localizationManager.getLocalizedSection(mcRPGPlayer, route));
        itemBuilder.addPlaceholders(getPlaceholders(mcRPGPlayer));
        return itemBuilder;
    }

    @NotNull
    public abstract Route getSpecificDisplayItemRoute();

    @NotNull
    public Map<String, String> getPlaceholders(@NotNull McRPGPlayer mcRPGPlayer) {
        return Map.of();
    }
}
