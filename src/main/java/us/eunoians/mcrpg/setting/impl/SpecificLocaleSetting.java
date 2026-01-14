package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.setting.slot.LocaleSettingSlot;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A locale setting that stores a specific locale code rather than using an enum constant.
 * <p>
 * This allows players to select any available locale dynamically, not just predefined options.
 * The setting stores the locale code (e.g., "en", "fr") as the value.
 */
public final class SpecificLocaleSetting implements LocalePlayerSetting {

    private final String localeCode;
    private final Locale locale;

    /**
     * Creates a new {@link SpecificLocaleSetting} for the given locale code.
     *
     * @param localeCode The locale code (e.g., "en", "en_US", "fr").
     */
    public SpecificLocaleSetting(@NotNull String localeCode) {
        this.localeCode = localeCode;
        String[] parts = localeCode.split("_");
        if (parts.length > 1) {
            this.locale = Locale.of(parts[0], parts[1]);
        } else {
            this.locale = Locale.of(parts[0]);
        }
    }

    /**
     * Gets the locale code stored in this setting.
     *
     * @return The locale code (e.g., "en", "fr").
     */
    @NotNull
    public String getLocaleCode() {
        return localeCode;
    }

    /**
     * Gets the {@link Locale} represented by this setting.
     *
     * @return The {@link Locale}.
     */
    @NotNull
    public Locale getLocale() {
        return locale;
    }

    @NotNull
    @Override
    public LinkedNode<? extends PlayerSetting> getFirstSetting() {
        return LocaleSetting.CLIENT_LOCALE.getFirstSetting();
    }

    @NotNull
    @Override
    public LinkedNode<? extends PlayerSetting> getNextSetting() {
        return LocaleSettingChain.getNextSettingNode(this);
    }

    @NotNull
    @Override
    public LocaleSettingSlot getSettingSlot(@NotNull McRPGPlayer player) {
        return new LocaleSettingSlot(player, this);
    }

    @NotNull
    @Override
    public Optional<? extends PlayerSetting> fromString(@NotNull String setting) {
        // Check if it's one of the enum values first
        if (setting.equalsIgnoreCase("CLIENT_LOCALE")) {
            return Optional.of(LocaleSetting.CLIENT_LOCALE);
        }
        if (setting.equalsIgnoreCase("SERVER_LOCALE")) {
            return Optional.of(LocaleSetting.SERVER_LOCALE);
        }

        // Check if it's a valid locale code from available locales
        List<String> availableLocaleCodes = getAvailableLocaleCodes();
        for (String code : availableLocaleCodes) {
            if (code.equalsIgnoreCase(setting)) {
                return Optional.of(new SpecificLocaleSetting(code));
            }
        }

        return Optional.empty();
    }

    @NotNull
    @Override
    public String name() {
        return localeCode;
    }

    /**
     * Gets a list of all unique available locale codes.
     * <p>
     * This queries the localization manager for all registered locales, which includes
     * both dynamically loaded locale files and any locales registered by third-party plugins.
     * <p>
     * Multiple files can share the same locale code (e.g., en_commands.yml and en_gui.yml
     * both with {@code locale: en}). This method returns each unique locale code only once.
     *
     * @return A list of unique locale codes (e.g., "en", "fr").
     */
    @NotNull
    public static List<String> getAvailableLocaleCodes() {
        // Query the localization manager for all registered locales
        // This includes both file-based locales and any registered by third-party plugins
        Set<Locale> registeredLocales = McRPG.getInstance()
                .registryAccess()
                .registry(com.diamonddagger590.mccore.registry.RegistryKey.MANAGER)
                .manager(us.eunoians.mcrpg.registry.manager.McRPGManagerKey.LOCALIZATION)
                .getRegisteredLocales();

        return registeredLocales.stream()
                .map(Locale::toString)
                .filter(code -> !code.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpecificLocaleSetting that = (SpecificLocaleSetting) o;
        return Objects.equals(localeCode, that.localeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localeCode);
    }
}
