package us.eunoians.mcrpg.quest.chain.availability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Configuration for time-based availability on a chain, quest, or board template.
 *
 * @param windows       named windows (at least one required)
 * @param timezone      the timezone for evaluating window boundaries
 * @param onWindowClose policy for active instances when the window closes
 *                      (only applicable to chains and quests, ignored for board templates)
 * @param gracePeriod   grace period duration for {@code EXPIRE_WITH_GRACE} policy
 *                      (null unless policy is EXPIRE_WITH_GRACE)
 */
public record AvailabilityConfig(
        @NotNull Map<String, AvailabilityWindowDefinition> windows,
        @NotNull ZoneId timezone,
        @NotNull WindowClosePolicy onWindowClose,
        @Nullable Duration gracePeriod
) {

    /**
     * Checks whether any window in this config is currently active.
     * Accepts the current time as a parameter to preserve testability
     * via {@link com.diamonddagger590.mccore.util.TimeProvider} — callers pass
     * {@code timeProvider.now().atZone(config.timezone())}.
     *
     * @param now the current time in the config's timezone
     * @return {@code true} if at least one window is active
     */
    public boolean isCurrentlyAvailable(@NotNull ZonedDateTime now) {
        return windows.values().stream().anyMatch(w -> w.isActive(now));
    }
}
