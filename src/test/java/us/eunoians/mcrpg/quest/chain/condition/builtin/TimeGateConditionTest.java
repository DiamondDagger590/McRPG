package us.eunoians.mcrpg.quest.chain.condition.builtin;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class TimeGateConditionTest extends McRPGBaseTest {

    @Nested
    @DisplayName("evaluate")
    class Evaluate {

        @Test
        @DisplayName("Returns true when current time is after the boundary")
        void evaluate_returnsTrue_whenNowIsAfterBoundary() {
            LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0, 0);
            ZoneId zone = ZoneOffset.UTC;
            TimeGateCondition condition = new TimeGateCondition(TimeGateChainConditionType.KEY, boundary, zone);

            Instant nowAfter = ZonedDateTime.of(2026, 7, 2, 12, 0, 0, 0, zone).toInstant();
            Player player = mock(Player.class);

            assertTrue(condition.evaluate(player, nowAfter));
        }

        @Test
        @DisplayName("Returns true when current time is exactly at the boundary")
        void evaluate_returnsTrue_whenNowIsExactlyAtBoundary() {
            LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0, 0);
            ZoneId zone = ZoneOffset.UTC;
            TimeGateCondition condition = new TimeGateCondition(TimeGateChainConditionType.KEY, boundary, zone);

            Instant nowExact = ZonedDateTime.of(2026, 7, 1, 0, 0, 0, 0, zone).toInstant();
            Player player = mock(Player.class);

            assertTrue(condition.evaluate(player, nowExact));
        }

        @Test
        @DisplayName("Returns false when current time is before the boundary")
        void evaluate_returnsFalse_whenNowIsBeforeBoundary() {
            LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0, 0);
            ZoneId zone = ZoneOffset.UTC;
            TimeGateCondition condition = new TimeGateCondition(TimeGateChainConditionType.KEY, boundary, zone);

            Instant nowBefore = ZonedDateTime.of(2026, 6, 30, 23, 59, 59, 0, zone).toInstant();
            Player player = mock(Player.class);

            assertFalse(condition.evaluate(player, nowBefore));
        }

        @Test
        @DisplayName("Respects timezone when evaluating — same instant, different local time")
        void evaluate_respectsTimezone() {
            LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0, 0);
            ZoneId newYork = ZoneId.of("America/New_York");
            TimeGateCondition condition = new TimeGateCondition(TimeGateChainConditionType.KEY, boundary, newYork);

            Instant utcMidnight = ZonedDateTime.of(2026, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();

            assertFalse(condition.evaluate(mock(Player.class), utcMidnight));

            Instant nyMidnight = ZonedDateTime.of(2026, 7, 1, 0, 0, 0, 0, newYork).toInstant();
            assertTrue(condition.evaluate(mock(Player.class), nyMidnight));
        }

        @Test
        @DisplayName("Far-future instant passes a near-term boundary")
        void evaluate_returnsTrue_whenFarFutureInstant() {
            LocalDateTime boundary = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
            ZoneId zone = ZoneOffset.UTC;
            TimeGateCondition condition = new TimeGateCondition(TimeGateChainConditionType.KEY, boundary, zone);

            Instant farFuture = ZonedDateTime.of(2030, 12, 31, 23, 59, 59, 0, zone).toInstant();
            assertTrue(condition.evaluate(mock(Player.class), farFuture));
        }

        @Test
        @DisplayName("Far-past instant fails a near-term boundary")
        void evaluate_returnsFalse_whenFarPastInstant() {
            LocalDateTime boundary = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
            ZoneId zone = ZoneOffset.UTC;
            TimeGateCondition condition = new TimeGateCondition(TimeGateChainConditionType.KEY, boundary, zone);

            Instant farPast = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, zone).toInstant();
            assertFalse(condition.evaluate(mock(Player.class), farPast));
        }
    }

    @Nested
    @DisplayName("record accessors")
    class RecordAccessors {

        @Test
        @DisplayName("getKey returns the condition type key")
        void getKey_returnsConditionTypeKey() {
            LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0, 0);
            ZoneId zone = ZoneOffset.UTC;
            TimeGateCondition condition = new TimeGateCondition(TimeGateChainConditionType.KEY, boundary, zone);

            assertEquals(TimeGateChainConditionType.KEY, condition.getKey());
        }

        @Test
        @DisplayName("after returns the configured boundary")
        void after_returnsConfiguredBoundary() {
            LocalDateTime boundary = LocalDateTime.of(2026, 8, 15, 14, 30, 0);
            ZoneId zone = ZoneOffset.UTC;
            TimeGateCondition condition = new TimeGateCondition(TimeGateChainConditionType.KEY, boundary, zone);

            assertEquals(boundary, condition.after());
        }

        @Test
        @DisplayName("timezone returns the configured zone")
        void timezone_returnsConfiguredZone() {
            LocalDateTime boundary = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
            ZoneId zone = ZoneId.of("Europe/London");
            TimeGateCondition condition = new TimeGateCondition(TimeGateChainConditionType.KEY, boundary, zone);

            assertSame(zone, condition.timezone());
        }
    }
}
