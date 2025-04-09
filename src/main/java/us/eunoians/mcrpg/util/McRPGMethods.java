package us.eunoians.mcrpg.util;

import com.diamonddagger590.mccore.parser.Parser;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.text.NumberFormat;

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

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
    static {
        NUMBER_FORMAT.setMaximumFractionDigits(2);
        NUMBER_FORMAT.setMinimumFractionDigits(1);
    }

    @NotNull
    public static NumberFormat getChanceNumberFormat() {
        return NUMBER_FORMAT;
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
        return McRPG.getInstance().getPapiHook().map(papiHook -> new Parser(papiHook.translateMessage(offlinePlayer, parser.getInputString()))).orElse(parser);
    }
}
