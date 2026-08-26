package us.eunoians.mcrpg.quest.board.template.condition;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.template.ResolvedVariableContext;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link TemplateCondition#evaluate} across all built-in condition types.
 * The existing {@link TemplateConditionParsingTest} covers YAML parsing; this class
 * covers the evaluation logic, constructor validation, and serialization round-trips.
 */
class TemplateConditionEvaluationTest extends McRPGBaseTest {

    @Nested
    @DisplayName("ChanceCondition")
    class ChanceConditionTests {

        @Test
        @DisplayName("evaluate returns true when random is null (pass-through)")
        void evaluate_returnsTrue_whenRandomIsNull() {
            ChanceCondition condition = new ChanceCondition(0.5);
            ConditionContext context = new ConditionContext(null, null, null, null, null, null);
            assertTrue(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate returns true when roll is below chance threshold")
        void evaluate_returnsTrue_whenRollBelowThreshold() {
            ChanceCondition condition = new ChanceCondition(0.5);
            Random seeded = new Random(42);
            double firstRoll = new Random(42).nextDouble();
            boolean expected = firstRoll < 0.5;
            ConditionContext context = new ConditionContext(null, null, seeded, null, null, null);
            assertEquals(expected, condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate always returns true for chance 1.0")
        void evaluate_alwaysTrue_whenChanceIsOne() {
            ChanceCondition condition = new ChanceCondition(1.0);
            Random random = new Random(0);
            ConditionContext context = new ConditionContext(null, null, random, null, null, null);
            for (int i = 0; i < 100; i++) {
                assertTrue(condition.evaluate(context));
            }
        }

        @Test
        @DisplayName("evaluate always returns false for chance 0.0")
        void evaluate_alwaysFalse_whenChanceIsZero() {
            ChanceCondition condition = new ChanceCondition(0.0);
            Random random = new Random(0);
            ConditionContext context = new ConditionContext(null, null, random, null, null, null);
            for (int i = 0; i < 100; i++) {
                assertFalse(condition.evaluate(context));
            }
        }

        @Test
        @DisplayName("constructor throws for negative chance")
        void constructor_throws_whenChanceNegative() {
            assertThrows(IllegalArgumentException.class, () -> new ChanceCondition(-0.1));
        }

        @Test
        @DisplayName("constructor throws for chance above 1.0")
        void constructor_throws_whenChanceAboveOne() {
            assertThrows(IllegalArgumentException.class, () -> new ChanceCondition(1.1));
        }

        @Test
        @DisplayName("serializeConfig round-trips chance value")
        void serializeConfig_roundTripsChance() {
            ChanceCondition condition = new ChanceCondition(0.75);
            Map<String, Object> serialized = condition.serializeConfig();
            assertEquals(0.75, (double) serialized.get("chance"), 1e-9);
        }

        @Test
        @DisplayName("getKey returns mcrpg:chance")
        void getKey_returnsMcrpgChance() {
            ChanceCondition condition = new ChanceCondition(0.5);
            assertEquals("mcrpg", condition.getKey().getNamespace());
            assertEquals("chance", condition.getKey().getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPG expansion key")
        void getExpansionKey_returnsMcRPGExpansion() {
            ChanceCondition condition = new ChanceCondition(0.5);
            assertTrue(condition.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, condition.getExpansionKey().get());
        }
    }

    @Nested
    @DisplayName("RarityCondition")
    class RarityConditionTests {

        private QuestRarityRegistry createRarityRegistry() {
            QuestRarityRegistry registry = new QuestRarityRegistry();
            registry.register(new QuestRarity(
                    NamespacedKey.fromString("mcrpg:common"), 100, 1.0, 1.0,
                    McRPGExpansion.EXPANSION_KEY));
            registry.register(new QuestRarity(
                    NamespacedKey.fromString("mcrpg:rare"), 50, 1.5, 1.5,
                    McRPGExpansion.EXPANSION_KEY));
            registry.register(new QuestRarity(
                    NamespacedKey.fromString("mcrpg:legendary"), 10, 3.0, 3.0,
                    McRPGExpansion.EXPANSION_KEY));
            return registry;
        }

        @Test
        @DisplayName("evaluate returns true when both rarity and registry are null (pass-through)")
        void evaluate_returnsTrue_whenRarityAndRegistryNull() {
            RarityCondition condition = new RarityCondition(NamespacedKey.fromString("mcrpg:rare"));
            ConditionContext context = new ConditionContext(null, null, null, null, null, null);
            assertTrue(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate returns true when rolled rarity is rarer than minimum")
        void evaluate_returnsTrue_whenRolledRarityIsRarer() {
            QuestRarityRegistry registry = createRarityRegistry();
            RarityCondition condition = new RarityCondition(NamespacedKey.fromString("mcrpg:rare"));
            ConditionContext context = new ConditionContext(
                    NamespacedKey.fromString("mcrpg:legendary"), registry, null, null, null, null);
            assertTrue(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate returns true when rolled rarity equals minimum")
        void evaluate_returnsTrue_whenRolledRarityEqualsMinimum() {
            QuestRarityRegistry registry = createRarityRegistry();
            RarityCondition condition = new RarityCondition(NamespacedKey.fromString("mcrpg:rare"));
            ConditionContext context = new ConditionContext(
                    NamespacedKey.fromString("mcrpg:rare"), registry, null, null, null, null);
            assertTrue(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate returns false when rolled rarity is more common than minimum")
        void evaluate_returnsFalse_whenRolledRarityIsMoreCommon() {
            QuestRarityRegistry registry = createRarityRegistry();
            RarityCondition condition = new RarityCondition(NamespacedKey.fromString("mcrpg:rare"));
            ConditionContext context = new ConditionContext(
                    NamespacedKey.fromString("mcrpg:common"), registry, null, null, null, null);
            assertFalse(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate returns false when rolled rarity key is not in registry")
        void evaluate_returnsFalse_whenRolledRarityUnknown() {
            QuestRarityRegistry registry = createRarityRegistry();
            RarityCondition condition = new RarityCondition(NamespacedKey.fromString("mcrpg:rare"));
            ConditionContext context = new ConditionContext(
                    NamespacedKey.fromString("mcrpg:unknown"), registry, null, null, null, null);
            assertFalse(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate returns false when minimum rarity key is not in registry")
        void evaluate_returnsFalse_whenMinimumRarityUnknown() {
            QuestRarityRegistry registry = createRarityRegistry();
            RarityCondition condition = new RarityCondition(NamespacedKey.fromString("mcrpg:mythic"));
            ConditionContext context = new ConditionContext(
                    NamespacedKey.fromString("mcrpg:common"), registry, null, null, null, null);
            assertFalse(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate returns true when only rarity is null (pass-through)")
        void evaluate_returnsTrue_whenOnlyRarityNull() {
            QuestRarityRegistry registry = createRarityRegistry();
            RarityCondition condition = new RarityCondition(NamespacedKey.fromString("mcrpg:rare"));
            ConditionContext context = new ConditionContext(null, registry, null, null, null, null);
            assertTrue(condition.evaluate(context));
        }

        @Test
        @DisplayName("serializeConfig round-trips minimum rarity key")
        void serializeConfig_roundTripsMinRarity() {
            RarityCondition condition = new RarityCondition(NamespacedKey.fromString("mcrpg:rare"));
            Map<String, Object> serialized = condition.serializeConfig();
            assertEquals("mcrpg:rare", serialized.get("min-rarity"));
        }
    }

    @Nested
    @DisplayName("PermissionCondition")
    @ExtendWith(McRPGPlayerExtension.class)
    class PermissionConditionTests {

        @Test
        @DisplayName("evaluate returns false when playerUUID is null")
        void evaluate_returnsFalse_whenPlayerUUIDNull() {
            PermissionCondition condition = new PermissionCondition("mcrpg.vip");
            ConditionContext context = new ConditionContext(null, null, null, null, null, null);
            assertFalse(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate returns false when player is offline")
        void evaluate_returnsFalse_whenPlayerOffline() {
            PermissionCondition condition = new PermissionCondition("mcrpg.vip");
            ConditionContext context = new ConditionContext(
                    null, null, null, null, UUID.randomUUID(), null);
            assertFalse(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate returns true when player has permission")
        void evaluate_returnsTrue_whenPlayerHasPermission(McRPGPlayer mcRPGPlayer) {
            PlayerMock playerMock = addPlayerToServer(mcRPGPlayer);
            playerMock.addAttachment(mcRPG, "mcrpg.vip", true);

            PermissionCondition condition = new PermissionCondition("mcrpg.vip");
            ConditionContext context = new ConditionContext(
                    null, null, null, null, mcRPGPlayer.getUUID(), null);
            assertTrue(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate returns false when player lacks permission")
        void evaluate_returnsFalse_whenPlayerLacksPermission(McRPGPlayer mcRPGPlayer) {
            addPlayerToServer(mcRPGPlayer);

            PermissionCondition condition = new PermissionCondition("mcrpg.vip");
            ConditionContext context = new ConditionContext(
                    null, null, null, null, mcRPGPlayer.getUUID(), null);
            assertFalse(condition.evaluate(context));
        }

        @Test
        @DisplayName("constructor throws for blank permission")
        void constructor_throws_whenPermissionBlank() {
            assertThrows(IllegalArgumentException.class, () -> new PermissionCondition("   "));
        }

        @Test
        @DisplayName("serializeConfig round-trips permission string")
        void serializeConfig_roundTripsPermission() {
            PermissionCondition condition = new PermissionCondition("mcrpg.quest.vip");
            Map<String, Object> serialized = condition.serializeConfig();
            assertEquals("mcrpg.quest.vip", serialized.get("permission"));
        }
    }

    @Nested
    @DisplayName("CompletionPrerequisiteCondition")
    class CompletionPrerequisiteConditionTests {

        @Test
        @DisplayName("evaluate returns false when playerUUID is null")
        void evaluate_returnsFalse_whenPlayerUUIDNull() {
            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(5, null, null);
            ConditionContext context = new ConditionContext(null, null, null, null, null, null);
            assertFalse(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate returns false when completionHistory is null")
        void evaluate_returnsFalse_whenHistoryNull() {
            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(5, null, null);
            ConditionContext context = new ConditionContext(
                    null, null, null, null, UUID.randomUUID(), null);
            assertFalse(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate returns true when completions meet threshold")
        void evaluate_returnsTrue_whenCompletionsMeetThreshold() {
            UUID playerUUID = UUID.randomUUID();
            QuestCompletionHistory history = (uuid, cat, rarity) -> 10;

            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(5, null, null);
            ConditionContext context = new ConditionContext(
                    null, null, null, null, playerUUID, history);
            assertTrue(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate returns true when completions exactly equal threshold")
        void evaluate_returnsTrue_whenCompletionsEqualThreshold() {
            UUID playerUUID = UUID.randomUUID();
            QuestCompletionHistory history = (uuid, cat, rarity) -> 5;

            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(5, null, null);
            ConditionContext context = new ConditionContext(
                    null, null, null, null, playerUUID, history);
            assertTrue(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate returns false when completions below threshold")
        void evaluate_returnsFalse_whenCompletionsBelowThreshold() {
            UUID playerUUID = UUID.randomUUID();
            QuestCompletionHistory history = (uuid, cat, rarity) -> 3;

            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(5, null, null);
            ConditionContext context = new ConditionContext(
                    null, null, null, null, playerUUID, history);
            assertFalse(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate passes category and rarity filters to history")
        void evaluate_passesFiltersToHistory() {
            UUID playerUUID = UUID.randomUUID();
            NamespacedKey categoryFilter = NamespacedKey.fromString("mcrpg:personal_daily");
            NamespacedKey rarityFilter = NamespacedKey.fromString("mcrpg:rare");

            QuestCompletionHistory history = (uuid, cat, rarity) -> {
                if (categoryFilter.equals(cat) && rarityFilter.equals(rarity)) {
                    return 10;
                }
                return 0;
            };

            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(
                    5, categoryFilter, rarityFilter);
            ConditionContext context = new ConditionContext(
                    null, null, null, null, playerUUID, history);
            assertTrue(condition.evaluate(context));
        }

        @Test
        @DisplayName("constructor throws for minCompletions less than 1")
        void constructor_throws_whenMinCompletionsLessThanOne() {
            assertThrows(IllegalArgumentException.class,
                    () -> new CompletionPrerequisiteCondition(0, null, null));
        }

        @Test
        @DisplayName("serializeConfig includes category and rarity when present")
        void serializeConfig_includesOptionalFields() {
            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(
                    7, NamespacedKey.fromString("mcrpg:daily"), NamespacedKey.fromString("mcrpg:rare"));
            Map<String, Object> serialized = condition.serializeConfig();
            assertEquals(7, serialized.get("min-completions"));
            assertEquals("mcrpg:daily", serialized.get("category"));
            assertEquals("mcrpg:rare", serialized.get("min-rarity"));
        }

        @Test
        @DisplayName("serializeConfig omits category and rarity when null")
        void serializeConfig_omitsNullFields() {
            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(3, null, null);
            Map<String, Object> serialized = condition.serializeConfig();
            assertEquals(3, serialized.get("min-completions"));
            assertFalse(serialized.containsKey("category"));
            assertFalse(serialized.containsKey("min-rarity"));
        }

        @Test
        @DisplayName("getCategoryKey returns empty when null")
        void getCategoryKey_returnsEmpty_whenNull() {
            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(1, null, null);
            assertTrue(condition.getCategoryKey().isEmpty());
        }

        @Test
        @DisplayName("getMinRarity returns empty when null")
        void getMinRarity_returnsEmpty_whenNull() {
            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(1, null, null);
            assertTrue(condition.getMinRarity().isEmpty());
        }
    }

    @Nested
    @DisplayName("VariableCondition")
    class VariableConditionTests {

        @Test
        @DisplayName("evaluate returns true when resolvedVariables is null (pass-through)")
        void evaluate_returnsTrue_whenResolvedVariablesNull() {
            VariableCondition condition = new VariableCondition("difficulty",
                    new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN, 5.0));
            ConditionContext context = new ConditionContext(null, null, null, null, null, null);
            assertTrue(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate returns false when variable is not in resolved values")
        void evaluate_returnsFalse_whenVariableNotFound() {
            ResolvedVariableContext vars = new ResolvedVariableContext(Map.of(), 1.0, 1.0, 1.0);
            VariableCondition condition = new VariableCondition("missing_var",
                    new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN, 5.0));
            ConditionContext context = new ConditionContext(null, null, null, vars, null, null);
            assertFalse(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate delegates to check when variable is found")
        void evaluate_delegatesToCheck_whenVariableFound() {
            ResolvedVariableContext vars = new ResolvedVariableContext(
                    Map.of("difficulty", 7.0), 1.0, 1.0, 1.0);
            VariableCondition condition = new VariableCondition("difficulty",
                    new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN, 5.0));
            ConditionContext context = new ConditionContext(null, null, null, vars, null, null);
            assertTrue(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate returns false when numeric check fails")
        void evaluate_returnsFalse_whenCheckFails() {
            ResolvedVariableContext vars = new ResolvedVariableContext(
                    Map.of("difficulty", 3.0), 1.0, 1.0, 1.0);
            VariableCondition condition = new VariableCondition("difficulty",
                    new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN, 5.0));
            ConditionContext context = new ConditionContext(null, null, null, vars, null, null);
            assertFalse(condition.evaluate(context));
        }

        @Test
        @DisplayName("evaluate with ContainsAny check on list value")
        void evaluate_containsAny_withListValue() {
            ResolvedVariableContext vars = new ResolvedVariableContext(
                    Map.of("target_blocks", List.of("DIAMOND_ORE", "STONE")), 1.0, 1.0, 1.0);
            VariableCondition condition = new VariableCondition("target_blocks",
                    new VariableCheck.ContainsAny(List.of("DIAMOND_ORE", "EMERALD_ORE")));
            ConditionContext context = new ConditionContext(null, null, null, vars, null, null);
            assertTrue(condition.evaluate(context));
        }

        @Test
        @DisplayName("serializeConfig round-trips ContainsAny check")
        void serializeConfig_roundTrips_containsAny() {
            VariableCondition condition = new VariableCondition("target_blocks",
                    new VariableCheck.ContainsAny(List.of("DIAMOND_ORE", "EMERALD_ORE")));
            Map<String, Object> serialized = condition.serializeConfig();
            assertEquals("target_blocks", serialized.get("name"));
            assertEquals(List.of("DIAMOND_ORE", "EMERALD_ORE"), serialized.get("contains-any"));
        }

        @Test
        @DisplayName("serializeConfig round-trips NumericComparison with greater-than")
        void serializeConfig_roundTrips_greaterThan() {
            VariableCondition condition = new VariableCondition("difficulty",
                    new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN, 5.0));
            Map<String, Object> serialized = condition.serializeConfig();
            assertEquals("difficulty", serialized.get("name"));
            assertEquals(5.0, (double) serialized.get("greater-than"), 1e-9);
        }

        @Test
        @DisplayName("serializeConfig round-trips NumericComparison with less-than")
        void serializeConfig_roundTrips_lessThan() {
            VariableCondition condition = new VariableCondition("difficulty",
                    new VariableCheck.NumericComparison(ComparisonOperator.LESS_THAN, 3.0));
            Map<String, Object> serialized = condition.serializeConfig();
            assertEquals(3.0, (double) serialized.get("less-than"), 1e-9);
        }

        @Test
        @DisplayName("serializeConfig round-trips NumericComparison with at-least")
        void serializeConfig_roundTrips_atLeast() {
            VariableCondition condition = new VariableCondition("difficulty",
                    new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN_OR_EQUAL, 2.5));
            Map<String, Object> serialized = condition.serializeConfig();
            assertEquals(2.5, (double) serialized.get("at-least"), 1e-9);
        }

        @Test
        @DisplayName("serializeConfig round-trips NumericComparison with at-most")
        void serializeConfig_roundTrips_atMost() {
            VariableCondition condition = new VariableCondition("difficulty",
                    new VariableCheck.NumericComparison(ComparisonOperator.LESS_THAN_OR_EQUAL, 10.0));
            Map<String, Object> serialized = condition.serializeConfig();
            assertEquals(10.0, (double) serialized.get("at-most"), 1e-9);
        }
    }

    @Nested
    @DisplayName("CompoundCondition")
    class CompoundConditionTests {

        @Test
        @DisplayName("ALL mode returns true when all children pass")
        void allMode_returnsTrue_whenAllChildrenPass() {
            Map<String, TemplateCondition> children = new LinkedHashMap<>();
            children.put("chance-always", new ChanceCondition(1.0));
            children.put("rarity-pass", new RarityCondition(NamespacedKey.fromString("mcrpg:common")));

            CompoundCondition compound = new CompoundCondition(children, CompoundCondition.LogicMode.ALL);
            ConditionContext context = new ConditionContext(null, null, new Random(0), null, null, null);
            assertTrue(compound.evaluate(context));
        }

        @Test
        @DisplayName("ALL mode returns false when any child fails")
        void allMode_returnsFalse_whenAnyChildFails() {
            Map<String, TemplateCondition> children = new LinkedHashMap<>();
            children.put("chance-always", new ChanceCondition(1.0));
            children.put("chance-never", new ChanceCondition(0.0));

            CompoundCondition compound = new CompoundCondition(children, CompoundCondition.LogicMode.ALL);
            ConditionContext context = new ConditionContext(null, null, new Random(0), null, null, null);
            assertFalse(compound.evaluate(context));
        }

        @Test
        @DisplayName("ANY mode returns true when at least one child passes")
        void anyMode_returnsTrue_whenAnyChildPasses() {
            Map<String, TemplateCondition> children = new LinkedHashMap<>();
            children.put("chance-never", new ChanceCondition(0.0));
            children.put("chance-always", new ChanceCondition(1.0));

            CompoundCondition compound = new CompoundCondition(children, CompoundCondition.LogicMode.ANY);
            ConditionContext context = new ConditionContext(null, null, new Random(0), null, null, null);
            assertTrue(compound.evaluate(context));
        }

        @Test
        @DisplayName("ANY mode returns false when all children fail")
        void anyMode_returnsFalse_whenAllChildrenFail() {
            Map<String, TemplateCondition> children = new LinkedHashMap<>();
            children.put("chance-never-1", new ChanceCondition(0.0));
            children.put("chance-never-2", new ChanceCondition(0.0));

            CompoundCondition compound = new CompoundCondition(children, CompoundCondition.LogicMode.ANY);
            ConditionContext context = new ConditionContext(null, null, new Random(0), null, null, null);
            assertFalse(compound.evaluate(context));
        }

        @Test
        @DisplayName("constructor throws for empty conditions map")
        void constructor_throws_whenConditionsEmpty() {
            assertThrows(IllegalArgumentException.class,
                    () -> new CompoundCondition(Map.of(), CompoundCondition.LogicMode.ALL));
        }

        @Test
        @DisplayName("getConditions returns unmodifiable map")
        void getConditions_returnsUnmodifiableMap() {
            Map<String, TemplateCondition> children = new LinkedHashMap<>();
            children.put("c1", new ChanceCondition(1.0));

            CompoundCondition compound = new CompoundCondition(children, CompoundCondition.LogicMode.ALL);
            assertThrows(UnsupportedOperationException.class,
                    () -> compound.getConditions().put("new", new ChanceCondition(0.5)));
        }

        @Test
        @DisplayName("serializeConfig uses 'all' key for ALL mode")
        void serializeConfig_usesAllKey_forAllMode() {
            Map<String, TemplateCondition> children = new LinkedHashMap<>();
            children.put("c1", new ChanceCondition(0.5));
            CompoundCondition compound = new CompoundCondition(children, CompoundCondition.LogicMode.ALL);

            Map<String, Object> serialized = compound.serializeConfig();
            assertTrue(serialized.containsKey("all"));
            assertFalse(serialized.containsKey("any"));
        }

        @Test
        @DisplayName("serializeConfig uses 'any' key for ANY mode")
        void serializeConfig_usesAnyKey_forAnyMode() {
            Map<String, TemplateCondition> children = new LinkedHashMap<>();
            children.put("c1", new ChanceCondition(0.5));
            CompoundCondition compound = new CompoundCondition(children, CompoundCondition.LogicMode.ANY);

            Map<String, Object> serialized = compound.serializeConfig();
            assertTrue(serialized.containsKey("any"));
            assertFalse(serialized.containsKey("all"));
        }

        @Test
        @DisplayName("serializeConfig injects type key for each child")
        @SuppressWarnings("unchecked")
        void serializeConfig_injectsTypeKey() {
            Map<String, TemplateCondition> children = new LinkedHashMap<>();
            children.put("chance-check", new ChanceCondition(0.5));
            CompoundCondition compound = new CompoundCondition(children, CompoundCondition.LogicMode.ALL);

            Map<String, Object> serialized = compound.serializeConfig();
            Map<String, Object> allChildren = (Map<String, Object>) serialized.get("all");
            Map<String, Object> childMap = (Map<String, Object>) allChildren.get("chance-check");
            assertEquals("mcrpg:chance", childMap.get("type"));
        }
    }

    @Nested
    @DisplayName("ConditionContext factory methods")
    class ConditionContextFactoryTests {

        @Test
        @DisplayName("forTemplateGeneration populates rarity, registry, random, vars; nulls player fields")
        void forTemplateGeneration_populatesCorrectFields() {
            NamespacedKey rarity = NamespacedKey.fromString("mcrpg:common");
            QuestRarityRegistry registry = new QuestRarityRegistry();
            Random random = new Random();
            ResolvedVariableContext vars = new ResolvedVariableContext(Map.of(), 1.0, 1.0, 1.0);

            ConditionContext ctx = ConditionContext.forTemplateGeneration(rarity, registry, random, vars);
            assertEquals(rarity, ctx.rolledRarity());
            assertEquals(registry, ctx.rarityRegistry());
            assertEquals(random, ctx.random());
            assertEquals(vars, ctx.resolvedVariables());
            assertNull(ctx.playerUUID());
            assertNull(ctx.completionHistory());
        }

        @Test
        @DisplayName("forPersonalGeneration populates all fields")
        void forPersonalGeneration_populatesAllFields() {
            NamespacedKey rarity = NamespacedKey.fromString("mcrpg:rare");
            QuestRarityRegistry registry = new QuestRarityRegistry();
            Random random = new Random();
            ResolvedVariableContext vars = new ResolvedVariableContext(Map.of(), 1.0, 1.0, 1.0);
            UUID uuid = UUID.randomUUID();
            QuestCompletionHistory history = (u, c, r) -> 0;

            ConditionContext ctx = ConditionContext.forPersonalGeneration(
                    rarity, registry, random, vars, uuid, history);
            assertEquals(rarity, ctx.rolledRarity());
            assertEquals(registry, ctx.rarityRegistry());
            assertEquals(random, ctx.random());
            assertEquals(vars, ctx.resolvedVariables());
            assertEquals(uuid, ctx.playerUUID());
            assertEquals(history, ctx.completionHistory());
        }

        @Test
        @DisplayName("forPrerequisiteCheck populates player and history only")
        void forPrerequisiteCheck_populatesPlayerAndHistory() {
            UUID uuid = UUID.randomUUID();
            QuestCompletionHistory history = (u, c, r) -> 0;

            ConditionContext ctx = ConditionContext.forPrerequisiteCheck(uuid, history);
            assertNull(ctx.rolledRarity());
            assertNull(ctx.rarityRegistry());
            assertNull(ctx.random());
            assertNull(ctx.resolvedVariables());
            assertEquals(uuid, ctx.playerUUID());
            assertEquals(history, ctx.completionHistory());
        }

        @Test
        @DisplayName("forRewardGrant populates player, rarity, and registry")
        void forRewardGrant_populatesPlayerRarityRegistry() {
            UUID uuid = UUID.randomUUID();
            NamespacedKey rarity = NamespacedKey.fromString("mcrpg:legendary");
            QuestRarityRegistry registry = new QuestRarityRegistry();

            ConditionContext ctx = ConditionContext.forRewardGrant(uuid, rarity, registry);
            assertEquals(rarity, ctx.rolledRarity());
            assertEquals(registry, ctx.rarityRegistry());
            assertNull(ctx.random());
            assertNull(ctx.resolvedVariables());
            assertEquals(uuid, ctx.playerUUID());
            assertNull(ctx.completionHistory());
        }
    }
}
