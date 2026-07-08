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
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestRepeatMode;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.objective.type.builtin.BlockBreakObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.builtin.MobKillObjectiveType;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.quest.reward.builtin.AbilityUpgradeNextTierRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.AbilityUpgradeRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.CommandRewardType;
import us.eunoians.mcrpg.quest.reward.builtin.ExperienceRewardType;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuestManagerStartQuestVariablesTest extends McRPGBaseTest {

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

    @DisplayName("Given expression required-progress referencing tier, when starting without tier var, then quest does not start")
    @Test
    public void startQuest_expressionReferencesTier_withoutTier_failsStart() {
        QuestManager questManager = new QuestManager(mcRPG);

        QuestDefinition def = questWithSingleObjectiveExpression("expr_missing_tier", "20*(tier^2)");
        Optional<?> started = questManager.startQuest(def, UUID.randomUUID(), Map.of(), new ManualQuestSource());

        assertTrue(started.isEmpty());
    }

    @DisplayName("Given expression required-progress not referencing tier, when starting without vars, then quest starts")
    @Test
    public void startQuest_expressionNoTier_withoutVars_starts() {
        QuestManager questManager = new QuestManager(mcRPG);

        QuestDefinition def = questWithSingleObjectiveExpression("expr_no_tier", "20*(2^2)");
        Optional<?> started = questManager.startQuest(def, UUID.randomUUID(), Map.of(), new ManualQuestSource());

        assertFalse(started.isEmpty());
    }

    @DisplayName("Given expression required-progress containing frontier/tier2 tokens, when starting without tier var, then quest starts")
    @Test
    public void startQuest_expressionContainsTierSubstring_withoutTier_starts() {
        QuestManager questManager = new QuestManager(mcRPG);

        QuestDefinition frontierDef = questWithSingleObjectiveExpression("expr_frontier", "frontier^2");
        assertFalse(questManager.startQuest(frontierDef, UUID.randomUUID(), Map.of("frontier", 5), new ManualQuestSource()).isEmpty());

        QuestDefinition tier2Def = questWithSingleObjectiveExpression("expr_tier2", "tier2^2");
        assertFalse(questManager.startQuest(tier2Def, UUID.randomUUID(), Map.of("tier2", 3), new ManualQuestSource()).isEmpty());
    }

    @DisplayName("Given expression required-progress referencing unknown variable, when starting without vars, then quest does not start")
    @Test
    public void startQuest_expressionUnknownVariable_withoutVars_failsStart() {
        QuestManager questManager = new QuestManager(mcRPG);

        QuestDefinition def = questWithSingleObjectiveExpression("expr_unknown_var", "10*(foo^2)");
        Optional<?> started = questManager.startQuest(def, UUID.randomUUID(), Map.of(), new ManualQuestSource());

        assertTrue(started.isEmpty());
    }

    @DisplayName("Given scope type without _scope suffix, when starting quest, then scope provider alias resolves")
    @Test
    public void startQuest_scopeAlias_resolves() {
        QuestManager questManager = new QuestManager(mcRPG);

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("scope_alias_smoke");
        Optional<?> started = questManager.startQuest(def, UUID.randomUUID(), Map.of(), new ManualQuestSource());

        assertFalse(started.isEmpty());
    }

    @DisplayName("Given scope type with _scope suffix, when starting quest, then it starts")
    @Test
    public void startQuest_scopeWithSuffix_starts() {
        QuestManager questManager = new QuestManager(mcRPG);

        QuestDefinition def = questWithSingleObjectiveExpression("scope_with_suffix", "10");
        // same quest as helper but with explicit _scope suffix
        QuestDefinition withScopeSuffix = new QuestDefinition.Builder(
                def.getQuestKey(),
                new NamespacedKey("mcrpg", "single_player_scope"),
                def.getPhases()
        ).rewards(def.getRewards())
                .repeatMode(def.getRepeatMode())
                .repeatCooldown(def.getRepeatCooldown().orElse(null))
                .repeatLimit(def.getRepeatLimit().orElse(-1))
                .expansionKey(def.getExpansionKey().orElse(null))
                .build();

        Optional<?> started = questManager.startQuest(withScopeSuffix, UUID.randomUUID(), Map.of(), new ManualQuestSource());
        assertFalse(started.isEmpty());
    }

    @DisplayName("Given online player with PreQuestStartEvent listener that cancels, when startQuest is called, then it returns empty")
    @Test
    public void startQuest_returnsEmpty_whenPreQuestStartEventCancelled() {
        QuestManager questManager = new QuestManager(mcRPG);
        var playerMock = server.addPlayer();

        server.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onPreStart(us.eunoians.mcrpg.event.quest.PreQuestStartEvent event) {
                event.setCancelled(true);
            }
        }, mcRPG);

        QuestDefinition def = questWithSingleObjectiveExpression("pre_start_cancel", "10");
        Optional<?> started = questManager.startQuest(def, playerMock.getUniqueId(), Map.of(), new ManualQuestSource());

        assertTrue(started.isEmpty());
    }

    private static QuestDefinition questWithSingleObjectiveExpression(String questKey, String requiredProgressExpr) {
        NamespacedKey objectiveKey = new NamespacedKey("mcrpg", questKey + "_obj");
        var objectiveType = QuestTestHelper.mockObjectiveType(questKey + "_type");
        QuestObjectiveDefinition objective = new QuestObjectiveDefinition(objectiveKey, objectiveType, requiredProgressExpr, List.of(), null);

        QuestStageDefinition stage = new QuestStageDefinition(
                new NamespacedKey("mcrpg", questKey + "_stage"),
                List.of(objective),
                List.of(),
                null
        );
        QuestPhaseDefinition phase = new QuestPhaseDefinition(0, PhaseCompletionMode.ALL, List.of(stage), List.of(), null);

        return new QuestDefinition.Builder(
                new NamespacedKey("mcrpg", questKey),
                new NamespacedKey("mcrpg", "single_player"),
                List.of(phase)
        ).build();
    }
}

