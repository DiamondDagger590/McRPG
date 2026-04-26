package us.eunoians.mcrpg.quest;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.StubConfigurableTierableAbility;
import us.eunoians.mcrpg.ability.StubTierableAbility;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.objective.type.builtin.BlockBreakObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.builtin.MobKillObjectiveType;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.quest.reward.builtin.AbilityUpgradeNextTierRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.AbilityUpgradeRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.CommandRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.ExperienceRewardType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuestManagerUpgradeQuestEnforcementTest extends McRPGBaseTest {

    @BeforeEach
    public void setup() {
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mock(ReloadableContentManager.class));
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(any(FileType.class))).thenReturn(mock(YamlDocument.class));
    }

    @Test
    public void givenMissingUpgradeQuestDefinition_whenQuestManagerLoads_thenTierableAbilityIsUnregistered() {
        // Ensure required registries exist for quest loading
        RegistryAccess registryAccess = RegistryAccess.registryAccess();

        // Ability registry is not registered by TestBootstrap; register it for this test.
        AbilityRegistry abilityRegistry = registryAccess.registry(McRPGRegistryKey.ABILITY);
        if (abilityRegistry == null) {
            registryAccess.register(new AbilityRegistry(mcRPG));
            abilityRegistry = registryAccess.registry(McRPGRegistryKey.ABILITY);
        }

        // Objective/reward types are needed to parse default quest resources during QuestManager construction.
        QuestObjectiveTypeRegistry objectiveTypes = registryAccess.registry(McRPGRegistryKey.QUEST_OBJECTIVE_TYPE);
        if (objectiveTypes.get(BlockBreakObjectiveType.KEY).isEmpty()) {
            objectiveTypes.register(new BlockBreakObjectiveType());
        }
        if (objectiveTypes.get(MobKillObjectiveType.KEY).isEmpty()) {
            objectiveTypes.register(new MobKillObjectiveType());
        }

        QuestRewardTypeRegistry rewardTypes = registryAccess.registry(McRPGRegistryKey.QUEST_REWARD_TYPE);
        if (rewardTypes.get(ExperienceRewardType.KEY).isEmpty()) {
            rewardTypes.register(new ExperienceRewardType());
        }
        if (rewardTypes.get(CommandRewardType.KEY).isEmpty()) {
            rewardTypes.register(new CommandRewardType());
        }
        if (rewardTypes.get(AbilityUpgradeRewardType.KEY).isEmpty()) {
            rewardTypes.register(new AbilityUpgradeRewardType());
        }
        if (rewardTypes.get(AbilityUpgradeNextTierRewardType.KEY).isEmpty()) {
            rewardTypes.register(new AbilityUpgradeNextTierRewardType());
        }

        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:missing_upgrade_def");
        abilityRegistry.register(new StubTierableAbility(mcRPG, abilityKey)
                .withUpgradeQuestKey(NamespacedKey.fromString("mcrpg:this_quest_does_not_exist")));

        QuestManager questManager = new QuestManager(mcRPG);
        questManager.loadQuestDefinitions();

        assertFalse(abilityRegistry.registered(abilityKey));
    }

    @Test
    public void givenInferredUpgradeQuestDefinitionExists_whenQuestManagerLoads_thenTierableAbilityRemainsRegistered() throws Exception {
        // Ensure required registries exist for quest loading
        RegistryAccess registryAccess = RegistryAccess.registryAccess();

        AbilityRegistry abilityRegistry = registryAccess.registry(McRPGRegistryKey.ABILITY);
        if (abilityRegistry == null) {
            registryAccess.register(new AbilityRegistry(mcRPG));
            abilityRegistry = registryAccess.registry(McRPGRegistryKey.ABILITY);
        }

        QuestObjectiveTypeRegistry objectiveTypes = registryAccess.registry(McRPGRegistryKey.QUEST_OBJECTIVE_TYPE);
        if (objectiveTypes.get(BlockBreakObjectiveType.KEY).isEmpty()) {
            objectiveTypes.register(new BlockBreakObjectiveType());
        }
        if (objectiveTypes.get(MobKillObjectiveType.KEY).isEmpty()) {
            objectiveTypes.register(new MobKillObjectiveType());
        }

        QuestRewardTypeRegistry rewardTypes = registryAccess.registry(McRPGRegistryKey.QUEST_REWARD_TYPE);
        if (rewardTypes.get(ExperienceRewardType.KEY).isEmpty()) {
            rewardTypes.register(new ExperienceRewardType());
        }
        if (rewardTypes.get(CommandRewardType.KEY).isEmpty()) {
            rewardTypes.register(new CommandRewardType());
        }
        if (rewardTypes.get(AbilityUpgradeRewardType.KEY).isEmpty()) {
            rewardTypes.register(new AbilityUpgradeRewardType());
        }
        if (rewardTypes.get(AbilityUpgradeNextTierRewardType.KEY).isEmpty()) {
            rewardTypes.register(new AbilityUpgradeNextTierRewardType());
        }

        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:inferred_pass");
        YamlDocument inferDoc = Mockito.mock(YamlDocument.class);
        Mockito.when(inferDoc.contains(any(Route.class))).thenReturn(false);
        abilityRegistry.register(new StubConfigurableTierableAbility(mcRPG, abilityKey, inferDoc));

        File questsDir = new File(mcRPG.getDataFolder(), "quests");
        File upgradesDir = new File(questsDir, "upgrades");
        upgradesDir.mkdirs();

        File inferredQuestFile = new File(questsDir, "inferred_pass_upgrade.yml");
        inferredQuestFile.deleteOnExit();
        String yaml = "quests:\n" +
                "  mcrpg:inferred_pass_upgrade:\n" +
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
                "                required-progress: 1\n";
        Files.writeString(inferredQuestFile.toPath(), yaml);

        QuestManager questManager = new QuestManager(mcRPG);
        questManager.loadQuestDefinitions();

        assertTrue(abilityRegistry.registered(abilityKey));
    }

}

