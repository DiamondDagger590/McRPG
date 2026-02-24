package us.eunoians.mcrpg.quest.board.refresh;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;

/**
 * Abstract class representing a category refresh trigger.
 * <p>
 * Phase 1 ships two time-based built-in types ({@code mcrpg:daily} and {@code mcrpg:weekly}).
 * The extension point supports future event-driven types (e.g., world boss respawn) that
 * register Bukkit event listeners instead of being polled by the rotation task.
 */
public abstract class RefreshType {

    private final NamespacedKey key;

    protected RefreshType(@NotNull NamespacedKey key) {
        this.key = key;
    }

    /**
     * Gets the unique key identifying this refresh type.
     *
     * @return the refresh type key
     */
    @NotNull
    public final NamespacedKey getKey() {
        return key;
    }

    /**
     * Whether this refresh type is time-based (polled by the rotation task) or
     * event-driven (triggered externally via Bukkit events).
     *
     * @return {@code true} if time-based
     */
    public abstract boolean isTimeBased();

    /**
     * For time-based types: checks whether a refresh should trigger, given the last
     * refresh epoch and the current time in the configured timezone.
     * Event-driven types return {@code false}.
     *
     * @param lastRefreshEpoch the epoch value from the last refresh
     * @param now              the current time in the server's configured timezone
     * @return {@code true} if a refresh should trigger
     */
    public abstract boolean shouldRefresh(long lastRefreshEpoch, @NotNull ZonedDateTime now);
}
