package us.eunoians.mcrpg.quest.board.refresh.builtin;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.board.refresh.RefreshType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;

/**
 * Time-based refresh type that triggers once per week on a configurable reset day.
 * <p>
 * The epoch value represents the ISO week-of-year combined with the year, encoded as
 * {@code year * 100 + weekOfYear} to produce a monotonically increasing value.
 */
public final class WeeklyRefreshType extends RefreshType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "weekly");

    private final DayOfWeek resetDay;

    public WeeklyRefreshType(@NotNull DayOfWeek resetDay) {
        super(KEY);
        this.resetDay = resetDay;
    }

    @NotNull
    public DayOfWeek getResetDay() {
        return resetDay;
    }

    @Override
    public boolean isTimeBased() {
        return true;
    }

    @Override
    public boolean shouldRefresh(long lastRefreshEpoch, @NotNull ZonedDateTime now) {
        if (now.getDayOfWeek() != resetDay) {
            return false;
        }
        long currentEpoch = computeEpoch(now);
        return currentEpoch > lastRefreshEpoch;
    }

    /**
     * Computes the weekly epoch for a given point in time, encoded as {@code year * 100 + weekOfYear}.
     *
     * @param dateTime the date/time to compute for
     * @return the weekly epoch value
     */
    public static long computeEpoch(@NotNull ZonedDateTime dateTime) {
        int year = dateTime.get(IsoFields.WEEK_BASED_YEAR);
        int week = dateTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return (long) year * 100 + week;
    }
}
