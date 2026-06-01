package us.eunoians.mcrpg.gui.tutorial.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.setting.PlayerSettingGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.gui.tutorial.DisableTutorialConfirmGui;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * Cancel button in {@link DisableTutorialConfirmGui}.
 * Returns the player to the settings GUI without changing the setting.
 */
public class DisableTutorialCancelSlot implements McRPGSlot {

    /**
     * Handles the click: opens the player settings GUI, discarding the disable action.
     *
     * @param mcRPGPlayer the player who clicked
     * @param clickType   the type of click
     * @return {@code true} always
     */
    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            PlayerSettingGui settingGui = new PlayerSettingGui(mcRPGPlayer);
            McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.GUI).trackPlayerGui(player, settingGui);
            player.openInventory(settingGui.getInventory());
        });
        return true;
    }

    /**
     * Builds the display item for the cancel button.
     *
     * @param mcRPGPlayer the player to build the item for
     * @return the cancel button item
     */
    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager locManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        return ItemBuilder.from(locManager.getLocalizedSection(
                        mcRPGPlayer, LocalizationKey.DISABLE_TUTORIAL_CANCEL_SLOT_DISPLAY_ITEM))
                .applyTagReplacements(locManager.getPaletteReplacements());
    }

    /**
     * Returns the GUI types this slot is valid in.
     *
     *     @return singleton set containing {@link DisableTutorialConfirmGui}
     */
    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(DisableTutorialConfirmGui.class);
    }
}
