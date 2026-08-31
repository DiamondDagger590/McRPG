package us.eunoians.mcrpg.quest.chain.condition.builtin;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
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

public class TimeGateConditionTest extends McRPGBaseTest {

    private static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "time_gate");

    @DisplayName("getKey returns the key passed at construction")
    @Test
    void getKey_returnsConstructedKey() {
        var condition = new TimeGateCondition(KEY, LocalDateTime.of(2026, 7, 1, 0, 0), ZoneId.of("UTC"));
        assertEquals(KEY, condition.getKey());
    }

    @DisplayName("evaluate returns false when current time is before the boundary")
    @Test
    void evaluate_returnsFalse_whenBeforeBoundary() {
        LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 12, 0);
        var condition = new TimeGateCondition(KEY, boundary, ZoneId.of("UTC"));

        Instant beforeBoundary = ZonedDateTime.of(2026, 7, 1, 11, 59, 59, 0, ZoneOffset.UTC).toInstant();
        assertFalse(condition.evaluate(mock(Player.class), beforeBoundary));
    }

    @DisplayName("evaluate returns true when current time is exactly at the boundary")
    @Test
    void evaluate_returnsTrue_whenExactlyAtBoundary() {
        LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 12, 0);
        var condition = new TimeGateCondition(KEY, boundary, ZoneId.of("UTC"));

        Instant atBoundary = ZonedDateTime.of(2026, 7, 1, 12, 0, 0, 0, ZoneOffset.UTC).toInstant();
        assertTrue(condition.evaluate(mock(Player.class), atBoundary));
    }

    @DisplayName("evaluate returns true when current time is after the boundary")
    @Test
    void evaluate_returnsTrue_whenAfterBoundary() {
        LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 12, 0);
        var condition = new TimeGateCondition(KEY, boundary, ZoneId.of("UTC"));

        Instant afterBoundary = ZonedDateTime.of(2026, 7, 2, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        assertTrue(condition.evaluate(mock(Player.class), afterBoundary));
    }

    @DisplayName("evaluate respects the configured timezone for boundary comparison")
    @Test
    void evaluate_respectsTimezone() {
        LocalDateTime boundary = LocalDateTime.of(2026, 7, 1, 12, 0);
        ZoneId eastern = ZoneId.of("America/New_York");
        var condition = new TimeGateCondition(KEY, boundary, eastern);

        Instant noonUtc = ZonedDateTime.of(2026, 7, 1, 12, 0, 0, 0, ZoneOffset.UTC).toInstant();
        assertFalse(condition.evaluate(mock(Player.class), noonUtc),
                "Noon UTC is 8 AM Eastern in July (EDT), which is before the noon-Eastern boundary");

        Instant fourPmUtc = ZonedDateTime.of(2026, 7, 1, 16, 0, 0, 0, ZoneOffset.UTC).toInstant();
        assertTrue(condition.evaluate(mock(Player.class), fourPmUtc),
                "4 PM UTC is noon Eastern in July (EDT), which matches the boundary exactly");
    }

    @DisplayName("evaluate handles different timezone producing different results for same instant")
    @Test
    void evaluate_differentTimezones_differentResults() {
        LocalDateTime boundary = LocalDateTime.of(2026, 1, 15, 3, 0);
        Instant instant = ZonedDateTime.of(2026, 1, 15, 3, 0, 0, 0, ZoneOffset.UTC).toInstant();

        var utcCondition = new TimeGateCondition(KEY, boundary, ZoneId.of("UTC"));
        assertTrue(utcCondition.evaluate(mock(Player.class), instant),
                "3 AM UTC with a 3 AM UTC boundary should pass");

        var tokyoCondition = new TimeGateCondition(KEY, boundary, ZoneId.of("Asia/Tokyo"));
        assertTrue(tokyoCondition.evaluate(mock(Player.class), instant),
                "3 AM UTC is noon JST, which is after 3 AM JST boundary");

        var losAngelesCondition = new TimeGateCondition(KEY, boundary, ZoneId.of("America/Los_Angeles"));
        assertFalse(losAngelesCondition.evaluate(mock(Player.class), instant),
                "3 AM UTC is 7 PM PST previous day, which is before the 3 AM PST boundary");
    }

    @DisplayName("record accessors return construction values")
    @Test
    void recordAccessors_returnConstructionValues() {
        LocalDateTime after = LocalDateTime.of(2026, 12, 25, 8, 30);
        ZoneId zone = ZoneId.of("Europe/London");
        var condition = new TimeGateCondition(KEY, after, zone);

        assertEquals(after, condition.after());
        assertEquals(zone, condition.timezone());
        assertEquals(KEY, condition.key());
    }
}
