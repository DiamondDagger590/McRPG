package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.Map;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ExperienceRewardType — extended coverage")
public class ExperienceRewardTypeCoverageTest extends McRPGBaseTest {

    private ExperienceRewardType baseType;

    @BeforeEach
    public void setup() {
        baseType = new ExperienceRewardType();
    }

    @Nested
    @DisplayName("getNumericAmount")
    class GetNumericAmount {

        @Test
        @DisplayName("base type returns zero amount")
        public void getNumericAmount_returnsZero_forBaseType() {
            OptionalLong amount = baseType.getNumericAmount();
            assertTrue(amount.isPresent());
            assertEquals(0L, amount.getAsLong());
        }

        @Test
        @DisplayName("configured type returns configured amount")
        public void getNumericAmount_returnsConfiguredAmount() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "MINING", "amount", 500L));
            assertEquals(500L, configured.getNumericAmount().getAsLong());
        }
    }

    @Nested
    @DisplayName("withAmountMultiplier")
    class WithAmountMultiplier {

        @Test
        @DisplayName("scales amount by multiplier")
        public void withAmountMultiplier_scalesAmount() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "MINING", "amount", 100L));
            ExperienceRewardType scaled = configured.withAmountMultiplier(2.5);

            assertEquals(250L, scaled.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("returns new instance, not the same object")
        public void withAmountMultiplier_returnsNewInstance() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "MINING", "amount", 100L));
            ExperienceRewardType scaled = configured.withAmountMultiplier(1.5);

            assertNotSame(configured, scaled);
        }

        @Test
        @DisplayName("enforces minimum of 1")
        public void withAmountMultiplier_enforcesMinimumOfOne() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "MINING", "amount", 1L));
            ExperienceRewardType scaled = configured.withAmountMultiplier(0.01);

            assertEquals(1L, scaled.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("preserves skill name after scaling")
        public void withAmountMultiplier_preservesSkillName() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "SWORDS", "amount", 200L));
            ExperienceRewardType scaled = configured.withAmountMultiplier(3.0);

            Map<String, Object> serialized = scaled.serializeConfig();
            assertEquals("SWORDS", serialized.get("skill"));
            assertEquals(600L, ((Number) serialized.get("amount")).longValue());
        }

        @Test
        @DisplayName("preserves display label after scaling")
        public void withAmountMultiplier_preservesDisplayLabel() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(
                    Map.of("skill", "MINING", "amount", 100L, "display", "my_label"));
            ExperienceRewardType scaled = configured.withAmountMultiplier(2.0);

            Map<String, Object> serialized = scaled.serializeConfig();
            assertEquals("my_label", serialized.get("display"));
        }
    }

    @Nested
    @DisplayName("withLocalizationRoute")
    class WithLocalizationRoute {

        @Test
        @DisplayName("returns new instance with route")
        public void withLocalizationRoute_returnsNewInstance() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "MINING", "amount", 100L));
            Route route = Route.fromString("quest.rewards.test-label");
            ExperienceRewardType withRoute = configured.withLocalizationRoute(route);

            assertNotSame(configured, withRoute);
        }

        @Test
        @DisplayName("route appears in serialized config")
        public void withLocalizationRoute_appearsInSerializedConfig() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "MINING", "amount", 100L));
            Route route = Route.fromString("quest.rewards.test-label");
            ExperienceRewardType withRoute = configured.withLocalizationRoute(route);

            Map<String, Object> serialized = withRoute.serializeConfig();
            assertEquals("quest.rewards.test-label", serialized.get("localization-route"));
        }
    }

    @Nested
    @DisplayName("withInlineDisplayLabel")
    class WithInlineDisplayLabel {

        @Test
        @DisplayName("returns new instance with label")
        public void withInlineDisplayLabel_returnsNewInstance() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "MINING", "amount", 100L));
            ExperienceRewardType withLabel = configured.withInlineDisplayLabel("my-display");

            assertNotSame(configured, withLabel);
        }

        @Test
        @DisplayName("label appears in serialized config")
        public void withInlineDisplayLabel_appearsInSerializedConfig() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "MINING", "amount", 100L));
            ExperienceRewardType withLabel = configured.withInlineDisplayLabel("custom_display");

            Map<String, Object> serialized = withLabel.serializeConfig();
            assertEquals("custom_display", serialized.get("display"));
        }
    }

    @Nested
    @DisplayName("describeForDisplay (no-arg)")
    class DescribeForDisplay {

        @Test
        @DisplayName("formats uppercase skill name as title case")
        public void describeForDisplay_formatsUppercaseSkill() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "SWORDS", "amount", 250L));
            String display = configured.describeForDisplay();

            assertEquals("250 Swords XP", display);
        }

        @Test
        @DisplayName("formats namespaced skill name correctly")
        public void describeForDisplay_formatsNamespacedSkill() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "mcrpg:mining", "amount", 100L));
            String display = configured.describeForDisplay();

            assertEquals("100 Mining XP", display);
        }

        @Test
        @DisplayName("handles empty skill name")
        public void describeForDisplay_handlesEmptySkill() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "", "amount", 50L));
            String display = configured.describeForDisplay();

            assertEquals("50 Unknown XP", display);
        }

        @Test
        @DisplayName("formats multi-word skill name")
        public void describeForDisplay_formatsMultiWordSkill() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "HEAVY_WEAPONS", "amount", 300L));
            String display = configured.describeForDisplay();

            assertEquals("300 Heavy Weapons XP", display);
        }

        @Test
        @DisplayName("base type shows 0 Unknown XP")
        public void describeForDisplay_baseType_showsZeroUnknown() {
            String display = baseType.describeForDisplay();
            assertEquals("0 Unknown XP", display);
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("empty display label omitted from serialization")
        public void serializeConfig_omitsEmptyDisplayLabel() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "MINING", "amount", 100L));
            Map<String, Object> serialized = configured.serializeConfig();

            assertFalse(serialized.containsKey("display"));
        }

        @Test
        @DisplayName("null localization route omitted from serialization")
        public void serializeConfig_omitsNullLocalizationRoute() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "MINING", "amount", 100L));
            Map<String, Object> serialized = configured.serializeConfig();

            assertFalse(serialized.containsKey("localization-route"));
        }

        @Test
        @DisplayName("non-empty display label included in serialization")
        public void serializeConfig_includesNonEmptyDisplayLabel() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(
                    Map.of("skill", "MINING", "amount", 100L, "display", "reward_xp"));
            Map<String, Object> serialized = configured.serializeConfig();

            assertTrue(serialized.containsKey("display"));
            assertEquals("reward_xp", serialized.get("display"));
        }

        @Test
        @DisplayName("round-trip with localization route preserves route")
        public void serializeConfig_roundTripWithRoute() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(
                    Map.of("skill", "MINING", "amount", 100L, "localization-route", "quest.rewards.test"));
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals("quest.rewards.test", serialized.get("localization-route"));
        }
    }

    @Nested
    @DisplayName("fromSerializedConfig")
    class FromSerializedConfig {

        @Test
        @DisplayName("handles Number types for amount")
        public void fromSerializedConfig_handlesNumberTypes() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "MINING", "amount", 500));
            assertEquals(500L, configured.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("preserves key across deserialization")
        public void fromSerializedConfig_preservesKey() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "MINING", "amount", 100L));
            assertEquals(ExperienceRewardType.KEY, configured.getKey());
        }
    }

    @Nested
    @DisplayName("grant")
    class Grant {

        @Test
        @DisplayName("no-ops when skill name is empty")
        public void grant_noOps_whenSkillNameEmpty() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "", "amount", 100L));
            var player = server.addPlayer();
            assertDoesNotThrow(() -> configured.grant(player));
        }

        @Test
        @DisplayName("no-ops when amount is zero")
        public void grant_noOps_whenAmountZero() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "MINING", "amount", 0L));
            var player = server.addPlayer();
            assertDoesNotThrow(() -> configured.grant(player));
        }

        @Test
        @DisplayName("no-ops when amount is negative")
        public void grant_noOps_whenAmountNegative() {
            ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "MINING", "amount", -10L));
            var player = server.addPlayer();
            assertDoesNotThrow(() -> configured.grant(player));
        }
    }
}
