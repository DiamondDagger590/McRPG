package us.eunoians.mcrpg.quest.board.category;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BoardSlotCategoryRegistry")
class BoardSlotCategoryRegistryTest {

    private static final NamespacedKey REFRESH_KEY = new NamespacedKey("mcrpg", "daily");
    private static final NamespacedKey SCOPE_KEY = new NamespacedKey("mcrpg", "single_player");

    private BoardSlotCategoryRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new BoardSlotCategoryRegistry();
    }

    private BoardSlotCategory buildCategory(String name, BoardSlotCategory.Visibility visibility, int priority) {
        return new BoardSlotCategory(
                new NamespacedKey("mcrpg", name),
                visibility,
                REFRESH_KEY,
                Duration.ofHours(24),
                Duration.ofHours(48),
                SCOPE_KEY,
                1, 5, 0.75, priority,
                null, null, null
        );
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("registers category successfully")
        void register_addsCategory() {
            BoardSlotCategory category = buildCategory("daily_shared", BoardSlotCategory.Visibility.SHARED, 10);
            registry.register(category);
            assertTrue(registry.registered(category));
        }

        @Test
        @DisplayName("replaces category with same key")
        void register_sameKey_replaces() {
            BoardSlotCategory original = buildCategory("daily_shared", BoardSlotCategory.Visibility.SHARED, 10);
            BoardSlotCategory replacement = buildCategory("daily_shared", BoardSlotCategory.Visibility.PERSONAL, 20);
            registry.register(original);
            registry.register(replacement);
            assertEquals(BoardSlotCategory.Visibility.PERSONAL,
                    registry.get(new NamespacedKey("mcrpg", "daily_shared")).orElseThrow().getVisibility());
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        @DisplayName("returns present for registered key")
        void get_registeredKey_returnsPresent() {
            BoardSlotCategory category = buildCategory("daily_shared", BoardSlotCategory.Visibility.SHARED, 10);
            registry.register(category);
            assertTrue(registry.get(new NamespacedKey("mcrpg", "daily_shared")).isPresent());
        }

        @Test
        @DisplayName("returns empty for unregistered key")
        void get_unregisteredKey_returnsEmpty() {
            assertTrue(registry.get(new NamespacedKey("mcrpg", "nonexistent")).isEmpty());
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("returns empty collection when empty")
        void getAll_empty_returnsEmpty() {
            assertTrue(registry.getAll().isEmpty());
        }

        @Test
        @DisplayName("returns all registered categories")
        void getAll_afterRegistration_returnsAll() {
            registry.register(buildCategory("cat_a", BoardSlotCategory.Visibility.SHARED, 1));
            registry.register(buildCategory("cat_b", BoardSlotCategory.Visibility.PERSONAL, 2));
            assertEquals(2, registry.getAll().size());
        }
    }

    @Nested
    @DisplayName("getAllByPriority")
    class GetAllByPriority {

        @Test
        @DisplayName("returns categories sorted by priority descending")
        void getAllByPriority_sortedDescending() {
            BoardSlotCategory low = buildCategory("low", BoardSlotCategory.Visibility.SHARED, 1);
            BoardSlotCategory mid = buildCategory("mid", BoardSlotCategory.Visibility.SHARED, 5);
            BoardSlotCategory high = buildCategory("high", BoardSlotCategory.Visibility.SHARED, 10);
            registry.register(low);
            registry.register(high);
            registry.register(mid);

            List<BoardSlotCategory> sorted = registry.getAllByPriority();
            assertEquals(3, sorted.size());
            assertEquals(10, sorted.get(0).getPriority());
            assertEquals(5, sorted.get(1).getPriority());
            assertEquals(1, sorted.get(2).getPriority());
        }

        @Test
        @DisplayName("returns empty list when registry is empty")
        void getAllByPriority_empty_returnsEmpty() {
            assertTrue(registry.getAllByPriority().isEmpty());
        }
    }

    @Nested
    @DisplayName("getByVisibility")
    class GetByVisibility {

        @Test
        @DisplayName("returns only categories with matching visibility")
        void getByVisibility_filtersCorrectly() {
            registry.register(buildCategory("shared_1", BoardSlotCategory.Visibility.SHARED, 1));
            registry.register(buildCategory("personal_1", BoardSlotCategory.Visibility.PERSONAL, 2));
            registry.register(buildCategory("shared_2", BoardSlotCategory.Visibility.SHARED, 3));
            registry.register(buildCategory("scoped_1", BoardSlotCategory.Visibility.SCOPED, 4));

            List<BoardSlotCategory> shared = registry.getByVisibility(BoardSlotCategory.Visibility.SHARED);
            assertEquals(2, shared.size());
            assertTrue(shared.stream().allMatch(c -> c.getVisibility() == BoardSlotCategory.Visibility.SHARED));

            List<BoardSlotCategory> personal = registry.getByVisibility(BoardSlotCategory.Visibility.PERSONAL);
            assertEquals(1, personal.size());

            List<BoardSlotCategory> scoped = registry.getByVisibility(BoardSlotCategory.Visibility.SCOPED);
            assertEquals(1, scoped.size());
        }

        @Test
        @DisplayName("returns empty list when no categories match")
        void getByVisibility_noMatch_returnsEmpty() {
            registry.register(buildCategory("shared_1", BoardSlotCategory.Visibility.SHARED, 1));
            assertTrue(registry.getByVisibility(BoardSlotCategory.Visibility.PERSONAL).isEmpty());
        }
    }

    @Nested
    @DisplayName("replaceConfigCategories")
    class ReplaceConfigCategories {

        @Test
        @DisplayName("replaces config-loaded categories with fresh set")
        void replaceConfigCategories_replacesOld() {
            BoardSlotCategory original = buildCategory("config_cat", BoardSlotCategory.Visibility.SHARED, 1);
            NamespacedKey originalKey = new NamespacedKey("mcrpg", "config_cat");
            registry.replaceConfigCategories(Map.of(originalKey, original));
            assertTrue(registry.get(originalKey).isPresent());

            BoardSlotCategory replacement = buildCategory("new_config_cat", BoardSlotCategory.Visibility.PERSONAL, 5);
            NamespacedKey replacementKey = new NamespacedKey("mcrpg", "new_config_cat");
            registry.replaceConfigCategories(Map.of(replacementKey, replacement));

            assertFalse(registry.get(originalKey).isPresent());
            assertTrue(registry.get(replacementKey).isPresent());
        }

        @Test
        @DisplayName("preserves expansion-registered categories")
        void replaceConfigCategories_preservesExpansion() {
            BoardSlotCategory expansion = buildCategory("expansion_cat", BoardSlotCategory.Visibility.SHARED, 1);
            registry.register(expansion);

            BoardSlotCategory configCat = buildCategory("config_cat", BoardSlotCategory.Visibility.SHARED, 2);
            NamespacedKey configKey = new NamespacedKey("mcrpg", "config_cat");
            registry.replaceConfigCategories(Map.of(configKey, configCat));

            registry.replaceConfigCategories(Map.of());

            assertTrue(registry.registered(expansion));
            assertFalse(registry.get(configKey).isPresent());
        }
    }

    @Nested
    @DisplayName("clear")
    class Clear {

        @Test
        @DisplayName("removes all categories")
        void clear_removesAll() {
            registry.register(buildCategory("cat_a", BoardSlotCategory.Visibility.SHARED, 1));
            registry.register(buildCategory("cat_b", BoardSlotCategory.Visibility.PERSONAL, 2));
            registry.replaceConfigCategories(Map.of(
                    new NamespacedKey("mcrpg", "config_cat"),
                    buildCategory("config_cat", BoardSlotCategory.Visibility.SHARED, 3)
            ));

            registry.clear();

            assertTrue(registry.getAll().isEmpty());
        }
    }

    @Nested
    @DisplayName("registered")
    class Registered {

        @Test
        @DisplayName("returns true for registered category")
        void registered_present_returnsTrue() {
            BoardSlotCategory category = buildCategory("test", BoardSlotCategory.Visibility.SHARED, 1);
            registry.register(category);
            assertTrue(registry.registered(category));
        }

        @Test
        @DisplayName("returns false for unregistered category")
        void registered_absent_returnsFalse() {
            BoardSlotCategory category = buildCategory("test", BoardSlotCategory.Visibility.SHARED, 1);
            assertFalse(registry.registered(category));
        }
    }
}
