package us.eunoians.mcrpg.configuration;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.objective.type.builtin.BlockBreakObjectiveType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestConfigLoaderTest extends McRPGBaseTest {

    private QuestConfigLoader loader;

    @BeforeEach
    public void setup() {
        loader = new QuestConfigLoader();
        QuestObjectiveTypeRegistry objReg = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_OBJECTIVE_TYPE);
        if (objReg.get(BlockBreakObjectiveType.KEY).isEmpty()) {
            objReg.register(new BlockBreakObjectiveType());
        }
    }

    @DisplayName("Given plain seconds, when parseDuration is called, then correct Duration is returned")
    @Test
    public void parseDuration_plainSeconds() {
        assertEquals(Duration.ofSeconds(3600), QuestConfigLoader.parseDuration("3600"));
    }

    @DisplayName("Given hours format, when parseDuration is called, then correct Duration is returned")
    @Test
    public void parseDuration_hoursOnly() {
        assertEquals(Duration.ofHours(24), QuestConfigLoader.parseDuration("24h"));
    }

    @DisplayName("Given days format, when parseDuration is called, then correct Duration is returned")
    @Test
    public void parseDuration_daysOnly() {
        assertEquals(Duration.ofDays(7), QuestConfigLoader.parseDuration("7d"));
    }

    @DisplayName("Given combined format, when parseDuration is called, then correct Duration is returned")
    @Test
    public void parseDuration_combined() {
        Duration expected = Duration.ofDays(1).plusHours(12).plusMinutes(30);
        assertEquals(expected, QuestConfigLoader.parseDuration("1d12h30m"));
    }

    @DisplayName("Given all units, when parseDuration is called, then correct Duration is returned")
    @Test
    public void parseDuration_withSeconds() {
        Duration expected = Duration.ofDays(1).plusHours(2).plusMinutes(3).plusSeconds(4);
        assertEquals(expected, QuestConfigLoader.parseDuration("1d2h3m4s"));
    }

    @DisplayName("Given null input, when parseDuration is called, then null is returned")
    @Test
    public void parseDuration_nullInput() {
        assertNull(QuestConfigLoader.parseDuration(null));
    }

    @DisplayName("Given empty input, when parseDuration is called, then null is returned")
    @Test
    public void parseDuration_emptyInput() {
        assertNull(QuestConfigLoader.parseDuration(""));
    }

    @DisplayName("Given invalid format, when parseDuration is called, then IllegalArgumentException is thrown")
    @Test
    public void parseDuration_invalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> QuestConfigLoader.parseDuration("abc"));
    }

    @DisplayName("Given valid YAML with a quest, when loading, then the quest definition is returned")
    @Test
    public void loadQuestsFromDirectory_validYaml_loadsDefinition() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_config_valid");
        tempDir.toFile().deleteOnExit();
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:test_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            File yamlFile = tempDir.resolve("test_quest.yml").toFile();
            yamlFile.deleteOnExit();
            Files.writeString(yamlFile.toPath(), yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile());
            assertEquals(1, result.size());
            assertTrue(result.containsKey(NamespacedKey.fromString("mcrpg:test_quest")));
        } finally {
            deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given required-progress expression, when instantiating with tier var, then required progress resolves")
    @Test
    public void requiredProgress_expression_resolvesWithTier() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_config_expr");
        tempDir.toFile().deleteOnExit();
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:test_expr:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: \"20*(tier^2)\"\n";
            File yamlFile = tempDir.resolve("test_expr.yml").toFile();
            yamlFile.deleteOnExit();
            Files.writeString(yamlFile.toPath(), yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile());
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:test_expr"));
            assertNotNull(def);

            QuestInstance instance = new QuestInstance(def, null, Map.of("tier", 2), new ManualQuestSource(), null);
            long required = instance.getQuestStageInstances().get(0).getQuestObjectives().get(0).getRequiredProgression();
            assertEquals(80L, required);
        } finally {
            deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given blank required-progress expression, when loading, then quest is skipped and result is empty")
    @Test
    public void requiredProgress_expression_blank_failsValidation() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_config_expr_blank");
        tempDir.toFile().deleteOnExit();
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:test_expr_blank:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: \"   \"\n";
            File yamlFile = tempDir.resolve("test_expr_blank.yml").toFile();
            yamlFile.deleteOnExit();
            Files.writeString(yamlFile.toPath(), yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile());
            assertTrue(result.isEmpty(), "Quest with blank required-progress should be skipped");
        } finally {
            deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given required-progress expression that resolves <= 0, when instantiating, then it throws")
    @Test
    public void requiredProgress_expression_resolvesNonPositive_throws() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_config_expr_nonpos");
        tempDir.toFile().deleteOnExit();
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:test_expr_nonpos:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: \"-1\"\n";
            File yamlFile = tempDir.resolve("test_expr_nonpos.yml").toFile();
            yamlFile.deleteOnExit();
            Files.writeString(yamlFile.toPath(), yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile());
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:test_expr_nonpos"));
            assertNotNull(def);

            assertThrows(IllegalArgumentException.class, () -> new QuestInstance(def, null, Map.of("tier", 2), new ManualQuestSource(), null));
        } finally {
            deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given an empty directory, when loading, then empty map is returned")
    @Test
    public void loadQuestsFromDirectory_emptyDirectory_returnsEmpty() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_config_empty");
        tempDir.toFile().deleteOnExit();
        try {
            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile());
            assertTrue(result.isEmpty());
        } finally {
            deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given a non-existent directory, when loading, then empty map is returned")
    @Test
    public void loadQuestsFromDirectory_nonExistentDirectory_returnsEmpty() {
        File nonExistent = new File("nonexistent_dir_" + System.currentTimeMillis());
        Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(nonExistent);
        assertTrue(result.isEmpty());
    }

    @DisplayName("Given YAML without quests section, when loading, then empty map is returned")
    @Test
    public void loadQuestsFromDirectory_noQuestsSection_returnsEmpty() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_config_no_quests");
        tempDir.toFile().deleteOnExit();
        try {
            File yamlFile = tempDir.resolve("no_quests.yml").toFile();
            yamlFile.deleteOnExit();
            Files.writeString(yamlFile.toPath(), "other:\n  key: value\n");

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile());
            assertTrue(result.isEmpty());
        } finally {
            deleteRecursively(tempDir.toFile());
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}
