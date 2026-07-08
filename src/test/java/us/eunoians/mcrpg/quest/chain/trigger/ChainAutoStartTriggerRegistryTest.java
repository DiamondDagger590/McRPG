package us.eunoians.mcrpg.quest.chain.trigger;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.trigger.builtin.FirstJoinChainAutoStartTrigger;
import us.eunoians.mcrpg.quest.chain.trigger.builtin.ManualChainAutoStartTrigger;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ChainAutoStartTriggerRegistry}.
 */
public class ChainAutoStartTriggerRegistryTest extends McRPGBaseTest {

    private ChainAutoStartTriggerRegistry registry;

    @BeforeEach
    void setup() {
        registry = new ChainAutoStartTriggerRegistry();
    }

    @Test
    @DisplayName("Given a trigger, When registered, Then it can be retrieved by key")
    void register_trigger_canBeRetrieved() {
        ManualChainAutoStartTrigger trigger = new ManualChainAutoStartTrigger();
        registry.register(trigger);

        Optional<ChainAutoStartTrigger> result = registry.get(ManualChainAutoStartTrigger.KEY);
        assertTrue(result.isPresent());
        assertEquals(trigger, result.get());
    }

    @Test
    @DisplayName("Given duplicate trigger key, When registered twice, Then IllegalStateException is thrown")
    void register_duplicateKey_throws() {
        registry.register(new ManualChainAutoStartTrigger());
        assertThrows(IllegalStateException.class, () -> registry.register(new ManualChainAutoStartTrigger()));
    }

    @Test
    @DisplayName("Given unregistered key, When queried, Then empty Optional is returned")
    void get_unknownKey_returnsEmpty() {
        NamespacedKey unknown = new NamespacedKey("mcrpg", "nonexistent");
        Optional<ChainAutoStartTrigger> result = registry.get(unknown);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Given a registered trigger, When registered() is called, Then true is returned")
    void registered_knownTrigger_returnsTrue() {
        ManualChainAutoStartTrigger trigger = new ManualChainAutoStartTrigger();
        registry.register(trigger);
        assertTrue(registry.registered(trigger));
    }

    @Test
    @DisplayName("Given an unregistered trigger, When registered() is called, Then false is returned")
    void registered_unknownTrigger_returnsFalse() {
        assertFalse(registry.registered(new ManualChainAutoStartTrigger()));
    }

    @Test
    @DisplayName("Given two registered triggers, When allTriggers() is queried, Then both are returned")
    void allTriggers_twoRegistered_returnsBoth() {
        registry.register(new ManualChainAutoStartTrigger());
        registry.register(new FirstJoinChainAutoStartTrigger());
        assertEquals(2, registry.allTriggers().size());
    }

    @Test
    @DisplayName("Given ManualChainAutoStartTrigger, When getKey() is called, Then mcrpg:manual is returned")
    void manualTrigger_getKey_returnsCorrectKey() {
        assertEquals(new NamespacedKey("mcrpg", "manual"), new ManualChainAutoStartTrigger().getKey());
    }

    @Test
    @DisplayName("Given FirstJoinChainAutoStartTrigger, When getKey() is called, Then mcrpg:first_join is returned")
    void firstJoinTrigger_getKey_returnsCorrectKey() {
        assertEquals(new NamespacedKey("mcrpg", "first_join"), new FirstJoinChainAutoStartTrigger().getKey());
    }

    @Test
    @DisplayName("Given ManualChainAutoStartTrigger, When getExpansionKey() is called, Then McRPGExpansion key is returned")
    void manualTrigger_getExpansionKey_returnsExpansionKey() {
        assertTrue(new ManualChainAutoStartTrigger().getExpansionKey().isPresent());
    }

    @Test
    @DisplayName("Given FirstJoinChainAutoStartTrigger, When getExpansionKey() is called, Then McRPGExpansion key is returned")
    void firstJoinTrigger_getExpansionKey_returnsExpansionKey() {
        assertTrue(new FirstJoinChainAutoStartTrigger().getExpansionKey().isPresent());
    }
}
