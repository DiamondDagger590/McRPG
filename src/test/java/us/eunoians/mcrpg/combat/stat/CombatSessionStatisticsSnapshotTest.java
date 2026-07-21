package us.eunoians.mcrpg.combat.stat;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CombatSessionStatisticsSnapshot")
class CombatSessionStatisticsSnapshotTest {

    private static final NamespacedKey DOUBLE_KEY = new NamespacedKey("mcrpg", "test_double_stat");
    private static final NamespacedKey LONG_KEY = new NamespacedKey("mcrpg", "test_long_stat");

    @Test
    @DisplayName("Constructor stores all values correctly")
    void constructor_storesAllValues() {
        CombatSessionStatisticsSnapshot snapshot = new CombatSessionStatisticsSnapshot(
                Map.of(DOUBLE_KEY, 4.5), Map.of(LONG_KEY, 3L));

        assertEquals(4.5, snapshot.getDouble(DOUBLE_KEY));
        assertEquals(3L, snapshot.getLong(LONG_KEY));
    }

    @Test
    @DisplayName("getDouble returns 0.0 for absent keys")
    void getDouble_returnsZero_forAbsentKey() {
        CombatSessionStatisticsSnapshot snapshot = new CombatSessionStatisticsSnapshot(Map.of(), Map.of());

        assertEquals(0.0, snapshot.getDouble(DOUBLE_KEY));
    }

    @Test
    @DisplayName("getLong returns 0 for absent keys")
    void getLong_returnsZero_forAbsentKey() {
        CombatSessionStatisticsSnapshot snapshot = new CombatSessionStatisticsSnapshot(Map.of(), Map.of());

        assertEquals(0L, snapshot.getLong(LONG_KEY));
    }

    @Test
    @DisplayName("getDouble(SESSION_DURATION) returns the duration written before snapshot")
    void getDouble_sessionDuration_returnsWrittenValue() {
        CombatSessionStatistics statistics = new CombatSessionStatistics();
        statistics.setDouble(CombatSessionStatisticKey.SESSION_DURATION, 12.5);

        CombatSessionStatisticsSnapshot snapshot = statistics.snapshot();

        assertEquals(12.5, snapshot.getDouble(CombatSessionStatisticKey.SESSION_DURATION));
    }

    @Test
    @DisplayName("getDoubleStatistics returns an unmodifiable map")
    void getDoubleStatistics_returnsUnmodifiable() {
        CombatSessionStatisticsSnapshot snapshot = new CombatSessionStatisticsSnapshot(
                Map.of(DOUBLE_KEY, 1.0), Map.of());

        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.getDoubleStatistics().put(LONG_KEY, 2.0));
    }

    @Test
    @DisplayName("getLongStatistics returns an unmodifiable map")
    void getLongStatistics_returnsUnmodifiable() {
        CombatSessionStatisticsSnapshot snapshot = new CombatSessionStatisticsSnapshot(
                Map.of(), Map.of(LONG_KEY, 1L));

        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.getLongStatistics().put(LONG_KEY, 2L));
    }
}
