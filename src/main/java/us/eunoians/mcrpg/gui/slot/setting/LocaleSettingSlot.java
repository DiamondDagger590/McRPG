package us.eunoians.mcrpg.gui.slot.setting;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.NativeLocale;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.impl.LocaleSetting;

/**
 * A {@link McRPGSettingSlot} that displays {@link LocaleSetting}s.
 */
public class LocaleSettingSlot extends McRPGSettingSlot<LocaleSetting> {

    public LocaleSettingSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull LocaleSetting setting) {
        super(mcRPGPlayer, setting);
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        switch (getSetting()) {
            case CLIENT_LOCALE -> {
                return ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.PLAYER_SETTINGS_GUI_LOCALE_SETTING_SLOT_CLIENT_LOCALE_DISPLAY_ITEM));
            }
            case SERVER_LOCALE -> {
                return ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.PLAYER_SETTINGS_GUI_LOCALE_SETTING_SLOT_SERVER_LOCALE_DISPLAY_ITEM));
            }
            // TODO https://github.com/DiamondDagger590/McRPG/issues/133
            default -> {
                return ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.PLAYER_SETTINGS_GUI_LOCALE_SETTING_SLOT_FALLBACK_LOCALE_DISPLAY_ITEM))
                        .addPlaceholder("locale", getSetting().getNativeLocale().orElse(NativeLocale.ENGLISH).getLocaleName());
            }
        }
    }
}
