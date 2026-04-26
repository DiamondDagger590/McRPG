package us.eunoians.mcrpg.quest;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.StubConfigurableTierableAbility;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.objective.type.builtin.BlockBreakObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.builtin.MobKillObjectiveType;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.quest.reward.builtin.AbilityUpgradeNextTierRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.AbilityUpgradeRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.CommandRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.ExperienceRewardType;
import us.eunoians.mcrpg.quest.source.builtin.AbilityUpgradeQuestSource;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UpgradeQuestOverrideWinsStartTest extends McRPGBaseTest {

    @BeforeEach
    public void setup() {
        RegistryAccess registryAccess = RegistryAccess.registryAccess();
        registryAccess.registry(RegistryKey.MANAGER).register(mock(ReloadableContentManager.class));
        FileManager fileManager = registryAccess.registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(any(FileType.class))).thenReturn(mock(YamlDocument.class));

        AbilityRegistry abilityRegistry = registryAccess.registry(McRPGRegistryKey.ABILITY);
        if (abilityRegistry == null) {
            registryAccess.register(new AbilityRegistry(mcRPG));
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
    }

    @DisplayName("Given both all-tiers and tier-specific upgrade quests, when starting upgrade quest, then tier-specific override is used")
    @Test
    public void overrideWins_whenBothConfigured_startsOverrideQuest() throws Exception {
        Path tmp = Files.createTempFile("override_wins", ".yml");
        tmp.toFile().deleteOnExit();
        String yaml =
                "ability:\n" +
                        "  tier-configuration:\n" +
                        "    all-tiers:\n" +
                        "      upgrade-quest: \"mcrpg:all_tiers_upgrade\"\n" +
                        "    tier-2:\n" +
                        "      upgrade-quest: \"mcrpg:specific_tier2\"\n";
        Files.writeString(tmp, yaml);
        YamlDocument doc = YamlDocument.create(tmp.toFile());

        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:dummy_override_e2e");
        StubConfigurableTierableAbility ability = new StubConfigurableTierableAbility(mcRPG, abilityKey, doc);

        QuestManager questManager = new QuestManager(mcRPG);

        QuestDefinition allTiersDef = QuestTestHelper.singlePhaseQuest("all_tiers_upgrade");
        QuestDefinition tier2Def = QuestTestHelper.singlePhaseQuest("specific_tier2");

        Optional<NamespacedKey> resolvedKey = ability.getUpgradeQuestKey(2);
        assertTrue(resolvedKey.isPresent());
        assertEquals(NamespacedKey.fromString("mcrpg:specific_tier2"), resolvedKey.get());

        Map<NamespacedKey, QuestDefinition> defs = Map.of(
                allTiersDef.getQuestKey(), allTiersDef,
                tier2Def.getQuestKey(), tier2Def
        );
        QuestDefinition chosen = defs.get(resolvedKey.get());
        assertNotNull(chosen);

        var started = questManager.startQuest(chosen, UUID.randomUUID(), Map.of("tier", 2), new AbilityUpgradeQuestSource());
        assertTrue(started.isPresent());
        assertEquals(NamespacedKey.fromString("mcrpg:specific_tier2"), started.get().getQuestKey());
    }

    @DisplayName("Given tier-specific upgrade quest configured but missing definition, when resolving upgrade quest, then all-tiers fallback is used")
    @Test
    public void fallbackToAllTiers_whenOverrideMissing_usesAllTiersQuest() throws Exception {
        Path tmp = Files.createTempFile("override_missing", ".yml");
        tmp.toFile().deleteOnExit();
        String yaml =
                "ability:\n" +
                        "  tier-configuration:\n" +
                        "    all-tiers:\n" +
                        "      upgrade-quest: \"mcrpg:all_tiers_upgrade\"\n" +
                        "    tier-2:\n" +
                        "      upgrade-quest: \"mcrpg:specific_tier2\"\n";
        Files.writeString(tmp, yaml);
        YamlDocument doc = YamlDocument.create(tmp.toFile());

        NamespacedKey abilityKey = NamespacedKey.fromString("mcrpg:dummy_override_missing");
        StubConfigurableTierableAbility ability = new StubConfigurableTierableAbility(mcRPG, abilityKey, doc);

        QuestManager questManager = new QuestManager(mcRPG);
        QuestDefinition allTiersDef = QuestTestHelper.singlePhaseQuest("all_tiers_upgrade");
        questManager.getQuestDefinitionRegistry().register(allTiersDef);

        Optional<QuestDefinition> resolved = questManager.resolveUpgradeQuestDefinition(ability, 2);
        assertTrue(resolved.isPresent());
        assertEquals(allTiersDef.getQuestKey(), resolved.get().getQuestKey());
    }

}

