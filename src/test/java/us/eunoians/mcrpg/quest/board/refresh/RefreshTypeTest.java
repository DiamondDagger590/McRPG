package us.eunoians.mcrpg.quest.board.refresh;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.refresh.builtin.DailyRefreshType;
import us.eunoians.mcrpg.quest.board.refresh.builtin.WeeklyRefreshType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RefreshTypeTest extends McRPGBaseTest {

    // DailyRefreshType tests

    @DisplayName("DailyRefreshType isTimeBased returns true")
    @Test
    void dailyRefreshType_isTimeBased_returnsTrue() {
        DailyRefreshType daily = new DailyRefreshType();
        assertTrue(daily.isTimeBased());
    }

    @DisplayName("DailyRefreshType shouldRefresh with same-day epoch returns false")
    @Test
    void dailyRefreshType_shouldRefresh_sameDayEpoch_returnsFalse() {
        DailyRefreshType daily = new DailyRefreshType();
        ZonedDateTime monday = ZonedDateTime.of(2026, 2, 23, 12, 0, 0, 0, ZoneId.of("UTC"));
        long sameDayEpoch = monday.toLocalDate().toEpochDay();
        assertFalse(daily.shouldRefresh(sameDayEpoch, monday));
    }

    @DisplayName("DailyRefreshType shouldRefresh with previous-day epoch returns true")
    @Test
    void dailyRefreshType_shouldRefresh_previousDayEpoch_returnsTrue() {
        DailyRefreshType daily = new DailyRefreshType();
        ZonedDateTime monday = ZonedDateTime.of(2026, 2, 23, 12, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime sunday = ZonedDateTime.of(2026, 2, 22, 12, 0, 0, 0, ZoneId.of("UTC"));
        long previousDayEpoch = sunday.toLocalDate().toEpochDay();
        assertTrue(daily.shouldRefresh(previousDayEpoch, monday));
    }

    @DisplayName("DailyRefreshType getKey returns correct key")
    @Test
    void dailyRefreshType_getKey_returnsCorrectKey() {
        DailyRefreshType daily = new DailyRefreshType();
        NamespacedKey expected = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "daily");
        assertEquals(expected, daily.getKey());
    }

    // WeeklyRefreshType tests

    @DisplayName("WeeklyRefreshType constructor takes DayOfWeek resetDay")
    @Test
    void weeklyRefreshType_constructor_takesResetDay() {
        WeeklyRefreshType weekly = new WeeklyRefreshType(DayOfWeek.MONDAY);
        assertEquals(DayOfWeek.MONDAY, weekly.getResetDay());
    }

    @DisplayName("WeeklyRefreshType isTimeBased returns true")
    @Test
    void weeklyRefreshType_isTimeBased_returnsTrue() {
        WeeklyRefreshType weekly = new WeeklyRefreshType(DayOfWeek.MONDAY);
        assertTrue(weekly.isTimeBased());
    }

    @DisplayName("WeeklyRefreshType shouldRefresh with same-week epoch and on reset day returns false")
    @Test
    void weeklyRefreshType_shouldRefresh_sameWeekOnResetDay_returnsFalse() {
        WeeklyRefreshType weekly = new WeeklyRefreshType(DayOfWeek.MONDAY);
        ZonedDateTime monday = ZonedDateTime.of(2026, 2, 23, 12, 0, 0, 0, ZoneId.of("UTC")); // Monday
        long sameWeekEpoch = WeeklyRefreshType.computeEpoch(monday);
        assertFalse(weekly.shouldRefresh(sameWeekEpoch, monday));
    }

    @DisplayName("WeeklyRefreshType shouldRefresh with previous-week epoch and on reset day returns true")
    @Test
    void weeklyRefreshType_shouldRefresh_previousWeekOnResetDay_returnsTrue() {
        WeeklyRefreshType weekly = new WeeklyRefreshType(DayOfWeek.MONDAY);
        ZonedDateTime monday = ZonedDateTime.of(2026, 2, 23, 12, 0, 0, 0, ZoneId.of("UTC")); // Monday
        ZonedDateTime lastMonday = ZonedDateTime.of(2026, 2, 16, 12, 0, 0, 0, ZoneId.of("UTC"));
        long previousWeekEpoch = WeeklyRefreshType.computeEpoch(lastMonday);
        assertTrue(weekly.shouldRefresh(previousWeekEpoch, monday));
    }

    @DisplayName("WeeklyRefreshType shouldRefresh on non-reset day returns false even if epoch is old")
    @Test
    void weeklyRefreshType_shouldRefresh_nonResetDay_returnsFalse() {
        WeeklyRefreshType weekly = new WeeklyRefreshType(DayOfWeek.MONDAY);
        // Tuesday - not reset day
        ZonedDateTime tuesday = ZonedDateTime.of(2026, 2, 24, 12, 0, 0, 0, ZoneId.of("UTC"));
        long twoWeeksAgoEpoch = WeeklyRefreshType.computeEpoch(
                ZonedDateTime.of(2026, 2, 9, 12, 0, 0, 0, ZoneId.of("UTC")));
        assertFalse(weekly.shouldRefresh(twoWeeksAgoEpoch, tuesday));
    }

    @DisplayName("WeeklyRefreshType computeEpoch returns year*100 + weekOfYear")
    @Test
    void weeklyRefreshType_computeEpoch_returnsYearTimes100PlusWeekOfYear() {
        ZonedDateTime monday = ZonedDateTime.of(2026, 2, 23, 12, 0, 0, 0, ZoneId.of("UTC"));
        long epoch = WeeklyRefreshType.computeEpoch(monday);
        // ISO week-based year 2026, week 9
        assertEquals(202600 + 9, epoch);
    }
}
