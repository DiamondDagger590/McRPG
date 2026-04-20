package us.eunoians.mcrpg.display.impl;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * Base type for all per-player visual displays managed by the
 * {@link us.eunoians.mcrpg.display.DisplayManager}.
 * <p>
 * Concrete implementations render to a specific UI surface (boss bar, action
 * bar, title, etc.) and are responsible for their own lifecycle on
 * {@link #cleanDisplay()}. Per-player display state is stored directly on the
 * {@link McRPGPlayer} and keyed by the concrete base class in the player's
 * display container — the {@link us.eunoians.mcrpg.display.DisplayManager} acts
 * purely as a coordinator/factory and does not own the state.
 */
public abstract class PlayerDisplay {

    private final McRPGPlayer mcRPGPlayer;

    protected PlayerDisplay(@NotNull McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;
    }

    /**
     * Gets the {@link McRPGPlayer} this display is attached to.
     *
     * @return The owning {@link McRPGPlayer}.
     */
    @NotNull
    public final McRPGPlayer getMcRPGPlayer() {
        return mcRPGPlayer;
    }

    /**
     * Cleans up any external state (components, boss bars, scheduled tasks, etc.)
     * owned by this display. Invoked automatically when the display is replaced
     * or removed from the player's display container.
     */
    public abstract void cleanDisplay();
}
