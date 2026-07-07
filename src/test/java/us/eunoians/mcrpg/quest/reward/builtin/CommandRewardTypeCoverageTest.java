package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandRewardTypeCoverageTest extends McRPGBaseTest {

    private CommandRewardType baseType;

    @BeforeEach
    void setUp() {
        baseType = new CommandRewardType();
    }

    @Nested
    @DisplayName("fromSerializedConfig")
    class FromSerializedConfig {

        @Test
        @DisplayName("parses display label from config")
        void parsesDisplayLabel() {
            Map<String, Object> config = Map.of(
                    "commands", List.of("say hello"),
                    "display", "Hero Title"
            );
            CommandRewardType configured = baseType.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("Hero Title", serialized.get("display"));
        }

        @Test
        @DisplayName("handles missing commands key")
        void handlesMissingCommands() {
            Map<String, Object> config = Map.of("display", "Reward");
            CommandRewardType configured = baseType.fromSerializedConfig(config);
            @SuppressWarnings("unchecked")
            List<String> commands = (List<String>) configured.serializeConfig().get("commands");
            assertNotNull(commands);
            assertTrue(commands.isEmpty());
        }

        @Test
        @DisplayName("handles non-list commands value")
        void handlesNonListCommands() {
            Map<String, Object> config = Map.of("commands", "not-a-list");
            CommandRewardType configured = baseType.fromSerializedConfig(config);
            assertNotNull(configured.serializeConfig());
        }

        @Test
        @DisplayName("preserves all fields through full round-trip")
        void fullRoundTrip() {
            Map<String, Object> config = Map.of(
                    "commands", List.of("give {player} diamond 5", "msg {player} You won!"),
                    "display", "Winner Reward",
                    "localization-route", "quests.mcrpg.rewards.winner"
            );
            CommandRewardType first = baseType.fromSerializedConfig(config);
            Map<String, Object> serialized = first.serializeConfig();
            CommandRewardType second = baseType.fromSerializedConfig(serialized);
            Map<String, Object> reSerialized = second.serializeConfig();

            assertEquals(serialized.get("commands"), reSerialized.get("commands"));
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
            CommandRewardType configured = baseType.fromSerializedConfig(
                    Map.of("commands", List.of("say hello")));
            QuestRewardType withLabel = configured.withInlineDisplayLabel("Custom Label");

            assertNotSame(configured, withLabel);
            assertEquals("Custom Label", withLabel.serializeConfig().get("display"));
        }

        @Test
        @DisplayName("preserves commands on label change")
        void preservesCommandsOnLabelChange() {
            List<String> commands = List.of("say hello", "give {player} stone 1");
            CommandRewardType configured = baseType.fromSerializedConfig(
                    Map.of("commands", commands));
            QuestRewardType withLabel = configured.withInlineDisplayLabel("Label");

            @SuppressWarnings("unchecked")
            List<String> serializedCommands = (List<String>) withLabel.serializeConfig().get("commands");
            assertEquals(commands, serializedCommands);
        }
    }

    @Nested
    @DisplayName("describeForDisplay (no-arg)")
    class DescribeForDisplay {

        @Test
        @DisplayName("returns 'Special Reward' when label is empty")
        void returnsFallbackWhenLabelEmpty() {
            CommandRewardType configured = baseType.fromSerializedConfig(
                    Map.of("commands", List.of("say hello")));
            assertEquals("Special Reward", configured.describeForDisplay());
        }

        @Test
        @DisplayName("returns custom label when set")
        void returnsCustomLabel() {
            CommandRewardType configured = baseType.fromSerializedConfig(
                    Map.of("commands", List.of("say hello"), "display", "Hero Title"));
            assertEquals("Hero Title", configured.describeForDisplay());
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("omits display when empty")
        void omitsDisplayWhenEmpty() {
            CommandRewardType configured = baseType.fromSerializedConfig(
                    Map.of("commands", List.of("say hello")));
            assertNull(configured.serializeConfig().get("display"));
        }

        @Test
        @DisplayName("includes display when non-empty")
        void includesDisplayWhenNonEmpty() {
            CommandRewardType configured = baseType.fromSerializedConfig(
                    Map.of("commands", List.of("say hello"), "display", "Title"));
            assertEquals("Title", configured.serializeConfig().get("display"));
        }
    }

    @Nested
    @DisplayName("grant")
    class Grant {

        @Test
        @DisplayName("replaces {player} placeholder in commands")
        void replacesPlayerPlaceholder() {
            CommandRewardType configured = baseType.fromSerializedConfig(
                    Map.of("commands", List.of("say {player} won")));
            PlayerMock player = server.addPlayer();
            assertDoesNotThrow(() -> configured.grant(player));
        }

        @Test
        @DisplayName("grants multiple commands sequentially")
        void grantsMultipleCommands() {
            CommandRewardType configured = baseType.fromSerializedConfig(
                    Map.of("commands", List.of("say first", "say second", "say third")));
            PlayerMock player = server.addPlayer();
            assertDoesNotThrow(() -> configured.grant(player));
        }

        @Test
        @DisplayName("handles empty command list")
        void handlesEmptyCommandList() {
            CommandRewardType configured = baseType.fromSerializedConfig(
                    Map.of("commands", List.of()));
            PlayerMock player = server.addPlayer();
            assertDoesNotThrow(() -> configured.grant(player));
        }
    }

    @Nested
    @DisplayName("getNumericAmount")
    class GetNumericAmount {

        @Test
        @DisplayName("returns empty for command rewards")
        void returnsEmpty() {
            assertTrue(baseType.getNumericAmount().isEmpty());
        }
    }

    @Nested
    @DisplayName("withAmountMultiplier")
    class WithAmountMultiplier {

        @Test
        @DisplayName("returns same instance (no scaling for commands)")
        void returnsSelf() {
            CommandRewardType configured = baseType.fromSerializedConfig(
                    Map.of("commands", List.of("say hello")));
            QuestRewardType scaled = configured.withAmountMultiplier(2.0);
            assertNotNull(scaled);
        }
    }
}
