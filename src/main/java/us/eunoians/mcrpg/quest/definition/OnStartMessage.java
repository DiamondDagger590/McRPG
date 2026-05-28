package us.eunoians.mcrpg.quest.definition;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * A message sent to players when a quest starts. This is not a reward — on-start messages
 * are informational only, keeping them separate from tangible rewards so that future chain
 * cascade auto-complete logic can cleanly skip messages without risking dropped rewards.
 * <p>
 * Each entry is either backed by a locale key (resolved via the localization manager) or a
 * list of inline MiniMessage strings. Locale keys take priority — if present, inline messages
 * are ignored at delivery time.
 *
 * @param localeKey      optional locale route key resolved via the localization manager;
 *                       if present, {@code inlineMessages} is ignored
 * @param inlineMessages fallback MiniMessage strings sent directly when no locale key is set
 */
public record OnStartMessage(
        @NotNull Optional<String> localeKey,
        @NotNull List<String> inlineMessages
) {

    /**
     * Creates an on-start message backed by a locale key.
     *
     * @param localeKey the locale route key
     * @return a new {@link OnStartMessage}
     */
    @NotNull
    public static OnStartMessage fromLocaleKey(@NotNull String localeKey) {
        return new OnStartMessage(Optional.of(localeKey), List.of());
    }

    /**
     * Creates an on-start message with inline MiniMessage strings.
     *
     * @param messages the inline messages
     * @return a new {@link OnStartMessage}
     */
    @NotNull
    public static OnStartMessage fromInline(@NotNull List<String> messages) {
        return new OnStartMessage(Optional.empty(), List.copyOf(messages));
    }
}
