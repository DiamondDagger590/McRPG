package us.eunoians.mcrpg.quest.chain.condition.builtin;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("TimeGateCondition")
class TimeGateConditionTest extends McRPGBaseTest {

    private static final NamespacedKey KEY = new NamespacedKey("mcrpg", "time_gate");
    private static final ZoneId UTC = ZoneId.of("UTC");

    @Test
    @DisplayName("evaluate returns false when current time is before the boundary")
    void evaluate_returnsFalse_whenBeforeBoundary() {
        LocalDateTime boundary = LocalDateTime.of(2025, 6, 15, 12, 0);
        TimeGateCondition condition = new TimeGateCondition(KEY, boundary, UTC);

        Instant beforeBoundary = ZonedDateTime.of(2025, 6, 15, 11, 59, 59, 0, UTC).toInstant();

        assertFalse(condition.evaluate(mock(Player.class), beforeBoundary));
    }

    @Test
    @DisplayName("evaluate returns true when current time is exactly at the boundary")
    void evaluate_returnsTrue_whenExactlyAtBoundary() {
        LocalDateTime boundary = LocalDateTime.of(2025, 6, 15, 12, 0);
        TimeGateCondition condition = new TimeGateCondition(KEY, boundary, UTC);

        Instant atBoundary = ZonedDateTime.of(2025, 6, 15, 12, 0, 0, 0, UTC).toInstant();

        assertTrue(condition.evaluate(mock(Player.class), atBoundary));
    }

    @Test
    @DisplayName("evaluate returns true when current time is after the boundary")
    void evaluate_returnsTrue_whenAfterBoundary() {
        LocalDateTime boundary = LocalDateTime.of(2025, 6, 15, 12, 0);
        TimeGateCondition condition = new TimeGateCondition(KEY, boundary, UTC);

        Instant afterBoundary = ZonedDateTime.of(2025, 6, 16, 0, 0, 0, 0, UTC).toInstant();

        assertTrue(condition.evaluate(mock(Player.class), afterBoundary));
    }

    @Test
    @DisplayName("evaluate respects timezone offset when comparing times")
    void evaluate_respectsTimezone_whenComparing() {
        ZoneId tokyo = ZoneId.of("Asia/Tokyo");
        LocalDateTime boundary = LocalDateTime.of(2025, 6, 15, 12, 0);
        TimeGateCondition condition = new TimeGateCondition(KEY, boundary, tokyo);

        Instant utcTime = ZonedDateTime.of(2025, 6, 15, 2, 59, 59, 0, UTC).toInstant();
        assertFalse(condition.evaluate(mock(Player.class), utcTime),
                "2:59 UTC is 11:59 JST which is before 12:00 boundary");

        Instant utcTimeAfter = ZonedDateTime.of(2025, 6, 15, 3, 0, 0, 0, UTC).toInstant();
        assertTrue(condition.evaluate(mock(Player.class), utcTimeAfter),
                "3:00 UTC is 12:00 JST which is exactly at the boundary");
    }

    @Test
    @DisplayName("evaluate returns true when boundary is far in the past")
    void evaluate_returnsTrue_whenBoundaryFarInPast() {
        LocalDateTime boundary = LocalDateTime.of(2020, 1, 1, 0, 0);
        TimeGateCondition condition = new TimeGateCondition(KEY, boundary, UTC);

        Instant now = ZonedDateTime.of(2025, 6, 15, 12, 0, 0, 0, UTC).toInstant();

        assertTrue(condition.evaluate(mock(Player.class), now));
    }

    @Test
    @DisplayName("evaluate returns false when boundary is far in the future")
    void evaluate_returnsFalse_whenBoundaryFarInFuture() {
        LocalDateTime boundary = LocalDateTime.of(2030, 12, 31, 23, 59);
        TimeGateCondition condition = new TimeGateCondition(KEY, boundary, UTC);

        Instant now = ZonedDateTime.of(2025, 6, 15, 12, 0, 0, 0, UTC).toInstant();

        assertFalse(condition.evaluate(mock(Player.class), now));
    }

    @Test
    @DisplayName("getKey returns the key provided at construction")
    void getKey_returnsConstructorKey() {
        TimeGateCondition condition = new TimeGateCondition(KEY, LocalDateTime.now(), UTC);

        assertEquals(KEY, condition.getKey());
    }

    @Test
    @DisplayName("record accessors return constructor values")
    void recordAccessors_returnConstructorValues() {
        LocalDateTime boundary = LocalDateTime.of(2025, 1, 1, 0, 0);
        ZoneId zone = ZoneId.of("America/New_York");
        TimeGateCondition condition = new TimeGateCondition(KEY, boundary, zone);

        assertEquals(KEY, condition.key());
        assertEquals(boundary, condition.after());
        assertEquals(zone, condition.timezone());
    }
}
