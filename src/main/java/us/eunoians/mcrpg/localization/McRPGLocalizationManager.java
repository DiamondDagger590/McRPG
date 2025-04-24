package us.eunoians.mcrpg.localization;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.localization.LocalizationManager;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.exception.localization.LocaleParseException;
import us.eunoians.mcrpg.exception.localization.NoLocalizationContainsMessageException;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.impl.LocaleSetting;

import java.util.Locale;
import java.util.Optional;

/**
 * Any messages sent for McRPG should pass through here in order to be translated.
 * <p>
 * Players are able to have multiple locals available to them, called a "locale chain". Locale chains
 * allows searching for translations while having multiple fallback {@link Locale}s supported as it's expected
 * various languages have varying degrees of coverage.
 * <p>
 * A locale chain starts with the player's {@link LocaleSetting}, followed by the player's client {@link Locale},
 * then followed by the server's default locale, then finally followed by {@link Locale#ENGLISH}. The expectation
 * is that english will be a resilient fallback source of truth. If for some reason the entire locale chain is missing a
 * translation, then {@link NoLocalizationContainsMessageException} will be thrown.
 * <p>
 * Third party plugins can add their own configuration files to be included for localization by using {@link #registerLanguageFile(com.diamonddagger590.mccore.localization.Localization)}.
 */
public final class McRPGLocalizationManager extends LocalizationManager<McRPG, McRPGPlayer> {

    public McRPGLocalizationManager(McRPG mcRPG) {
        super(mcRPG);
    }

    /**
     * Gets the {@link Locale} from the provided {@link LocaleSetting}.
     *
     * @param localeSetting The {@link LocaleSetting} to pull a {@link Locale} from.
     * @return An {@link Optional} containing the {@link Locale} from the provided {@link LocaleSetting} if there is one.
     * Otherwise, the optional will be empty.
     */
    @NotNull
    private Optional<Locale> getLocaleFromSetting(@NotNull LocaleSetting localeSetting) {
        switch (localeSetting) {
            case CLIENT_LOCALE -> {
                // We don't return anything because this is next in the chain anyways so we can ignore it
                return Optional.empty();
            }
            case SERVER_LOCALE -> {
                // The locale chain's first node will contain the server default.
                // If this is the case, then order will essentially go server default -> client -> english
                return Optional.of(localeChain.getContent().getNodeValue());
            }
            default -> {
                return Optional.of(localeSetting.getNativeLocale().orElse(NativeLocale.ENGLISH).getLocale());
            }
        }
    }

    @NotNull
    @Override
    protected ReloadableContent<LinkedNode<Locale>> generateLocaleChain() {
        return new ReloadableContent<>(plugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG), MainConfigFile.SERVER_DEFAULT_LOCALE, ((yamlDocument, route) -> {
            String serverDefaultLocaleString = yamlDocument.getString(route);
            String[] locale = serverDefaultLocaleString.split("_");
            Locale serverDefaultLocale = locale.length > 1 ? Locale.of(locale[0], locale[1]) : Locale.of(locale[0]);
            if (serverDefaultLocale == null) {
                throw new LocaleParseException(serverDefaultLocaleString);

            }

            LinkedNode<Locale> serverDefaultLocaleNode = new LinkedNode<>(serverDefaultLocale);
            serverDefaultLocaleNode.setNext(new LinkedNode<>(Locale.ENGLISH));
            return serverDefaultLocaleNode;
        }));
    }
}
