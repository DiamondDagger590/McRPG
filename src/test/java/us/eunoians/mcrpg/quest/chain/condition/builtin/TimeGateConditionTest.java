package us.eunoians.mcrpg.quest.chain.condition.builtin;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class TimeGateConditionTest {

    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");

    @Nested
    @DisplayName("TimeGateCondition")
    class ConditionTests {

        @Test
        @DisplayName("evaluate returns true when current time is after the boundary")
        void evaluate_returnsTrue_whenAfterBoundary() {
            LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0);
            TimeGateCondition condition = new TimeGateCondition(
                    TimeGateChainConditionType.KEY, boundary, UTC);

            Instant afterBoundary = ZonedDateTime.of(2026, 7, 15, 12, 0, 0, 0, UTC).toInstant();
            assertTrue(condition.evaluate(mock(Player.class), afterBoundary));
        }

        @Test
        @DisplayName("evaluate returns false when current time is before the boundary")
        void evaluate_returnsFalse_whenBeforeBoundary() {
            LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0);
            TimeGateCondition condition = new TimeGateCondition(
                    TimeGateChainConditionType.KEY, boundary, UTC);

            Instant beforeBoundary = ZonedDateTime.of(2026, 6, 15, 12, 0, 0, 0, UTC).toInstant();
            assertFalse(condition.evaluate(mock(Player.class), beforeBoundary));
        }

        @Test
        @DisplayName("evaluate returns true when current time is exactly at the boundary")
        void evaluate_returnsTrue_whenExactlyAtBoundary() {
            LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0);
            TimeGateCondition condition = new TimeGateCondition(
                    TimeGateChainConditionType.KEY, boundary, UTC);

            Instant atBoundary = ZonedDateTime.of(2026, 7, 1, 0, 0, 0, 0, UTC).toInstant();
            assertTrue(condition.evaluate(mock(Player.class), atBoundary));
        }

        @Test
        @DisplayName("evaluate respects the configured timezone")
        void evaluate_respectsTimezone() {
            LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0);
            TimeGateCondition condition = new TimeGateCondition(
                    TimeGateChainConditionType.KEY, boundary, NEW_YORK);

            // 2026-07-01 03:00 UTC = 2026-06-30 23:00 ET (before boundary)
            Instant beforeInNewYork = ZonedDateTime.of(2026, 7, 1, 3, 0, 0, 0, UTC).toInstant();
            assertFalse(condition.evaluate(mock(Player.class), beforeInNewYork));

            // 2026-07-01 05:00 UTC = 2026-07-01 01:00 ET (after boundary)
            Instant afterInNewYork = ZonedDateTime.of(2026, 7, 1, 5, 0, 0, 0, UTC).toInstant();
            assertTrue(condition.evaluate(mock(Player.class), afterInNewYork));
        }

        @Test
        @DisplayName("getKey returns the time gate key")
        void getKey_returnsTimeGateKey() {
            LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0);
            TimeGateCondition condition = new TimeGateCondition(
                    TimeGateChainConditionType.KEY, boundary, UTC);

            assertEquals(TimeGateChainConditionType.KEY, condition.getKey());
        }

        @Test
        @DisplayName("evaluate returns true when current time is one second after boundary")
        void evaluate_returnsTrue_whenOneSecondAfterBoundary() {
            LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 12, 0, 0);
            TimeGateCondition condition = new TimeGateCondition(
                    TimeGateChainConditionType.KEY, boundary, UTC);

            Instant oneSecondAfter = ZonedDateTime.of(2026, 7, 1, 12, 0, 1, 0, UTC).toInstant();
            assertTrue(condition.evaluate(mock(Player.class), oneSecondAfter));
        }

        @Test
        @DisplayName("evaluate returns false when current time is one second before boundary")
        void evaluate_returnsFalse_whenOneSecondBeforeBoundary() {
            LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 12, 0, 0);
            TimeGateCondition condition = new TimeGateCondition(
                    TimeGateChainConditionType.KEY, boundary, UTC);

            Instant oneSecondBefore = ZonedDateTime.of(2026, 7, 1, 11, 59, 59, 0, UTC).toInstant();
            assertFalse(condition.evaluate(mock(Player.class), oneSecondBefore));
        }
    }
}
