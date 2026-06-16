package us.eunoians.mcrpg.quest.board.rarity;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestRarityCoverageTest extends McRPGBaseTest {

    private static final NamespacedKey EXPANSION_KEY = new NamespacedKey("mcrpg", "mcrpg");
    private static final NamespacedKey COMMON_KEY = new NamespacedKey("mcrpg", "common");
    private static final NamespacedKey RARE_KEY = new NamespacedKey("mcrpg", "rare");
    private static final NamespacedKey LEGENDARY_KEY = new NamespacedKey("mcrpg", "legendary");

    @Nested
    @DisplayName("QuestRarity configureIcon")
    class ConfigureIconTests {

        @Test
        @DisplayName("configureIcon applies section without material key to existing builder")
        void configureIcon_sectionWithoutMaterial_appliesWithoutRecreating() throws IOException {
            YamlDocument doc = YamlDocument.create(new ByteArrayInputStream("display:\n  amount: 1\n".getBytes()));
            Section section = doc.getSection("display");

            QuestRarity rarity = new QuestRarity(COMMON_KEY, 60, 1.0, 1.0, EXPANSION_KEY, section, null);
            ItemBuilder original = ItemBuilder.from(new ItemStack(Material.PAPER));
            ItemBuilder result = rarity.configureIcon(original);

            assertNotNull(result);
        }

        @Test
        @DisplayName("configureIcon with section containing material key recreates builder")
        void configureIcon_sectionWithMaterial_recreatesBuilder() throws IOException {
            YamlDocument doc = YamlDocument.create(
                    new ByteArrayInputStream("display:\n  material: DIAMOND\n".getBytes()));
            Section section = doc.getSection("display");

            QuestRarity rarity = new QuestRarity(COMMON_KEY, 60, 1.0, 1.0, EXPANSION_KEY, section, null);
            ItemBuilder original = ItemBuilder.from(new ItemStack(Material.PAPER));
            ItemBuilder result = rarity.configureIcon(original);

            assertNotNull(result);
            assertNotSame(original, result);
        }

        @Test
        @DisplayName("configureIcon returns same builder when icon section is null")
        void configureIcon_nullSection_returnsSameBuilder() {
            QuestRarity rarity = new QuestRarity(COMMON_KEY, 60, 1.0, 1.0, EXPANSION_KEY);
            ItemBuilder builder = ItemBuilder.from(new ItemStack(Material.PAPER));
            ItemBuilder result = rarity.configureIcon(builder);

            assertSame(builder, result);
        }
    }

    @Nested
    @DisplayName("QuestRarityRegistry advanced coverage")
    class RegistryAdvancedTests {

        @Test
        @DisplayName("registered(QuestRarity) returns true for registered rarity")
        void registered_returnsTrue_whenRegistered() {
            QuestRarityRegistry registry = new QuestRarityRegistry();
            QuestRarity rarity = new QuestRarity(COMMON_KEY, 60, 1.0, 1.0, EXPANSION_KEY);
            registry.register(rarity);

            assertTrue(registry.registered(rarity));
        }

        @Test
        @DisplayName("registered(QuestRarity) returns false for unregistered rarity")
        void registered_returnsFalse_whenNotRegistered() {
            QuestRarityRegistry registry = new QuestRarityRegistry();
            QuestRarity rarity = new QuestRarity(COMMON_KEY, 60, 1.0, 1.0, EXPANSION_KEY);

            assertFalse(registry.registered(rarity));
        }

        @Test
        @DisplayName("getAll returns all registered rarities")
        void getAll_returnsAllRegistered() {
            QuestRarityRegistry registry = new QuestRarityRegistry();
            QuestRarity common = new QuestRarity(COMMON_KEY, 60, 1.0, 1.0, EXPANSION_KEY);
            QuestRarity rare = new QuestRarity(RARE_KEY, 10, 2.0, 2.0, EXPANSION_KEY);
            registry.register(common);
            registry.register(rare);

            assertEquals(2, registry.getAll().size());
        }

        @Test
        @DisplayName("register replaces existing rarity with same key")
        void register_replacesExistingKey() {
            QuestRarityRegistry registry = new QuestRarityRegistry();
            QuestRarity original = new QuestRarity(COMMON_KEY, 60, 1.0, 1.0, EXPANSION_KEY);
            QuestRarity replacement = new QuestRarity(COMMON_KEY, 80, 1.5, 1.5, EXPANSION_KEY);

            registry.register(original);
            registry.register(replacement);

            assertEquals(1, registry.getAll().size());
            assertEquals(80, registry.get(COMMON_KEY).orElseThrow().getWeight());
        }

        @Test
        @DisplayName("rollRarity respects weight distribution across many rolls")
        void rollRarity_respectsWeightDistribution() {
            QuestRarityRegistry registry = new QuestRarityRegistry();
            QuestRarity common = new QuestRarity(COMMON_KEY, 90, 1.0, 1.0, EXPANSION_KEY);
            QuestRarity legendary = new QuestRarity(LEGENDARY_KEY, 10, 3.0, 3.0, EXPANSION_KEY);
            registry.register(common);
            registry.register(legendary);

            int commonCount = 0;
            Random random = new Random(12345);
            for (int i = 0; i < 1000; i++) {
                if (registry.rollRarity(random).getKey().equals(COMMON_KEY)) {
                    commonCount++;
                }
            }
            assertTrue(commonCount > 800, "Common (weight 90) should appear >80% of the time, got " + commonCount);
            assertTrue(commonCount < 980, "Legendary (weight 10) should appear sometimes, common was " + commonCount);
        }

        @Test
        @DisplayName("clear also resets config tracking")
        void clear_resetsConfigTracking() {
            QuestRarityRegistry registry = new QuestRarityRegistry();
            QuestRarity rarity = new QuestRarity(COMMON_KEY, 60, 1.0, 1.0, EXPANSION_KEY);
            registry.register(rarity);
            registry.clear();

            assertTrue(registry.getAll().isEmpty());
            assertTrue(registry.getRegisteredKeys().isEmpty());
        }

        @Test
        @DisplayName("get returns empty for unregistered key")
        void get_returnsEmpty_whenNotRegistered() {
            QuestRarityRegistry registry = new QuestRarityRegistry();
            assertTrue(registry.get(COMMON_KEY).isEmpty());
        }

        @Test
        @DisplayName("getRegisteredKeys returns immutable set")
        void getRegisteredKeys_returnsImmutableSet() {
            QuestRarityRegistry registry = new QuestRarityRegistry();
            QuestRarity rarity = new QuestRarity(COMMON_KEY, 60, 1.0, 1.0, EXPANSION_KEY);
            registry.register(rarity);

            assertThrows(UnsupportedOperationException.class,
                    () -> registry.getRegisteredKeys().add(RARE_KEY));
        }
    }
}
