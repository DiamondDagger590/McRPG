package us.eunoians.mcrpg.configuration;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.TestFileUtils;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainRepeatMode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link QuestChainConfigLoader}.
 */
public class QuestChainConfigLoaderTest extends McRPGBaseTest {

    private QuestChainConfigLoader loader;

    @BeforeEach
    void setup() {
        loader = new QuestChainConfigLoader();
    }

    @Test
    @DisplayName("Given a valid chain YAML, When loaded, Then chain definition is parsed correctly")
    void loadChains_validYaml_parsesDefinition() throws IOException {
        Path tempDir = Files.createTempDirectory("chain_config_valid");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "chains.yml",
                    "quest-chain-file: true\n" +
                    "chains:\n" +
                    "  mcrpg:test_chain:\n" +
                    "    display-name: \"Test Chain\"\n" +
                    "    source: mcrpg:manual\n" +
                    "    auto-start:\n" +
                    "      trigger: mcrpg:manual\n" +
                    "    repeat-mode: once\n" +
                    "    steps:\n" +
                    "      step_one:\n" +
                    "        quest: mcrpg:example_quest\n");

            Map<NamespacedKey, QuestChainDefinition> result = loader.loadChainsFromDirectory(tempDir.toFile());

            assertEquals(1, result.size());
            NamespacedKey chainKey = new NamespacedKey("mcrpg", "test_chain");
            assertTrue(result.containsKey(chainKey));
            QuestChainDefinition def = result.get(chainKey);
            assertEquals("Test Chain", def.getDisplayName());
            assertEquals(QuestChainRepeatMode.ONCE, def.getRepeatMode());
            assertEquals(1, def.getSteps().size());
            assertEquals(new NamespacedKey("mcrpg", "example_quest"), def.getSteps().get(0).questKey());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given a file without quest-chain-file marker, When loaded, Then file is silently skipped")
    void loadChains_missingMarker_fileSkipped() throws IOException {
        Path tempDir = Files.createTempDirectory("chain_config_no_marker");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "quests.yml",
                    "chains:\n" +
                    "  mcrpg:test_chain:\n" +
                    "    source: mcrpg:manual\n" +
                    "    auto-start:\n" +
                    "      trigger: mcrpg:manual\n" +
                    "    steps:\n" +
                    "      step_one:\n" +
                    "        quest: mcrpg:example_quest\n");

            Map<NamespacedKey, QuestChainDefinition> result = loader.loadChainsFromDirectory(tempDir.toFile());
            assertTrue(result.isEmpty());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given chain file with no chains section, When loaded, Then file is skipped with warning")
    void loadChains_missingChainsSection_fileSkipped() throws IOException {
        Path tempDir = Files.createTempDirectory("chain_config_no_chains");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "chains.yml", "quest-chain-file: true\n");

            Map<NamespacedKey, QuestChainDefinition> result = loader.loadChainsFromDirectory(tempDir.toFile());
            assertTrue(result.isEmpty());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given a chain missing required source field, When loaded, Then entry is skipped")
    void loadChains_missingSource_chainSkipped() throws IOException {
        Path tempDir = Files.createTempDirectory("chain_config_no_source");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "chains.yml",
                    "quest-chain-file: true\n" +
                    "chains:\n" +
                    "  mcrpg:test_chain:\n" +
                    "    auto-start:\n" +
                    "      trigger: mcrpg:manual\n" +
                    "    steps:\n" +
                    "      step_one:\n" +
                    "        quest: mcrpg:example_quest\n");

            Map<NamespacedKey, QuestChainDefinition> result = loader.loadChainsFromDirectory(tempDir.toFile());
            assertTrue(result.isEmpty());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given a chain missing required auto-start.trigger field, When loaded, Then entry is skipped")
    void loadChains_missingTrigger_chainSkipped() throws IOException {
        Path tempDir = Files.createTempDirectory("chain_config_no_trigger");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "chains.yml",
                    "quest-chain-file: true\n" +
                    "chains:\n" +
                    "  mcrpg:test_chain:\n" +
                    "    source: mcrpg:manual\n" +
                    "    steps:\n" +
                    "      step_one:\n" +
                    "        quest: mcrpg:example_quest\n");

            Map<NamespacedKey, QuestChainDefinition> result = loader.loadChainsFromDirectory(tempDir.toFile());
            assertTrue(result.isEmpty());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given a chain missing required steps section, When loaded, Then entry is skipped")
    void loadChains_missingSteps_chainSkipped() throws IOException {
        Path tempDir = Files.createTempDirectory("chain_config_no_steps");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "chains.yml",
                    "quest-chain-file: true\n" +
                    "chains:\n" +
                    "  mcrpg:test_chain:\n" +
                    "    source: mcrpg:manual\n" +
                    "    auto-start:\n" +
                    "      trigger: mcrpg:manual\n");

            Map<NamespacedKey, QuestChainDefinition> result = loader.loadChainsFromDirectory(tempDir.toFile());
            assertTrue(result.isEmpty());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given omitted repeat-mode, When loaded, Then repeat-mode defaults to ONCE")
    void loadChains_omittedRepeatMode_defaultsToOnce() throws IOException {
        Path tempDir = Files.createTempDirectory("chain_config_default_repeat");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "chains.yml",
                    "quest-chain-file: true\n" +
                    "chains:\n" +
                    "  mcrpg:test_chain:\n" +
                    "    source: mcrpg:manual\n" +
                    "    auto-start:\n" +
                    "      trigger: mcrpg:manual\n" +
                    "    steps:\n" +
                    "      step_one:\n" +
                    "        quest: mcrpg:example_quest\n");

            Map<NamespacedKey, QuestChainDefinition> result = loader.loadChainsFromDirectory(tempDir.toFile());
            assertEquals(1, result.size());
            assertEquals(QuestChainRepeatMode.ONCE, result.values().iterator().next().getRepeatMode());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given repeat-mode: cooldown-limited, When loaded, Then repeat-mode parses correctly")
    void loadChains_cooldownLimitedRepeatMode_parsesCorrectly() throws IOException {
        Path tempDir = Files.createTempDirectory("chain_config_cooldown_limited");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "chains.yml",
                    "quest-chain-file: true\n" +
                    "chains:\n" +
                    "  mcrpg:test_chain:\n" +
                    "    source: mcrpg:manual\n" +
                    "    auto-start:\n" +
                    "      trigger: mcrpg:manual\n" +
                    "    repeat-mode: cooldown-limited\n" +
                    "    steps:\n" +
                    "      step_one:\n" +
                    "        quest: mcrpg:example_quest\n");

            Map<NamespacedKey, QuestChainDefinition> result = loader.loadChainsFromDirectory(tempDir.toFile());
            assertEquals(1, result.size());
            assertEquals(QuestChainRepeatMode.COOLDOWN_LIMITED, result.values().iterator().next().getRepeatMode());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given invalid repeat-mode value, When loaded, Then defaults to ONCE with warning")
    void loadChains_invalidRepeatMode_defaultsToOnce() throws IOException {
        Path tempDir = Files.createTempDirectory("chain_config_invalid_repeat");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "chains.yml",
                    "quest-chain-file: true\n" +
                    "chains:\n" +
                    "  mcrpg:test_chain:\n" +
                    "    source: mcrpg:manual\n" +
                    "    auto-start:\n" +
                    "      trigger: mcrpg:manual\n" +
                    "    repeat-mode: invalid_mode\n" +
                    "    steps:\n" +
                    "      step_one:\n" +
                    "        quest: mcrpg:example_quest\n");

            Map<NamespacedKey, QuestChainDefinition> result = loader.loadChainsFromDirectory(tempDir.toFile());
            assertEquals(1, result.size());
            assertEquals(QuestChainRepeatMode.ONCE, result.values().iterator().next().getRepeatMode());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given absent display-name, When loaded, Then display name falls back to key value portion")
    void loadChains_missingDisplayName_fallsBackToKeyValue() throws IOException {
        Path tempDir = Files.createTempDirectory("chain_config_no_display");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "chains.yml",
                    "quest-chain-file: true\n" +
                    "chains:\n" +
                    "  mcrpg:tutorial_chain:\n" +
                    "    source: mcrpg:manual\n" +
                    "    auto-start:\n" +
                    "      trigger: mcrpg:manual\n" +
                    "    steps:\n" +
                    "      step_one:\n" +
                    "        quest: mcrpg:example_quest\n");

            Map<NamespacedKey, QuestChainDefinition> result = loader.loadChainsFromDirectory(tempDir.toFile());
            assertEquals(1, result.size());
            assertEquals("tutorial_chain", result.values().iterator().next().getDisplayName());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given duplicate chain keys across two files, When loaded, Then only the first-loaded wins")
    void loadChains_duplicateChainKeyAcrossFiles_firstLoaded() throws IOException {
        Path tempDir = Files.createTempDirectory("chain_config_duplicate");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "a_chains.yml",
                    "quest-chain-file: true\n" +
                    "chains:\n" +
                    "  mcrpg:test_chain:\n" +
                    "    display-name: \"First\"\n" +
                    "    source: mcrpg:manual\n" +
                    "    auto-start:\n" +
                    "      trigger: mcrpg:manual\n" +
                    "    steps:\n" +
                    "      step_one:\n" +
                    "        quest: mcrpg:example_quest\n");
            TestFileUtils.writeFile(tempDir, "b_chains.yml",
                    "quest-chain-file: true\n" +
                    "chains:\n" +
                    "  mcrpg:test_chain:\n" +
                    "    display-name: \"Second\"\n" +
                    "    source: mcrpg:manual\n" +
                    "    auto-start:\n" +
                    "      trigger: mcrpg:manual\n" +
                    "    steps:\n" +
                    "      step_one:\n" +
                    "        quest: mcrpg:example_quest\n");

            Map<NamespacedKey, QuestChainDefinition> result = loader.loadChainsFromDirectory(tempDir.toFile());
            assertEquals(1, result.size());
            assertEquals("First", result.values().iterator().next().getDisplayName());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given invalid NamespacedKey under chains section, When loaded, Then entry is skipped with warning")
    void loadChains_invalidChainKey_entrySkipped() throws IOException {
        Path tempDir = Files.createTempDirectory("chain_config_invalid_key");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "chains.yml",
                    "quest-chain-file: true\n" +
                    "chains:\n" +
                    "  not_a_namespaced_key:\n" +
                    "    source: mcrpg:manual\n" +
                    "    auto-start:\n" +
                    "      trigger: mcrpg:manual\n" +
                    "    steps:\n" +
                    "      step_one:\n" +
                    "        quest: mcrpg:example_quest\n");

            Map<NamespacedKey, QuestChainDefinition> result = loader.loadChainsFromDirectory(tempDir.toFile());
            assertTrue(result.isEmpty());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given a chain with explicit on-quest-expire on a step, When loaded, Then step carries that value")
    void loadChains_explicitOnQuestExpire_stepCarriesValue() throws IOException {
        Path tempDir = Files.createTempDirectory("chain_config_expire");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "chains.yml",
                    "quest-chain-file: true\n" +
                    "chains:\n" +
                    "  mcrpg:test_chain:\n" +
                    "    source: mcrpg:manual\n" +
                    "    auto-start:\n" +
                    "      trigger: mcrpg:manual\n" +
                    "    steps:\n" +
                    "      step_one:\n" +
                    "        quest: mcrpg:example_quest\n" +
                    "        on-quest-expire: fail-chain\n");

            Map<NamespacedKey, QuestChainDefinition> result = loader.loadChainsFromDirectory(tempDir.toFile());
            assertEquals(1, result.size());
            assertEquals("fail-chain", result.values().iterator().next().getSteps().get(0).onQuestExpire());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given a chain step missing quest field, When loaded, Then that step is skipped and chain is invalid")
    void loadChains_stepMissingQuestField_chainSkipped() throws IOException {
        Path tempDir = Files.createTempDirectory("chain_config_step_no_quest");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "chains.yml",
                    "quest-chain-file: true\n" +
                    "chains:\n" +
                    "  mcrpg:test_chain:\n" +
                    "    source: mcrpg:manual\n" +
                    "    auto-start:\n" +
                    "      trigger: mcrpg:manual\n" +
                    "    steps:\n" +
                    "      step_one:\n" +
                    "        on-quest-expire: fail-chain\n");

            // All steps skipped → chain has no valid steps → chain skipped entirely
            Map<NamespacedKey, QuestChainDefinition> result = loader.loadChainsFromDirectory(tempDir.toFile());
            assertTrue(result.isEmpty());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given a directory that does not exist, When loaded, Then empty map is returned")
    void loadChains_nonExistentDirectory_returnsEmpty() throws IOException {
        Path tempDir = Files.createTempDirectory("chain_config_nonexistent_parent");
        tempDir.toFile().deleteOnExit();
        try {
            File nonExistent = new File(tempDir.toFile(), "nonexistent");
            Map<NamespacedKey, QuestChainDefinition> result = loader.loadChainsFromDirectory(nonExistent);
            assertTrue(result.isEmpty());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given two valid chains across two files, When loaded, Then both are returned")
    void loadChains_twoFilesWithDistinctChains_bothLoaded() throws IOException {
        Path tempDir = Files.createTempDirectory("chain_config_two_files");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "a.yml",
                    "quest-chain-file: true\n" +
                    "chains:\n" +
                    "  mcrpg:chain_a:\n" +
                    "    source: mcrpg:manual\n" +
                    "    auto-start:\n" +
                    "      trigger: mcrpg:manual\n" +
                    "    steps:\n" +
                    "      step_one:\n" +
                    "        quest: mcrpg:example_quest\n");
            TestFileUtils.writeFile(tempDir, "b.yml",
                    "quest-chain-file: true\n" +
                    "chains:\n" +
                    "  mcrpg:chain_b:\n" +
                    "    source: mcrpg:manual\n" +
                    "    auto-start:\n" +
                    "      trigger: mcrpg:first_join\n" +
                    "    steps:\n" +
                    "      step_one:\n" +
                    "        quest: mcrpg:example_quest\n");

            Map<NamespacedKey, QuestChainDefinition> result = loader.loadChainsFromDirectory(tempDir.toFile());
            assertEquals(2, result.size());
            assertTrue(result.containsKey(new NamespacedKey("mcrpg", "chain_a")));
            assertTrue(result.containsKey(new NamespacedKey("mcrpg", "chain_b")));
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }
}
