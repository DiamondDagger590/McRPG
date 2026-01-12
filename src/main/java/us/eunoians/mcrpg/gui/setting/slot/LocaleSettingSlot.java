package us.eunoians.mcrpg.gui.setting.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.McRPGSetting;
import us.eunoians.mcrpg.setting.impl.LocaleSetting;
import us.eunoians.mcrpg.setting.impl.SpecificLocaleSetting;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link McRPGSettingSlot} that displays locale settings.
 * <p>
 * Supports both {@link LocaleSetting} (CLIENT_LOCALE, SERVER_LOCALE) and
 * {@link SpecificLocaleSetting} (specific locale codes like "en", "fr").
 */
public class LocaleSettingSlot extends McRPGSettingSlot<McRPGSetting> {

    public LocaleSettingSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull LocaleSetting setting) {
        super(mcRPGPlayer, setting);
    }

    public LocaleSettingSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull SpecificLocaleSetting setting) {
        super(mcRPGPlayer, setting);
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGSetting setting = getSetting();

        if (setting instanceof LocaleSetting localeSetting) {
            return switch (localeSetting) {
                case CLIENT_LOCALE -> ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.PLAYER_SETTINGS_GUI_LOCALE_SETTING_SLOT_CLIENT_LOCALE_DISPLAY_ITEM));
                case SERVER_LOCALE -> ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.PLAYER_SETTINGS_GUI_LOCALE_SETTING_SLOT_SERVER_LOCALE_DISPLAY_ITEM));
            };
        } else if (setting instanceof SpecificLocaleSetting specificLocaleSetting) {
            // Get the locale name from the locale file
            String localeName = getLocaleDisplayName(specificLocaleSetting.getLocaleCode());

            // Use the fallback locale display item with the locale name placeholder
            return ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION)
                    .getLocalizedSection(mcRPGPlayer, LocalizationKey.PLAYER_SETTINGS_GUI_LOCALE_SETTING_SLOT_FALLBACK_LOCALE_DISPLAY_ITEM))
                    .addPlaceholders(Map.of("locale", localeName));
        }

        // Fallback - shouldn't happen
        return ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedSection(mcRPGPlayer, LocalizationKey.PLAYER_SETTINGS_GUI_LOCALE_SETTING_SLOT_CLIENT_LOCALE_DISPLAY_ITEM));
    }

    /**
     * Gets the display name for a locale code from its locale file.
     *
     * @param localeCode The locale code (e.g., "en", "fr").
     * @return The display name from the locale file, or the locale code if not found.
     */
    @NotNull
    private String getLocaleDisplayName(@NotNull String localeCode) {
        List<YamlDocument> localizationFiles = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getLocalizationFiles();

        Optional<YamlDocument> matchingDoc = localizationFiles.stream()
                .filter(doc -> localeCode.equals(doc.getString("locale")))
                .findFirst();

        if (matchingDoc.isPresent()) {
            String localeName = matchingDoc.get().getString("locale-name");
            if (localeName != null && !localeName.isBlank()) {
                return localeName;
            }
        }

        // Fallback to the locale code itself
        return localeCode;
    }
}
