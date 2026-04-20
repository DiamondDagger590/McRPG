package us.eunoians.mcrpg.display.impl;

/**
 * Marker interface for any {@link PlayerDisplay} that wants to be driven by the
 * {@link us.eunoians.mcrpg.display.DisplayManager}'s per-tick loop.
 * <p>
 * Displays that are purely event-driven (for example {@code ExperienceDisplay}
 * variants that react to XP gains) should not implement this; only displays
 * that genuinely need per-tick work (HUD rendering, animation, countdown
 * resolution) should opt in.
 */
public interface TickablePlayerDisplay {

    /**
     * Called once per HUD tick for every registered {@link PlayerDisplay} that
     * implements this interface.
     *
     * @param currentTick    The current server tick (see
     *                       {@link org.bukkit.Bukkit#getCurrentTick()}).
     * @param secondsElapsed Real seconds elapsed since the previous tick,
     *                       suitable for time-based regen math.
     */
    void tick(long currentTick, double secondsElapsed);
}
