package us.eunoians.mcrpg.listener.quest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the threshold-crossing detection logic in {@link QuestProgressNotificationListener}.
 * These tests operate on the pure static helper and do not require MockBukkit.
 */
public class QuestProgressNotificationListenerTest {

    @DisplayName("No threshold crossed when progress stays below every threshold")
    @Test
    void findCrossedThreshold_noCrossing_returnsNegativeOne() {
        // 0 → 20% on a [25, 50, 75] set — nothing crossed
        int result = QuestProgressNotificationListener.findCrossedThreshold(0, 20, 100, List.of(25, 50, 75));
        assertEquals(-1, result);
    }

    @DisplayName("Returns the first threshold crossed (25) when progress moves from 0 to 30")
    @Test
    void findCrossedThreshold_crossesFirstThreshold() {
        int result = QuestProgressNotificationListener.findCrossedThreshold(0, 30, 100, List.of(25, 50, 75));
        assertEquals(25, result);
    }

    @DisplayName("Returns the first threshold crossed (50) when progress is already past 25")
    @Test
    void findCrossedThreshold_crossesMiddleThreshold() {
        // already at 30%, delta takes us to 55% — crosses 50
        int result = QuestProgressNotificationListener.findCrossedThreshold(30, 25, 100, List.of(25, 50, 75));
        assertEquals(50, result);
    }

    @DisplayName("Returns the last threshold (75) when crossing it exactly")
    @Test
    void findCrossedThreshold_crossesLastThreshold() {
        // at 70%, delta takes to exactly 75%
        int result = QuestProgressNotificationListener.findCrossedThreshold(70, 5, 100, List.of(25, 50, 75));
        assertEquals(75, result);
    }

    @DisplayName("Returns the FIRST threshold crossed when a single delta spans multiple thresholds")
    @Test
    void findCrossedThreshold_multipleThresholdsCrossed_returnsFirst() {
        // 0 → 80% in one step crosses 25, 50, and 75 — should return 25
        int result = QuestProgressNotificationListener.findCrossedThreshold(0, 80, 100, List.of(25, 50, 75));
        assertEquals(25, result);
    }

    @DisplayName("Returns -1 when already above every threshold and progress continues")
    @Test
    void findCrossedThreshold_alreadyAboveAllThresholds_returnsNegativeOne() {
        // at 80%, delta takes to 90% — all thresholds already passed
        int result = QuestProgressNotificationListener.findCrossedThreshold(80, 10, 100, List.of(25, 50, 75));
        assertEquals(-1, result);
    }

    @DisplayName("Threshold at exactly old percentage is not considered crossed (strict less-than)")
    @Test
    void findCrossedThreshold_oldPctEqualsThreshold_notCrossed() {
        // exactly at 50% before delta — should not re-fire the 50% threshold
        int result = QuestProgressNotificationListener.findCrossedThreshold(50, 5, 100, List.of(25, 50, 75));
        assertEquals(-1, result);
    }

    @DisplayName("Empty threshold list returns -1")
    @Test
    void findCrossedThreshold_emptyThresholds_returnsNegativeOne() {
        int result = QuestProgressNotificationListener.findCrossedThreshold(0, 100, 100, List.of());
        assertEquals(-1, result);
    }

    @DisplayName("Custom single-threshold [50]: crossing 50 returns 50")
    @Test
    void findCrossedThreshold_singleThreshold_crossesIt() {
        int result = QuestProgressNotificationListener.findCrossedThreshold(40, 15, 100, List.of(50));
        assertEquals(50, result);
    }

    @DisplayName("Custom single-threshold [50]: not crossing returns -1")
    @Test
    void findCrossedThreshold_singleThreshold_doesNotCrossIt() {
        int result = QuestProgressNotificationListener.findCrossedThreshold(40, 5, 100, List.of(50));
        assertEquals(-1, result);
    }

    @DisplayName("Works correctly with non-100 required progression (e.g. required=200)")
    @Test
    void findCrossedThreshold_nonStandardRequired_crossesThreshold() {
        // required=200, progress 0→60 = 30%, crosses 25
        int result = QuestProgressNotificationListener.findCrossedThreshold(0, 60, 200, List.of(25, 50, 75));
        assertEquals(25, result);
    }
}
