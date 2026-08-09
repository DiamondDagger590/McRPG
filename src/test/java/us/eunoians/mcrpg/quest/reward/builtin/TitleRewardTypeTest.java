package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("TitleRewardType")
public class TitleRewardTypeTest extends McRPGBaseTest {

    private TitleRewardType baseType;

    @BeforeEach
    public void setup() {
        baseType = new TitleRewardType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @Test
        @DisplayName("getKey returns mcrpg:title_message")
        public void getKey_returnsTitleMessageKey() {
            assertEquals(TitleRewardType.KEY, baseType.getKey());
        }

        @Test
        @DisplayName("key namespace is mcrpg")
        public void getKey_namespaceIsMcrpg() {
            assertEquals("mcrpg", baseType.getKey().getNamespace());
        }

        @Test
        @DisplayName("key value is title_message")
        public void getKey_valueIsTitleMessage() {
            assertEquals("title_message", baseType.getKey().getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPGExpansion key")
        public void getExpansionKey_returnsMcRPGExpansionKey() {
            assertTrue(baseType.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, baseType.getExpansionKey().get());
        }
    }

    @Nested
    @DisplayName("describeForDisplay")
    class DescribeForDisplay {

        @Test
        @DisplayName("returns empty string")
        public void describeForDisplay_returnsEmptyString() {
            assertEquals("", baseType.describeForDisplay());
        }

        @Test
        @DisplayName("configured instance also returns empty string")
        public void describeForDisplay_configuredInstance_returnsEmptyString() {
            TitleRewardType configured = baseType.fromSerializedConfig(
                    Map.of("title", "Hello", "subtitle", "World"));
            assertEquals("", configured.describeForDisplay());
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("base instance serializes with defaults")
        public void serializeConfig_baseInstance_containsDefaults() {
            Map<String, Object> serialized = baseType.serializeConfig();
            assertEquals("", serialized.get("title"));
            assertEquals("", serialized.get("subtitle"));
            assertEquals(10, serialized.get("fade-in"));
            assertEquals(70, serialized.get("stay"));
            assertEquals(20, serialized.get("fade-out"));
        }

        @Test
        @DisplayName("configured instance serializes all fields")
        public void serializeConfig_configuredInstance_containsAllFields() {
            TitleRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "title", "Quest Complete!",
                    "subtitle", "Well done.",
                    "fade-in", 5,
                    "stay", 40,
                    "fade-out", 10));
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals("Quest Complete!", serialized.get("title"));
            assertEquals("Well done.", serialized.get("subtitle"));
            assertEquals(5, serialized.get("fade-in"));
            assertEquals(40, serialized.get("stay"));
            assertEquals(10, serialized.get("fade-out"));
        }
    }

    @Nested
    @DisplayName("fromSerializedConfig")
    class FromSerializedConfig {

        @Test
        @DisplayName("round-trips all fields correctly")
        public void fromSerializedConfig_roundTripsCorrectly() {
            Map<String, Object> config = Map.of(
                    "title", "<primary>Tutorial Complete!",
                    "subtitle", "<body>You're ready.",
                    "fade-in", 15,
                    "stay", 80,
                    "fade-out", 25);
            TitleRewardType configured = baseType.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals("<primary>Tutorial Complete!", serialized.get("title"));
            assertEquals("<body>You're ready.", serialized.get("subtitle"));
            assertEquals(15, serialized.get("fade-in"));
            assertEquals(80, serialized.get("stay"));
            assertEquals(25, serialized.get("fade-out"));
        }

        @Test
        @DisplayName("missing title defaults to empty string")
        public void fromSerializedConfig_missingTitle_defaultsToEmpty() {
            TitleRewardType configured = baseType.fromSerializedConfig(
                    Map.of("subtitle", "Just a subtitle"));
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("", serialized.get("title"));
        }

        @Test
        @DisplayName("missing subtitle defaults to empty string")
        public void fromSerializedConfig_missingSubtitle_defaultsToEmpty() {
            TitleRewardType configured = baseType.fromSerializedConfig(
                    Map.of("title", "Just a title"));
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("", serialized.get("subtitle"));
        }

        @Test
        @DisplayName("missing timing fields default correctly")
        public void fromSerializedConfig_missingTimings_defaultCorrectly() {
            TitleRewardType configured = baseType.fromSerializedConfig(
                    Map.of("title", "Hello"));
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals(10, serialized.get("fade-in"));
            assertEquals(70, serialized.get("stay"));
            assertEquals(20, serialized.get("fade-out"));
        }

        @Test
        @DisplayName("non-numeric fade-in falls back to default")
        public void fromSerializedConfig_nonNumericFadeIn_fallsBackToDefault() {
            Map<String, Object> config = new HashMap<>();
            config.put("title", "Hello");
            config.put("fade-in", "not_a_number");

            TitleRewardType configured = baseType.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals(10, serialized.get("fade-in"));
        }

        @Test
        @DisplayName("non-numeric stay falls back to default")
        public void fromSerializedConfig_nonNumericStay_fallsBackToDefault() {
            Map<String, Object> config = new HashMap<>();
            config.put("title", "Hello");
            config.put("stay", "invalid");

            TitleRewardType configured = baseType.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals(70, serialized.get("stay"));
        }

        @Test
        @DisplayName("non-numeric fade-out falls back to default")
        public void fromSerializedConfig_nonNumericFadeOut_fallsBackToDefault() {
            Map<String, Object> config = new HashMap<>();
            config.put("title", "Hello");
            config.put("fade-out", "bad");

            TitleRewardType configured = baseType.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals(20, serialized.get("fade-out"));
        }

        @Test
        @DisplayName("empty config returns instance with all defaults")
        public void fromSerializedConfig_emptyConfig_usesAllDefaults() {
            TitleRewardType configured = baseType.fromSerializedConfig(Map.of());
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("", serialized.get("title"));
            assertEquals("", serialized.get("subtitle"));
            assertEquals(10, serialized.get("fade-in"));
            assertEquals(70, serialized.get("stay"));
            assertEquals(20, serialized.get("fade-out"));
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("parses all fields")
        public void parseConfig_allFields() {
            Section section = mock(Section.class);
            when(section.contains("title")).thenReturn(true);
            when(section.getString("title")).thenReturn("Big Title");
            when(section.contains("subtitle")).thenReturn(true);
            when(section.getString("subtitle")).thenReturn("Small Sub");
            when(section.get("fade-in")).thenReturn(5);
            when(section.get("stay")).thenReturn(40);
            when(section.get("fade-out")).thenReturn(10);

            TitleRewardType parsed = baseType.parseConfig(section);
            Map<String, Object> serialized = parsed.serializeConfig();

            assertEquals("Big Title", serialized.get("title"));
            assertEquals("Small Sub", serialized.get("subtitle"));
            assertEquals(5, serialized.get("fade-in"));
            assertEquals(40, serialized.get("stay"));
            assertEquals(10, serialized.get("fade-out"));
        }

        @Test
        @DisplayName("missing title and subtitle default to empty")
        public void parseConfig_missingTitleAndSubtitle_defaultToEmpty() {
            Section section = mock(Section.class);
            when(section.contains("title")).thenReturn(false);
            when(section.contains("subtitle")).thenReturn(false);
            when(section.get("fade-in")).thenReturn(null);
            when(section.get("stay")).thenReturn(null);
            when(section.get("fade-out")).thenReturn(null);

            TitleRewardType parsed = baseType.parseConfig(section);
            Map<String, Object> serialized = parsed.serializeConfig();

            assertEquals("", serialized.get("title"));
            assertEquals("", serialized.get("subtitle"));
        }

        @Test
        @DisplayName("returns new instance")
        public void parseConfig_returnsNewInstance() {
            Section section = mock(Section.class);
            when(section.contains("title")).thenReturn(false);
            when(section.contains("subtitle")).thenReturn(false);
            when(section.get("fade-in")).thenReturn(null);
            when(section.get("stay")).thenReturn(null);
            when(section.get("fade-out")).thenReturn(null);

            TitleRewardType parsed = baseType.parseConfig(section);
            assertNotSame(baseType, parsed);
        }

        @Test
        @DisplayName("null getString returns empty title")
        public void parseConfig_nullGetString_defaultsToEmpty() {
            Section section = mock(Section.class);
            when(section.contains("title")).thenReturn(true);
            when(section.getString("title")).thenReturn(null);
            when(section.contains("subtitle")).thenReturn(false);
            when(section.get("fade-in")).thenReturn(null);
            when(section.get("stay")).thenReturn(null);
            when(section.get("fade-out")).thenReturn(null);

            TitleRewardType parsed = baseType.parseConfig(section);
            Map<String, Object> serialized = parsed.serializeConfig();
            assertEquals("", serialized.get("title"));
        }
    }

    @Nested
    @DisplayName("grant")
    class Grant {

        @Test
        @DisplayName("base instance grant does not throw")
        public void grant_baseInstance_doesNotThrow() {
            PlayerMock player = server.addPlayer();
            assertDoesNotThrow(() -> baseType.grant(player));
        }

        @Test
        @DisplayName("configured instance shows title to player")
        public void grant_configuredInstance_showsTitle() {
            TitleRewardType configured = baseType.fromSerializedConfig(Map.of(
                    "title", "Congrats!",
                    "subtitle", "You did it.",
                    "fade-in", 10,
                    "stay", 70,
                    "fade-out", 20));
            PlayerMock player = server.addPlayer();
            assertDoesNotThrow(() -> configured.grant(player));
        }
    }

    @Nested
    @DisplayName("Default QuestRewardType methods")
    class DefaultMethods {

        @Test
        @DisplayName("withAmountMultiplier returns this")
        public void withAmountMultiplier_returnsSameInstance() {
            assertEquals(baseType, baseType.withAmountMultiplier(2.0));
        }

        @Test
        @DisplayName("isScalable returns false")
        public void isScalable_returnsFalse() {
            assertFalse(baseType.isScalable());
        }

        @Test
        @DisplayName("getNumericAmount returns empty")
        public void getNumericAmount_returnsEmpty() {
            assertTrue(baseType.getNumericAmount().isEmpty());
        }

        @Test
        @DisplayName("withExactAmount returns this")
        public void withExactAmount_returnsSameInstance() {
            assertEquals(baseType, baseType.withExactAmount(10));
        }
    }
}
