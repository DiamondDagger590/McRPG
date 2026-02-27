package us.eunoians.mcrpg.quest.board.template.condition;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TemplateConditionRegistryTest {

    private TemplateConditionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new TemplateConditionRegistry();
    }

    @Test
    @DisplayName("register and retrieve built-in condition by key")
    void registerAndRetrieve() {
        RarityCondition condition = new RarityCondition(NamespacedKey.fromString("mcrpg:rare"));
        registry.register(condition);

        Optional<TemplateCondition> retrieved = registry.get(condition.getKey());
        assertTrue(retrieved.isPresent());
        assertSame(condition, retrieved.get());
    }

    @Test
    @DisplayName("unregistered key returns empty")
    void unregisteredKeyReturnsEmpty() {
        Optional<TemplateCondition> result = registry.get(NamespacedKey.fromString("mcrpg:nonexistent"));
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("duplicate registration throws")
    void duplicateRegistrationThrows() {
        RarityCondition condition = new RarityCondition(NamespacedKey.fromString("mcrpg:rare"));
        registry.register(condition);
        assertThrows(IllegalStateException.class, () -> registry.register(condition));
    }

    @Test
    @DisplayName("register third-party condition type")
    void thirdPartyCondition() {
        NamespacedKey key = NamespacedKey.fromString("myplugin:custom_check");
        TemplateCondition custom = new TemplateCondition() {
            @NotNull @Override public NamespacedKey getKey() { return key; }
            @Override public boolean evaluate(@NotNull ConditionContext context) { return true; }
            @NotNull @Override public TemplateCondition fromConfig(@NotNull Section section) { return this; }
            @NotNull @Override public Optional<NamespacedKey> getExpansionKey() { return Optional.empty(); }
        };

        registry.register(custom);
        assertTrue(registry.get(key).isPresent());
        assertTrue(registry.registered(custom));
    }

    @Test
    @DisplayName("getAll returns all registered conditions")
    void getAllReturnsAll() {
        registry.register(new RarityCondition(NamespacedKey.fromString("mcrpg:rare")));
        registry.register(new ChanceCondition(0.5));
        assertEquals(2, registry.getAll().size());
    }

    @Test
    @DisplayName("getRegisteredKeys returns all keys")
    void getRegisteredKeys() {
        RarityCondition rarity = new RarityCondition(NamespacedKey.fromString("mcrpg:rare"));
        ChanceCondition chance = new ChanceCondition(0.5);
        registry.register(rarity);
        registry.register(chance);
        assertEquals(2, registry.getRegisteredKeys().size());
        assertTrue(registry.getRegisteredKeys().contains(rarity.getKey()));
        assertTrue(registry.getRegisteredKeys().contains(chance.getKey()));
    }
}
