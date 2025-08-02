package us.eunoians.mcrpg.localization;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Locale;
import java.util.Optional;

/**
 * All {@link Locale}s that McRPG natively supports should be defined here.
 * <p>
 * Additionally, any time a new locale is added here then it also should be
 * added to {@link us.eunoians.mcrpg.setting.impl.LocaleSetting}.
 */
public enum NativeLocale implements McRPGLocalization {

    ENGLISH(Locale.ENGLISH, FileType.ENGLISH_LANGUAGE_FILE, "English"),
    ;

    private final Locale locale;
    private final FileType fileType;
    private final String localeName;

    NativeLocale(@NotNull Locale locale, @NotNull FileType fileType, @NotNull String localeName) {
        this.locale = locale;
        this.fileType = fileType;
        this.localeName = localeName;
    }

    /**
     * Gets the name used to display this locale.
     *
     * @return The name used to display this locale.
     */
    @NotNull
    public String getLocaleName() {
        return localeName;
    }

    @NotNull
    @Override
    public Locale getLocale() {
        return locale;
    }

    @NotNull
    @Override
    public YamlDocument getConfigurationFile() {
        return McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(fileType);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
