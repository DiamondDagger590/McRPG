package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.setting.slot.LocaleSettingSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * This setting allows players to change what localization is shown to them.
 * <p>
 * The default order of localization goes (on best effort basis): locale setting -> client locale -> server default -> english.
 * If any localization is missing a string, then it goes to the next locale in the chain until it eventually defaults to english.
 * <p>
 * The setting cycles through: CLIENT_LOCALE -> SERVER_LOCALE -> [each available locale] -> back to CLIENT_LOCALE
 */
public enum LocaleSetting implements LocalePlayerSetting {

    CLIENT_LOCALE,
    SERVER_LOCALE,
    ;

    /**
     * Gets the first (default) setting for new players.
     * <p>
     * The default is determined by the config option {@code configuration.localization.default-player-locale-setting}.
     * Valid values are:
     * <ul>
     *   <li>{@code CLIENT_LOCALE} - Use the player's Minecraft client language</li>
     *   <li>{@code SERVER_LOCALE} - Use the server's default locale</li>
     *   <li>Any locale code (e.g., "en", "lt", "fr") - Force a specific language</li>
     * </ul>
     *
     * @return A {@link LinkedNode} containing the default setting.
     */
    @NotNull
    @Override
    public LinkedNode<? extends PlayerSetting> getFirstSetting() {
        String configDefault = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.MAIN_CONFIG)
                .getString(MainConfigFile.DEFAULT_PLAYER_LOCALE_SETTING, "CLIENT_LOCALE");

        // Check if it's one of the enum values
        if (configDefault.equalsIgnoreCase("CLIENT_LOCALE")) {
            return new LinkedNode<>(CLIENT_LOCALE);
        } else if (configDefault.equalsIgnoreCase("SERVER_LOCALE")) {
            return new LinkedNode<>(SERVER_LOCALE);
        }

        // Check if it's a valid locale code
        List<String> availableLocales = SpecificLocaleSetting.getAvailableLocaleCodes();
        for (String code : availableLocales) {
            if (code.equalsIgnoreCase(configDefault)) {
                return new LinkedNode<>(new SpecificLocaleSetting(code));
            }
        }

        // Fallback to CLIENT_LOCALE if config value is invalid
        return new LinkedNode<>(CLIENT_LOCALE);
    }

    @NotNull
    @Override
    public LinkedNode<? extends PlayerSetting> getNextSetting() {
        return switch (this) {
            case CLIENT_LOCALE -> new LinkedNode<>(SERVER_LOCALE);
            case SERVER_LOCALE -> {
                // After SERVER_LOCALE, go to the first available specific locale
                List<String> availableLocales = SpecificLocaleSetting.getAvailableLocaleCodes();
                if (!availableLocales.isEmpty()) {
                    yield new LinkedNode<>(new SpecificLocaleSetting(availableLocales.getFirst()));
                }
                // If no locales available, wrap back to CLIENT_LOCALE
                yield new LinkedNode<>(CLIENT_LOCALE);
            }
        };
    }

    @NotNull
    @Override
    public LocaleSettingSlot getSettingSlot(@NotNull McRPGPlayer player) {
        return new LocaleSettingSlot(player, this);
    }

    @NotNull
    @Override
    public Optional<? extends PlayerSetting> fromString(@NotNull String setting) {
        // Check if it's one of the enum values
        Optional<LocaleSetting> enumMatch = Arrays.stream(values())
                .filter(localeSetting -> localeSetting.toString().equalsIgnoreCase(setting))
                .findFirst();

        if (enumMatch.isPresent()) {
            return enumMatch;
        }

        // Check if it's a valid locale code
        List<String> availableLocales = SpecificLocaleSetting.getAvailableLocaleCodes();
        for (String code : availableLocales) {
            if (code.equalsIgnoreCase(setting)) {
                return Optional.of(new SpecificLocaleSetting(code));
            }
        }

        return Optional.empty();
    }
}
