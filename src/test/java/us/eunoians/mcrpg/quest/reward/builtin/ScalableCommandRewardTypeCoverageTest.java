package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScalableCommandRewardTypeCoverageTest extends McRPGBaseTest {

    private ScalableCommandRewardType baseType;

    @BeforeEach
    void setUp() {
        baseType = new ScalableCommandRewardType();
    }

    @Nested
    @DisplayName("Key and expansion")
    class KeyAndExpansion {

        @Test
        @DisplayName("getKey returns scalable_command key")
        void getKey_returnsScalableCommandKey() {
            assertEquals(ScalableCommandRewardType.KEY, baseType.getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPGExpansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            assertTrue(baseType.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, baseType.getExpansionKey().orElseThrow());
        }
    }

    @Nested
    @DisplayName("fromSerializedConfig")
    class FromSerializedConfig {

        @Test
        @DisplayName("parses display label from config")
        void parsesDisplayLabel() {
            Map<String, Object> config = Map.of(
                    "command", "give {player} diamond {amount}",
                    "base-amount", 10L,
                    "display", "Diamond Reward"
            );
            ScalableCommandRewardType configured = baseType.fromSerializedConfig(config);
            assertEquals("Diamond Reward", configured.serializeConfig().get("display"));
        }

        @Test
        @DisplayName("handles Integer type for base-amount")
        void handlesIntegerBaseAmount() {
            Map<String, Object> config = Map.of(
                    "command", "give {player} diamond {amount}",
                    "base-amount", 10
            );
            ScalableCommandRewardType configured = baseType.fromSerializedConfig(config);
            assertEquals(10L, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("defaults base-amount to 0 when absent")
        void defaultsBaseAmountToZero() {
            Map<String, Object> config = Map.of(
                    "command", "give {player} diamond {amount}"
            );
            ScalableCommandRewardType configured = baseType.fromSerializedConfig(config);
            assertEquals(0L, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("defaults command to empty when absent")
        void defaultsCommandToEmpty() {
            Map<String, Object> config = Map.of("base-amount", 5L);
            ScalableCommandRewardType configured = baseType.fromSerializedConfig(config);
            assertNotNull(configured.serializeConfig().get("command"));
        }

        @Test
        @DisplayName("full round-trip preserves all fields")
        void fullRoundTrip() {
            Map<String, Object> config = Map.of(
                    "command", "eco give {player} {amount}",
                    "base-amount", 50L,
                    "display", "Gold Reward",
                    "localization-route", "quests.mcrpg.rewards.gold"
            );
            ScalableCommandRewardType first = baseType.fromSerializedConfig(config);
            Map<String, Object> serialized = first.serializeConfig();
            ScalableCommandRewardType second = baseType.fromSerializedConfig(serialized);
            Map<String, Object> reSerialized = second.serializeConfig();

            assertEquals(serialized.get("command"), reSerialized.get("command"));
            assertEquals(serialized.get("base-amount"), reSerialized.get("base-amount"));
            assertEquals(serialized.get("display"), reSerialized.get("display"));
            assertEquals(serialized.get("localization-route"), reSerialized.get("localization-route"));
        }
    }

    @Nested
    @DisplayName("withInlineDisplayLabel")
    class WithInlineDisplayLabel {

        @Test
        @DisplayName("returns new instance with label set")
        void returnsNewInstanceWithLabel() {
            ScalableCommandRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "command", "eco give {player} {amount}",
                    "base-amount", 10L
            ));
            QuestRewardType withLabel = configured.withInlineDisplayLabel("Custom Gold");

            assertNotSame(configured, withLabel);
            assertEquals("Custom Gold", withLabel.serializeConfig().get("display"));
        }

        @Test
        @DisplayName("preserves command and amount on label change")
        void preservesFieldsOnLabelChange() {
            ScalableCommandRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "command", "eco give {player} {amount}",
                    "base-amount", 100L
            ));
            QuestRewardType withLabel = configured.withInlineDisplayLabel("Money");

            assertEquals("eco give {player} {amount}", withLabel.serializeConfig().get("command"));
            assertEquals(100L, withLabel.getNumericAmount().orElse(-1));
        }
    }

    @Nested
    @DisplayName("describeForDisplay (no-arg)")
    class DescribeForDisplay {

        @Test
        @DisplayName("returns label with amount suffix when label is set")
        void returnsLabelWithAmountSuffix() {
            ScalableCommandRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "command", "eco give {player} {amount}",
                    "base-amount", 10L,
                    "display", "Gold"
            ));
            assertEquals("Gold (x10)", configured.describeForDisplay());
        }

        @Test
        @DisplayName("returns default with amount suffix when label is empty")
        void returnsDefaultWithAmountSuffix() {
            ScalableCommandRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "command", "eco give {player} {amount}",
                    "base-amount", 25L
            ));
            assertEquals("Scaled Reward (x25)", configured.describeForDisplay());
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("omits display when empty")
        void omitsDisplayWhenEmpty() {
            ScalableCommandRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "command", "give {player} diamond {amount}",
                    "base-amount", 5L
            ));
            assertNull(configured.serializeConfig().get("display"));
        }

        @Test
        @DisplayName("includes display when non-empty")
        void includesDisplayWhenNonEmpty() {
            ScalableCommandRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "command", "give {player} diamond {amount}",
                    "base-amount", 5L,
                    "display", "Diamonds"
            ));
            assertEquals("Diamonds", configured.serializeConfig().get("display"));
        }

        @Test
        @DisplayName("always includes command and base-amount")
        void alwaysIncludesCommandAndAmount() {
            ScalableCommandRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "command", "eco give {player} {amount}",
                    "base-amount", 30L
            ));
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("eco give {player} {amount}", serialized.get("command"));
            assertEquals(30L, serialized.get("base-amount"));
        }
    }

    @Nested
    @DisplayName("grant")
    class Grant {

        @Test
        @DisplayName("replaces {player} and {amount} placeholders")
        void replacesPlaceholders() {
            ScalableCommandRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "command", "give {player} diamond {amount}",
                    "base-amount", 5L
            ));
            PlayerMock player = server.addPlayer();
            assertDoesNotThrow(() -> configured.grant(player));
        }

        @Test
        @DisplayName("does nothing with empty command template")
        void doesNothingWithEmptyCommand() {
            ScalableCommandRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "command", "",
                    "base-amount", 5L
            ));
            PlayerMock player = server.addPlayer();
            assertDoesNotThrow(() -> configured.grant(player));
        }

        @Test
        @DisplayName("handles default instance grant (empty command)")
        void handlesDefaultInstanceGrant() {
            PlayerMock player = server.addPlayer();
            assertDoesNotThrow(() -> baseType.grant(player));
        }
    }

    @Nested
    @DisplayName("withAmountMultiplier")
    class WithAmountMultiplier {

        @Test
        @DisplayName("preserves display label on scaling")
        void preservesDisplayLabel() {
            ScalableCommandRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "command", "eco give {player} {amount}",
                    "base-amount", 100L,
                    "display", "Gold Coins"
            ));
            QuestRewardType scaled = configured.withAmountMultiplier(0.5);
            assertEquals("Gold Coins", scaled.serializeConfig().get("display"));
        }

        @Test
        @DisplayName("preserves command template on scaling")
        void preservesCommandTemplate() {
            ScalableCommandRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "command", "eco give {player} {amount}",
                    "base-amount", 100L
            ));
            QuestRewardType scaled = configured.withAmountMultiplier(0.5);
            assertEquals("eco give {player} {amount}", scaled.serializeConfig().get("command"));
        }
    }

    @Nested
    @DisplayName("withLocalizationRoute")
    class WithLocalizationRoute {

        @Test
        @DisplayName("preserves command and amount")
        void preservesCommandAndAmount() {
            ScalableCommandRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "command", "eco give {player} {amount}",
                    "base-amount", 42L
            ));
            Route route = Route.fromString("quests.mcrpg.rewards.coins");
            QuestRewardType withRoute = configured.withLocalizationRoute(route);

            assertEquals("eco give {player} {amount}", withRoute.serializeConfig().get("command"));
            assertEquals(42L, withRoute.getNumericAmount().orElse(-1));
            assertEquals("quests.mcrpg.rewards.coins",
                    withRoute.serializeConfig().get("localization-route"));
        }
    }
}
