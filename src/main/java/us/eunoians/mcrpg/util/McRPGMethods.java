package us.eunoians.mcrpg.util;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryKey;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.registry.plugin.McRPGPluginHookKey;

/**
 * A collection of various utility methods that may be shared across McRPG.
 */
public class McRPGMethods {

    private static final String MCRPG_NAMESPACED_KEY = "mcrpg";

    /**
     * Gets the namespace for this plugin, allowing instantiation
     * of {@link org.bukkit.NamespacedKey}s for {@link McRPG} without requiring
     * an instance of it.
     *
     * @return The namespace for this plugin.
     */
    @NotNull
    public static String getMcRPGNamespace() {
        return MCRPG_NAMESPACED_KEY;
    }

    /**
     * Translates the provided message into a {@link Component}.
     *
     * @param message The message to be translated.
     * @return A {@link Component} version of the message.
     */
    @NotNull
    public static Component translate(@NotNull String message) {
        return McRPG.getInstance().getMiniMessage().deserialize(message);
    }

    /**
     * Consumes a {@link Parser} and runs it through {@link PlaceholderAPI} if PAPI is enabled,
     * allowing for the use of PAPI placeholders in parser equations.
     *
     * @param parser        The {@link Parser} to have placeholders replaced.
     * @param offlinePlayer The {@link OfflinePlayer} to use for PAPI's context.
     * @return A {@link Parser} with any PAPI placeholders replaced if PAPI is enabled, or the parser
     * originally passed in.
     */
    @NotNull
    public static Parser parseWithPapi(@NotNull Parser parser, @NotNull OfflinePlayer offlinePlayer) {
        return McRPG.getInstance().registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(McRPGPluginHookKey.PAPI).map(papiHook -> new Parser(papiHook.translateMessage(offlinePlayer, parser.getInputString()))).orElse(parser);
    }

    /**
     * Runs a plain string through {@link PlaceholderAPI} if PAPI is enabled, replacing any
     * {@code %placeholder%} tokens for the given player. When PAPI is not installed the
     * original string is returned unchanged.
     *
     * @param message the string to resolve placeholders in
     * @param player  the {@link OfflinePlayer} whose context PAPI uses for resolution
     * @return the string with PAPI placeholders replaced, or the original string if PAPI is absent
     */
    @NotNull
    public static String applyPapi(@NotNull String message, @NotNull OfflinePlayer player) {
        return McRPG.getInstance().registryAccess()
                .registry(RegistryKey.PLUGIN_HOOK)
                .pluginHook(McRPGPluginHookKey.PAPI)
                .map(papiHook -> papiHook.translateMessage(player, message))
                .orElse(message);
    }

    /**
     * Parses a {@link NamespacedKey} from a config string. Supports both fully-qualified
     * {@code "namespace:key"} form and bare keys, which are automatically namespaced under
     * {@code mcrpg:}. The input is lowercased before parsing.
     *
     * @param input the string to parse; may be {@code null} or blank
     * @return the parsed key, or {@code null} if {@code input} is {@code null} or blank
     */
    @Nullable
    public static NamespacedKey parseNamespacedKey(@Nullable String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        if (input.contains(":")) {
            return NamespacedKey.fromString(input.toLowerCase());
        }
        return new NamespacedKey(getMcRPGNamespace(), input.toLowerCase());
    }

    /**
     * Formats a millisecond duration into a compact human-readable string.
     * Examples: {@code "2h 30m"}, {@code "45m"}, {@code "<1m"}.
     *
     * @param millis the duration in milliseconds
     * @return a formatted duration string
     */
    @NotNull
    public static String formatDuration(long millis) {
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h");
        }
        if (minutes > 0) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append(minutes).append("m");
        }
        if (sb.isEmpty()) {
            sb.append("<1m");
        }
        return sb.toString();
    }
}
