package us.eunoians.mcrpg.display.hud;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A single piece of content that can occupy the action bar HUD's center zone.
 * <p>
 * Implementations are evaluated once per HUD tick. The returned {@link Optional}
 * controls slot visibility:
 * <ul>
 *     <li>A present value is rendered as the center content for that frame.</li>
 *     <li>An empty value signals the content has completed its lifecycle
 *         (expired, cleared, etc.); the owning {@link ActionBarHudDisplay}
 *         evicts the slot and falls through to the next-highest-priority
 *         slot.</li>
 * </ul>
 * Instances are expected to be effectively immutable after construction. Time-
 * driven behaviour (expiry, countdown) is driven by the {@code currentTick}
 * argument, not by internal mutable state, so repeated renders are idempotent
 * for a given tick.
 */
public interface ActionBarCenterContent {

    /**
     * Renders this content for the provided server tick.
     *
     * @param currentTick The current server tick.
     * @return An {@link Optional} containing the rendered component, or empty
     * if the content is no longer active.
     */
    @NotNull
    Optional<Component> render(long currentTick);

    /**
     * Measures the rendered pixel width of this content against {@code widths}.
     * The {@link ActionBarHudDisplay} needs this value every frame to compute
     * center-zone padding; the default implementation simply flattens the
     * rendered component but implementations backed by immutable content or
     * coarse-grained state transitions (per-second countdowns, static
     * messages, combo dots that only change on input) should override this to
     * memoise the width and skip the per-frame flatten.
     *
     * @param widths      The {@link FontWidthTable} used to measure widths.
     * @param currentTick The current server tick.
     * @return The rendered pixel width, or {@code 0} if the content renders
     * empty for {@code currentTick}.
     */
    default int getPixelWidth(@NotNull FontWidthTable widths, long currentTick) {
        return render(currentTick).map(widths::getWidth).orElse(0);
    }
}
