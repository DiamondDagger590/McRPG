package us.eunoians.mcrpg.task.quest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpiredQuestScanTaskTest {

    @DisplayName("Zero milliseconds formats as 0h 0m")
    @Test
    void formatTimeRemaining_zero_returnsZero() {
        assertEquals("0h 0m", ExpiredQuestScanTask.formatTimeRemaining(0));
    }

    @DisplayName("Negative milliseconds clamp to 0h 0m")
    @Test
    void formatTimeRemaining_negative_clampedToZero() {
        assertEquals("0h 0m", ExpiredQuestScanTask.formatTimeRemaining(-1));
        assertEquals("0h 0m", ExpiredQuestScanTask.formatTimeRemaining(-60_000));
    }

    @DisplayName("Exactly one hour formats as 1h 0m")
    @Test
    void formatTimeRemaining_oneHour_returnsOneHourZeroMinutes() {
        long oneHourMs = 60L * 60_000L;
        assertEquals("1h 0m", ExpiredQuestScanTask.formatTimeRemaining(oneHourMs));
    }

    @DisplayName("90 minutes formats as 1h 30m")
    @Test
    void formatTimeRemaining_ninetyMinutes_returnsOneHourThirtyMinutes() {
        long ninetyMinutesMs = 90L * 60_000L;
        assertEquals("1h 30m", ExpiredQuestScanTask.formatTimeRemaining(ninetyMinutesMs));
    }

    @DisplayName("25 minutes formats as 0h 25m")
    @Test
    void formatTimeRemaining_twentyFiveMinutes_returnsZeroHoursTwentyFiveMinutes() {
        long twentyFiveMinutesMs = 25L * 60_000L;
        assertEquals("0h 25m", ExpiredQuestScanTask.formatTimeRemaining(twentyFiveMinutesMs));
    }

    @DisplayName("Large value (3h 45m) formats correctly")
    @Test
    void formatTimeRemaining_largeValue_formatsCorrectly() {
        long ms = (3L * 60 + 45) * 60_000L;
        assertEquals("3h 45m", ExpiredQuestScanTask.formatTimeRemaining(ms));
    }
}
