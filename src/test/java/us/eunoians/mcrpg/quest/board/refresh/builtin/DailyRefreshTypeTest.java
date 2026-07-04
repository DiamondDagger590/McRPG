package us.eunoians.mcrpg.quest.board.refresh.builtin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DailyRefreshType")
class DailyRefreshTypeTest {

    private final DailyRefreshType daily = new DailyRefreshType();

    @Nested
    @DisplayName("getKey")
    class GetKey {

        @Test
        @DisplayName("returns mcrpg:daily")
        void getKey_returnsMcrpgDaily() {
            assertEquals("mcrpg", daily.getKey().getNamespace());
            assertEquals("daily", daily.getKey().getKey());
        }
    }

    @Nested
    @DisplayName("isTimeBased")
    class IsTimeBased {

        @Test
        @DisplayName("returns true")
        void isTimeBased_returnsTrue() {
            assertTrue(daily.isTimeBased());
        }
    }

    @Nested
    @DisplayName("shouldRefresh")
    class ShouldRefresh {

        @Test
        @DisplayName("returns true when current day is after last refresh day")
        void shouldRefresh_nextDay_returnsTrue() {
            ZonedDateTime now = ZonedDateTime.of(2025, 7, 15, 10, 0, 0, 0, ZoneId.of("UTC"));
            long lastRefreshEpoch = LocalDate.of(2025, 7, 14).toEpochDay();
            assertTrue(daily.shouldRefresh(lastRefreshEpoch, now));
        }

        @Test
        @DisplayName("returns false when current day equals last refresh day")
        void shouldRefresh_sameDay_returnsFalse() {
            ZonedDateTime now = ZonedDateTime.of(2025, 7, 15, 23, 59, 59, 0, ZoneId.of("UTC"));
            long lastRefreshEpoch = LocalDate.of(2025, 7, 15).toEpochDay();
            assertFalse(daily.shouldRefresh(lastRefreshEpoch, now));
        }

        @Test
        @DisplayName("returns false when current day is before last refresh day")
        void shouldRefresh_beforeLastRefresh_returnsFalse() {
            ZonedDateTime now = ZonedDateTime.of(2025, 7, 14, 12, 0, 0, 0, ZoneId.of("UTC"));
            long lastRefreshEpoch = LocalDate.of(2025, 7, 15).toEpochDay();
            assertFalse(daily.shouldRefresh(lastRefreshEpoch, now));
        }

        @Test
        @DisplayName("returns true when multiple days have elapsed")
        void shouldRefresh_multipleDaysLater_returnsTrue() {
            ZonedDateTime now = ZonedDateTime.of(2025, 7, 20, 0, 0, 0, 0, ZoneId.of("UTC"));
            long lastRefreshEpoch = LocalDate.of(2025, 7, 15).toEpochDay();
            assertTrue(daily.shouldRefresh(lastRefreshEpoch, now));
        }

        @Test
        @DisplayName("returns true at midnight boundary crossing")
        void shouldRefresh_midnightBoundary_returnsTrue() {
            ZonedDateTime now = ZonedDateTime.of(2025, 7, 16, 0, 0, 0, 0, ZoneId.of("UTC"));
            long lastRefreshEpoch = LocalDate.of(2025, 7, 15).toEpochDay();
            assertTrue(daily.shouldRefresh(lastRefreshEpoch, now));
        }

        @Test
        @DisplayName("handles year boundary correctly")
        void shouldRefresh_yearBoundary_returnsTrue() {
            ZonedDateTime now = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
            long lastRefreshEpoch = LocalDate.of(2025, 12, 31).toEpochDay();
            assertTrue(daily.shouldRefresh(lastRefreshEpoch, now));
        }

        @Test
        @DisplayName("respects timezone of ZonedDateTime")
        void shouldRefresh_differentTimezone_usesLocalDate() {
            ZonedDateTime nowTokyo = ZonedDateTime.of(2025, 7, 16, 1, 0, 0, 0, ZoneId.of("Asia/Tokyo"));
            long lastRefreshEpoch = LocalDate.of(2025, 7, 15).toEpochDay();
            assertTrue(daily.shouldRefresh(lastRefreshEpoch, nowTokyo));
        }

        @Test
        @DisplayName("returns false with epoch 0 on epoch day 0")
        void shouldRefresh_epochZero_sameDay_returnsFalse() {
            ZonedDateTime epoch = ZonedDateTime.of(1970, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC"));
            assertFalse(daily.shouldRefresh(0, epoch));
        }
    }
}
