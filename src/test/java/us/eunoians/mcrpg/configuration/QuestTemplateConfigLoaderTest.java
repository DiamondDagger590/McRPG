package us.eunoians.mcrpg.configuration;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.eunoians.mcrpg.quest.board.template.QuestTemplate;
import us.eunoians.mcrpg.quest.board.template.RarityOverride;
import us.eunoians.mcrpg.quest.board.template.variable.PoolVariable;
import us.eunoians.mcrpg.quest.board.template.variable.RangeVariable;
import us.eunoians.mcrpg.quest.board.template.variable.TemplateVariable;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class QuestTemplateConfigLoaderTest {

    private QuestTemplateConfigLoader loader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        loader = new QuestTemplateConfigLoader(Logger.getLogger("TestLoader"));
    }

    @Test
    @DisplayName("Load single template from file")
    void loadSingleTemplate() throws IOException {
        writeYaml("test.yml", """
                quest-templates:
                  mcrpg:daily_mining:
                    display-name-route: "quests.templates.daily-mining.display-name"
                    board-eligible: true
                    scope: mcrpg:single_player
                    supported-rarities: [COMMON, UNCOMMON]
                    variables:
                      block_count:
                        type: RANGE
                        base:
                          min: 32
                          max: 64
                    phases:
                      mine-phase:
                        completion-mode: ALL
                        stages:
                          mine-stage:
                            objectives:
                              break-blocks:
                                type: mcrpg:block_break
                                required-progress: "block_count"
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        assertEquals(1, result.size());

        QuestTemplate template = result.get(NamespacedKey.fromString("mcrpg:daily_mining"));
        assertNotNull(template);
        assertEquals(NamespacedKey.fromString("mcrpg:daily_mining"), template.getKey());
        assertTrue(template.isBoardEligible());
        assertEquals(2, template.getSupportedRarities().size());
        assertEquals(1, template.getPhases().size());
        assertEquals(PhaseCompletionMode.ALL, template.getPhases().get(0).completionMode());
    }

    @Test
    @DisplayName("Load multiple templates from one file")
    void loadMultipleTemplatesFromOneFile() throws IOException {
        writeYaml("multi.yml", """
                quest-templates:
                  mcrpg:template_a:
                    display-name-route: "a.display"
                    supported-rarities: [COMMON]
                    variables:
                      count:
                        type: RANGE
                        base:
                          min: 1
                          max: 10
                    phases:
                      phase-1:
                        stages:
                          stage-1:
                            objectives:
                              obj:
                                type: mcrpg:block_break
                                required-progress: 10
                  mcrpg:template_b:
                    display-name-route: "b.display"
                    supported-rarities: [UNCOMMON]
                    phases:
                      phase-1:
                        stages:
                          stage-1:
                            objectives:
                              obj:
                                type: mcrpg:entity_slay
                                required-progress: 5
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        assertEquals(2, result.size());
        assertNotNull(result.get(NamespacedKey.fromString("mcrpg:template_a")));
        assertNotNull(result.get(NamespacedKey.fromString("mcrpg:template_b")));
    }

    @Test
    @DisplayName("Multiple files in directory are loaded")
    void multipleFilesInDirectory() throws IOException {
        writeYaml("file1.yml", """
                quest-templates:
                  mcrpg:from_file1:
                    display-name-route: "f1.display"
                    supported-rarities: [COMMON]
                    phases:
                      p:
                        stages:
                          s:
                            objectives:
                              o:
                                type: mcrpg:block_break
                                required-progress: 10
                """);

        writeYaml("file2.yml", """
                quest-templates:
                  mcrpg:from_file2:
                    display-name-route: "f2.display"
                    supported-rarities: [RARE]
                    phases:
                      p:
                        stages:
                          s:
                            objectives:
                              o:
                                type: mcrpg:entity_slay
                                required-progress: 5
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Pool variable parsing: min/max selections, per-rarity weights, values")
    void poolVariableParsing() throws IOException {
        writeYaml("pool.yml", """
                quest-templates:
                  mcrpg:pool_test:
                    display-name-route: "pool.display"
                    supported-rarities: [COMMON, RARE]
                    variables:
                      target_blocks:
                        type: POOL
                        min-selections: 1
                        max-selections: 2
                        pools:
                          stone:
                            difficulty: 1.0
                            weight:
                              COMMON: 80
                              RARE: 10
                            values: [STONE, COBBLESTONE]
                          ores:
                            difficulty: 1.5
                            weight:
                              COMMON: 20
                              RARE: 90
                            values: [IRON_ORE, DIAMOND_ORE]
                    phases:
                      p:
                        stages:
                          s:
                            objectives:
                              o:
                                type: mcrpg:block_break
                                required-progress: 10
                                config:
                                  blocks: target_blocks
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        QuestTemplate template = result.get(NamespacedKey.fromString("mcrpg:pool_test"));
        assertNotNull(template);

        TemplateVariable var = template.getVariables().get("target_blocks");
        assertNotNull(var);
        assertInstanceOf(PoolVariable.class, var);

        PoolVariable pool = (PoolVariable) var;
        assertEquals(1, pool.getMinSelections());
        assertEquals(2, pool.getMaxSelections());
        assertEquals(2, pool.getPools().size());
        assertEquals("stone", pool.getPools().get(0).name());
        assertEquals(1.0, pool.getPools().get(0).difficulty());
        assertEquals(80, pool.getPools().get(0).getWeightForRarity(NamespacedKey.fromString("mcrpg:common")));
    }

    @Test
    @DisplayName("Range variable parsing: min/max")
    void rangeVariableParsing() throws IOException {
        writeYaml("range.yml", """
                quest-templates:
                  mcrpg:range_test:
                    display-name-route: "range.display"
                    supported-rarities: [COMMON]
                    variables:
                      block_count:
                        type: RANGE
                        base:
                          min: 32
                          max: 64
                    phases:
                      p:
                        stages:
                          s:
                            objectives:
                              o:
                                type: mcrpg:block_break
                                required-progress: "block_count"
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        QuestTemplate template = result.get(NamespacedKey.fromString("mcrpg:range_test"));
        RangeVariable range = (RangeVariable) template.getVariables().get("block_count");
        assertEquals(32.0, range.getMin());
        assertEquals(64.0, range.getMax());
    }

    @Test
    @DisplayName("Range variable rejects min <= 0")
    void rangeVariable_rejectsNonPositiveMin() throws IOException {
        writeYaml("bad_range.yml", """
                quest-templates:
                  mcrpg:bad_range:
                    display-name-route: "bad.display"
                    supported-rarities: [COMMON]
                    variables:
                      count:
                        type: RANGE
                        base:
                          min: 0
                          max: 10
                    phases:
                      p:
                        stages:
                          s:
                            objectives:
                              o:
                                type: mcrpg:block_break
                                required-progress: 10
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Rarity overrides parsing")
    void rarityOverridesParsing() throws IOException {
        writeYaml("override.yml", """
                quest-templates:
                  mcrpg:override_test:
                    display-name-route: "override.display"
                    supported-rarities: [COMMON, RARE]
                    rarity-overrides:
                      RARE:
                        difficulty-multiplier: 2.0
                        reward-multiplier: 2.5
                    phases:
                      p:
                        stages:
                          s:
                            objectives:
                              o:
                                type: mcrpg:block_break
                                required-progress: 10
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        QuestTemplate template = result.get(NamespacedKey.fromString("mcrpg:override_test"));
        assertNotNull(template);

        Map<NamespacedKey, RarityOverride> overrides = template.getRarityOverrides();
        assertEquals(1, overrides.size());
        RarityOverride rareOverride = overrides.get(NamespacedKey.fromString("mcrpg:rare"));
        assertNotNull(rareOverride);
        assertEquals(2.0, rareOverride.difficultyMultiplier());
        assertEquals(2.5, rareOverride.rewardMultiplier());
    }

    @Test
    @DisplayName("Invalid variable type produces error and skips template")
    void invalidVariableType_skipsTemplate() throws IOException {
        writeYaml("bad_type.yml", """
                quest-templates:
                  mcrpg:bad_type:
                    display-name-route: "bad.display"
                    supported-rarities: [COMMON]
                    variables:
                      x:
                        type: INVALID
                    phases:
                      p:
                        stages:
                          s:
                            objectives:
                              o:
                                type: mcrpg:block_break
                                required-progress: 10
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Empty directory produces no templates and no error")
    void emptyDirectory_noTemplates() {
        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("loadTemplatesFromDirectories merges templates from multiple directories")
    void loadFromMultipleDirectories() throws IOException {
        Path dir1 = tempDir.resolve("dir1");
        Path dir2 = tempDir.resolve("dir2");
        Files.createDirectories(dir1);
        Files.createDirectories(dir2);

        Files.writeString(dir1.resolve("a.yml"), """
                quest-templates:
                  mcrpg:from_dir1:
                    display-name-route: "d1.display"
                    supported-rarities: [COMMON]
                    phases:
                      p:
                        stages:
                          s:
                            objectives:
                              o:
                                type: mcrpg:block_break
                                required-progress: 10
                """);

        Files.writeString(dir2.resolve("b.yml"), """
                quest-templates:
                  mcrpg:from_dir2:
                    display-name-route: "d2.display"
                    supported-rarities: [RARE]
                    phases:
                      p:
                        stages:
                          s:
                            objectives:
                              o:
                                type: mcrpg:entity_slay
                                required-progress: 5
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectories(
                List.of(dir1.toFile(), dir2.toFile()));
        assertEquals(2, result.size());
        assertNotNull(result.get(NamespacedKey.fromString("mcrpg:from_dir1")));
        assertNotNull(result.get(NamespacedKey.fromString("mcrpg:from_dir2")));
    }

    @Test
    @DisplayName("Duplicate key across directories: last-loaded wins")
    void duplicateKeyAcrossDirectories_lastWins() throws IOException {
        Path dir1 = tempDir.resolve("dir1");
        Path dir2 = tempDir.resolve("dir2");
        Files.createDirectories(dir1);
        Files.createDirectories(dir2);

        String templateYaml = """
                quest-templates:
                  mcrpg:shared_key:
                    display-name-route: "%s"
                    supported-rarities: [COMMON]
                    phases:
                      p:
                        stages:
                          s:
                            objectives:
                              o:
                                type: mcrpg:block_break
                                required-progress: 10
                """;

        Files.writeString(dir1.resolve("a.yml"), templateYaml.formatted("dir1.display"));
        Files.writeString(dir2.resolve("a.yml"), templateYaml.formatted("dir2.display"));

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectories(
                List.of(dir1.toFile(), dir2.toFile()));
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Undeclared variable reference in objective config produces error")
    void undeclaredVariableRef_inObjectiveConfig_error() throws IOException {
        writeYaml("undeclared.yml", """
                quest-templates:
                  mcrpg:undeclared_var:
                    display-name-route: "test.display"
                    supported-rarities: [COMMON]
                    variables:
                      block_count:
                        type: RANGE
                        base:
                          min: 1
                          max: 10
                    phases:
                      p:
                        stages:
                          s:
                            objectives:
                              o:
                                type: mcrpg:block_break
                                required-progress: "nonexistent_var"
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Expression syntax validation: trailing operator rejected")
    void expressionSyntax_trailingOperator_rejected() throws IOException {
        writeYaml("bad_expr.yml", """
                quest-templates:
                  mcrpg:bad_expr:
                    display-name-route: "test.display"
                    supported-rarities: [COMMON]
                    variables:
                      block_count:
                        type: RANGE
                        base:
                          min: 1
                          max: 10
                    phases:
                      p:
                        stages:
                          s:
                            objectives:
                              o:
                                type: mcrpg:block_break
                                required-progress: "block_count * 5 *"
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Expression syntax validation: valid expression accepted")
    void expressionSyntax_validExpression_accepted() throws IOException {
        writeYaml("good_expr.yml", """
                quest-templates:
                  mcrpg:good_expr:
                    display-name-route: "test.display"
                    supported-rarities: [COMMON]
                    variables:
                      block_count:
                        type: RANGE
                        base:
                          min: 1
                          max: 10
                    phases:
                      p:
                        stages:
                          s:
                            objectives:
                              o:
                                type: mcrpg:block_break
                                required-progress: "block_count * 5"
                    rewards:
                      xp:
                        type: mcrpg:experience
                        amount: "block_count * 5 * difficulty"
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("CustomBlockWrapper identifiers accepted in pool values")
    void customBlockWrapperInPoolValues() throws IOException {
        writeYaml("custom.yml", """
                quest-templates:
                  mcrpg:custom_blocks:
                    display-name-route: "custom.display"
                    supported-rarities: [COMMON]
                    variables:
                      target_blocks:
                        type: POOL
                        min-selections: 1
                        max-selections: 1
                        pools:
                          custom:
                            difficulty: 1.0
                            weight:
                              COMMON: 100
                            values: ["itemsadder:ruby_ore", "oraxen:mithril_ore", DIAMOND_ORE]
                    phases:
                      p:
                        stages:
                          s:
                            objectives:
                              o:
                                type: mcrpg:block_break
                                required-progress: 10
                                config:
                                  blocks: target_blocks
                """);

        Map<NamespacedKey, QuestTemplate> result = loader.loadTemplatesFromDirectory(tempDir.toFile());
        assertEquals(1, result.size());

        QuestTemplate template = result.get(NamespacedKey.fromString("mcrpg:custom_blocks"));
        PoolVariable pool = (PoolVariable) template.getVariables().get("target_blocks");
        assertEquals(3, pool.getPools().get(0).values().size());
        assertTrue(pool.getPools().get(0).values().contains("itemsadder:ruby_ore"));
    }

    private void writeYaml(String fileName, String content) throws IOException {
        Files.writeString(tempDir.resolve(fileName), content);
    }
}
