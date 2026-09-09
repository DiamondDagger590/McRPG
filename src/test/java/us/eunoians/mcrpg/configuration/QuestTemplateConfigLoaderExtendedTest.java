package us.eunoians.mcrpg.configuration;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.eunoians.mcrpg.quest.board.template.ObjectiveSelectionConfig;
import us.eunoians.mcrpg.quest.board.template.QuestTemplate;
import us.eunoians.mcrpg.quest.board.template.TemplateRewardDefinition;
import us.eunoians.mcrpg.quest.board.template.TemplateStageDefinition;
import us.eunoians.mcrpg.TestFileUtils;
import us.eunoians.mcrpg.quest.board.template.condition.ConditionParser;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateConditionRegistry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestTemplateConfigLoaderExtendedTest {

    private QuestTemplateConfigLoader loader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        loader = new QuestTemplateConfigLoader(Logger.getLogger("TestLoader"), new ConditionParser(new TemplateConditionRegistry()));
    }

    @DisplayName("Given objective-selection config with WEIGHTED_RANDOM mode, when loading, then ObjectiveSelectionConfig is parsed")
    @Test
    void loadTemplates_parsesObjectiveSelection_whenWeightedRandomMode() throws IOException {
        TestFileUtils.writeFile(tempDir,"weighted.yml", """
                quest-templates:
                  mcrpg:weighted_template:
                    display-name-route: "quests.templates.weighted.display-name"
                    board-eligible: true
                    scope: mcrpg:single_player
                    supported-rarities: [COMMON]
                    phases:
                      phase:
                        completion-mode: ALL
                        stages:
                          stage:
                            objective-selection:
                              mode: WEIGHTED_RANDOM
                              min-count: 2
                              max-count: 4
                            objectives:
                              break-stone:
                                type: mcrpg:block_break
                                required-progress: 10
                                weight: 5
                              break-dirt:
                                type: mcrpg:block_break
                                required-progress: 20
                                weight: 3
                              break-iron:
                                type: mcrpg:block_break
                                required-progress: 5
                                weight: 2
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        QuestTemplate template = result.get(NamespacedKey.fromString("mcrpg:weighted_template"));
        assertNotNull(template);

        TemplateStageDefinition stage = template.getPhases().get(0).stages().get(0);
        Optional<ObjectiveSelectionConfig> selOpt = stage.getObjectiveSelection();
        assertTrue(selOpt.isPresent());
        ObjectiveSelectionConfig sel = selOpt.get();
        assertEquals(ObjectiveSelectionConfig.ObjectiveSelectionMode.WEIGHTED_RANDOM, sel.mode());
        assertEquals(2, sel.minCount());
        assertEquals(4, sel.maxCount());
    }

    @DisplayName("Given stage without objective-selection, when loading, then ObjectiveSelectionConfig is absent")
    @Test
    void loadTemplates_returnsEmptySelection_whenNoObjectiveSelectionSection() throws IOException {
        TestFileUtils.writeFile(tempDir,"no_sel.yml", """
                quest-templates:
                  mcrpg:no_sel_template:
                    display-name-route: "quests.templates.no-sel.display-name"
                    board-eligible: true
                    scope: mcrpg:single_player
                    supported-rarities: [COMMON]
                    phases:
                      phase:
                        completion-mode: ALL
                        stages:
                          stage:
                            objectives:
                              break-blocks:
                                type: mcrpg:block_break
                                required-progress: 10
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        QuestTemplate template = result.get(NamespacedKey.fromString("mcrpg:no_sel_template"));
        assertNotNull(template);

        TemplateStageDefinition stage = template.getPhases().get(0).stages().get(0);
        assertFalse(stage.getObjectiveSelection().isPresent());
    }

    @DisplayName("Given template with rewards section, when loading, then reward definitions are parsed")
    @Test
    void loadTemplates_parsesRewards_whenRewardsSectionPresent() throws IOException {
        TestFileUtils.writeFile(tempDir,"rewards.yml", """
                quest-templates:
                  mcrpg:reward_template:
                    display-name-route: "quests.templates.reward.display-name"
                    board-eligible: true
                    scope: mcrpg:single_player
                    supported-rarities: [COMMON]
                    rewards:
                      xp_reward:
                        type: mcrpg:experience
                        amount: "block_count * 10"
                      cmd_reward:
                        type: mcrpg:command
                        command: "give {player} diamond 1"
                    variables:
                      block_count:
                        type: RANGE
                        base:
                          min: 10
                          max: 50
                    phases:
                      phase:
                        completion-mode: ALL
                        stages:
                          stage:
                            objectives:
                              break-blocks:
                                type: mcrpg:block_break
                                required-progress: "block_count"
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        QuestTemplate template = result.get(NamespacedKey.fromString("mcrpg:reward_template"));
        assertNotNull(template);

        assertEquals(2, template.getRewards().size());
        TemplateRewardDefinition xpReward = template.getRewards().get(0);
        assertEquals(NamespacedKey.fromString("mcrpg:experience"), xpReward.typeKey());
        assertEquals("xp_reward", xpReward.label());
        assertEquals("block_count * 10", xpReward.config().get("amount"));
    }

    @DisplayName("Given template with no rewards section, when loading, then rewards list is empty")
    @Test
    void loadTemplates_returnsEmptyRewards_whenNoRewardsSection() throws IOException {
        TestFileUtils.writeFile(tempDir,"no_rewards.yml", """
                quest-templates:
                  mcrpg:no_reward_template:
                    display-name-route: "quests.templates.no-reward.display-name"
                    board-eligible: true
                    scope: mcrpg:single_player
                    supported-rarities: [COMMON]
                    phases:
                      phase:
                        completion-mode: ALL
                        stages:
                          stage:
                            objectives:
                              break-blocks:
                                type: mcrpg:block_break
                                required-progress: 10
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        QuestTemplate template = result.get(NamespacedKey.fromString("mcrpg:no_reward_template"));
        assertNotNull(template);
        assertTrue(template.getRewards().isEmpty());
    }

    @DisplayName("Given template with display section, when loading, then inline display is populated")
    @Test
    void loadTemplates_parsesInlineDisplay_whenDisplaySectionPresent() throws IOException {
        TestFileUtils.writeFile(tempDir,"display.yml", """
                quest-templates:
                  mcrpg:display_template:
                    display-name-route: "quests.templates.display.display-name"
                    board-eligible: true
                    scope: mcrpg:single_player
                    supported-rarities: [COMMON]
                    display:
                      name: "Mining Quest"
                      description: "Mine some blocks"
                      objectives:
                        break_blocks: "Break stones"
                    phases:
                      phase:
                        completion-mode: ALL
                        stages:
                          stage:
                            objectives:
                              break-blocks:
                                type: mcrpg:block_break
                                required-progress: 10
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        QuestTemplate template = result.get(NamespacedKey.fromString("mcrpg:display_template"));
        assertNotNull(template);

        Map<String, String> display = template.getInlineDisplay();
        assertEquals("Mining Quest", display.get("name"));
        assertEquals("Mine some blocks", display.get("description"));
        assertEquals("Break stones", display.get("objective.break_blocks"));
    }

    @DisplayName("Given expression referencing undeclared variable, when loading, then template is skipped")
    @Test
    void loadTemplates_skipsTemplate_whenUndeclaredVariableInProgress() throws IOException {
        TestFileUtils.writeFile(tempDir,"undeclared.yml", """
                quest-templates:
                  mcrpg:undeclared_template:
                    display-name-route: "quests.templates.undeclared.display-name"
                    board-eligible: true
                    scope: mcrpg:single_player
                    supported-rarities: [COMMON]
                    variables:
                      block_count:
                        type: RANGE
                        base:
                          min: 10
                          max: 50
                    phases:
                      phase:
                        completion-mode: ALL
                        stages:
                          stage:
                            objectives:
                              break-blocks:
                                type: mcrpg:block_break
                                required-progress: "unknown_var * 10"
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        assertTrue(result.isEmpty(), "Template with undeclared variable in expression should be skipped");
    }

    @DisplayName("Given expression with invalid syntax, when loading, then template is skipped")
    @Test
    void loadTemplates_skipsTemplate_whenInvalidExpressionSyntax() throws IOException {
        TestFileUtils.writeFile(tempDir,"bad_expr.yml", """
                quest-templates:
                  mcrpg:bad_expr_template:
                    display-name-route: "quests.templates.bad-expr.display-name"
                    board-eligible: true
                    scope: mcrpg:single_player
                    supported-rarities: [COMMON]
                    variables:
                      count:
                        type: RANGE
                        base:
                          min: 10
                          max: 50
                    phases:
                      phase:
                        completion-mode: ALL
                        stages:
                          stage:
                            objectives:
                              break-blocks:
                                type: mcrpg:block_break
                                required-progress: "count * * 10"
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        assertTrue(result.isEmpty(), "Template with invalid expression syntax should be skipped");
    }

    @DisplayName("Given template with reward expression referencing undeclared variable, when loading, then template is skipped")
    @Test
    void loadTemplates_skipsTemplate_whenUndeclaredVariableInReward() throws IOException {
        TestFileUtils.writeFile(tempDir,"reward_undeclared.yml", """
                quest-templates:
                  mcrpg:reward_undeclared:
                    display-name-route: "quests.templates.reward-undeclared.display-name"
                    board-eligible: true
                    scope: mcrpg:single_player
                    supported-rarities: [COMMON]
                    variables:
                      block_count:
                        type: RANGE
                        base:
                          min: 10
                          max: 50
                    rewards:
                      xp_reward:
                        type: mcrpg:experience
                        amount: "block_count + missing_var"
                    phases:
                      phase:
                        completion-mode: ALL
                        stages:
                          stage:
                            objectives:
                              break-blocks:
                                type: mcrpg:block_break
                                required-progress: "block_count"
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        assertTrue(result.isEmpty(), "Template with undeclared variable in reward expression should be skipped");
    }

    @DisplayName("Given template with pool variable in objective config, when loading, then config map preserves raw string")
    @Test
    void loadTemplates_preservesPoolReference_whenPoolVariableInConfig() throws IOException {
        TestFileUtils.writeFile(tempDir,"pool_config.yml", """
                quest-templates:
                  mcrpg:pool_config_template:
                    display-name-route: "quests.templates.pool-config.display-name"
                    board-eligible: true
                    scope: mcrpg:single_player
                    supported-rarities: [COMMON]
                    variables:
                      block_type:
                        type: POOL
                        pools:
                          common_stones:
                            difficulty: 1.0
                            weight:
                              COMMON: 100
                            values: [STONE, GRANITE]
                          rare_ores:
                            difficulty: 3.0
                            weight:
                              COMMON: 100
                            values: [DIAMOND_ORE]
                    phases:
                      phase:
                        completion-mode: ALL
                        stages:
                          stage:
                            objectives:
                              break-blocks:
                                type: mcrpg:block_break
                                required-progress: 10
                                config:
                                  material: "block_type"
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        QuestTemplate template = result.get(NamespacedKey.fromString("mcrpg:pool_config_template"));
        assertNotNull(template);

        var objectives = template.getPhases().get(0).stages().get(0).objectives();
        assertEquals(1, objectives.size());
        assertEquals("block_type", objectives.get(0).config().get("material"));
    }

    @DisplayName("Given template with no phases section, when loading, then template is skipped")
    @Test
    void loadTemplates_skipsTemplate_whenNoPhasesSection() throws IOException {
        TestFileUtils.writeFile(tempDir,"no_phases.yml", """
                quest-templates:
                  mcrpg:no_phases_template:
                    display-name-route: "quests.templates.no-phases.display-name"
                    board-eligible: true
                    scope: mcrpg:single_player
                    supported-rarities: [COMMON]
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        assertTrue(result.isEmpty(), "Template with no phases should be skipped");
    }

    @DisplayName("Given template with empty objectives in a stage, when loading, then template is skipped")
    @Test
    void loadTemplates_skipsTemplate_whenEmptyObjectives() throws IOException {
        TestFileUtils.writeFile(tempDir,"empty_obj.yml", """
                quest-templates:
                  mcrpg:empty_obj_template:
                    display-name-route: "quests.templates.empty-obj.display-name"
                    board-eligible: true
                    scope: mcrpg:single_player
                    supported-rarities: [COMMON]
                    phases:
                      phase:
                        completion-mode: ALL
                        stages:
                          stage:
                            objectives:
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        assertTrue(result.isEmpty(), "Template with empty objectives should be skipped");
    }

    @DisplayName("Given template with difficulty variable in expression, when loading, then expression validates successfully")
    @Test
    void loadTemplates_loadsTemplate_whenDifficultyBuiltinVariableUsed() throws IOException {
        TestFileUtils.writeFile(tempDir,"difficulty.yml", """
                quest-templates:
                  mcrpg:difficulty_template:
                    display-name-route: "quests.templates.difficulty.display-name"
                    board-eligible: true
                    scope: mcrpg:single_player
                    supported-rarities: [COMMON]
                    phases:
                      phase:
                        completion-mode: ALL
                        stages:
                          stage:
                            objectives:
                              break-blocks:
                                type: mcrpg:block_break
                                required-progress: "difficulty * 50"
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        QuestTemplate template = result.get(NamespacedKey.fromString("mcrpg:difficulty_template"));
        assertNotNull(template, "Template using built-in 'difficulty' variable should load successfully");
    }

}
