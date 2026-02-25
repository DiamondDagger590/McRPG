package us.eunoians.mcrpg.configuration;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.category.BoardSlotCategory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoardCategoryConfigLoaderTest extends McRPGBaseTest {

    @TempDir(cleanup = CleanupMode.NEVER)
    File tempDir;

    private BoardCategoryConfigLoader loader;

    @BeforeEach
    void setUp() {
        loader = new BoardCategoryConfigLoader();
    }

    private File writeYaml(String filename, String content) throws IOException {
        File file = new File(tempDir, filename);
        Files.writeString(file.toPath(), content);
        return file;
    }

    @DisplayName("loadCategoriesFromDirectory loads valid YAML file")
    @Test
    void loadCategoriesFromDirectory_loadsValidYamlFile() throws IOException {
        String yaml = """
                daily-personal:
                  visibility: PERSONAL
                  refresh-type: daily
                  refresh-interval: 24h
                  completion-time: 48h
                  scope-provider: single_player
                  min: 1
                  max: 3
                  chance-per-slot: 0.75
                  priority: 10
                """;
        writeYaml("categories.yml", yaml);

        Map<NamespacedKey, BoardSlotCategory> result = loader.loadCategoriesFromDirectory(tempDir);

        assertEquals(1, result.size());
        NamespacedKey key = new NamespacedKey("mcrpg", "daily_personal");
        BoardSlotCategory cat = result.get(key);
        assertNotNull(cat);
        assertEquals(key, cat.getKey());
        assertEquals(BoardSlotCategory.Visibility.PERSONAL, cat.getVisibility());
        assertEquals(new NamespacedKey("mcrpg", "daily"), cat.getRefreshTypeKey());
        assertEquals(java.time.Duration.ofHours(24), cat.getRefreshInterval());
        assertEquals(java.time.Duration.ofHours(48), cat.getCompletionTime());
        assertEquals(new NamespacedKey("mcrpg", "single_player"), cat.getScopeProviderKey());
        assertEquals(1, cat.getMin());
        assertEquals(3, cat.getMax());
        assertEquals(0.75, cat.getChancePerSlot());
        assertEquals(10, cat.getPriority());
    }

    @DisplayName("loadCategoriesFromDirectory returns empty for nonexistent directory")
    @Test
    void loadCategoriesFromDirectory_returnsEmptyForNonexistentDirectory() {
        File nonExistent = new File(tempDir, "nonexistent_" + System.currentTimeMillis());

        Map<NamespacedKey, BoardSlotCategory> result = loader.loadCategoriesFromDirectory(nonExistent);

        assertTrue(result.isEmpty());
    }

    @DisplayName("loadCategoriesFromDirectory returns empty for empty directory")
    @Test
    void loadCategoriesFromDirectory_returnsEmptyForEmptyDirectory() {
        Map<NamespacedKey, BoardSlotCategory> result = loader.loadCategoriesFromDirectory(tempDir);

        assertTrue(result.isEmpty());
    }

    @DisplayName("loadCategoriesFromDirectory auto-namespaces plain keys")
    @Test
    void loadCategoriesFromDirectory_autoNamespacesPlainKeys() throws IOException {
        String yaml = """
                weekly-shared:
                  visibility: SHARED
                  refresh-type: weekly
                  refresh-interval: 7d
                  completion-time: 168h
                  scope-provider: single_player
                  min: 0
                  max: 2
                  chance-per-slot: 1.0
                  priority: 5
                """;
        writeYaml("weekly.yml", yaml);

        Map<NamespacedKey, BoardSlotCategory> result = loader.loadCategoriesFromDirectory(tempDir);

        NamespacedKey expectedKey = new NamespacedKey("mcrpg", "weekly_shared");
        assertTrue(result.containsKey(expectedKey));
        assertEquals(expectedKey, result.get(expectedKey).getKey());
    }

    @DisplayName("loadCategoriesFromDirectory parses namespaced keys as-is")
    @Test
    void loadCategoriesFromDirectory_parsesNamespacedKeysAsIs() throws IOException {
        String yaml = """
                custom:my_category:
                  visibility: SHARED
                  refresh-type: daily
                  refresh-interval: 24h
                  completion-time: 24h
                  scope-provider: single_player
                  min: 0
                  max: 1
                  chance-per-slot: 1.0
                  priority: 0
                """;
        writeYaml("custom.yml", yaml);

        Map<NamespacedKey, BoardSlotCategory> result = loader.loadCategoriesFromDirectory(tempDir);

        NamespacedKey expectedKey = NamespacedKey.fromString("custom:my_category");
        assertTrue(result.containsKey(expectedKey));
        assertEquals(expectedKey, result.get(expectedKey).getKey());
    }

    @DisplayName("loadCategoriesFromDirectory loads multiple categories")
    @Test
    void loadCategoriesFromDirectory_loadsMultipleCategories() throws IOException {
        String yaml = """
                daily-personal:
                  visibility: PERSONAL
                  refresh-type: daily
                  refresh-interval: 24h
                  completion-time: 48h
                  scope-provider: single_player
                  min: 1
                  max: 3
                  chance-per-slot: 0.75
                  priority: 10
                weekly-shared:
                  visibility: SHARED
                  refresh-type: weekly
                  refresh-interval: 7d
                  completion-time: 168h
                  scope-provider: single_player
                  min: 0
                  max: 2
                  chance-per-slot: 1.0
                  priority: 5
                """;
        writeYaml("multi.yml", yaml);

        Map<NamespacedKey, BoardSlotCategory> result = loader.loadCategoriesFromDirectory(tempDir);

        assertEquals(2, result.size());
        assertTrue(result.containsKey(new NamespacedKey("mcrpg", "daily_personal")));
        assertTrue(result.containsKey(new NamespacedKey("mcrpg", "weekly_shared")));
    }

    @DisplayName("loadCategoriesFromDirectory parses max-active-per-entity when present")
    @Test
    void loadCategoriesFromDirectory_parsesMaxActivePerEntity() throws IOException {
        String yaml = """
                land-daily:
                  visibility: SCOPED
                  refresh-type: daily
                  refresh-interval: 24h
                  completion-time: 48h
                  scope-provider: mcrpg:land_scope
                  min: 1
                  max: 2
                  chance-per-slot: 1.0
                  priority: 5
                  max-active-per-entity: 3
                """;
        writeYaml("land.yml", yaml);

        Map<NamespacedKey, BoardSlotCategory> result = loader.loadCategoriesFromDirectory(tempDir);

        NamespacedKey key = new NamespacedKey("mcrpg", "land_daily");
        BoardSlotCategory cat = result.get(key);
        assertNotNull(cat);
        assertEquals(OptionalInt.of(3), cat.getMaxActivePerEntity());
    }

    @DisplayName("loadCategoriesFromDirectory returns empty max-active-per-entity when absent")
    @Test
    void loadCategoriesFromDirectory_maxActivePerEntityAbsent_returnsEmpty() throws IOException {
        String yaml = """
                basic:
                  visibility: PERSONAL
                  refresh-type: daily
                  refresh-interval: 24h
                  completion-time: 24h
                  scope-provider: single_player
                  min: 0
                  max: 1
                  chance-per-slot: 1.0
                  priority: 0
                """;
        writeYaml("basic.yml", yaml);

        Map<NamespacedKey, BoardSlotCategory> result = loader.loadCategoriesFromDirectory(tempDir);

        NamespacedKey key = new NamespacedKey("mcrpg", "basic");
        BoardSlotCategory cat = result.get(key);
        assertNotNull(cat);
        assertEquals(OptionalInt.empty(), cat.getMaxActivePerEntity());
    }
}
