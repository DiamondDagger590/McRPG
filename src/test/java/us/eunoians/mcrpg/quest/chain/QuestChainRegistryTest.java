package us.eunoians.mcrpg.quest.chain;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link QuestChainRegistry}.
 */
public class QuestChainRegistryTest extends McRPGBaseTest {

    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("test", "chain_one");
    private static final NamespacedKey CHAIN_KEY_2 = new NamespacedKey("test", "chain_two");
    private static final NamespacedKey TRIGGER_KEY = new NamespacedKey("test", "trigger_a");
    private static final NamespacedKey TRIGGER_KEY_2 = new NamespacedKey("test", "trigger_b");
    private static final NamespacedKey QUEST_KEY_1 = new NamespacedKey("test", "quest_one");
    private static final NamespacedKey QUEST_KEY_2 = new NamespacedKey("test", "quest_two");
    private static final NamespacedKey SOURCE_KEY = new NamespacedKey("test", "source");

    private QuestChainRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new QuestChainRegistry();
    }

    /**
     * Builds a minimal valid {@link QuestChainDefinition} for a given chain key and trigger.
     */
    private QuestChainDefinition buildChain(NamespacedKey chainKey, NamespacedKey triggerKey,
                                             NamespacedKey... questKeys) {
        List<QuestChainStep> steps = new java.util.ArrayList<>();
        for (NamespacedKey questKey : questKeys) {
            steps.add(QuestChainStep.simple(questKey));
        }
        if (steps.isEmpty()) {
            steps.add(QuestChainStep.simple(new NamespacedKey("test", "default_quest")));
        }
        return new QuestChainDefinition.Builder(chainKey, SOURCE_KEY, triggerKey, steps).build();
    }

    @Test
    void register_addsDefinitionAndIsRetrivedById() {
        var definition = buildChain(CHAIN_KEY, TRIGGER_KEY, QUEST_KEY_1);
        registry.register(definition);

        var result = registry.get(CHAIN_KEY);
        assertTrue(result.isPresent());
        assertEquals(definition, result.get());
    }

    @Test
    void get_unknownKey_returnsEmpty() {
        assertTrue(registry.get(CHAIN_KEY).isEmpty());
    }

    @Test
    void register_duplicateKey_throwsIllegalState() {
        var def1 = buildChain(CHAIN_KEY, TRIGGER_KEY, QUEST_KEY_1);
        var def2 = buildChain(CHAIN_KEY, TRIGGER_KEY_2, QUEST_KEY_2);
        registry.register(def1);
        assertThrows(IllegalStateException.class, () -> registry.register(def2));
    }

    @Test
    void registered_returnsTrueForKnownDefinition() {
        var definition = buildChain(CHAIN_KEY, TRIGGER_KEY, QUEST_KEY_1);
        registry.register(definition);
        assertTrue(registry.registered(definition));
    }

    @Test
    void registered_returnsFalseForUnknownDefinition() {
        var definition = buildChain(CHAIN_KEY, TRIGGER_KEY, QUEST_KEY_1);
        assertFalse(registry.registered(definition));
    }

    @Test
    void getChainsForTrigger_returnsMatchingChains() {
        var def1 = buildChain(CHAIN_KEY, TRIGGER_KEY, QUEST_KEY_1);
        var def2 = buildChain(CHAIN_KEY_2, TRIGGER_KEY, QUEST_KEY_2);
        registry.register(def1);
        registry.register(def2);

        List<QuestChainDefinition> result = registry.getChainsForTrigger(TRIGGER_KEY);
        assertEquals(2, result.size());
        assertTrue(result.contains(def1));
        assertTrue(result.contains(def2));
    }

    @Test
    void getChainsForTrigger_unknownTrigger_returnsEmptyList() {
        assertTrue(registry.getChainsForTrigger(TRIGGER_KEY).isEmpty());
    }

    @Test
    void getChainsForTrigger_separatesTriggers() {
        var def1 = buildChain(CHAIN_KEY, TRIGGER_KEY, QUEST_KEY_1);
        var def2 = buildChain(CHAIN_KEY_2, TRIGGER_KEY_2, QUEST_KEY_2);
        registry.register(def1);
        registry.register(def2);

        assertEquals(List.of(def1), registry.getChainsForTrigger(TRIGGER_KEY));
        assertEquals(List.of(def2), registry.getChainsForTrigger(TRIGGER_KEY_2));
    }

    @Test
    void allChains_returnsAllRegistered() {
        var def1 = buildChain(CHAIN_KEY, TRIGGER_KEY, QUEST_KEY_1);
        var def2 = buildChain(CHAIN_KEY_2, TRIGGER_KEY_2, QUEST_KEY_2);
        registry.register(def1);
        registry.register(def2);

        var all = registry.allChains();
        assertEquals(2, all.size());
        assertTrue(all.contains(def1));
        assertTrue(all.contains(def2));
    }

    @Test
    void clear_removesAllDefinitionsAndTriggerIndex() {
        var def1 = buildChain(CHAIN_KEY, TRIGGER_KEY, QUEST_KEY_1);
        registry.register(def1);
        registry.clear();

        assertTrue(registry.get(CHAIN_KEY).isEmpty());
        assertTrue(registry.getChainsForTrigger(TRIGGER_KEY).isEmpty());
        assertTrue(registry.allChains().isEmpty());
    }

    @Test
    void clear_thenReRegisterSameKey_succeeds() {
        var def1 = buildChain(CHAIN_KEY, TRIGGER_KEY, QUEST_KEY_1);
        registry.register(def1);
        registry.clear();

        // After clear, re-registering same key should not throw
        var def2 = buildChain(CHAIN_KEY, TRIGGER_KEY_2, QUEST_KEY_2);
        registry.register(def2);
        assertTrue(registry.get(CHAIN_KEY).isPresent());
    }
}
