package us.eunoians.mcrpg.task.player;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class McRPGPlayerLoadTaskTest {

    @DisplayName("Given wait period = 0 and player offline for 100s, then adjusted time is 100s")
    @Test
    public void calculateAdjustedOfflineTime_returnsFullTime_whenWaitPeriodIsZero() {
        Instant now = Instant.now();
        Instant logoutTime = now.minusSeconds(100);

        int adjustedTime = McRPGPlayerLoadTask.calculateAdjustedOfflineTime(logoutTime, now, 0.0);

        assertEquals(100, adjustedTime);
    }

    @DisplayName("Given wait period = 30s and player offline for 100s, then adjusted time is 70s")
    @Test
    public void calculateAdjustedOfflineTime_returnsReducedTime_whenWaitPeriodIsLessThanOfflineTime() {
        Instant now = Instant.now();
        Instant logoutTime = now.minusSeconds(100);

        int adjustedTime = McRPGPlayerLoadTask.calculateAdjustedOfflineTime(logoutTime, now, 30.0);

        assertEquals(70, adjustedTime);
    }

    @DisplayName("Given wait period = 100s and player offline for 100s, then adjusted time is 0s")
    @Test
    public void calculateAdjustedOfflineTime_returnsZero_whenWaitPeriodEqualsOfflineTime() {
        Instant now = Instant.now();
        Instant logoutTime = now.minusSeconds(100);

        int adjustedTime = McRPGPlayerLoadTask.calculateAdjustedOfflineTime(logoutTime, now, 100.0);

        assertEquals(0, adjustedTime);
    }

    @DisplayName("Given wait period = 100s and player offline for 50s, then adjusted time is 0s (not negative)")
    @Test
    public void calculateAdjustedOfflineTime_returnsZero_whenWaitPeriodExceedsOfflineTime() {
        Instant now = Instant.now();
        Instant logoutTime = now.minusSeconds(50);

        int adjustedTime = McRPGPlayerLoadTask.calculateAdjustedOfflineTime(logoutTime, now, 100.0);

        assertEquals(0, adjustedTime);
    }

    @DisplayName("Given wait period = 60s and player offline for 90s, then adjusted time is 30s")
    @Test
    public void calculateAdjustedOfflineTime_returnsCorrectTime_withVariousValues() {
        Instant now = Instant.now();
        Instant logoutTime = now.minusSeconds(90);

        int adjustedTime = McRPGPlayerLoadTask.calculateAdjustedOfflineTime(logoutTime, now, 60.0);

        assertEquals(30, adjustedTime);
    }
}
