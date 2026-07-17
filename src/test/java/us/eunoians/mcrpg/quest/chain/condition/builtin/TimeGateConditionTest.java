package us.eunoians.mcrpg.quest.chain.condition.builtin;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class TimeGateConditionTest extends McRPGBaseTest {

    private static final NamespacedKey KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "time_gate");
    private static final ZoneId UTC = ZoneId.of("UTC");

    @Nested
    @DisplayName("evaluate")
    class EvaluateTests {

        @Test
        @DisplayName("returns true when now is after boundary")
        void evaluate_returnsTrue_whenNowIsAfterBoundary() {
            LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0, 0);
            TimeGateCondition condition = new TimeGateCondition(KEY, boundary, UTC);

            Instant afterBoundary = ZonedDateTime.of(2026, 7, 2, 12, 0, 0, 0, UTC).toInstant();
            assertTrue(condition.evaluate(mock(Player.class), afterBoundary));
        }

        @Test
        @DisplayName("returns false when now is before boundary")
        void evaluate_returnsFalse_whenNowIsBeforeBoundary() {
            LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0, 0);
            TimeGateCondition condition = new TimeGateCondition(KEY, boundary, UTC);

            Instant beforeBoundary = ZonedDateTime.of(2026, 6, 30, 23, 59, 59, 0, UTC).toInstant();
            assertFalse(condition.evaluate(mock(Player.class), beforeBoundary));
        }

        @Test
        @DisplayName("returns true when now is exactly at boundary")
        void evaluate_returnsTrue_whenNowIsExactlyAtBoundary() {
            LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0, 0);
            TimeGateCondition condition = new TimeGateCondition(KEY, boundary, UTC);

            Instant exactBoundary = ZonedDateTime.of(2026, 7, 1, 0, 0, 0, 0, UTC).toInstant();
            assertTrue(condition.evaluate(mock(Player.class), exactBoundary));
        }

        @Test
        @DisplayName("respects timezone offset for evaluation")
        void evaluate_respectsTimezoneOffset() {
            LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0, 0);
            ZoneId eastern = ZoneId.of("America/New_York");
            TimeGateCondition condition = new TimeGateCondition(KEY, boundary, eastern);

            // 2026-07-01 03:00 UTC = 2026-06-30 23:00 Eastern (still before midnight boundary)
            Instant beforeInEastern = ZonedDateTime.of(2026, 7, 1, 3, 0, 0, 0, UTC).toInstant();
            assertFalse(condition.evaluate(mock(Player.class), beforeInEastern));

            // 2026-07-01 05:00 UTC = 2026-07-01 01:00 Eastern (after midnight boundary)
            Instant afterInEastern = ZonedDateTime.of(2026, 7, 1, 5, 0, 0, 0, UTC).toInstant();
            assertTrue(condition.evaluate(mock(Player.class), afterInEastern));
        }

        @Test
        @DisplayName("works with far-future boundary")
        void evaluate_worksWith_farFutureBoundary() {
            LocalDateTime farFuture = LocalDateTime.of(2099, 12, 31, 23, 59, 59);
            TimeGateCondition condition = new TimeGateCondition(KEY, farFuture, UTC);

            Instant now = ZonedDateTime.of(2026, 7, 17, 0, 0, 0, 0, UTC).toInstant();
            assertFalse(condition.evaluate(mock(Player.class), now));
        }

        @Test
        @DisplayName("works with past boundary")
        void evaluate_worksWith_pastBoundary() {
            LocalDateTime past = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
            TimeGateCondition condition = new TimeGateCondition(KEY, past, UTC);

            Instant now = ZonedDateTime.of(2026, 7, 17, 0, 0, 0, 0, UTC).toInstant();
            assertTrue(condition.evaluate(mock(Player.class), now));
        }
    }

    @Nested
    @DisplayName("record accessors")
    class AccessorTests {

        @Test
        @DisplayName("getKey returns the condition type key")
        void getKey_returnsConditionTypeKey() {
            TimeGateCondition condition = new TimeGateCondition(
                    KEY, LocalDateTime.of(2026, 1, 1, 0, 0), UTC);
            assertEquals(KEY, condition.getKey());
        }

        @Test
        @DisplayName("after returns the configured boundary")
        void after_returnsConfiguredBoundary() {
            LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 12, 30, 45);
            TimeGateCondition condition = new TimeGateCondition(KEY, boundary, UTC);
            assertEquals(boundary, condition.after());
        }

        @Test
        @DisplayName("timezone returns the configured zone")
        void timezone_returnsConfiguredZone() {
            ZoneId tokyo = ZoneId.of("Asia/Tokyo");
            TimeGateCondition condition = new TimeGateCondition(
                    KEY, LocalDateTime.of(2026, 1, 1, 0, 0), tokyo);
            assertEquals(tokyo, condition.timezone());
        }
    }
}
