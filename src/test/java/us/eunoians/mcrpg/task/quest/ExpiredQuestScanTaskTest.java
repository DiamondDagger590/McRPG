package us.eunoians.mcrpg.task.quest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpiredQuestScanTaskTest {

    @Nested
    @DisplayName("formatTimeRemaining")
    class FormatTimeRemainingTests {

        @DisplayName("Standard durations format correctly")
        @ParameterizedTest(name = "{0}ms => \"{1}\"")
        @CsvSource({
                "0, 0h 0m",
                "1, 0h 0m",
                "59999, 0h 0m",
                "60000, 0h 1m",
                "1500000, 0h 25m",
                "3600000, 1h 0m",
                "5400000, 1h 30m",
                "13500000, 3h 45m",
                "360000000, 100h 0m"
        })
        void formatTimeRemaining_standardDurations(long millis, String expected) {
            assertEquals(expected, ExpiredQuestScanTask.formatTimeRemaining(millis));
        }

        @DisplayName("Negative values clamp to 0h 0m")
        @ParameterizedTest(name = "{0}ms => \"0h 0m\"")
        @CsvSource({
                "-1",
                "-60000",
                "-9223372036854775808"
        })
        void formatTimeRemaining_negativeValues_clampToZero(long millis) {
            assertEquals("0h 0m", ExpiredQuestScanTask.formatTimeRemaining(millis));
        }

        @DisplayName("Sub-minute values truncate to 0h 0m")
        @Test
        void formatTimeRemaining_subMinute_truncatesToZero() {
            assertEquals("0h 0m", ExpiredQuestScanTask.formatTimeRemaining(30_000));
            assertEquals("0h 0m", ExpiredQuestScanTask.formatTimeRemaining(59_999));
        }

        @DisplayName("Boundary between 0m and 1m is exactly 60000ms")
        @Test
        void formatTimeRemaining_minuteBoundary() {
            assertEquals("0h 0m", ExpiredQuestScanTask.formatTimeRemaining(59_999));
            assertEquals("0h 1m", ExpiredQuestScanTask.formatTimeRemaining(60_000));
        }

        @DisplayName("Boundary between 59m and 1h is exactly 3600000ms")
        @Test
        void formatTimeRemaining_hourBoundary() {
            assertEquals("0h 59m", ExpiredQuestScanTask.formatTimeRemaining(3_599_999));
            assertEquals("1h 0m", ExpiredQuestScanTask.formatTimeRemaining(3_600_000));
        }

        @DisplayName("24+ hours formats correctly without wrapping")
        @Test
        void formatTimeRemaining_moreThan24Hours_doesNotWrap() {
            long twentyFiveHours = 25L * 60 * 60_000;
            assertEquals("25h 0m", ExpiredQuestScanTask.formatTimeRemaining(twentyFiveHours));
        }
    }
}
