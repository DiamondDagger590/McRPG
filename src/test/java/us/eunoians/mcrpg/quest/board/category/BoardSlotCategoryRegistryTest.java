package us.eunoians.mcrpg.quest.board.category;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoardSlotCategoryRegistryTest extends McRPGBaseTest {

    private final BoardSlotCategoryRegistry registry = new BoardSlotCategoryRegistry();

    @DisplayName("register and get by key returns registered category")
    @Test
    void register_get_returnsCategory() {
        BoardSlotCategory cat = category("test", 5, BoardSlotCategory.Visibility.SHARED);
        registry.register(cat);
        assertTrue(registry.get(new NamespacedKey("mcrpg", "test")).isPresent());
        assertSame(cat, registry.get(new NamespacedKey("mcrpg", "test")).orElseThrow());
    }

    @DisplayName("unregistered key returns empty Optional")
    @Test
    void get_unregisteredKey_returnsEmpty() {
        assertTrue(registry.get(new NamespacedKey("mcrpg", "nonexistent")).isEmpty());
    }

    @DisplayName("getAll returns all registered categories")
    @Test
    void getAll_returnsAllRegistered() {
        BoardSlotCategory a = category("a", 1, BoardSlotCategory.Visibility.SHARED);
        BoardSlotCategory b = category("b", 2, BoardSlotCategory.Visibility.PERSONAL);
        registry.register(a);
        registry.register(b);
        assertEquals(2, registry.getAll().size());
        assertTrue(registry.getAll().contains(a));
        assertTrue(registry.getAll().contains(b));
    }

    @DisplayName("getAllByPriority returns categories sorted by priority descending")
    @Test
    void getAllByPriority_returnsSortedDescending() {
        BoardSlotCategory low = category("low", 1, BoardSlotCategory.Visibility.SHARED);
        BoardSlotCategory mid = category("mid", 5, BoardSlotCategory.Visibility.SHARED);
        BoardSlotCategory high = category("high", 10, BoardSlotCategory.Visibility.SHARED);
        registry.register(low);
        registry.register(mid);
        registry.register(high);
        var sorted = registry.getAllByPriority();
        assertEquals(high, sorted.get(0));
        assertEquals(mid, sorted.get(1));
        assertEquals(low, sorted.get(2));
    }

    @DisplayName("getByVisibility filters by visibility")
    @Test
    void getByVisibility_filtersByVisibility() {
        BoardSlotCategory shared1 = category("s1", 1, BoardSlotCategory.Visibility.SHARED);
        BoardSlotCategory shared2 = category("s2", 2, BoardSlotCategory.Visibility.SHARED);
        BoardSlotCategory personal = category("p", 3, BoardSlotCategory.Visibility.PERSONAL);
        registry.register(shared1);
        registry.register(shared2);
        registry.register(personal);

        var shared = registry.getByVisibility(BoardSlotCategory.Visibility.SHARED);
        assertEquals(2, shared.size());
        assertTrue(shared.contains(shared1));
        assertTrue(shared.contains(shared2));

        var personalList = registry.getByVisibility(BoardSlotCategory.Visibility.PERSONAL);
        assertEquals(1, personalList.size());
        assertSame(personal, personalList.get(0));
    }

    @DisplayName("replaceConfigCategories replaces config-loaded but keeps expansion-registered")
    @Test
    void replaceConfigCategories_replacesConfigKeepsExpansion() {
        BoardSlotCategory expansion = category("expansion", 100, BoardSlotCategory.Visibility.SCOPED);
        registry.register(expansion);

        BoardSlotCategory config1 = category("config1", 1, BoardSlotCategory.Visibility.SHARED);
        BoardSlotCategory config2 = category("config2", 2, BoardSlotCategory.Visibility.SHARED);
        registry.replaceConfigCategories(Map.of(
                new NamespacedKey("mcrpg", "config1"), config1,
                new NamespacedKey("mcrpg", "config2"), config2
        ));

        assertTrue(registry.get(new NamespacedKey("mcrpg", "expansion")).isPresent());
        assertTrue(registry.get(new NamespacedKey("mcrpg", "config1")).isPresent());
        assertTrue(registry.get(new NamespacedKey("mcrpg", "config2")).isPresent());
        assertEquals(3, registry.getAll().size());

        BoardSlotCategory config3 = category("config3", 3, BoardSlotCategory.Visibility.PERSONAL);
        BoardSlotCategory config4 = category("config4", 4, BoardSlotCategory.Visibility.PERSONAL);
        registry.replaceConfigCategories(Map.of(
                new NamespacedKey("mcrpg", "config3"), config3,
                new NamespacedKey("mcrpg", "config4"), config4
        ));

        assertTrue(registry.get(new NamespacedKey("mcrpg", "expansion")).isPresent());
        assertFalse(registry.get(new NamespacedKey("mcrpg", "config1")).isPresent());
        assertFalse(registry.get(new NamespacedKey("mcrpg", "config2")).isPresent());
        assertTrue(registry.get(new NamespacedKey("mcrpg", "config3")).isPresent());
        assertTrue(registry.get(new NamespacedKey("mcrpg", "config4")).isPresent());
        assertEquals(3, registry.getAll().size());
    }

    @DisplayName("clear empties registry")
    @Test
    void clear_emptiesRegistry() {
        registry.register(category("a", 1, BoardSlotCategory.Visibility.SHARED));
        registry.replaceConfigCategories(Map.of(
                new NamespacedKey("mcrpg", "c"), category("c", 2, BoardSlotCategory.Visibility.PERSONAL)
        ));
        assertFalse(registry.getAll().isEmpty());
        registry.clear();
        assertTrue(registry.getAll().isEmpty());
        assertTrue(registry.get(new NamespacedKey("mcrpg", "a")).isEmpty());
    }

    @DisplayName("registered returns true for registered category")
    @Test
    void registered_registeredCategory_returnsTrue() {
        BoardSlotCategory cat = category("r", 1, BoardSlotCategory.Visibility.SHARED);
        registry.register(cat);
        assertTrue(registry.registered(cat));
    }

    @DisplayName("registered returns false for unregistered category")
    @Test
    void registered_unregisteredCategory_returnsFalse() {
        BoardSlotCategory cat = category("u", 1, BoardSlotCategory.Visibility.SHARED);
        assertFalse(registry.registered(cat));
    }

    private static BoardSlotCategory category(String name, int priority, BoardSlotCategory.Visibility vis) {
        return new BoardSlotCategory(
                new NamespacedKey("mcrpg", name), vis,
                new NamespacedKey("mcrpg", "daily"), Duration.ofHours(24),
                Duration.ofHours(48), new NamespacedKey("mcrpg", "single_player"),
                1, 5, 0.5, priority, null, null, null);
    }
}
