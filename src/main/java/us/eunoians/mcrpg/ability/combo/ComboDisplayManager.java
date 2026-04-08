package us.eunoians.mcrpg.ability.combo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Handles the action-bar display that shows a player's current combo progress.
 * <p>
 * Uses filled circles (⬤) for entered inputs and empty circles (○) for remaining
 * slots, colour-coded by input type:
 * <ul>
 *   <li>Gold ⬤ = Right click</li>
 *   <li>Aqua ⬤ = Left click</li>
 *   <li>Dark grey ○ = Not yet entered</li>
 * </ul>
 */
public final class ComboDisplayManager {

    private static final String FILLED_CIRCLE = "⬤";
    private static final String EMPTY_CIRCLE = "○";

    private ComboDisplayManager() {}

    /**
     * Sends (or refreshes) the action bar showing the given combo sequence progress.
     *
     * @param player   The player to show the action bar to.
     * @param sequence The inputs entered so far.
     */
    public static void updateDisplay(@NotNull Player player, @NotNull List<ComboInput> sequence) {
        int totalLength = ComboPattern.SLOT_1.getLength();
        player.sendActionBar(buildDisplay(sequence, totalLength));
    }

    /**
     * Clears the action bar by sending an empty component.
     *
     * @param player The player whose action bar should be cleared.
     */
    public static void clearDisplay(@NotNull Player player) {
        player.sendActionBar(Component.empty());
    }

    @NotNull
    private static Component buildDisplay(@NotNull List<ComboInput> sequence, int totalLength) {
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
