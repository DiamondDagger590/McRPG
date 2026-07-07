package us.eunoians.mcrpg.quest.board.template.condition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("RarityCondition")
class RarityConditionTest {

    private static final NamespacedKey COMMON = NamespacedKey.fromString("mcrpg:common");
    private static final NamespacedKey RARE = NamespacedKey.fromString("mcrpg:rare");
    private static final NamespacedKey LEGENDARY = NamespacedKey.fromString("mcrpg:legendary");
    private static final NamespacedKey UNKNOWN = NamespacedKey.fromString("mcrpg:unknown");

    private QuestRarityRegistry rarityRegistry;

    @BeforeEach
    void setUp() {
        rarityRegistry = mock(QuestRarityRegistry.class);

        QuestRarity commonRarity = mock(QuestRarity.class);
        QuestRarity rareRarity = mock(QuestRarity.class);
        QuestRarity legendaryRarity = mock(QuestRarity.class);

        when(commonRarity.getWeight()).thenReturn(100);
        when(rareRarity.getWeight()).thenReturn(20);
        when(legendaryRarity.getWeight()).thenReturn(5);

        when(rarityRegistry.get(COMMON)).thenReturn(Optional.of(commonRarity));
        when(rarityRegistry.get(RARE)).thenReturn(Optional.of(rareRarity));
        when(rarityRegistry.get(LEGENDARY)).thenReturn(Optional.of(legendaryRarity));
        when(rarityRegistry.get(UNKNOWN)).thenReturn(Optional.empty());
    }

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("no-arg prototype uses KEY as placeholder rarity")
        void noArgConstructor_usesKeyAsPlaceholder() {
            RarityCondition prototype = new RarityCondition();
            assertEquals(RarityCondition.KEY, prototype.getMinimumRarity());
        }
    }

    @Nested
    @DisplayName("getters")
    class Getters {

        @Test
        @DisplayName("getKey returns mcrpg:rarity_gate")
        void getKey_returnsMcrpgRarityGate() {
            assertEquals(NamespacedKey.fromString("mcrpg:rarity_gate"), new RarityCondition(RARE).getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPGExpansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            Optional<NamespacedKey> key = new RarityCondition(RARE).getExpansionKey();
            assertTrue(key.isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, key.get());
        }

        @Test
        @DisplayName("getMinimumRarity returns configured rarity key")
        void getMinimumRarity_returnsConfiguredKey() {
            assertEquals(RARE, new RarityCondition(RARE).getMinimumRarity());
        }
    }

    @Nested
    @DisplayName("evaluate edge cases")
    class EvaluateEdgeCases {

        @Test
        @DisplayName("unregistered rolled rarity returns false")
        void evaluate_returnsFalse_whenRolledRarityUnregistered() {
            RarityCondition condition = new RarityCondition(RARE);
            ConditionContext ctx = new ConditionContext(UNKNOWN, rarityRegistry, null, null, null, null);
            assertFalse(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("unregistered minimum rarity returns false")
        void evaluate_returnsFalse_whenMinimumRarityUnregistered() {
            RarityCondition condition = new RarityCondition(UNKNOWN);
            ConditionContext ctx = new ConditionContext(COMMON, rarityRegistry, null, null, null, null);
            assertFalse(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("both null fields return true (pass-through)")
        void evaluate_returnsTrue_whenBothFieldsNull() {
            RarityCondition condition = new RarityCondition(RARE);
            ConditionContext ctx = new ConditionContext(null, null, null, null, null, null);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("same rarity as minimum passes (equal weight)")
        void evaluate_returnsTrue_whenWeightsEqual() {
            RarityCondition condition = new RarityCondition(RARE);
            ConditionContext ctx = new ConditionContext(RARE, rarityRegistry, null, null, null, null);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("rarer than minimum passes (lower weight)")
        void evaluate_returnsTrue_whenRolledIsRarer() {
            RarityCondition condition = new RarityCondition(RARE);
            ConditionContext ctx = new ConditionContext(LEGENDARY, rarityRegistry, null, null, null, null);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("less rare than minimum fails (higher weight)")
        void evaluate_returnsFalse_whenRolledIsLessRare() {
            RarityCondition condition = new RarityCondition(RARE);
            ConditionContext ctx = new ConditionContext(COMMON, rarityRegistry, null, null, null, null);
            assertFalse(condition.evaluate(ctx));
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("serializes min-rarity key")
        void serializeConfig_containsMinRarityKey() {
            Map<String, Object> config = new RarityCondition(RARE).serializeConfig();
            assertEquals("mcrpg:rare", config.get("min-rarity"));
        }

        @Test
        @DisplayName("serialized map has exactly one entry")
        void serializeConfig_hasOneEntry() {
            Map<String, Object> config = new RarityCondition(RARE).serializeConfig();
            assertEquals(1, config.size());
        }
    }
}
