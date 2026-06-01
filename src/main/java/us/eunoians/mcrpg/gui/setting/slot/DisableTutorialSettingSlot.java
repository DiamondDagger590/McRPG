package us.eunoians.mcrpg.gui.setting.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.tutorial.DisableTutorialConfirmGui;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.impl.DisableTutorialSetting;

import org.bukkit.event.inventory.ClickType;

/**
 * GUI slot for the {@link DisableTutorialSetting}. Disabling the tutorial is
 * a one-way player decision — re-enabling requires admin intervention via
 * {@code /mcrpg quest chain reset}.
 * <p>
 * Click behavior:
 * <ul>
 *   <li>When ENABLED → opens {@link DisableTutorialConfirmGui} (destructive, one-way)</li>
 *   <li>When DISABLED → plays deny sound + action bar message (no toggle back)</li>
 * </ul>
 */
public class DisableTutorialSettingSlot extends McRPGSettingSlot<DisableTutorialSetting> {

    /**
     * Creates a new slot for the given player and current setting value.
     *
     * @param player         the player the slot is displayed for
     * @param currentSetting the player's current tutorial setting
     */
    public DisableTutorialSettingSlot(@NotNull McRPGPlayer player,
                                      @NotNull DisableTutorialSetting currentSetting) {
        super(player, currentSetting);
    }

    /**
     * Handles a click on this slot.
     * <p>
     * When the setting is {@link DisableTutorialSetting#ENABLED}, opens the
     * {@link DisableTutorialConfirmGui}. When already {@link DisableTutorialSetting#DISABLED},
     * plays a deny sound and sends an action bar message — the tutorial cannot be re-enabled
     * from the player GUI.
     *
     * @param mcRPGPlayer the clicking player
     * @param clickType   the type of click performed
     * @return {@code true} always (click consumed)
     */
    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        if (getSetting() == DisableTutorialSetting.ENABLED) {
            mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
                DisableTutorialConfirmGui confirmGui = new DisableTutorialConfirmGui(mcRPGPlayer);
                McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.GUI).trackPlayerGui(player, confirmGui);
                player.openInventory(confirmGui.getInventory());
            });
            return true;
        }
        // DISABLED — one-way door, play deny feedback
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            McRPGLocalizationManager locManager = RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
            player.sendActionBar(McRPG.getInstance().getMiniMessage().deserialize(
                    locManager.getLocalizedMessage(mcRPGPlayer,
                            LocalizationKey.TUTORIAL_SETTING_DISABLED_DENY_MESSAGE)));
        });
        return true;
    }

    /**
     * Builds the display item for this slot. Returns a different item depending on
     * whether the tutorial is currently enabled or disabled.
     *
     * @param mcRPGPlayer the player to build the item for
     * @return the item for this slot
     */
    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager locManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        if (getSetting() == DisableTutorialSetting.ENABLED) {
            return ItemBuilder.from(locManager.getLocalizedSection(
                            mcRPGPlayer, LocalizationKey.TUTORIAL_SETTING_SLOT_ENABLED_DISPLAY_ITEM))
                    .applyTagReplacements(locManager.getPaletteReplacements());
        }
        return ItemBuilder.from(locManager.getLocalizedSection(
                        mcRPGPlayer, LocalizationKey.TUTORIAL_SETTING_SLOT_DISABLED_DISPLAY_ITEM))
                .applyTagReplacements(locManager.getPaletteReplacements());
    }
}
