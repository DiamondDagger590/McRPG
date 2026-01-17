package us.eunoians.mcrpg.localization;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.Locale;
import java.util.Optional;

/**
 * A dynamically loaded locale that reads its locale code from a config value.
 * <p>
 * This allows locale files to be automatically discovered and loaded from a folder
 * without requiring manual registration in Java code.
 */
public class DynamicLocale implements McRPGLocalization {

    private final Locale locale;
    private final YamlDocument configurationFile;

    /**
     * Creates a new {@link DynamicLocale} from the provided {@link YamlDocument}.
     * The locale code is read from the "locale" key in the file.
     *
     * @param configurationFile The {@link YamlDocument} containing the locale data.
     * @throws IllegalArgumentException If the file does not contain a "locale" key.
     */
    public DynamicLocale(@NotNull YamlDocument configurationFile) {
        this.configurationFile = configurationFile;

        String localeString = configurationFile.getString("locale");
        if (localeString == null || localeString.isBlank()) {
            throw new IllegalArgumentException("Locale file must contain a 'locale' key with a valid locale code");
        }

        // Parse locale string (supports both "en" and "en_US" formats)
        String[] parts = localeString.split("_");
        if (parts.length > 1) {
            this.locale = Locale.of(parts[0], parts[1]);
        } else {
            this.locale = Locale.of(parts[0]);
        }
    }

    @NotNull
    @Override
    public Locale getLocale() {
        return locale;
    }

    @NotNull
    @Override
    public YamlDocument getConfigurationFile() {
        return configurationFile;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
