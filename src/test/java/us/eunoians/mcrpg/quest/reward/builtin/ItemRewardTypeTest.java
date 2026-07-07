package us.eunoians.mcrpg.quest.reward.builtin;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemRewardTypeTest extends McRPGBaseTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("getKey returns mcrpg:item")
        void getKey_returnsItemKey() {
            ItemRewardType type = new ItemRewardType();
            assertEquals(new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "item"), type.getKey());
        }

        @Test
        @DisplayName("getNumericAmount returns zero")
        void getNumericAmount_returnsZero() {
            ItemRewardType type = new ItemRewardType();
            OptionalLong amount = type.getNumericAmount();
            assertTrue(amount.isPresent());
            assertEquals(0, amount.getAsLong());
        }

        @Test
        @DisplayName("describeForDisplay returns '0x Item' for empty config")
        void describeForDisplay_emptyConfig() {
            ItemRewardType type = new ItemRewardType();
            assertEquals("0x Item", type.describeForDisplay());
        }
    }

    @Nested
    @DisplayName("serializeConfig and fromSerializedConfig")
    class SerializationRoundTrip {

        @Test
        @DisplayName("Round-trip preserves item config and amount")
        void roundTrip_preservesData() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            Map<String, Object> itemConfig = new LinkedHashMap<>();
            itemConfig.put("material", "DIAMOND");
            serialized.put("item", itemConfig);
            serialized.put("amount", 5);

            ItemRewardType base = new ItemRewardType();
            QuestRewardType configured = base.fromSerializedConfig(serialized);

            Map<String, Object> result = configured.serializeConfig();
            @SuppressWarnings("unchecked")
            Map<String, Object> resultItem = (Map<String, Object>) result.get("item");

            assertEquals("DIAMOND", resultItem.get("material"));
            assertEquals(5, result.get("amount"));
        }

        @Test
        @DisplayName("Missing amount defaults to 1")
        void fromSerializedConfig_defaultsToOne_whenAmountMissing() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            serialized.put("item", Map.of("material", "STONE"));

            ItemRewardType base = new ItemRewardType();
            QuestRewardType configured = base.fromSerializedConfig(serialized);

            assertEquals(OptionalLong.of(1), configured.getNumericAmount());
        }

        @Test
        @DisplayName("Amount inside item section is used when no top-level amount")
        void fromSerializedConfig_usesNestedAmount_whenNoTopLevel() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            Map<String, Object> itemConfig = new LinkedHashMap<>();
            itemConfig.put("material", "IRON_INGOT");
            itemConfig.put("amount", 10);
            serialized.put("item", itemConfig);

            ItemRewardType base = new ItemRewardType();
            QuestRewardType configured = base.fromSerializedConfig(serialized);

            assertEquals(OptionalLong.of(10), configured.getNumericAmount());
        }

        @Test
        @DisplayName("Top-level amount takes precedence over nested amount")
        void fromSerializedConfig_usesTopLevelAmount_whenBothPresent() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            Map<String, Object> itemConfig = new LinkedHashMap<>();
            itemConfig.put("material", "GOLD_INGOT");
            itemConfig.put("amount", 99);
            serialized.put("item", itemConfig);
            serialized.put("amount", 3);

            ItemRewardType base = new ItemRewardType();
            QuestRewardType configured = base.fromSerializedConfig(serialized);

            assertEquals(OptionalLong.of(3), configured.getNumericAmount());
        }

        @Test
        @DisplayName("Display label is preserved in round-trip")
        void displayLabel_preservedInRoundTrip() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            serialized.put("item", Map.of("material", "EMERALD"));
            serialized.put("amount", 1);
            serialized.put("display", "Shiny Emerald");

            ItemRewardType base = new ItemRewardType();
            QuestRewardType configured = base.fromSerializedConfig(serialized);
            Map<String, Object> result = configured.serializeConfig();

            assertEquals("Shiny Emerald", result.get("display"));
        }

        @Test
        @DisplayName("Localization route is preserved in round-trip")
        void localizationRoute_preservedInRoundTrip() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            serialized.put("item", Map.of("material", "EMERALD"));
            serialized.put("amount", 1);
            serialized.put("localization-route", "quests.mcrpg.test.rewards.emerald");

            ItemRewardType base = new ItemRewardType();
            QuestRewardType configured = base.fromSerializedConfig(serialized);
            Map<String, Object> result = configured.serializeConfig();

            assertEquals("quests.mcrpg.test.rewards.emerald", result.get("localization-route"));
        }

        @Test
        @DisplayName("Empty display label is not serialized")
        void serializeConfig_omitsDisplay_whenLabelEmpty() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            serialized.put("item", Map.of("material", "STONE"));
            serialized.put("amount", 1);

            ItemRewardType base = new ItemRewardType();
            QuestRewardType configured = base.fromSerializedConfig(serialized);
            Map<String, Object> result = configured.serializeConfig();

            assertFalse(result.containsKey("display"));
        }

        @Test
        @DisplayName("Missing item config uses 'Item' as fallback material")
        void fromSerializedConfig_fallbackToItem_whenItemMissing() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            serialized.put("amount", 1);

            ItemRewardType base = new ItemRewardType();
            QuestRewardType configured = base.fromSerializedConfig(serialized);

            assertEquals("1x Item", configured.describeForDisplay());
        }
    }

    @Nested
    @DisplayName("describeForDisplay")
    class DescribeForDisplay {

        @Test
        @DisplayName("Formats material name with amount")
        void formatsMaterialNameWithAmount() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            serialized.put("item", Map.of("material", "DIAMOND_SWORD"));
            serialized.put("amount", 3);

            QuestRewardType configured = new ItemRewardType().fromSerializedConfig(serialized);
            assertEquals("3x Diamond sword", configured.describeForDisplay());
        }

        @Test
        @DisplayName("Single item formatted correctly")
        void singleItem() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            serialized.put("item", Map.of("material", "DIAMOND"));
            serialized.put("amount", 1);

            QuestRewardType configured = new ItemRewardType().fromSerializedConfig(serialized);
            assertEquals("1x Diamond", configured.describeForDisplay());
        }

        @Test
        @DisplayName("Underscore-separated material is formatted with spaces")
        void describeForDisplay_replacesUnderscoresWithSpaces() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            serialized.put("item", Map.of("material", "IRON_PICKAXE"));
            serialized.put("amount", 2);

            QuestRewardType configured = new ItemRewardType().fromSerializedConfig(serialized);
            assertEquals("2x Iron pickaxe", configured.describeForDisplay());
        }
    }

    @Nested
    @DisplayName("withAmountMultiplier")
    class WithAmountMultiplier {

        @Test
        @DisplayName("Scales amount by multiplier")
        void scalesAmount() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            serialized.put("item", Map.of("material", "DIAMOND"));
            serialized.put("amount", 10);

            QuestRewardType configured = new ItemRewardType().fromSerializedConfig(serialized);
            QuestRewardType scaled = configured.withAmountMultiplier(0.5);

            assertEquals(OptionalLong.of(5), scaled.getNumericAmount());
        }

        @Test
        @DisplayName("Clamps minimum to 1")
        void clampsMinimumToOne() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            serialized.put("item", Map.of("material", "DIAMOND"));
            serialized.put("amount", 1);

            QuestRewardType configured = new ItemRewardType().fromSerializedConfig(serialized);
            QuestRewardType scaled = configured.withAmountMultiplier(0.01);

            assertEquals(OptionalLong.of(1), scaled.getNumericAmount());
        }

        @Test
        @DisplayName("Returns a new instance")
        void returnsNewInstance() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            serialized.put("item", Map.of("material", "DIAMOND"));
            serialized.put("amount", 10);

            QuestRewardType configured = new ItemRewardType().fromSerializedConfig(serialized);
            QuestRewardType scaled = configured.withAmountMultiplier(2.0);

            assertNotSame(configured, scaled);
            assertEquals(OptionalLong.of(10), configured.getNumericAmount());
            assertEquals(OptionalLong.of(20), scaled.getNumericAmount());
        }

        @Test
        @DisplayName("Rounds to nearest integer")
        void roundsToNearestInteger() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            serialized.put("item", Map.of("material", "DIAMOND"));
            serialized.put("amount", 3);

            QuestRewardType configured = new ItemRewardType().fromSerializedConfig(serialized);
            QuestRewardType scaled = configured.withAmountMultiplier(0.5);

            assertEquals(OptionalLong.of(2), scaled.getNumericAmount());
        }
    }

    @Nested
    @DisplayName("withLocalizationRoute")
    class WithLocalizationRoute {

        @Test
        @DisplayName("Returns a new instance with route in serialized output")
        void withLocalizationRoute_returnsNewInstanceWithRoute() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            serialized.put("item", Map.of("material", "DIAMOND"));
            serialized.put("amount", 1);

            QuestRewardType configured = new ItemRewardType().fromSerializedConfig(serialized);
            QuestRewardType withRoute = configured.withLocalizationRoute(
                    dev.dejvokep.boostedyaml.route.Route.fromString("test.route"));

            assertNotSame(configured, withRoute);
            assertEquals("test.route", withRoute.serializeConfig().get("localization-route"));
        }
    }

    @Nested
    @DisplayName("withInlineDisplayLabel")
    class WithInlineDisplayLabel {

        @Test
        @DisplayName("Returns a new instance with label in serialized output")
        void returnsNewInstanceWithLabel() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            serialized.put("item", Map.of("material", "DIAMOND"));
            serialized.put("amount", 1);

            QuestRewardType configured = new ItemRewardType().fromSerializedConfig(serialized);
            QuestRewardType withLabel = configured.withInlineDisplayLabel("Pretty Diamond");

            assertNotSame(configured, withLabel);
            assertEquals("Pretty Diamond", withLabel.serializeConfig().get("display"));
        }
    }

    @Nested
    @DisplayName("getNumericAmount")
    class GetNumericAmount {

        @Test
        @DisplayName("Returns the configured amount")
        void returnsConfiguredAmount() {
            Map<String, Object> serialized = new LinkedHashMap<>();
            serialized.put("item", Map.of("material", "EMERALD"));
            serialized.put("amount", 42);

            QuestRewardType configured = new ItemRewardType().fromSerializedConfig(serialized);
            assertEquals(OptionalLong.of(42), configured.getNumericAmount());
        }
    }
}
