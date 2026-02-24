package us.eunoians.mcrpg.quest.board.refresh;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.refresh.builtin.DailyRefreshType;
import us.eunoians.mcrpg.quest.board.refresh.builtin.WeeklyRefreshType;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RefreshTypeRegistryTest extends McRPGBaseTest {

    private final RefreshTypeRegistry registry = new RefreshTypeRegistry();

    @DisplayName("register and get by key returns registered type")
    @Test
    void register_get_returnsType() {
        DailyRefreshType daily = new DailyRefreshType();
        registry.register(daily);
        assertTrue(registry.get(new NamespacedKey("mcrpg", "daily")).isPresent());
        assertSame(daily, registry.get(daily.getKey()).orElseThrow());
    }

    @DisplayName("unregistered key returns empty Optional")
    @Test
    void get_unregisteredKey_returnsEmpty() {
        assertTrue(registry.get(new NamespacedKey("mcrpg", "nonexistent")).isEmpty());
    }

    @DisplayName("duplicate registration throws IllegalStateException")
    @Test
    void register_duplicateKey_throws() {
        registry.register(new DailyRefreshType());
        assertThrows(IllegalStateException.class, () -> registry.register(new DailyRefreshType()));
    }

    @DisplayName("getAll returns all registered types")
    @Test
    void getAll_returnsAllRegistered() {
        DailyRefreshType daily = new DailyRefreshType();
        WeeklyRefreshType weekly = new WeeklyRefreshType(DayOfWeek.MONDAY);
        registry.register(daily);
        registry.register(weekly);
        assertEquals(2, registry.getAll().size());
        assertTrue(registry.getAll().contains(daily));
        assertTrue(registry.getAll().contains(weekly));
    }

    @DisplayName("getTimeBasedTypes returns only time-based types")
    @Test
    void getTimeBasedTypes_returnsOnlyTimeBased() {
        DailyRefreshType daily = new DailyRefreshType();
        WeeklyRefreshType weekly = new WeeklyRefreshType(DayOfWeek.SUNDAY);
        EventDrivenRefreshType eventDriven = new EventDrivenRefreshType();
        registry.register(daily);
        registry.register(weekly);
        registry.register(eventDriven);

        var timeBased = registry.getTimeBasedTypes();
        assertEquals(2, timeBased.size());
        assertTrue(timeBased.contains(daily));
        assertTrue(timeBased.contains(weekly));
        assertFalse(timeBased.contains(eventDriven));
    }

    @DisplayName("registered returns true for registered type")
    @Test
    void registered_registeredType_returnsTrue() {
        DailyRefreshType daily = new DailyRefreshType();
        registry.register(daily);
        assertTrue(registry.registered(daily));
    }

    @DisplayName("registered returns false for unregistered type")
    @Test
    void registered_unregisteredType_returnsFalse() {
        DailyRefreshType daily = new DailyRefreshType();
        assertFalse(registry.registered(daily));
    }

    private static class EventDrivenRefreshType extends RefreshType {
        EventDrivenRefreshType() {
            super(new NamespacedKey("mcrpg", "event_driven"));
        }

        @Override
        public boolean isTimeBased() {
            return false;
        }

        @Override
        public boolean shouldRefresh(long lastRefreshEpoch, ZonedDateTime now) {
            return false;
        }
    }
}
