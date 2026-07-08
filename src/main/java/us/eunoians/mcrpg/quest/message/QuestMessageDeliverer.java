package us.eunoians.mcrpg.quest.message;

import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Delivers quest-related messages to players using locale resolution with inline MiniMessage fallback.
 * <p>
 * Resolution order:
 * <ol>
 *   <li>If a locale key is provided and the player's McRPGPlayer is available, resolve via
 *       {@link McRPGLocalizationManager#getLocalizedMessageAsComponent}</li>
 *   <li>On locale resolution failure, or if no key is provided, parse and send inline MiniMessage strings</li>
 * </ol>
 */
public class QuestMessageDeliverer {

    @NotNull
    private final McRPGLocalizationManager localizationManager;
    @NotNull
    private final MiniMessage miniMessage;
    @NotNull
    private final Logger logger;

    /**
     * Creates a new message deliverer.
     *
     * @param localizationManager the localization manager for locale key resolution
     * @param miniMessage         the MiniMessage instance for parsing inline strings
     * @param logger              the logger for warning/error output
     */
    public QuestMessageDeliverer(@NotNull McRPGLocalizationManager localizationManager,
                                 @NotNull MiniMessage miniMessage,
                                 @NotNull Logger logger) {
        this.localizationManager = localizationManager;
        this.miniMessage = miniMessage;
        this.logger = logger;
    }

    /**
     * Delivers a message to a player using a pre-parsed locale {@link Route}. Attempts locale
     * resolution first; falls back to inline MiniMessage strings on failure or if no route is provided.
     * <p>
     * Prefer this overload in hot paths where the same route is delivered to multiple players
     * (e.g., in the per-player loop of {@link us.eunoians.mcrpg.listener.quest.QuestStartMessageListener})
     * so that the route is parsed only once outside the loop rather than once per player.
     *
     * @param player         the Bukkit player to send messages to
     * @param mcRPGPlayer    the McRPG player wrapper (nullable — if null, skips locale resolution)
     * @param localeRoute    the pre-parsed locale route (nullable — if null, uses inline messages)
     * @param inlineMessages fallback MiniMessage strings to send if locale resolution fails
     */
    public void deliver(@NotNull Player player,
                        @Nullable McRPGPlayer mcRPGPlayer,
                        @Nullable Route localeRoute,
                        @NotNull List<String> inlineMessages) {
        if (localeRoute != null && mcRPGPlayer != null) {
            try {
                Component resolved = localizationManager.getLocalizedMessageAsComponent(mcRPGPlayer, localeRoute);
                player.sendMessage(resolved);
                return;
            } catch (Exception e) {
                logger.log(Level.WARNING,
                        "Failed to resolve localization route '" + localeRoute + "' for player " + player.getName()
                                + " — falling back to inline messages", e);
            }
        }
        sendInlineMessages(player, inlineMessages);
    }

    /**
     * Delivers a message to a player. Attempts locale key resolution first; falls back to inline
     * MiniMessage strings on failure or if no key is provided.
     * <p>
     * Prefer {@link #deliver(Player, McRPGPlayer, Route, List)} when the same key is delivered to
     * multiple players in a loop — it avoids re-parsing the route on each iteration.
     *
     * @param player         the Bukkit player to send messages to
     * @param mcRPGPlayer    the McRPG player wrapper (nullable — if null, skips locale resolution)
     * @param localeKey      the locale route key string (nullable — if null or empty, uses inline)
     * @param inlineMessages fallback MiniMessage strings to send if locale resolution fails
     */
    public void deliver(@NotNull Player player,
                        @Nullable McRPGPlayer mcRPGPlayer,
                        @Nullable String localeKey,
                        @NotNull List<String> inlineMessages) {
        Route route = (localeKey != null && !localeKey.isEmpty()) ? Route.fromString(localeKey) : null;
        deliver(player, mcRPGPlayer, route, inlineMessages);
    }

    /**
     * Sends inline MiniMessage strings to the player. Each message is independently parsed
     * so a malformed message does not abort delivery of subsequent messages.
     *
     * @param player   the player to send messages to
     * @param messages the inline MiniMessage strings to parse and send
     */
    private void sendInlineMessages(@NotNull Player player, @NotNull List<String> messages) {
        for (String inline : messages) {
            try {
                player.sendMessage(miniMessage.deserialize(inline));
            } catch (Exception e) {
                logger.log(Level.WARNING,
                        "Failed to parse inline MiniMessage string: " + inline, e);
            }
        }
    }
}
