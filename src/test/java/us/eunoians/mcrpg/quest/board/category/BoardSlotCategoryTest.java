package us.eunoians.mcrpg.quest.board.category;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoardSlotCategoryTest extends McRPGBaseTest {

    @DisplayName("construction with all fields returns correct values from getters")
    @Test
    void construction_allFields_gettersReturnCorrectValues() {
        NamespacedKey key = new NamespacedKey("mcrpg", "test");
        NamespacedKey refreshTypeKey = new NamespacedKey("mcrpg", "daily");
        NamespacedKey scopeProviderKey = new NamespacedKey("mcrpg", "single_player");
        Duration refreshInterval = Duration.ofHours(24);
        Duration completionTime = Duration.ofHours(48);
        Duration appearanceCooldown = Duration.ofMinutes(30);
        String requiredPermission = "mcrpg.quest.vip";

        BoardSlotCategory cat = new BoardSlotCategory(key, BoardSlotCategory.Visibility.SHARED,
                refreshTypeKey, refreshInterval, completionTime, scopeProviderKey,
                1, 5, 0.5, 10, appearanceCooldown, requiredPermission);

        assertEquals(key, cat.getKey());
        assertEquals(BoardSlotCategory.Visibility.SHARED, cat.getVisibility());
        assertEquals(refreshTypeKey, cat.getRefreshTypeKey());
        assertEquals(refreshInterval, cat.getRefreshInterval());
        assertEquals(completionTime, cat.getCompletionTime());
        assertEquals(scopeProviderKey, cat.getScopeProviderKey());
        assertEquals(1, cat.getMin());
        assertEquals(5, cat.getMax());
        assertEquals(0.5, cat.getChancePerSlot());
        assertEquals(10, cat.getPriority());
        assertEquals(Optional.of(appearanceCooldown), cat.getAppearanceCooldown());
        assertEquals(Optional.of(requiredPermission), cat.getRequiredPermission());
    }

    @DisplayName("getAppearanceCooldown returns Optional.empty when null")
    @Test
    void getAppearanceCooldown_null_returnsEmpty() {
        BoardSlotCategory cat = category("a", 1, BoardSlotCategory.Visibility.PERSONAL);
        assertTrue(cat.getAppearanceCooldown().isEmpty());
    }

    @DisplayName("getAppearanceCooldown returns Optional with value when set")
    @Test
    void getAppearanceCooldown_set_returnsOptionalWithValue() {
        Duration cooldown = Duration.ofHours(2);
        BoardSlotCategory cat = new BoardSlotCategory(
                new NamespacedKey("mcrpg", "x"), BoardSlotCategory.Visibility.SCOPED,
                new NamespacedKey("mcrpg", "daily"), Duration.ofHours(24), Duration.ofHours(48),
                new NamespacedKey("mcrpg", "single_player"), 1, 5, 0.5, 1, cooldown, null);
        assertTrue(cat.getAppearanceCooldown().isPresent());
        assertEquals(cooldown, cat.getAppearanceCooldown().orElseThrow());
    }

    @DisplayName("getRequiredPermission returns Optional.empty when null")
    @Test
    void getRequiredPermission_null_returnsEmpty() {
        BoardSlotCategory cat = category("a", 1, BoardSlotCategory.Visibility.PERSONAL);
        assertTrue(cat.getRequiredPermission().isEmpty());
    }

    @DisplayName("getRequiredPermission returns Optional with value when set")
    @Test
    void getRequiredPermission_set_returnsOptionalWithValue() {
        String perm = "mcrpg.special";
        BoardSlotCategory cat = new BoardSlotCategory(
                new NamespacedKey("mcrpg", "x"), BoardSlotCategory.Visibility.SHARED,
                new NamespacedKey("mcrpg", "daily"), Duration.ofHours(24), Duration.ofHours(48),
                new NamespacedKey("mcrpg", "single_player"), 1, 5, 0.5, 1, null, perm);
        assertTrue(cat.getRequiredPermission().isPresent());
        assertEquals(perm, cat.getRequiredPermission().orElseThrow());
    }

    @DisplayName("getVisibility returns SHARED for SHARED visibility")
    @Test
    void getVisibility_shared_returnsShared() {
        BoardSlotCategory cat = category("s", 1, BoardSlotCategory.Visibility.SHARED);
        assertEquals(BoardSlotCategory.Visibility.SHARED, cat.getVisibility());
    }

    @DisplayName("getVisibility returns PERSONAL for PERSONAL visibility")
    @Test
    void getVisibility_personal_returnsPersonal() {
        BoardSlotCategory cat = category("p", 1, BoardSlotCategory.Visibility.PERSONAL);
        assertEquals(BoardSlotCategory.Visibility.PERSONAL, cat.getVisibility());
    }

    @DisplayName("getVisibility returns SCOPED for SCOPED visibility")
    @Test
    void getVisibility_scoped_returnsScoped() {
        BoardSlotCategory cat = category("sc", 1, BoardSlotCategory.Visibility.SCOPED);
        assertEquals(BoardSlotCategory.Visibility.SCOPED, cat.getVisibility());
    }

    private static BoardSlotCategory category(String name, int priority, BoardSlotCategory.Visibility vis) {
        return new BoardSlotCategory(
                new NamespacedKey("mcrpg", name), vis,
                new NamespacedKey("mcrpg", "daily"), Duration.ofHours(24), Duration.ofHours(48),
                new NamespacedKey("mcrpg", "single_player"), 1, 5, 0.5, priority, null, null);
    }
}
