package us.eunoians.mcrpg.quest.board.template.condition;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Additional tests for {@link ConditionParser} covering methods not exercised by
 * {@link TemplateConditionParsingTest}: {@code parsePrerequisiteBlock},
 * {@code parseExplicitType}, and the {@code less-than} variable shorthand.
 */
class ConditionParserAdditionalTest {

    private TemplateConditionRegistry registry;
    private ConditionParser parser;

    private static Section sectionFrom(String yaml) {
        try {
            return YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse test YAML", e);
        }
    }

    @BeforeEach
    void setUp() {
        registry = new TemplateConditionRegistry();
        parser = new ConditionParser(registry);
    }

    @Nested
    @DisplayName("parsePrerequisiteBlock")
    class ParsePrerequisiteBlock {

        @Test
        @DisplayName("returns null when no prerequisite key is present")
        void returnsNull_whenNoPrerequisiteKey() throws IOException {
            YamlDocument doc = YamlDocument.create(new ByteArrayInputStream(
                    "other-key: value\n".getBytes(StandardCharsets.UTF_8)));
            assertNull(parser.parsePrerequisiteBlock(doc));
        }

        @Test
        @DisplayName("parses prerequisite block containing a chance condition")
        void parsesPrerequisiteBlock_withChanceCondition() {
            Section section = sectionFrom("prerequisite:\n  chance: 0.5\n");
            TemplateCondition condition = parser.parsePrerequisiteBlock(section);
            assertNotNull(condition);
            assertInstanceOf(ChanceCondition.class, condition);
            assertEquals(0.5, ((ChanceCondition) condition).getChance(), 1e-9);
        }

        @Test
        @DisplayName("parses prerequisite block containing a min-completions condition")
        void parsesPrerequisiteBlock_withMinCompletions() {
            Section section = sectionFrom("prerequisite:\n  min-completions: 10\n");
            TemplateCondition condition = parser.parsePrerequisiteBlock(section);
            assertInstanceOf(CompletionPrerequisiteCondition.class, condition);
            assertEquals(10, ((CompletionPrerequisiteCondition) condition).getMinCompletions());
        }

        @Test
        @DisplayName("parses prerequisite block containing a compound condition")
        void parsesPrerequisiteBlock_withCompoundCondition() {
            String yaml = "prerequisite:\n  all:\n    c1:\n      chance: 1.0\n    c2:\n      permission: mcrpg.vip\n";
            Section section = sectionFrom(yaml);
            TemplateCondition condition = parser.parsePrerequisiteBlock(section);
            assertInstanceOf(CompoundCondition.class, condition);
            CompoundCondition compound = (CompoundCondition) condition;
            assertEquals(CompoundCondition.LogicMode.ALL, compound.getMode());
            assertEquals(2, compound.getConditions().size());
        }
    }

    @Nested
    @DisplayName("parseExplicitType")
    class ParseExplicitType {

        @Test
        @DisplayName("resolves registered type via explicit type key")
        void resolvesRegisteredType() {
            registry.register(new RarityCondition());
            Section section = sectionFrom("type: mcrpg:rarity_gate\nmin-rarity: mcrpg:rare\n");
            TemplateCondition condition = parser.parseSingle(section);
            assertInstanceOf(RarityCondition.class, condition);
            assertEquals(NamespacedKey.fromString("mcrpg:rare"),
                    ((RarityCondition) condition).getMinimumRarity());
        }

        @Test
        @DisplayName("resolves ChanceCondition via explicit type key")
        void resolvesChanceViaExplicitType() {
            registry.register(new ChanceCondition());
            Section section = sectionFrom("type: mcrpg:chance\nchance: 0.75\n");
            TemplateCondition condition = parser.parseSingle(section);
            assertInstanceOf(ChanceCondition.class, condition);
            assertEquals(0.75, ((ChanceCondition) condition).getChance(), 1e-9);
        }

        @Test
        @DisplayName("resolves VariableCondition via explicit type key")
        void resolvesVariableViaExplicitType() {
            registry.register(new VariableCondition());
            Section section = sectionFrom("type: mcrpg:variable_check\nname: difficulty\ngreater-than: 1.5\n");
            TemplateCondition condition = parser.parseSingle(section);
            assertInstanceOf(VariableCondition.class, condition);
            VariableCondition vc = (VariableCondition) condition;
            assertEquals("difficulty", vc.getVariableName());
            assertInstanceOf(VariableCheck.NumericComparison.class, vc.getCheck());
        }

        @Test
        @DisplayName("resolves PermissionCondition via explicit type key")
        void resolvesPermissionViaExplicitType() {
            registry.register(new PermissionCondition());
            Section section = sectionFrom("type: mcrpg:permission_check\npermission: mcrpg.quest.veteran\n");
            TemplateCondition condition = parser.parseSingle(section);
            assertInstanceOf(PermissionCondition.class, condition);
            assertEquals("mcrpg.quest.veteran", ((PermissionCondition) condition).getPermission());
        }

        @Test
        @DisplayName("resolves CompletionPrerequisiteCondition via explicit type key")
        void resolvesCompletionPrerequisiteViaExplicitType() {
            registry.register(new CompletionPrerequisiteCondition());
            Section section = sectionFrom("type: mcrpg:completion_prerequisite\nmin-completions: 3\ncategory: mcrpg:personal_weekly\n");
            TemplateCondition condition = parser.parseSingle(section);
            assertInstanceOf(CompletionPrerequisiteCondition.class, condition);
            CompletionPrerequisiteCondition cpc = (CompletionPrerequisiteCondition) condition;
            assertEquals(3, cpc.getMinCompletions());
            assertEquals(NamespacedKey.fromString("mcrpg:personal_weekly"),
                    cpc.getCategoryKey().orElse(null));
        }

        @Test
        @DisplayName("resolves third-party custom condition via explicit type key")
        void resolvesThirdPartyCondition() {
            NamespacedKey customKey = NamespacedKey.fromString("myplugin:custom_gate");
            TemplateCondition custom = new TemplateCondition() {
                @NotNull @Override public NamespacedKey getKey() { return customKey; }
                @Override public boolean evaluate(@NotNull ConditionContext context) { return true; }
                @NotNull @Override public TemplateCondition fromConfig(@NotNull Section section, @NotNull ConditionParser parser) { return this; }
                @NotNull @Override public Optional<NamespacedKey> getExpansionKey() { return Optional.empty(); }
            };
            registry.register(custom);
            Section section = sectionFrom("type: myplugin:custom_gate\n");
            TemplateCondition result = parser.parseSingle(section);
            assertNotNull(result);
        }

        @Test
        @DisplayName("throws for unregistered type key")
        void throwsForUnregisteredType() {
            Section section = sectionFrom("type: mcrpg:nonexistent\n");
            assertThrows(IllegalArgumentException.class, () -> parser.parseSingle(section));
        }
    }

    @Nested
    @DisplayName("Shorthand: less-than variable check")
    class LessThanShorthand {

        @Test
        @DisplayName("parses variable less-than into VariableCondition with NumericComparison")
        void parsesVariableLessThan() {
            String yaml = "variable:\n  name: block_count\n  less-than: 100.0\n";
            Section section = sectionFrom(yaml);
            VariableCondition condition = (VariableCondition) parser.parseSingle(section);
            assertInstanceOf(VariableCheck.NumericComparison.class, condition.getCheck());
            VariableCheck.NumericComparison comparison = (VariableCheck.NumericComparison) condition.getCheck();
            assertEquals(ComparisonOperator.LESS_THAN, comparison.operator());
            assertEquals(100.0, comparison.threshold(), 1e-9);
        }
    }

    @Nested
    @DisplayName("parseVariableCheck — unrecognized check key")
    class ParseVariableCheckUnrecognized {

        @Test
        @DisplayName("throws for variable section with no recognized check key")
        void throwsForUnrecognizedCheckKey() {
            String yaml = "variable:\n  name: block_count\n  unknown-op: 5.0\n";
            Section section = sectionFrom(yaml);
            assertThrows(IllegalArgumentException.class, () -> parser.parseSingle(section));
        }
    }
}
