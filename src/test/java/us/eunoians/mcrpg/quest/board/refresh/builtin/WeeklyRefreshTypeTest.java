package us.eunoians.mcrpg.quest.board.refresh.builtin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("WeeklyRefreshType")
class WeeklyRefreshTypeTest {

    private final WeeklyRefreshType weekly = new WeeklyRefreshType(DayOfWeek.MONDAY);

    @Nested
    @DisplayName("getKey")
    class GetKey {

        @Test
        @DisplayName("returns mcrpg:weekly")
        void getKey_returnsMcrpgWeekly() {
            assertEquals("mcrpg", weekly.getKey().getNamespace());
            assertEquals("weekly", weekly.getKey().getKey());
        }
    }

    @Nested
    @DisplayName("isTimeBased")
    class IsTimeBased {

        @Test
        @DisplayName("returns true")
        void isTimeBased_returnsTrue() {
            assertTrue(weekly.isTimeBased());
        }
    }

    @Nested
    @DisplayName("getResetDay")
    class GetResetDay {

        @ParameterizedTest
        @EnumSource(DayOfWeek.class)
        @DisplayName("returns configured reset day")
        void getResetDay_returnsConfiguredDay(DayOfWeek day) {
            WeeklyRefreshType type = new WeeklyRefreshType(day);
            assertEquals(day, type.getResetDay());
        }
    }

    @Nested
    @DisplayName("shouldRefresh")
    class ShouldRefresh {

        @Test
        @DisplayName("returns true on reset day when epoch is newer")
        void shouldRefresh_resetDayNewerEpoch_returnsTrue() {
            // 2025-07-14 is a Monday
            ZonedDateTime now = ZonedDateTime.of(2025, 7, 14, 10, 0, 0, 0, ZoneId.of("UTC"));
            long lastRefreshEpoch = WeeklyRefreshType.computeEpoch(
                    ZonedDateTime.of(2025, 7, 7, 10, 0, 0, 0, ZoneId.of("UTC")));
            assertTrue(weekly.shouldRefresh(lastRefreshEpoch, now));
        }

        @Test
        @DisplayName("returns false on non-reset day")
        void shouldRefresh_nonResetDay_returnsFalse() {
            // 2025-07-15 is a Tuesday
            ZonedDateTime now = ZonedDateTime.of(2025, 7, 15, 10, 0, 0, 0, ZoneId.of("UTC"));
            long lastRefreshEpoch = WeeklyRefreshType.computeEpoch(
                    ZonedDateTime.of(2025, 7, 7, 10, 0, 0, 0, ZoneId.of("UTC")));
            assertFalse(weekly.shouldRefresh(lastRefreshEpoch, now));
        }

        @Test
        @DisplayName("returns false on reset day when epoch matches current week")
        void shouldRefresh_resetDaySameEpoch_returnsFalse() {
            // 2025-07-14 is a Monday
            ZonedDateTime now = ZonedDateTime.of(2025, 7, 14, 23, 59, 0, 0, ZoneId.of("UTC"));
            long lastRefreshEpoch = WeeklyRefreshType.computeEpoch(now);
            assertFalse(weekly.shouldRefresh(lastRefreshEpoch, now));
        }

        @Test
        @DisplayName("returns true across year boundary on reset day")
        void shouldRefresh_yearBoundary_returnsTrue() {
            // 2025-12-29 is a Monday (ISO week 1 of 2026)
            ZonedDateTime now = ZonedDateTime.of(2025, 12, 29, 10, 0, 0, 0, ZoneId.of("UTC"));
            long lastRefreshEpoch = WeeklyRefreshType.computeEpoch(
                    ZonedDateTime.of(2025, 12, 22, 10, 0, 0, 0, ZoneId.of("UTC")));
            assertTrue(weekly.shouldRefresh(lastRefreshEpoch, now));
        }

        @Test
        @DisplayName("returns false when current epoch is before last refresh epoch")
        void shouldRefresh_epochBefore_returnsFalse() {
            // 2025-07-14 is a Monday
            ZonedDateTime now = ZonedDateTime.of(2025, 7, 14, 10, 0, 0, 0, ZoneId.of("UTC"));
            long futureEpoch = WeeklyRefreshType.computeEpoch(
                    ZonedDateTime.of(2025, 7, 21, 10, 0, 0, 0, ZoneId.of("UTC")));
            assertFalse(weekly.shouldRefresh(futureEpoch, now));
        }

        @Test
        @DisplayName("respects different reset days")
        void shouldRefresh_differentResetDay_respectsConfig() {
            WeeklyRefreshType fridayWeekly = new WeeklyRefreshType(DayOfWeek.FRIDAY);
            // 2025-07-18 is a Friday
            ZonedDateTime friday = ZonedDateTime.of(2025, 7, 18, 10, 0, 0, 0, ZoneId.of("UTC"));
            long lastEpoch = WeeklyRefreshType.computeEpoch(
                    ZonedDateTime.of(2025, 7, 11, 10, 0, 0, 0, ZoneId.of("UTC")));
            assertTrue(fridayWeekly.shouldRefresh(lastEpoch, friday));

            // Monday should not trigger Friday-configured refresh
            ZonedDateTime monday = ZonedDateTime.of(2025, 7, 14, 10, 0, 0, 0, ZoneId.of("UTC"));
            assertFalse(fridayWeekly.shouldRefresh(lastEpoch, monday));
        }

        @Test
        @DisplayName("respects timezone of ZonedDateTime")
        void shouldRefresh_differentTimezone_usesLocalDay() {
            // 2025-07-14 00:30 Tokyo = 2025-07-13 15:30 UTC (still Sunday in UTC, but Monday in Tokyo)
            ZonedDateTime tokyoMonday = ZonedDateTime.of(2025, 7, 14, 0, 30, 0, 0, ZoneId.of("Asia/Tokyo"));
            long lastEpoch = WeeklyRefreshType.computeEpoch(
                    ZonedDateTime.of(2025, 7, 7, 10, 0, 0, 0, ZoneId.of("UTC")));
            assertTrue(weekly.shouldRefresh(lastEpoch, tokyoMonday));
        }
    }

    @Nested
    @DisplayName("computeEpoch")
    class ComputeEpoch {

        @Test
        @DisplayName("encodes year and week as year*100+week")
        void computeEpoch_encodesYearAndWeek() {
            // 2025-07-14 is ISO week 29 of 2025
            ZonedDateTime date = ZonedDateTime.of(2025, 7, 14, 10, 0, 0, 0, ZoneId.of("UTC"));
            assertEquals(202529L, WeeklyRefreshType.computeEpoch(date));
        }

        @Test
        @DisplayName("first week of year produces correct epoch")
        void computeEpoch_firstWeek() {
            // 2025-01-06 is ISO week 2 of 2025
            ZonedDateTime date = ZonedDateTime.of(2025, 1, 6, 0, 0, 0, 0, ZoneId.of("UTC"));
            assertEquals(202502L, WeeklyRefreshType.computeEpoch(date));
        }

        @Test
        @DisplayName("last week of year produces correct epoch")
        void computeEpoch_lastWeek() {
            // 2025-12-22 is ISO week 52 of 2025
            ZonedDateTime date = ZonedDateTime.of(2025, 12, 22, 0, 0, 0, 0, ZoneId.of("UTC"));
            assertEquals(202552L, WeeklyRefreshType.computeEpoch(date));
        }

        @Test
        @DisplayName("monotonically increasing across weeks")
        void computeEpoch_monotonicallyIncreasing() {
            ZonedDateTime week1 = ZonedDateTime.of(2025, 7, 7, 0, 0, 0, 0, ZoneId.of("UTC"));
            ZonedDateTime week2 = ZonedDateTime.of(2025, 7, 14, 0, 0, 0, 0, ZoneId.of("UTC"));
            assertTrue(WeeklyRefreshType.computeEpoch(week2) > WeeklyRefreshType.computeEpoch(week1));
        }

        @Test
        @DisplayName("same week returns same epoch regardless of day within week")
        void computeEpoch_sameWeekSameEpoch() {
            ZonedDateTime monday = ZonedDateTime.of(2025, 7, 14, 0, 0, 0, 0, ZoneId.of("UTC"));
            ZonedDateTime friday = ZonedDateTime.of(2025, 7, 18, 23, 59, 0, 0, ZoneId.of("UTC"));
            assertEquals(WeeklyRefreshType.computeEpoch(monday), WeeklyRefreshType.computeEpoch(friday));
        }
    }
}
