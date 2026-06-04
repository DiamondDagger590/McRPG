package us.eunoians.mcrpg.quest.chain.availability;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;

/**
 * A named time window with a start and end boundary. Handles year-wrapping
 * for recurring windows (e.g., December 1 to January 3).
 *
 * @param name  server-owner-defined name for this window
 * @param from  the window start boundary
 * @param until the window end boundary
 */
public record AvailabilityWindowDefinition(
        @NotNull String name,
        @NotNull WindowBoundary from,
        @NotNull WindowBoundary until
) {

    /**
     * Checks whether the given time falls within this window.
     * For recurring windows that wrap around the year (e.g., Dec 1 to Jan 3):
     * resolves both boundaries in the reference year first. If from &gt; until
     * in the same year, the window wraps: {@code now >= from || now <= until}.
     *
     * @param now the current time in the window's timezone
     * @return {@code true} if the current time is within the window
     */
    public boolean isActive(@NotNull ZonedDateTime now) {
        ZonedDateTime start = from.toZonedDateTime(now, 0);
        ZonedDateTime end = until.toZonedDateTime(now, 0);

        if (!start.isAfter(end)) {
            return !now.isBefore(start) && !now.isAfter(end);
        } else {
            return !now.isBefore(start) || !now.isAfter(end);
        }
    }
}
