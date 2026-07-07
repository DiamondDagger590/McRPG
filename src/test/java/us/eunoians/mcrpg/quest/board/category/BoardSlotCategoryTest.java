package us.eunoians.mcrpg.quest.board.category;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BoardSlotCategory")
class BoardSlotCategoryTest {

    private static final NamespacedKey TEST_KEY = new NamespacedKey("mcrpg", "test_category");
    private static final NamespacedKey REFRESH_KEY = new NamespacedKey("mcrpg", "daily");
    private static final NamespacedKey SCOPE_KEY = new NamespacedKey("mcrpg", "single_player");

    private BoardSlotCategory buildCategory(Duration appearanceCooldown,
                                            String requiredPermission,
                                            Integer maxActivePerEntity) {
        return new BoardSlotCategory(
                TEST_KEY,
                BoardSlotCategory.Visibility.SHARED,
                REFRESH_KEY,
                Duration.ofHours(24),
                Duration.ofHours(48),
                SCOPE_KEY,
                1,
                5,
                0.75,
                10,
                appearanceCooldown,
                requiredPermission,
                maxActivePerEntity
        );
    }

    private BoardSlotCategory buildDefaultCategory() {
        return buildCategory(null, null, null);
    }

    @Nested
    @DisplayName("constructor and getters")
    class ConstructorAndGetters {

        @Test
        @DisplayName("getKey returns constructor value")
        void getKey_returnsConstructorValue() {
            BoardSlotCategory category = buildDefaultCategory();
            assertEquals(TEST_KEY, category.getKey());
        }

        @Test
        @DisplayName("getVisibility returns constructor value")
        void getVisibility_returnsConstructorValue() {
            BoardSlotCategory category = buildDefaultCategory();
            assertEquals(BoardSlotCategory.Visibility.SHARED, category.getVisibility());
        }

        @Test
        @DisplayName("getRefreshTypeKey returns constructor value")
        void getRefreshTypeKey_returnsConstructorValue() {
            BoardSlotCategory category = buildDefaultCategory();
            assertEquals(REFRESH_KEY, category.getRefreshTypeKey());
        }

        @Test
        @DisplayName("getRefreshInterval returns constructor value")
        void getRefreshInterval_returnsConstructorValue() {
            BoardSlotCategory category = buildDefaultCategory();
            assertEquals(Duration.ofHours(24), category.getRefreshInterval());
        }

        @Test
        @DisplayName("getCompletionTime returns constructor value")
        void getCompletionTime_returnsConstructorValue() {
            BoardSlotCategory category = buildDefaultCategory();
            assertEquals(Duration.ofHours(48), category.getCompletionTime());
        }

        @Test
        @DisplayName("getScopeProviderKey returns constructor value")
        void getScopeProviderKey_returnsConstructorValue() {
            BoardSlotCategory category = buildDefaultCategory();
            assertEquals(SCOPE_KEY, category.getScopeProviderKey());
        }

        @Test
        @DisplayName("getMin returns constructor value")
        void getMin_returnsConstructorValue() {
            BoardSlotCategory category = buildDefaultCategory();
            assertEquals(1, category.getMin());
        }

        @Test
        @DisplayName("getMax returns constructor value")
        void getMax_returnsConstructorValue() {
            BoardSlotCategory category = buildDefaultCategory();
            assertEquals(5, category.getMax());
        }

        @Test
        @DisplayName("getChancePerSlot returns constructor value")
        void getChancePerSlot_returnsConstructorValue() {
            BoardSlotCategory category = buildDefaultCategory();
            assertEquals(0.75, category.getChancePerSlot());
        }

        @Test
        @DisplayName("getPriority returns constructor value")
        void getPriority_returnsConstructorValue() {
            BoardSlotCategory category = buildDefaultCategory();
            assertEquals(10, category.getPriority());
        }
    }

    @Nested
    @DisplayName("Visibility enum")
    class VisibilityEnum {

        @ParameterizedTest
        @EnumSource(BoardSlotCategory.Visibility.class)
        @DisplayName("all values are accessible")
        void allValues_areAccessible(BoardSlotCategory.Visibility visibility) {
            BoardSlotCategory category = new BoardSlotCategory(
                    TEST_KEY, visibility, REFRESH_KEY, Duration.ofHours(24),
                    Duration.ofHours(48), SCOPE_KEY, 1, 5, 0.75, 10, null, null, null
            );
            assertEquals(visibility, category.getVisibility());
        }
    }

    @Nested
    @DisplayName("getAppearanceCooldown")
    class GetAppearanceCooldown {

        @Test
        @DisplayName("returns empty when null")
        void getAppearanceCooldown_null_returnsEmpty() {
            BoardSlotCategory category = buildCategory(null, null, null);
            assertTrue(category.getAppearanceCooldown().isEmpty());
        }

        @Test
        @DisplayName("returns present with value when set")
        void getAppearanceCooldown_set_returnsValue() {
            Duration cooldown = Duration.ofHours(6);
            BoardSlotCategory category = buildCategory(cooldown, null, null);
            assertTrue(category.getAppearanceCooldown().isPresent());
            assertEquals(cooldown, category.getAppearanceCooldown().orElseThrow());
        }
    }

    @Nested
    @DisplayName("getRequiredPermission")
    class GetRequiredPermission {

        @Test
        @DisplayName("returns empty when null")
        void getRequiredPermission_null_returnsEmpty() {
            BoardSlotCategory category = buildCategory(null, null, null);
            assertTrue(category.getRequiredPermission().isEmpty());
        }

        @Test
        @DisplayName("returns present with value when set")
        void getRequiredPermission_set_returnsValue() {
            BoardSlotCategory category = buildCategory(null, "mcrpg.board.premium", null);
            assertTrue(category.getRequiredPermission().isPresent());
            assertEquals("mcrpg.board.premium", category.getRequiredPermission().orElseThrow());
        }
    }

    @Nested
    @DisplayName("getMaxActivePerEntity")
    class GetMaxActivePerEntity {

        @Test
        @DisplayName("returns empty when null")
        void getMaxActivePerEntity_null_returnsEmpty() {
            BoardSlotCategory category = buildCategory(null, null, null);
            assertFalse(category.getMaxActivePerEntity().isPresent());
        }

        @Test
        @DisplayName("returns present with value when set")
        void getMaxActivePerEntity_set_returnsValue() {
            BoardSlotCategory category = buildCategory(null, null, 3);
            assertTrue(category.getMaxActivePerEntity().isPresent());
            assertEquals(3, category.getMaxActivePerEntity().orElseThrow());
        }
    }
}
