package us.eunoians.mcrpg.gui.tutorial.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.gui.tutorial.DisableTutorialConfirmGui;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * Informational slot in {@link DisableTutorialConfirmGui}. Non-interactive.
 * Displays a warning about what disabling the tutorial entails.
 */
public class DisableTutorialInfoSlot implements McRPGSlot {

    /**
     * Non-interactive slot — clicking does nothing.
     *
     * @param mcRPGPlayer the player who clicked
     * @param clickType   the type of click
     * @return {@code false} — no action taken
     */
    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        return false;
    }

    /**
     * Builds the display item for this informational panel.
     *
     * @param mcRPGPlayer the player to build the item for
     * @return the info panel item
     */
    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager locManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        return ItemBuilder.from(locManager.getLocalizedSection(
                        mcRPGPlayer, LocalizationKey.DISABLE_TUTORIAL_INFO_SLOT_DISPLAY_ITEM))
                .applyTagReplacements(locManager.getPaletteReplacements());
    }

    /**
     * Returns the GUI types this slot is valid in.
     *
     * @return singleton set containing {@link DisableTutorialConfirmGui}
     */
    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(DisableTutorialConfirmGui.class);
    }
}
