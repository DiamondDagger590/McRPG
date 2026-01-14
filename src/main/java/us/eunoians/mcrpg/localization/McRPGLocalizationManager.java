package us.eunoians.mcrpg.localization;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.localization.LocalizationManager;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.setting.PlayerSetting;
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
import us.eunoians.mcrpg.setting.impl.LocalePlayerSetting;
import us.eunoians.mcrpg.setting.impl.SpecificLocaleSetting;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

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
     * Gets all locales that have been registered with this manager.
     * <p>
     * This includes locales from dynamically loaded locale files as well as
     * any locales registered by third-party plugins via {@link #registerLanguageFile(com.diamonddagger590.mccore.localization.Localization)}.
     *
     * @return An unmodifiable set of all registered locales.
     */
    @NotNull
    public Set<Locale> getRegisteredLocales() {
        return Set.copyOf(localizations.keySet());
    }

    /**
     * Gets the locale chain for a player, taking into account their locale setting.
     * <p>
     * The locale chain order depends on the player's setting:
     * <ul>
     *   <li>{@link LocaleSetting#CLIENT_LOCALE}: client locale -> server default -> english</li>
     *   <li>{@link LocaleSetting#SERVER_LOCALE}: server default -> client locale -> english</li>
     *   <li>{@link SpecificLocaleSetting}: specific locale -> client locale -> server default -> english</li>
     * </ul>
     *
     * @param corePlayer The player to get the locale chain for.
     * @return The locale chain for the player.
     */
    @NotNull
    @Override
    public LinkedNode<Locale> getLocaleChain(@NotNull McRPGPlayer corePlayer) {
        Optional<? extends PlayerSetting> settingOptional = corePlayer.getPlayerSetting(LocalePlayerSetting.SETTING_KEY);

        if (settingOptional.isPresent()) {
            PlayerSetting setting = settingOptional.get();

            if (setting instanceof SpecificLocaleSetting specificLocaleSetting) {
                // Specific locale -> client locale -> server default -> english
                LinkedNode<Locale> specificLocaleNode = new LinkedNode<>(specificLocaleSetting.getLocale());
                specificLocaleNode.setNext(super.getLocaleChain(corePlayer));
                return specificLocaleNode;
            } else if (setting instanceof LocaleSetting localeSetting) {
                return switch (localeSetting) {
                    case CLIENT_LOCALE -> super.getLocaleChain(corePlayer);
                    case SERVER_LOCALE -> {
                        // Server default first, then client locale, then english
                        // The base localeChain already has server default -> english
                        LinkedNode<Locale> serverFirstChain = new LinkedNode<>(localeChain.getContent().getNodeValue());

                        // Add client locale next if available
                        var clientLocaleOptional = corePlayer.getAsBukkitPlayer().map(org.bukkit.entity.Player::locale);
                        if (clientLocaleOptional.isPresent()) {
                            LinkedNode<Locale> clientLocaleNode = new LinkedNode<>(clientLocaleOptional.get());
                            clientLocaleNode.setNext(new LinkedNode<>(Locale.ENGLISH));
                            serverFirstChain.setNext(clientLocaleNode);
                        } else {
                            serverFirstChain.setNext(new LinkedNode<>(Locale.ENGLISH));
                        }

                        yield serverFirstChain;
                    }
                };
            }
        }

        // Default: use parent implementation (client locale -> server default -> english)
        return super.getLocaleChain(corePlayer);
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
