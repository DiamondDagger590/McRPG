package us.eunoians.mcrpg.quest.board.rarity;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestRarityRegistryTest extends McRPGBaseTest {

    private static final NamespacedKey MCRPG_EXPANSION = new NamespacedKey("mcrpg", "mcrpg");

    private QuestRarityRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new QuestRarityRegistry();
    }

    @DisplayName("register and get by key returns registered rarity")
    @Test
    void registerAndRetrieveByKey() {
        var common = new QuestRarity(new NamespacedKey("mcrpg", "common"), 60, 1.0, 1.0, MCRPG_EXPANSION);
        registry.register(common);

        var retrieved = registry.get(new NamespacedKey("mcrpg", "common"));
        assertTrue(retrieved.isPresent());
        assertSame(common, retrieved.get());
    }

    @DisplayName("rollRarity with single rarity always returns it")
    @Test
    void rollRaritySingleRarity() {
        var common = new QuestRarity(new NamespacedKey("mcrpg", "common"), 60, 1.0, 1.0, MCRPG_EXPANSION);
        registry.register(common);

        for (int i = 0; i < 10; i++) {
            assertSame(common, registry.rollRarity(new Random()));
        }
    }

    @DisplayName("rollRarity with seeded Random is deterministic")
    @Test
    void rollRaritySeededDeterministic() {
        var common = new QuestRarity(new NamespacedKey("mcrpg", "common"), 60, 1.0, 1.0, MCRPG_EXPANSION);
        var rare = new QuestRarity(new NamespacedKey("mcrpg", "rare"), 10, 1.0, 1.0, MCRPG_EXPANSION);
        registry.register(common);
        registry.register(rare);

        var result1 = registry.rollRarity(new Random(42));
        var result2 = registry.rollRarity(new Random(42));
        assertEquals(result1, result2);
    }

    @DisplayName("rollRarity on empty registry throws IllegalStateException")
    @Test
    void rollRarityEmptyThrows() {
        assertThrows(IllegalStateException.class, () -> registry.rollRarity(new Random()));
    }

    @DisplayName("replaceConfigRarities replaces config rarities, expansion rarities untouched")
    @Test
    void replaceConfigRaritiesPreservesExpansionRarities() {
        var expansionRarity = new QuestRarity(new NamespacedKey("mcrpg", "expansion_only"), 50, 1.0, 1.0, MCRPG_EXPANSION);
        registry.register(expansionRarity);

        var common = new QuestRarity(new NamespacedKey("mcrpg", "common"), 60, 1.0, 1.0, MCRPG_EXPANSION);
        var rare = new QuestRarity(new NamespacedKey("mcrpg", "rare"), 10, 1.0, 1.0, MCRPG_EXPANSION);
        registry.replaceConfigRarities(Map.of(
                new NamespacedKey("mcrpg", "common"), common,
                new NamespacedKey("mcrpg", "rare"), rare
        ));

        assertTrue(registry.get(new NamespacedKey("mcrpg", "expansion_only")).isPresent());
        assertTrue(registry.get(new NamespacedKey("mcrpg", "common")).isPresent());
        assertTrue(registry.get(new NamespacedKey("mcrpg", "rare")).isPresent());

        var uncommon = new QuestRarity(new NamespacedKey("mcrpg", "uncommon"), 30, 1.0, 1.0, MCRPG_EXPANSION);
        registry.replaceConfigRarities(Map.of(
                new NamespacedKey("mcrpg", "uncommon"), uncommon
        ));

        assertTrue(registry.get(new NamespacedKey("mcrpg", "expansion_only")).isPresent());
        assertTrue(registry.get(new NamespacedKey("mcrpg", "uncommon")).isPresent());
        assertTrue(registry.get(new NamespacedKey("mcrpg", "common")).isEmpty());
        assertTrue(registry.get(new NamespacedKey("mcrpg", "rare")).isEmpty());
    }

    @DisplayName("clear empties registry")
    @Test
    void clearEmptiesRegistry() {
        var common = new QuestRarity(new NamespacedKey("mcrpg", "common"), 60, 1.0, 1.0, MCRPG_EXPANSION);
        registry.register(common);
        registry.clear();

        assertTrue(registry.get(new NamespacedKey("mcrpg", "common")).isEmpty());
        assertTrue(registry.getRegisteredKeys().isEmpty());
    }

    @DisplayName("getRegisteredKeys returns all keys")
    @Test
    void getRegisteredKeysReturnsAll() {
        var common = new QuestRarity(new NamespacedKey("mcrpg", "common"), 60, 1.0, 1.0, MCRPG_EXPANSION);
        var rare = new QuestRarity(new NamespacedKey("mcrpg", "rare"), 10, 1.0, 1.0, MCRPG_EXPANSION);
        registry.register(common);
        registry.register(rare);

        var keys = registry.getRegisteredKeys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains(new NamespacedKey("mcrpg", "common")));
        assertTrue(keys.contains(new NamespacedKey("mcrpg", "rare")));
    }
}
