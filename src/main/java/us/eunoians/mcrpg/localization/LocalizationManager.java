package us.eunoians.mcrpg.localization;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.util.LinkedNode;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.exception.localization.LocaleParseException;
import us.eunoians.mcrpg.exception.localization.NoLocalizationContainsMessageException;
import us.eunoians.mcrpg.setting.impl.LocaleSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
 * Third party plugins can add their own configuration files to be included for localization by using {@link #registerLanguageFile(McRPGLocalization)}.
 */
public final class LocalizationManager {

    private final McRPG mcRPG;
    private final Map<Locale, List<YamlDocument>> localizations;
    private final ReloadableContent<LinkedNode<Locale>> localeChain;

    public LocalizationManager(McRPG mcRPG) {
        this.mcRPG = mcRPG;
        this.localizations = new HashMap<>();
        this.localeChain = new ReloadableContent<>(mcRPG.getFileManager().getFile(FileType.MAIN_CONFIG), MainConfigFile.SERVER_DEFAULT_LOCALE, ((yamlDocument, route) -> {
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
        mcRPG.getReloadableContentRegistry().trackReloadableContent(localeChain);
    }

    /**
     * Gets a localized {@link Component} using the provided {@link Route} to find a translated message.
     *
     * @param player The {@link McRPGPlayer} to localize for.
     * @param route  The {@link Route} to check for a translated message.
     * @return A localized {@link Component} using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the player's locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public Component getLocalizedMessageAsComponent(@NotNull McRPGPlayer player, @NotNull Route route) {
        return mcRPG.getMiniMessage().deserialize(getLocalizedMessage(player, route));
    }

    /**
     * Gets a localized message using the provided {@link Route} to find a translated message.
     *
     * @param player The {@link McRPGPlayer} to localize for.
     * @param route  The {@link Route} to check for a translated message.
     * @return A localized message using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the player's locale
     *                                                chain that supports the provided route.
     */
    public String getLocalizedMessage(@NotNull McRPGPlayer player, @NotNull Route route) {
        LinkedNode<Locale> locales = getLocaleChain(player);
        Set<Locale> processedLocales = new HashSet<>();
        while (locales.hasNext()) {
            Locale locale = locales.getNodeValue();
            // We don't want to process locales twice
            if (processedLocales.contains(locale)) {
                continue;
            }
            // Mark that it has now been processed
            processedLocales.add(locale);
            // If we support this localization
            if (localizations.containsKey(locale)) {
                List<YamlDocument> documents = localizations.get(locale);
                // Check all registered configurations for the message
                for (YamlDocument yamlDocument : documents) {
                    if (yamlDocument.contains(route)) {
                        var papiHookOptional = mcRPG.getPapiHook();
                        var playerOptional = player.getAsBukkitPlayer();
                        String message = yamlDocument.getString(route);
                        if (papiHookOptional.isPresent() && playerOptional.isPresent()) {
                            message = papiHookOptional.get().translateMessage(playerOptional.get(), message);
                        }
                        return message;
                    }
                }
            }
        }
        // If we reach here, then that means no languages support the message which shouldn't be true.
        // English should always be supported.
        throw new NoLocalizationContainsMessageException(route, processedLocales);
    }

    @NotNull
    public Section getLocalizedSection(@NotNull McRPGPlayer player, @NotNull Route route) {
        LinkedNode<Locale> locales = getLocaleChain(player);
        Set<Locale> processedLocales = new HashSet<>();
        while (locales.hasNext()) {
            Locale locale = locales.getNodeValue();
            // We don't want to process locales twice
            if (processedLocales.contains(locale)) {
                continue;
            }
            // Mark that it has now been processed
            processedLocales.add(locale);
            // If we support this localization
            if (localizations.containsKey(locale)) {
                List<YamlDocument> documents = localizations.get(locale);
                // Check all registered configurations for the message
                for (YamlDocument yamlDocument : documents) {
                    if (yamlDocument.contains(route)) {
                        return yamlDocument.getSection(route);
                    }
                }
            }
        }
        // If we reach here, then that means no languages support the message which shouldn't be true.
        // English should always be supported.
        throw new NoLocalizationContainsMessageException(route, processedLocales);
    }

    /**
     * Gets the "locale chain" for the provided {@link McRPGPlayer}.
     *
     * @param mcRPGPlayer The {@link McRPGPlayer} to get the locale chain for.
     * @return The "locale chain" for the provided player. It's expected that there will be an end {@link LinkedNode} where
     * {@link LinkedNode#hasNext()} returns {@code false}.
     */
    @NotNull
    public LinkedNode<Locale> getLocaleChain(@NotNull McRPGPlayer mcRPGPlayer) {
        LocaleSetting localeSetting = (LocaleSetting) mcRPGPlayer.getPlayerSetting(LocaleSetting.SETTING_KEY).orElse(LocaleSetting.CLIENT_LOCALE);
        var localeOptional = getLocaleFromSetting(localeSetting);
        var clientLocaleOptional = getClientLocale(mcRPGPlayer);
        if (localeOptional.isPresent()) {
            LinkedNode<Locale> rootNode = new LinkedNode<>(localeOptional.get());
            if (clientLocaleOptional.isPresent()) {
                LinkedNode<Locale> clientLocaleNode = new LinkedNode<>(clientLocaleOptional.get());
                clientLocaleNode.setNext(localeChain.getContent());
                rootNode.setNext(clientLocaleNode);
            } else {
                rootNode.setNext(localeChain.getContent());
            }
            return rootNode;
        } else if (clientLocaleOptional.isPresent()) {
            LinkedNode<Locale> clientLocaleNode = new LinkedNode<>(clientLocaleOptional.get());
            clientLocaleNode.setNext(localeChain.getContent());
            return clientLocaleNode;
        } else {
            return localeChain.getContent();
        }
    }

    /**
     * Registers the provided {@link McRPGLocalization} to be supported by McRPG.
     *
     * @param mcRPGLocalization The {@link McRPGLocalization} to add to McRPG.
     */
    public void registerLanguageFile(@NotNull McRPGLocalization mcRPGLocalization) {
        localizations.computeIfAbsent(mcRPGLocalization.getLocale(), k -> new ArrayList<>()).add(mcRPGLocalization.getConfigurationFile());
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

    /**
     * Gets the {@link Locale} from the provided {@link McRPGPlayer}'s client.
     *
     * @param mcRPGPlayer The {@link McRPGPlayer} to get the client locale from.
     * @return An {@link Optional} containing the {@link McRPGPlayer}'s client {@link Locale}
     * if possible.
     */
    @NotNull
    private Optional<Locale> getClientLocale(@NotNull McRPGPlayer mcRPGPlayer) {
        return mcRPGPlayer.getAsBukkitPlayer().map(Player::locale);
    }
}
