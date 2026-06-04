package us.eunoians.mcrpg.quest.chain.availability;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.ZonedDateTime;

/**
 * Represents one boundary (start or end) of an availability window.
 * Fixed boundaries have a specific year; recurring boundaries repeat yearly.
 * Third-party plugins may implement custom boundary types for advanced
 * scheduling needs (e.g., cron-based or external-calendar-driven boundaries).
 */
public interface WindowBoundary {

    /**
     * Resolves this boundary to a concrete {@link ZonedDateTime} in the context
     * of the given reference time. Fixed boundaries ignore the reference year.
     * Recurring boundaries resolve to the given year plus the offset.
     *
     * @param referenceNow the current time for year context
     * @param yearOffset   offset from the reference year (0 = same year, 1 = next year)
     * @return the resolved datetime
     */
    @NotNull
    ZonedDateTime toZonedDateTime(@NotNull ZonedDateTime referenceNow, int yearOffset);

    /**
     * A one-time boundary at a specific date and time (includes year).
     * Once the date passes, the window is permanently closed.
     *
     * @param dateTime the specific date and time for this boundary
     */
    record Fixed(@NotNull LocalDateTime dateTime) implements WindowBoundary {
        @Override
        @NotNull
        public ZonedDateTime toZonedDateTime(@NotNull ZonedDateTime referenceNow, int yearOffset) {
            return dateTime.atZone(referenceNow.getZone());
        }
    }

    /**
     * A yearly repeating boundary defined by month-day and time (no year).
     * Resolves to the reference year plus the given offset, enabling
     * year-wrapping windows (e.g., December 1 to January 3).
     *
     * @param monthDay the month and day for this boundary
     * @param time     the time of day for this boundary
     */
    record Recurring(@NotNull MonthDay monthDay, @NotNull LocalTime time) implements WindowBoundary {
        @Override
        @NotNull
        public ZonedDateTime toZonedDateTime(@NotNull ZonedDateTime referenceNow, int yearOffset) {
            int year = referenceNow.getYear() + yearOffset;
            return monthDay.atYear(year).atTime(time).atZone(referenceNow.getZone());
        }
    }
}
