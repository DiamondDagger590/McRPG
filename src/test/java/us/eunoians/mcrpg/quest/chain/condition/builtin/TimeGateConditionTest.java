package us.eunoians.mcrpg.quest.chain.condition.builtin;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("TimeGateCondition")
public class TimeGateConditionTest extends McRPGBaseTest {

    private final Player mockPlayer = mock(Player.class);

    @Test
    @DisplayName("returns false when current time is before the boundary")
    public void evaluate_beforeBoundary_returnsFalse() {
        LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0, 0);
        TimeGateCondition condition = new TimeGateCondition(
                TimeGateChainConditionType.KEY, boundary, ZoneOffset.UTC);

        Instant beforeBoundary = ZonedDateTime.of(2026, 6, 30, 23, 59, 59, 0, ZoneOffset.UTC).toInstant();
        assertFalse(condition.evaluate(mockPlayer, beforeBoundary));
    }

    @Test
    @DisplayName("returns true when current time is exactly at the boundary")
    public void evaluate_exactlyAtBoundary_returnsTrue() {
        LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0, 0);
        TimeGateCondition condition = new TimeGateCondition(
                TimeGateChainConditionType.KEY, boundary, ZoneOffset.UTC);

        Instant atBoundary = ZonedDateTime.of(2026, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        assertTrue(condition.evaluate(mockPlayer, atBoundary));
    }

    @Test
    @DisplayName("returns true when current time is after the boundary")
    public void evaluate_afterBoundary_returnsTrue() {
        LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0, 0);
        TimeGateCondition condition = new TimeGateCondition(
                TimeGateChainConditionType.KEY, boundary, ZoneOffset.UTC);

        Instant afterBoundary = ZonedDateTime.of(2026, 7, 1, 0, 0, 1, 0, ZoneOffset.UTC).toInstant();
        assertTrue(condition.evaluate(mockPlayer, afterBoundary));
    }

    @Test
    @DisplayName("cross-timezone: UTC time before boundary but local time after boundary returns true")
    public void evaluate_crossTimezone_localTimeAfterBoundary_returnsTrue() {
        LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 0, 0, 0);
        ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");
        TimeGateCondition condition = new TimeGateCondition(
                TimeGateChainConditionType.KEY, boundary, tokyoZone);

        // 2026-06-30T20:00:00 UTC = 2026-07-01T05:00:00 Asia/Tokyo (after boundary in Tokyo)
        Instant utcTime = ZonedDateTime.of(2026, 6, 30, 20, 0, 0, 0, ZoneOffset.UTC).toInstant();
        assertTrue(condition.evaluate(mockPlayer, utcTime));
    }

    @Test
    @DisplayName("cross-timezone: UTC time after boundary but local time before boundary returns false")
    public void evaluate_crossTimezone_localTimeBeforeBoundary_returnsFalse() {
        LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 12, 0, 0);
        ZoneId newYorkZone = ZoneId.of("America/New_York");
        TimeGateCondition condition = new TimeGateCondition(
                TimeGateChainConditionType.KEY, boundary, newYorkZone);

        // 2026-07-01T14:00:00 UTC = 2026-07-01T10:00:00 America/New_York (before 12:00 boundary)
        Instant utcTime = ZonedDateTime.of(2026, 7, 1, 14, 0, 0, 0, ZoneOffset.UTC).toInstant();
        assertFalse(condition.evaluate(mockPlayer, utcTime));
    }
}
