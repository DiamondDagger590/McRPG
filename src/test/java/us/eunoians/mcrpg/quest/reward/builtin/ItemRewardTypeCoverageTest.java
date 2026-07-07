package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemRewardTypeCoverageTest extends McRPGBaseTest {

    private ItemRewardType baseType;

    @BeforeEach
    void setUp() {
        baseType = new ItemRewardType();
    }

    @Nested
    @DisplayName("Key and expansion")
    class KeyAndExpansion {

        @Test
        @DisplayName("getKey returns item key")
        void getKey_returnsItemKey() {
            assertEquals(ItemRewardType.KEY, baseType.getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPGExpansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            assertTrue(baseType.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, baseType.getExpansionKey().orElseThrow());
        }
    }

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("numeric amount is zero")
        void getNumericAmount_returnsZero() {
            assertEquals(0L, baseType.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("describeForDisplay returns formatted default")
        void describeForDisplay_returnsDefault() {
            assertNotNull(baseType.describeForDisplay());
        }
    }

    @Nested
    @DisplayName("fromSerializedConfig")
    class FromSerializedConfig {

        @Test
        @DisplayName("parses item config and top-level amount")
        void parsesItemConfigAndAmount() {
            Map<String, Object> config = Map.of(
                    "item", Map.of("material", "DIAMOND"),
                    "amount", 5
            );
            QuestRewardType configured = baseType.fromSerializedConfig(config);
            assertEquals(5L, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("falls back to nested amount when top-level absent")
        void fallsBackToNestedAmount() {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("material", "DIAMOND");
            itemMap.put("amount", 3);
            Map<String, Object> config = Map.of("item", itemMap);
            QuestRewardType configured = baseType.fromSerializedConfig(config);
            assertEquals(3L, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("defaults amount to 1 when absent from both levels")
        void defaultsAmountToOne() {
            Map<String, Object> config = Map.of(
                    "item", Map.of("material", "DIAMOND")
            );
            QuestRewardType configured = baseType.fromSerializedConfig(config);
            assertEquals(1L, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("parses display label from config")
        void parsesDisplayLabel() {
            Map<String, Object> config = Map.of(
                    "item", Map.of("material", "DIAMOND"),
                    "amount", 1,
                    "display", "Shiny Gem"
            );
            QuestRewardType configured = baseType.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("Shiny Gem", serialized.get("display"));
        }

        @Test
        @DisplayName("parses localization route from config")
        void parsesLocalizationRoute() {
            Map<String, Object> config = Map.of(
                    "item", Map.of("material", "DIAMOND"),
                    "amount", 1,
                    "localization-route", "quests.mcrpg.rewards.gem"
            );
            QuestRewardType configured = baseType.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("quests.mcrpg.rewards.gem", serialized.get("localization-route"));
        }

        @Test
        @DisplayName("handles non-Map item value gracefully")
        void handlesNonMapItemGracefully() {
            Map<String, Object> config = Map.of(
                    "item", "not-a-map",
                    "amount", 1
            );
            QuestRewardType configured = baseType.fromSerializedConfig(config);
            assertEquals(1L, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("handles missing item key")
        void handlesMissingItemKey() {
            Map<String, Object> config = Map.of("amount", 2);
            QuestRewardType configured = baseType.fromSerializedConfig(config);
            assertEquals(2L, configured.getNumericAmount().orElse(-1));
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("round-trips item config and amount")
        void roundTripsItemConfigAndAmount() {
            Map<String, Object> config = Map.of(
                    "item", Map.of("material", "GOLD_INGOT"),
                    "amount", 10
            );
            QuestRewardType configured = baseType.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals(10, serialized.get("amount"));
            @SuppressWarnings("unchecked")
            Map<String, Object> itemMap = (Map<String, Object>) serialized.get("item");
            assertEquals("GOLD_INGOT", itemMap.get("material"));
        }

        @Test
        @DisplayName("omits display label when empty")
        void omitsDisplayLabelWhenEmpty() {
            Map<String, Object> config = Map.of(
                    "item", Map.of("material", "DIAMOND"),
                    "amount", 1
            );
            QuestRewardType configured = baseType.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertNull(serialized.get("display"));
        }

        @Test
        @DisplayName("omits localization route when null")
        void omitsLocalizationRouteWhenNull() {
            Map<String, Object> config = Map.of(
                    "item", Map.of("material", "DIAMOND"),
                    "amount", 1
            );
            QuestRewardType configured = baseType.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertNull(serialized.get("localization-route"));
        }

        @Test
        @DisplayName("includes display label when set")
        void includesDisplayLabelWhenSet() {
            Map<String, Object> config = Map.of(
                    "item", Map.of("material", "DIAMOND"),
                    "amount", 1,
                    "display", "Pretty Diamond"
            );
            QuestRewardType configured = baseType.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("Pretty Diamond", serialized.get("display"));
        }

        @Test
        @DisplayName("full round-trip preserves all fields")
        void fullRoundTrip() {
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("item", Map.of("material", "EMERALD"));
            config.put("amount", 7);
            config.put("display", "Green Gem");
            config.put("localization-route", "quests.mcrpg.rewards.emerald");

            QuestRewardType first = baseType.fromSerializedConfig(config);
            Map<String, Object> serialized = first.serializeConfig();
            QuestRewardType second = baseType.fromSerializedConfig(serialized);
            Map<String, Object> reSerialized = second.serializeConfig();

            assertEquals(serialized.get("amount"), reSerialized.get("amount"));
            assertEquals(serialized.get("display"), reSerialized.get("display"));
            assertEquals(serialized.get("localization-route"), reSerialized.get("localization-route"));
        }
    }

    @Nested
    @DisplayName("withAmountMultiplier")
    class WithAmountMultiplier {

        @Test
        @DisplayName("scales amount correctly")
        void scalesAmount() {
            QuestRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "item", Map.of("material", "DIAMOND"),
                    "amount", 10
            ));
            QuestRewardType scaled = configured.withAmountMultiplier(0.5);
            assertEquals(5L, scaled.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("enforces minimum of 1")
        void enforcesMinimumOfOne() {
            QuestRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "item", Map.of("material", "DIAMOND"),
                    "amount", 10
            ));
            QuestRewardType scaled = configured.withAmountMultiplier(0.001);
            assertEquals(1L, scaled.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("returns new instance")
        void returnsNewInstance() {
            QuestRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "item", Map.of("material", "DIAMOND"),
                    "amount", 10
            ));
            QuestRewardType scaled = configured.withAmountMultiplier(2.0);
            assertNotSame(configured, scaled);
            assertEquals(10L, configured.getNumericAmount().orElse(-1));
            assertEquals(20L, scaled.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("preserves localization route")
        void preservesLocalizationRoute() {
            Route route = Route.fromString("quests.mcrpg.rewards.gem");
            QuestRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "item", Map.of("material", "DIAMOND"),
                    "amount", 10
            )).withLocalizationRoute(route);
            QuestRewardType scaled = configured.withAmountMultiplier(0.5);
            assertEquals("quests.mcrpg.rewards.gem",
                    scaled.serializeConfig().get("localization-route"));
        }
    }

    @Nested
    @DisplayName("withLocalizationRoute")
    class WithLocalizationRoute {

        @Test
        @DisplayName("returns new instance with route set")
        void returnsNewInstanceWithRoute() {
            Route route = Route.fromString("quests.mcrpg.rewards.gem");
            QuestRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "item", Map.of("material", "DIAMOND"),
                    "amount", 1
            ));
            QuestRewardType withRoute = configured.withLocalizationRoute(route);

            assertNotSame(configured, withRoute);
            assertEquals("quests.mcrpg.rewards.gem",
                    withRoute.serializeConfig().get("localization-route"));
        }
    }

    @Nested
    @DisplayName("withInlineDisplayLabel")
    class WithInlineDisplayLabel {

        @Test
        @DisplayName("returns new instance with label set")
        void returnsNewInstanceWithLabel() {
            QuestRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "item", Map.of("material", "DIAMOND"),
                    "amount", 1
            ));
            QuestRewardType withLabel = configured.withInlineDisplayLabel("Sparkle Diamond");

            assertNotSame(configured, withLabel);
            assertEquals("Sparkle Diamond",
                    withLabel.serializeConfig().get("display"));
        }
    }

    @Nested
    @DisplayName("describeForDisplay (no-arg)")
    class DescribeForDisplay {

        @Test
        @DisplayName("formats material name with amount prefix")
        void formatsMaterialName() {
            QuestRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "item", Map.of("material", "DIAMOND"),
                    "amount", 3
            ));
            assertEquals("3x Diamond", configured.describeForDisplay());
        }

        @Test
        @DisplayName("handles underscore-separated material names")
        void handlesUnderscoreSeparatedNames() {
            QuestRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "item", Map.of("material", "GOLD_INGOT"),
                    "amount", 5
            ));
            assertEquals("5x Gold ingot", configured.describeForDisplay());
        }

        @Test
        @DisplayName("defaults to Item when material absent")
        void defaultsToItemWhenMaterialAbsent() {
            QuestRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "item", Map.of(),
                    "amount", 1
            ));
            assertEquals("1x Item", configured.describeForDisplay());
        }
    }

    @Nested
    @DisplayName("getNumericAmount")
    class GetNumericAmount {

        @Test
        @DisplayName("returns configured amount")
        void returnsConfiguredAmount() {
            QuestRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "item", Map.of("material", "DIAMOND"),
                    "amount", 42
            ));
            OptionalLong amount = configured.getNumericAmount();
            assertTrue(amount.isPresent());
            assertEquals(42L, amount.getAsLong());
        }
    }

    @Nested
    @DisplayName("grant")
    class Grant {

        @Test
        @DisplayName("grants item to player inventory")
        void grantsItemToPlayer() {
            QuestRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "item", Map.of("material", "DIAMOND"),
                    "amount", 3
            ));
            PlayerMock player = server.addPlayer();
            assertDoesNotThrow(() -> configured.grant(player));

            boolean hasDiamonds = false;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == Material.DIAMOND) {
                    hasDiamonds = true;
                    assertEquals(3, item.getAmount());
                    break;
                }
            }
            assertTrue(hasDiamonds, "Player should have diamonds in inventory");
        }

        @Test
        @DisplayName("does nothing with empty item config")
        void doesNothingWithEmptyConfig() {
            PlayerMock player = server.addPlayer();
            assertDoesNotThrow(() -> baseType.grant(player));
        }

        @Test
        @DisplayName("does nothing with zero amount")
        void doesNothingWithZeroAmount() {
            QuestRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "item", Map.of("material", "DIAMOND"),
                    "amount", 0
            ));
            PlayerMock player = server.addPlayer();
            assertDoesNotThrow(() -> configured.grant(player));
        }

        @Test
        @DisplayName("handles nested item config with enchantments")
        void handlesNestedConfig() {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("material", "DIAMOND_SWORD");
            Map<String, Object> config = Map.of(
                    "item", itemMap,
                    "amount", 1
            );
            QuestRewardType configured = baseType.fromSerializedConfig(config);
            PlayerMock player = server.addPlayer();
            assertDoesNotThrow(() -> configured.grant(player));
        }
    }
}
