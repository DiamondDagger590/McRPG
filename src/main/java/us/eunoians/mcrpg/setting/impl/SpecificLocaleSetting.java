package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.util.LinkedNode;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.setting.slot.LocaleSettingSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.McRPGSetting;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * A locale setting that stores a specific locale code rather than using an enum constant.
 * <p>
 * This allows players to select any available locale dynamically, not just predefined options.
 * The setting stores the locale code (e.g., "en", "fr") as the value.
 */
public final class SpecificLocaleSetting implements McRPGSetting {

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
    public NamespacedKey getSettingKey() {
        return LocaleSetting.SETTING_KEY;
    }

    @NotNull
    @Override
    public LinkedNode<? extends PlayerSetting> getFirstSetting() {
        return LocaleSetting.CLIENT_LOCALE.getFirstSetting();
    }

    @NotNull
    @Override
    public LinkedNode<? extends PlayerSetting> getNextSetting() {
        // Get all available locale codes and find the next one
        List<String> availableLocaleCodes = getAvailableLocaleCodes();
        int currentIndex = -1;

        for (int i = 0; i < availableLocaleCodes.size(); i++) {
            if (availableLocaleCodes.get(i).equals(this.localeCode)) {
                currentIndex = i;
                break;
            }
        }

        // If this is the last locale or not found, wrap around to CLIENT_LOCALE
        if (currentIndex == -1 || currentIndex >= availableLocaleCodes.size() - 1) {
            return new LinkedNode<>(LocaleSetting.CLIENT_LOCALE);
        }

        // Return the next available locale
        return new LinkedNode<>(new SpecificLocaleSetting(availableLocaleCodes.get(currentIndex + 1)));
    }

    @NotNull
    @Override
    public LocaleSettingSlot getSettingSlot(@NotNull McRPGPlayer player) {
        return new LocaleSettingSlot(player, this);
    }

    @Override
    public void onSettingChange(@NotNull CorePlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
        LocaleSetting.refreshPlayerSettingGui(player);
    }

    @NotNull
    @Override
    public Optional<? extends PlayerSetting> fromString(@NotNull String setting) {
        // Check if it's one of the enum values first
        if (setting.equalsIgnoreCase("CLIENT_LOCALE") || setting.equalsIgnoreCase("SERVER_LOCALE")) {
            return LocaleSetting.CLIENT_LOCALE.fromString(setting);
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
     * Gets a list of all unique available locale codes from the loaded localization files.
     * <p>
     * Multiple files can share the same locale code (e.g., en_commands.yml and en_gui.yml
     * both with {@code locale: en}). This method returns each unique locale code only once.
     *
     * @return A list of unique locale codes (e.g., "en", "fr").
     */
    @NotNull
    public static List<String> getAvailableLocaleCodes() {
        List<YamlDocument> localizationFiles = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getLocalizationFiles();

        return localizationFiles.stream()
                .map(doc -> doc.getString("locale"))
                .filter(Objects::nonNull)
                .filter(code -> !code.isBlank())
                .distinct()
                .toList();
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
