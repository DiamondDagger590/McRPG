package us.eunoians.mcrpg.combat.stat;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CombatSessionStatistics")
class CombatSessionStatisticsTest {

    private static final NamespacedKey DOUBLE_KEY = new NamespacedKey("mcrpg", "test_double_stat");
    private static final NamespacedKey LONG_KEY = new NamespacedKey("mcrpg", "test_long_stat");

    private CombatSessionStatistics statistics;

    @BeforeEach
    void setUp() {
        statistics = new CombatSessionStatistics();
    }

    @Test
    @DisplayName("new container has zero values for all keys")
    void newContainer_hasZeroValues() {
        assertEquals(0.0, statistics.getDouble(DOUBLE_KEY));
        assertEquals(0L, statistics.getLong(LONG_KEY));
    }

    @Test
    @DisplayName("incrementDouble accumulates correctly across multiple calls")
    void incrementDouble_accumulates() {
        statistics.incrementDouble(DOUBLE_KEY, 2.5);
        statistics.incrementDouble(DOUBLE_KEY, 1.5);

        assertEquals(4.0, statistics.getDouble(DOUBLE_KEY));
    }

    @Test
    @DisplayName("incrementLong accumulates correctly across multiple calls")
    void incrementLong_accumulates() {
        statistics.incrementLong(LONG_KEY, 3);
        statistics.incrementLong(LONG_KEY, 4);

        assertEquals(7L, statistics.getLong(LONG_KEY));
    }

    @Test
    @DisplayName("incrementDouble subtracts when given a negative delta")
    void incrementDouble_subtracts_whenNegative() {
        statistics.incrementDouble(DOUBLE_KEY, 5.0);
        statistics.incrementDouble(DOUBLE_KEY, -2.0);

        assertEquals(3.0, statistics.getDouble(DOUBLE_KEY));
    }

    @Test
    @DisplayName("incrementLong subtracts when given a negative delta")
    void incrementLong_subtracts_whenNegative() {
        statistics.incrementLong(LONG_KEY, 5);
        statistics.incrementLong(LONG_KEY, -2);

        assertEquals(3L, statistics.getLong(LONG_KEY));
    }

    @Test
    @DisplayName("getDouble returns 0.0 for an unset key")
    void getDouble_returnsZero_whenUnset() {
        assertEquals(0.0, statistics.getDouble(DOUBLE_KEY));
    }

    @Test
    @DisplayName("getLong returns 0 for an unset key")
    void getLong_returnsZero_whenUnset() {
        assertEquals(0L, statistics.getLong(LONG_KEY));
    }

    @Test
    @DisplayName("setDouble overwrites previous values")
    void setDouble_overwritesPreviousValue() {
        statistics.incrementDouble(DOUBLE_KEY, 10.0);
        statistics.setDouble(DOUBLE_KEY, 3.0);

        assertEquals(3.0, statistics.getDouble(DOUBLE_KEY));
    }

    @Test
    @DisplayName("setLong overwrites previous values")
    void setLong_overwritesPreviousValue() {
        statistics.incrementLong(LONG_KEY, 10);
        statistics.setLong(LONG_KEY, 3);

        assertEquals(3L, statistics.getLong(LONG_KEY));
    }

    @Test
    @DisplayName("snapshot returns an immutable snapshot with the current values")
    void snapshot_returnsCurrentValues() {
        statistics.incrementDouble(DOUBLE_KEY, 5.0);
        statistics.incrementLong(LONG_KEY, 2);

        CombatSessionStatisticsSnapshot snapshot = statistics.snapshot();

        assertEquals(5.0, snapshot.getDouble(DOUBLE_KEY));
        assertEquals(2L, snapshot.getLong(LONG_KEY));
    }

    @Test
    @DisplayName("snapshot is not affected by subsequent mutation of the source container")
    void snapshot_isIndependentOfLaterMutation() {
        statistics.incrementDouble(DOUBLE_KEY, 5.0);
        CombatSessionStatisticsSnapshot snapshot = statistics.snapshot();

        statistics.incrementDouble(DOUBLE_KEY, 100.0);

        assertEquals(5.0, snapshot.getDouble(DOUBLE_KEY));
    }
}
