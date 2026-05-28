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

    @DisplayName("Given a trigger, When registered, Then it can be retrieved by key")
    @Test
    void register_trigger_canBeRetrieved() {
        ManualChainAutoStartTrigger trigger = new ManualChainAutoStartTrigger();
        registry.register(trigger);

        Optional<ChainAutoStartTrigger> result = registry.get(ManualChainAutoStartTrigger.KEY);
        assertTrue(result.isPresent());
        assertEquals(trigger, result.get());
    }

    @DisplayName("Given duplicate trigger key, When registered twice, Then IllegalStateException is thrown")
    @Test
    void register_duplicateKey_throws() {
        registry.register(new ManualChainAutoStartTrigger());
        assertThrows(IllegalStateException.class, () -> registry.register(new ManualChainAutoStartTrigger()));
    }

    @DisplayName("Given unregistered key, When queried, Then empty Optional is returned")
    @Test
    void get_unknownKey_returnsEmpty() {
        NamespacedKey unknown = new NamespacedKey("mcrpg", "nonexistent");
        Optional<ChainAutoStartTrigger> result = registry.get(unknown);
        assertFalse(result.isPresent());
    }

    @DisplayName("Given a registered trigger, When registered() is called, Then true is returned")
    @Test
    void registered_knownTrigger_returnsTrue() {
        ManualChainAutoStartTrigger trigger = new ManualChainAutoStartTrigger();
        registry.register(trigger);
        assertTrue(registry.registered(trigger));
    }

    @DisplayName("Given an unregistered trigger, When registered() is called, Then false is returned")
    @Test
    void registered_unknownTrigger_returnsFalse() {
        assertFalse(registry.registered(new ManualChainAutoStartTrigger()));
    }

    @DisplayName("Given two registered triggers, When allTriggers() is queried, Then both are returned")
    @Test
    void allTriggers_twoRegistered_returnsBoth() {
        registry.register(new ManualChainAutoStartTrigger());
        registry.register(new FirstJoinChainAutoStartTrigger());
        assertEquals(2, registry.allTriggers().size());
    }

    @DisplayName("Given ManualChainAutoStartTrigger, When getKey() is called, Then mcrpg:manual is returned")
    @Test
    void manualTrigger_getKey_returnsCorrectKey() {
        assertEquals(new NamespacedKey("mcrpg", "manual"), new ManualChainAutoStartTrigger().getKey());
    }

    @DisplayName("Given FirstJoinChainAutoStartTrigger, When getKey() is called, Then mcrpg:first_join is returned")
    @Test
    void firstJoinTrigger_getKey_returnsCorrectKey() {
        assertEquals(new NamespacedKey("mcrpg", "first_join"), new FirstJoinChainAutoStartTrigger().getKey());
    }

    @DisplayName("Given ManualChainAutoStartTrigger, When getExpansionKey() is called, Then McRPGExpansion key is returned")
    @Test
    void manualTrigger_getExpansionKey_returnsExpansionKey() {
        assertTrue(new ManualChainAutoStartTrigger().getExpansionKey().isPresent());
    }

    @DisplayName("Given FirstJoinChainAutoStartTrigger, When getExpansionKey() is called, Then McRPGExpansion key is returned")
    @Test
    void firstJoinTrigger_getExpansionKey_returnsExpansionKey() {
        assertTrue(new FirstJoinChainAutoStartTrigger().getExpansionKey().isPresent());
    }
}
