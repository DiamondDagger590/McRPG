package us.eunoians.mcrpg.configuration;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.distribution.DistributionTierConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardSplitMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.objective.type.builtin.BlockBreakObjectiveType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestConfigLoaderDistributionTest extends McRPGBaseTest {

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

    @DisplayName("Given quest YAML with reward-distribution, when loading, then distribution config is parsed")
    @Test
    public void rewardDistribution_questLevel_parsed() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_dist_test");
        tempDir.toFile().deleteOnExit();
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:dist_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    reward-distribution:\n" +
                    "      top-tier:\n" +
                    "        type: \"mcrpg:top_players\"\n" +
                    "        split-mode: INDIVIDUAL\n" +
                    "        top-player-count: 3\n" +
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
            File yamlFile = tempDir.resolve("dist_quest.yml").toFile();
            yamlFile.deleteOnExit();
            Files.writeString(yamlFile.toPath(), yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile());
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:dist_quest"));
            assertNotNull(def);

            Optional<RewardDistributionConfig> distOpt = def.getRewardDistribution();
            assertTrue(distOpt.isPresent());
            RewardDistributionConfig config = distOpt.get();
            assertEquals(1, config.getTiers().size());

            DistributionTierConfig tier = config.getTiers().get(0);
            assertEquals("top-tier", tier.getTierKey());
            assertEquals(NamespacedKey.fromString("mcrpg:top_players"), tier.getTypeKey());
            assertEquals(RewardSplitMode.INDIVIDUAL, tier.getSplitMode());
            assertEquals(Optional.of(3), tier.getTopPlayerCount());
        } finally {
            deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given quest YAML with split mode and contribution threshold, when loading, then parsed correctly")
    @Test
    public void rewardDistribution_splitModeAndThreshold_parsed() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_dist_split");
        tempDir.toFile().deleteOnExit();
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:split_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    reward-distribution:\n" +
                    "      threshold-tier:\n" +
                    "        type: \"mcrpg:contribution_threshold\"\n" +
                    "        split-mode: SPLIT_EVEN\n" +
                    "        min-contribution-percent: 10.0\n" +
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
            File yamlFile = tempDir.resolve("split_quest.yml").toFile();
            yamlFile.deleteOnExit();
            Files.writeString(yamlFile.toPath(), yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile());
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:split_quest"));
            assertNotNull(def);

            RewardDistributionConfig config = def.getRewardDistribution().orElseThrow();
            DistributionTierConfig tier = config.getTiers().get(0);
            assertEquals(RewardSplitMode.SPLIT_EVEN, tier.getSplitMode());
            assertEquals(Optional.of(10.0), tier.getMinContributionPercent());
        } finally {
            deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given quest YAML with rarity gates, when loading, then rarity keys are parsed")
    @Test
    public void rewardDistribution_rarityGates_parsed() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_dist_rarity");
        tempDir.toFile().deleteOnExit();
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:rarity_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    reward-distribution:\n" +
                    "      rare-tier:\n" +
                    "        type: \"mcrpg:participated\"\n" +
                    "        min-rarity: \"mcrpg:rare\"\n" +
                    "      epic-tier:\n" +
                    "        type: \"mcrpg:membership\"\n" +
                    "        required-rarity: \"mcrpg:epic\"\n" +
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
            File yamlFile = tempDir.resolve("rarity_quest.yml").toFile();
            yamlFile.deleteOnExit();
            Files.writeString(yamlFile.toPath(), yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile());
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:rarity_quest"));
            assertNotNull(def);

            RewardDistributionConfig config = def.getRewardDistribution().orElseThrow();
            assertEquals(2, config.getTiers().size());
        } finally {
            deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given quest YAML without reward-distribution, when loading, then distribution is empty")
    @Test
    public void rewardDistribution_absent_returnsEmpty() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_dist_absent");
        tempDir.toFile().deleteOnExit();
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:no_dist_quest:\n" +
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
            File yamlFile = tempDir.resolve("no_dist_quest.yml").toFile();
            yamlFile.deleteOnExit();
            Files.writeString(yamlFile.toPath(), yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile());
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:no_dist_quest"));
            assertNotNull(def);
            assertFalse(def.getRewardDistribution().isPresent());
        } finally {
            deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given stage-level reward-distribution, when loading, then stage def has distribution config")
    @Test
    public void rewardDistribution_stageLevel_parsed() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_dist_stage");
        tempDir.toFile().deleteOnExit();
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:stage_dist_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            reward-distribution:\n" +
                    "              all-participants:\n" +
                    "                type: \"mcrpg:participated\"\n" +
                    "                split-mode: SPLIT_PROPORTIONAL\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            File yamlFile = tempDir.resolve("stage_dist_quest.yml").toFile();
            yamlFile.deleteOnExit();
            Files.writeString(yamlFile.toPath(), yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile());
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:stage_dist_quest"));
            assertNotNull(def);
            assertFalse(def.getRewardDistribution().isPresent());

            var stageDef = def.getPhases().get(0).getStages().get(0);
            assertTrue(stageDef.getRewardDistribution().isPresent());
            DistributionTierConfig tier = stageDef.getRewardDistribution().get().getTiers().get(0);
            assertEquals(RewardSplitMode.SPLIT_PROPORTIONAL, tier.getSplitMode());
            assertEquals(NamespacedKey.fromString("mcrpg:participated"), tier.getTypeKey());
        } finally {
            deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given custom type-parameters section, when loading, then params are stored in typeParameters map")
    @Test
    public void rewardDistribution_customTypeParameters_parsed() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_dist_params");
        tempDir.toFile().deleteOnExit();
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:custom_params_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    reward-distribution:\n" +
                    "      custom-tier:\n" +
                    "        type: \"mcrpg:top_players\"\n" +
                    "        top-player-count: 5\n" +
                    "        type-parameters:\n" +
                    "          custom-flag: true\n" +
                    "          custom-value: 42\n" +
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
            File yamlFile = tempDir.resolve("custom_params_quest.yml").toFile();
            yamlFile.deleteOnExit();
            Files.writeString(yamlFile.toPath(), yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile());
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:custom_params_quest"));
            assertNotNull(def);

            DistributionTierConfig tier = def.getRewardDistribution().orElseThrow().getTiers().get(0);
            assertEquals(Optional.of(5), tier.getTopPlayerCount());
            assertEquals(Optional.of(true), tier.getTypeParameter("custom-flag", Boolean.class));
            assertEquals(Optional.of(42), tier.getTypeParameter("custom-value", Integer.class));
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
