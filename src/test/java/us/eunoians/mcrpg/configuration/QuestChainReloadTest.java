package us.eunoians.mcrpg.configuration;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.TestFileUtils;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainRegistry;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.objective.type.builtin.BlockBreakObjectiveType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the two-phase reload behaviour in {@link QuestChainConfigLoader#loadChainsFromPaths}
 * and the {@link QuestConfigLoader} chain-file flagging integration.
 */
public class QuestChainReloadTest extends McRPGBaseTest {

    private static final String CHAIN_YAML = """
            quest-chain-file: true
            chains:
              mcrpg:reload_chain:
                display-name: "Reload Chain"
                source: mcrpg:manual
                auto-start:
                  trigger: mcrpg:manual
                repeat-mode: once
                steps:
                  step_one:
                    quest: mcrpg:example_quest
            """;

    private static final String QUEST_YAML =
            "quests:\n" +
            "  mcrpg:example_quest:\n" +
            "    scope: \"mcrpg:single_player\"\n" +
            "    phases:\n" +
            "      phase_one:\n" +
            "        completion-mode: ALL\n" +
            "        stages:\n" +
            "          stage_one:\n" +
            "            key: \"mcrpg:stage_one\"\n" +
            "            objectives:\n" +
            "              obj_one:\n" +
            "                key: \"mcrpg:obj_one\"\n" +
            "                type: \"mcrpg:block_break\"\n" +
            "                required-progress: 5\n";

    private QuestChainConfigLoader chainLoader;
    private QuestConfigLoader questLoader;
    private QuestChainRegistry chainRegistry;

    @BeforeEach
    void setup() {
        chainLoader = new QuestChainConfigLoader();
        questLoader = new QuestConfigLoader(new us.eunoians.mcrpg.quest.board.template.condition.ConditionParser(
                McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.TEMPLATE_CONDITION)
        ));
        chainRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.QUEST_CHAIN);
        chainRegistry.clear();

        QuestObjectiveTypeRegistry objReg = McRPG.getInstance().registryAccess()
                .registry(McRPGRegistryKey.QUEST_OBJECTIVE_TYPE);
        if (objReg.get(BlockBreakObjectiveType.KEY).isEmpty()) {
            objReg.register(new BlockBreakObjectiveType());
        }
    }

    @AfterEach
    void tearDown() {
        chainRegistry.clear();
    }

    @Test
    @DisplayName("Given a chain file, when QuestConfigLoader scans the directory, then chain file path is returned in QuestLoadResult")
    void loadQuestsFromDirectory_chainFile_flaggedInResult() throws IOException {
        Path tempDir = Files.createTempDirectory("reload_chain_flag");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "example_chain.yml", CHAIN_YAML);

            QuestLoadResult result = questLoader.loadQuestsFromDirectory(tempDir.toFile());

            assertTrue(result.definitions().isEmpty(), "Chain file should not contribute quest definitions");
            assertEquals(1, result.chainFiles().size(), "Chain file path should be collected");
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given a non-chain file, when QuestConfigLoader scans the directory, then it is not flagged as a chain file")
    void loadQuestsFromDirectory_questFile_notFlaggedAsChain() throws IOException {
        Path tempDir = Files.createTempDirectory("reload_quest_flag");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "example_quest.yml", QUEST_YAML);

            QuestLoadResult result = questLoader.loadQuestsFromDirectory(tempDir.toFile());

            assertTrue(result.chainFiles().isEmpty(), "Quest file should not be collected as a chain file");
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given both a chain file and a quest file in the same directory, when scanned, each is routed to the correct result slot")
    void loadQuestsFromDirectory_mixedFiles_routedCorrectly() throws IOException {
        Path tempDir = Files.createTempDirectory("reload_mixed");
        tempDir.toFile().deleteOnExit();
        try {
            TestFileUtils.writeFile(tempDir, "example_quest.yml", QUEST_YAML);
            TestFileUtils.writeFile(tempDir, "example_chain.yml", CHAIN_YAML);

            QuestLoadResult result = questLoader.loadQuestsFromDirectory(tempDir.toFile());

            assertFalse(result.definitions().isEmpty(), "Quest definitions should be loaded");
            assertEquals(1, result.chainFiles().size(), "One chain file should be collected");
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given flagged chain file paths, when loadChainsFromPaths is called, then chain definitions are parsed")
    void loadChainsFromPaths_validPaths_loadsChains() throws IOException {
        Path tempDir = Files.createTempDirectory("reload_chain_paths");
        tempDir.toFile().deleteOnExit();
        try {
            Path chainFile = TestFileUtils.writeFile(tempDir, "example_chain.yml", CHAIN_YAML);

            Map<NamespacedKey, QuestChainDefinition> chains = chainLoader.loadChainsFromPaths(List.of(chainFile));

            assertEquals(1, chains.size());
            assertTrue(chains.containsKey(new NamespacedKey("mcrpg", "reload_chain")));
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @Test
    @DisplayName("Given an empty path list, when loadChainsFromPaths is called, then no chains are returned")
    void loadChainsFromPaths_emptyList_returnsEmpty() {
        Map<NamespacedKey, QuestChainDefinition> chains = chainLoader.loadChainsFromPaths(List.of());
        assertTrue(chains.isEmpty());
    }

    @Test
    @DisplayName("Given two chain files sharing a chain key, when loadChainsFromPaths is called, then only the first is loaded")
    void loadChainsFromPaths_duplicateKey_firstWins() throws IOException {
        Path tempDir = Files.createTempDirectory("reload_chain_dup");
        tempDir.toFile().deleteOnExit();
        try {
            Path first = TestFileUtils.writeFile(tempDir, "a_chain.yml", CHAIN_YAML);
            Path second = TestFileUtils.writeFile(tempDir, "b_chain.yml", CHAIN_YAML);

            Map<NamespacedKey, QuestChainDefinition> chains = chainLoader.loadChainsFromPaths(List.of(first, second));

            assertEquals(1, chains.size(), "Duplicate chain key should be skipped");
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

}
