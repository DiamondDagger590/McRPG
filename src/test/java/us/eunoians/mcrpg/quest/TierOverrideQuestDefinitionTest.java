package us.eunoians.mcrpg.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.QuestConfigLoader;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestRepeatMode;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.objective.type.builtin.BlockBreakObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.builtin.MobKillObjectiveType;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.quest.reward.builtin.AbilityUpgradeRewardType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TierOverrideQuestDefinitionTest extends McRPGBaseTest {

    private QuestConfigLoader loader;

    @BeforeEach
    public void setup() {
        loader = new QuestConfigLoader();

        QuestObjectiveTypeRegistry objReg = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_OBJECTIVE_TYPE);
        if (objReg.get(BlockBreakObjectiveType.KEY).isEmpty()) {
            objReg.register(new BlockBreakObjectiveType());
        }
        if (objReg.get(MobKillObjectiveType.KEY).isEmpty()) {
            objReg.register(new MobKillObjectiveType());
        }

        QuestRewardTypeRegistry rewardReg = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_REWARD_TYPE);
        if (rewardReg.get(AbilityUpgradeRewardType.KEY).isEmpty()) {
            rewardReg.register(new AbilityUpgradeRewardType());
        }
    }

    @DisplayName("Given tier override quest YAML, when loaded, then rewards use fixed-tier ability_upgrade")
    @Test
    public void tierOverrideQuests_useFixedTierReward() throws IOException {
        String resourcePath = "quests/upgrades/tier_override_ability_upgrades.yml";
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertNotNull(in, "Missing classpath resource: " + resourcePath);
            String yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);

            Path tempDir = Files.createTempDirectory("tier_override_test");
            tempDir.toFile().deleteOnExit();
            try {
                File yamlFile = tempDir.resolve("tier_override.yml").toFile();
                yamlFile.deleteOnExit();
                Files.writeString(yamlFile.toPath(), yaml, StandardCharsets.UTF_8);

                Map<NamespacedKey, QuestDefinition> defs = loader.loadQuestsFromDirectory(tempDir.toFile());

                QuestDefinition def = defs.get(NamespacedKey.fromString("mcrpg:enhanced_bleed_tier3"));
                assertNotNull(def);
                assertEquals(QuestRepeatMode.ONCE, def.getRepeatMode());

                assertTrue(def.getRewards().size() >= 1);
                QuestRewardType reward = def.getRewards().get(0);
                assertTrue(reward instanceof AbilityUpgradeRewardType);

                Map<String, Object> cfg = reward.serializeConfig();
                assertEquals("mcrpg:enhanced_bleed", cfg.get("ability"));
                assertEquals(3, ((Number) cfg.get("tier")).intValue());
            } finally {
                deleteRecursively(tempDir.toFile());
            }
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

