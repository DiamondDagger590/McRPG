package us.eunoians.mcrpg.ability.combo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

/**
 * Handles the subtitle display that shows a player's current combo progress.
 * <p>
 * The subtitle uses filled circles (⬤) to show entered inputs and empty circles (○)
 * for remaining slots, colour-coded by input type:
 * <ul>
 *   <li>Gold ⬤ = Right click</li>
 *   <li>Aqua ⬤ = Left click</li>
 *   <li>Dark grey ○ = Not yet entered</li>
 * </ul>
 * <p>
 * Example after entering R then R for a 3-click pattern:
 * {@code <gold>⬤ ⬤</gold> <dark_gray>○</dark_gray>}
 */
public final class ComboDisplayManager {

    private static final String FILLED_CIRCLE = "⬤";
    private static final String EMPTY_CIRCLE = "○";

    /** How long the subtitle stays visible between refreshes (ms). Refreshed on each input. */
    private static final long HOLD_DURATION_MS = 1200L;

    /** Fade-out duration (ms) when no new input arrives. */
    private static final long FADE_OUT_MS = 300L;

    private ComboDisplayManager() {}

    /**
     * Sends (or refreshes) the subtitle showing the given combo sequence progress.
     * <p>
     * The total expected length is inferred from the maximum pattern length (3).
     * Fade-in is always zero so updates appear instantly.
     *
     * @param player   The player to show the subtitle to.
     * @param sequence The inputs entered so far.
     */
    public static void updateDisplay(@NotNull Player player, @NotNull List<ComboInput> sequence) {
        int totalLength = ComboPattern.SLOT_1.getLength(); // All patterns have the same length (3)
        Component subtitle = buildSubtitle(sequence, totalLength);

        Title title = Title.title(
                Component.empty(),
                subtitle,
                Title.Times.times(Duration.ZERO, Duration.ofMillis(HOLD_DURATION_MS), Duration.ofMillis(FADE_OUT_MS))
        );
        player.showTitle(title);
    }

    /**
     * Clears the subtitle by resetting the player's title display.
     *
     * @param player The player whose subtitle should be cleared.
     */
    public static void clearDisplay(@NotNull Player player) {
        player.resetTitle();
    }

    @NotNull
    private static Component buildSubtitle(@NotNull List<ComboInput> sequence, int totalLength) {
        Component result = Component.empty();
        for (int i = 0; i < totalLength; i++) {
            if (i > 0) {
                result = result.append(Component.text(" "));
            }
            if (i < sequence.size()) {
                ComboInput input = sequence.get(i);
                NamedTextColor color = (input == ComboInput.RIGHT) ? NamedTextColor.GOLD : NamedTextColor.AQUA;
                result = result.append(Component.text(FILLED_CIRCLE, color));
            } else {
                result = result.append(Component.text(EMPTY_CIRCLE, NamedTextColor.DARK_GRAY));
            }
        }
        return result;
    }
}
