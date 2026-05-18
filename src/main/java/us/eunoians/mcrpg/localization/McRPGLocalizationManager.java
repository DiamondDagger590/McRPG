package us.eunoians.mcrpg.localization;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.localization.LocalizationManager;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.util.LinkedNode;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.entity.Player;
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

import java.util.LinkedHashMap;
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
 * Third party plugins can add their own configuration files to be included for localization by using {@link #registerLanguageFile(com.diamonddagger590.mccore.localization.Localization)}.
 * <p>
 * Player-facing decimal formatting uses {@link #getDisplayDecimalFormatter()}, which is constructed by this manager
 * and reads {@link #getLocaleChain(McRPGPlayer)} / {@link #getServerDefaultLocale()} for locale resolution.
 */
public final class McRPGLocalizationManager extends LocalizationManager<McRPG, McRPGPlayer> {

    private final McRPGDisplayDecimalFormatter displayDecimalFormatter;
    private final ReloadableContent<Map<String, String>> paletteReplacements;

    public McRPGLocalizationManager(McRPG mcRPG) {
        super(mcRPG);
        this.displayDecimalFormatter = new McRPGDisplayDecimalFormatter(this);
        this.paletteReplacements = buildPaletteReplacements(mcRPG);
        mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.RELOADABLE_CONTENT)
                .trackReloadableContent(paletteReplacements);
    }

    /**
     * Builds the palette replacement map from {@code config.yml}. Each palette role
     * (e.g., {@code "primary"}) maps to its configured MiniMessage value
     * (e.g., {@code <color:#D4A76A>}). The map keys are the full placeholder strings
     * including angle brackets (e.g., {@code <primary>}).
     * <p>
     * Close tags (e.g., {@code </primary>}) are also mapped. When the configured value
     * is a MiniMessage color tag like {@code <color:#D4A76A>}, the close tag maps to the
     * corresponding MiniMessage close form ({@code </color:#D4A76A>}). For named tags like
     * {@code <gray>}, the close tag maps to {@code </gray>}. This ensures server owners can
     * use natural MiniMessage notation (e.g., {@code <primary>50%</primary>}) and have it
     * resolve correctly.
     *
     * @param mcRPG The plugin instance.
     * @return A {@link ReloadableContent} wrapping the palette replacement map.
     */
    @NotNull
    private ReloadableContent<Map<String, String>> buildPaletteReplacements(@NotNull McRPG mcRPG) {
        YamlDocument config = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.MAIN_CONFIG);
        return new ReloadableContent<>(config, MainConfigFile.PALETTE_PRIMARY, (doc, ignored) -> {
            Map<String, String> map = new LinkedHashMap<>();
            addPaletteEntry(map, "primary", doc.getString(MainConfigFile.PALETTE_PRIMARY, "<color:#D4A76A>"));
            addPaletteEntry(map, "hint", doc.getString(MainConfigFile.PALETTE_HINT, "<color:#E8C97A>"));
            addPaletteEntry(map, "mana", doc.getString(MainConfigFile.PALETTE_MANA, "<color:#5EA8FF>"));
            addPaletteEntry(map, "ability-active", doc.getString(MainConfigFile.PALETTE_ABILITY_ACTIVE, "<color:#FF7B5E>"));
            addPaletteEntry(map, "ability-passive", doc.getString(MainConfigFile.PALETTE_ABILITY_PASSIVE, "<color:#7FB87F>"));
            addPaletteEntry(map, "ability-innate", doc.getString(MainConfigFile.PALETTE_ABILITY_INNATE, "<color:#9E9E9E>"));
            addPaletteEntry(map, "body", doc.getString(MainConfigFile.PALETTE_BODY, "<gray>"));
            addPaletteEntry(map, "positive", doc.getString(MainConfigFile.PALETTE_POSITIVE, "<green>"));
            addPaletteEntry(map, "negative", doc.getString(MainConfigFile.PALETTE_NEGATIVE, "<red>"));
            addPaletteEntry(map, "warning", doc.getString(MainConfigFile.PALETTE_WARNING, "<yellow>"));
            return map;
        });
    }

    /**
     * Adds both the open and close tag entries for a palette role to the replacement map.
     * The close tag value is derived from the open tag value by inserting a {@code /} after
     * the opening {@code <} (e.g., {@code <gray>} → {@code </gray>}).
     *
     * @param map       The replacement map to populate.
     * @param roleName  The palette role name without angle brackets (e.g., {@code "primary"}).
     * @param openValue The configured MiniMessage open tag value (e.g., {@code <color:#D4A76A>}).
     */
    private void addPaletteEntry(@NotNull Map<String, String> map, @NotNull String roleName, @NotNull String openValue) {
        map.put("<" + roleName + ">", openValue);
        String closeValue = openValue.startsWith("<") ? "</" + openValue.substring(1) : openValue;
        map.put("</" + roleName + ">", closeValue);
    }

    /**
     * Returns the current palette replacement map. Used by slot classes when applying
     * tag replacements to {@link com.diamonddagger590.mccore.builder.item.impl.ItemBuilder}s
     * built from localized sections.
     *
     * @return The palette replacement map (keys are {@code <placeholder>}, values are MiniMessage strings).
     */
    @NotNull
    public Map<String, String> getPaletteReplacements() {
        return paletteReplacements.getContent();
    }

    /**
     * Applies palette color replacement to the given string. Replaces semantic
     * placeholders ({@code <primary>}, {@code <hint>}, etc.) with their configured
     * MiniMessage values ({@code <color:#D4A76A>}, etc.).
     * <p>
     * This is a public alias for {@link #postProcessResolvedString(String)} that slot
     * classes can call on dynamically-constructed lore lines that are not resolved
     * through the locale chain.
     *
     * @param raw The raw string potentially containing palette placeholders.
     * @return The string with palette placeholders resolved to MiniMessage color tags.
     */
    @NotNull
    public String resolvePaletteColors(@NotNull String raw) {
        return postProcessResolvedString(raw);
    }

    @NotNull
    @Override
    protected String postProcessResolvedString(@NotNull String raw) {
        String result = raw;
        for (Map.Entry<String, String> entry : paletteReplacements.getContent().entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Returns the shared formatter for locale-aware player-facing decimals. The manager constructs this instance;
     * the formatter resolves locales by calling {@link #getLocaleChain(McRPGPlayer)} and
     * {@link #getServerDefaultLocale()} internally.
     *
     * @return the display decimal formatter
     */
    @NotNull
    public McRPGDisplayDecimalFormatter getDisplayDecimalFormatter() {
        return displayDecimalFormatter;
    }

    /**
     * Returns the server's configured default {@link Locale} (the head of the server default locale chain).
     * Package-private so {@link McRPGDisplayDecimalFormatter} can use it as the fallback when formatting for
     * non-player audiences.
     *
     * @return the server default locale
     */
    @NotNull
    Locale getServerDefaultLocale() {
        return localeChain.getContent().getNodeValue();
    }

    /**
     * Resolves a literal MiniMessage template string by substituting the supplied placeholder map.
     * This is an overload of {@link #getLocalizedMessage} for inline display strings sourced from
     * quest YAML (e.g. {@code display.rewards.<label>}) where there is no {@link dev.dejvokep.boostedyaml.route.Route}
     * to look up — it applies the same {@code <key>} placeholder substitution semantics as the
     * route-based variant so that tokens like {@code <amount>} or {@code <skill>} resolve uniformly.
     * <p>
     * The returned string may contain MiniMessage styling tags (e.g. {@code <gold>}) which are
     * preserved as-is for downstream MiniMessage parsing by the GUI layer.
     *
     * @param template     raw template string containing {@code <key>} placeholders and optional
     *                     MiniMessage tags
     * @param placeholders map of placeholder key names to their substitution values
     * @return the template with all {@code <key>} tokens replaced by their corresponding values
     */
    @NotNull
    public String getLocalizedMessage(@NotNull String template, @NotNull Map<String, String> placeholders) {
        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        return result;
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
