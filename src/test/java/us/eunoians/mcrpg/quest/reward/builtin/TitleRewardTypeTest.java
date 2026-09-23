package us.eunoians.mcrpg.quest.reward.builtin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TitleRewardTypeTest extends McRPGBaseTest {

    private TitleRewardType type;

    @BeforeEach
    void setup() {
        type = new TitleRewardType();
    }

    @DisplayName("getKey returns mcrpg:title_message")
    @Test
    void getKey_returnsExpectedKey() {
        assertEquals(TitleRewardType.KEY, type.getKey());
        assertEquals("mcrpg:title_message", type.getKey().toString());
    }

    @DisplayName("getExpansionKey returns McRPGExpansion key")
    @Test
    void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }

    @DisplayName("describeForDisplay returns empty string")
    @Test
    void describeForDisplay_returnsEmptyString() {
        assertEquals("", type.describeForDisplay());
    }

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructorTests {

        @DisplayName("serializeConfig returns empty strings and default tick values")
        @Test
        void serializeConfig_defaultInstance_returnsDefaults() {
            Map<String, Object> config = type.serializeConfig();
            assertEquals("", config.get("title"));
            assertEquals("", config.get("subtitle"));
            assertEquals(10, config.get("fade-in"));
            assertEquals(70, config.get("stay"));
            assertEquals(20, config.get("fade-out"));
        }
    }

    @Nested
    @DisplayName("Serialization round-trip")
    class SerializationTests {

        @DisplayName("round-trip preserves all fields")
        @Test
        void fromSerializedConfig_roundTrip_preservesAllFields() {
            Map<String, Object> config = new HashMap<>();
            config.put("title", "<primary>Quest Complete!");
            config.put("subtitle", "<body>Well done.");
            config.put("fade-in", 5);
            config.put("stay", 100);
            config.put("fade-out", 30);

            TitleRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals("<primary>Quest Complete!", serialized.get("title"));
            assertEquals("<body>Well done.", serialized.get("subtitle"));
            assertEquals(5, serialized.get("fade-in"));
            assertEquals(100, serialized.get("stay"));
            assertEquals(30, serialized.get("fade-out"));
        }

        @DisplayName("missing keys use empty string and default ticks")
        @Test
        void fromSerializedConfig_missingKeys_usesDefaults() {
            TitleRewardType configured = type.fromSerializedConfig(Map.of());
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals("", serialized.get("title"));
            assertEquals("", serialized.get("subtitle"));
            assertEquals(10, serialized.get("fade-in"));
            assertEquals(70, serialized.get("stay"));
            assertEquals(20, serialized.get("fade-out"));
        }

        @DisplayName("Number values in config are parsed as ints")
        @Test
        void fromSerializedConfig_numberValues_parsedCorrectly() {
            Map<String, Object> config = new HashMap<>();
            config.put("title", "Title");
            config.put("subtitle", "Sub");
            config.put("fade-in", 15L);
            config.put("stay", 80.0);
            config.put("fade-out", (short) 25);

            TitleRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals(15, serialized.get("fade-in"));
            assertEquals(80, serialized.get("stay"));
            assertEquals(25, serialized.get("fade-out"));
        }

        @DisplayName("non-numeric values fall back to defaults")
        @Test
        void fromSerializedConfig_nonNumericValues_fallsBackToDefaults() {
            Map<String, Object> config = new HashMap<>();
            config.put("title", "Title");
            config.put("subtitle", "Sub");
            config.put("fade-in", "not_a_number");
            config.put("stay", "invalid");
            config.put("fade-out", "bad");

            TitleRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals(10, serialized.get("fade-in"));
            assertEquals(70, serialized.get("stay"));
            assertEquals(20, serialized.get("fade-out"));
        }

        @DisplayName("null values fall back to defaults")
        @Test
        void fromSerializedConfig_nullValues_fallsBackToDefaults() {
            Map<String, Object> config = new HashMap<>();
            config.put("title", "Title");
            config.put("subtitle", "Sub");

            TitleRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals(10, serialized.get("fade-in"));
            assertEquals(70, serialized.get("stay"));
            assertEquals(20, serialized.get("fade-out"));
        }
    }

    @Nested
    @DisplayName("Scalability")
    class ScalabilityTests {

        @DisplayName("getNumericAmount returns empty for title rewards")
        @Test
        void getNumericAmount_returnsEmpty() {
            assertTrue(type.getNumericAmount().isEmpty());
        }

        @DisplayName("withAmountMultiplier returns same instance")
        @Test
        void withAmountMultiplier_returnsSelf() {
            TitleRewardType configured = type.fromSerializedConfig(
                    Map.of("title", "Test", "subtitle", "Sub"));
            assertSame(configured, configured.withAmountMultiplier(2.0));
        }
    }
}
