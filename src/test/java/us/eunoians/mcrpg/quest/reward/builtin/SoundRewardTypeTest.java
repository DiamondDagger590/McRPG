package us.eunoians.mcrpg.quest.reward.builtin;

import org.bukkit.Sound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoundRewardTypeTest extends McRPGBaseTest {

    private SoundRewardType type;

    @BeforeEach
    void setup() {
        type = new SoundRewardType();
    }

    @DisplayName("getKey returns mcrpg:sound")
    @Test
    void getKey_returnsExpectedKey() {
        assertEquals(SoundRewardType.KEY, type.getKey());
        assertEquals("mcrpg:sound", type.getKey().toString());
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

        @DisplayName("serializeConfig returns empty map when sound is null")
        @Test
        void serializeConfig_defaultInstance_returnsEmptyMap() {
            Map<String, Object> config = type.serializeConfig();
            assertTrue(config.isEmpty());
        }
    }

    @Nested
    @DisplayName("Serialization round-trip")
    class SerializationTests {

        @DisplayName("round-trip preserves sound, volume, and pitch")
        @Test
        void fromSerializedConfig_roundTrip_preservesAllFields() {
            Map<String, Object> config = new HashMap<>();
            config.put("sound", "UI_TOAST_CHALLENGE_COMPLETE");
            config.put("volume", 0.8);
            config.put("pitch", 1.5);

            SoundRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals(Sound.valueOf("UI_TOAST_CHALLENGE_COMPLETE").name(), serialized.get("sound"));
            assertEquals(0.8f, ((Number) serialized.get("volume")).floatValue(), 0.01f);
            assertEquals(1.5f, ((Number) serialized.get("pitch")).floatValue(), 0.01f);
        }

        @DisplayName("missing volume and pitch use defaults of 1.0")
        @Test
        void fromSerializedConfig_missingVolumeAndPitch_usesDefaults() {
            Map<String, Object> config = Map.of("sound", "ENTITY_PLAYER_LEVELUP");

            SoundRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals(Sound.valueOf("ENTITY_PLAYER_LEVELUP").name(), serialized.get("sound"));
            assertEquals(1.0f, ((Number) serialized.get("volume")).floatValue(), 0.01f);
            assertEquals(1.0f, ((Number) serialized.get("pitch")).floatValue(), 0.01f);
        }

        @DisplayName("missing sound key returns no-op instance with empty map")
        @Test
        void fromSerializedConfig_missingSound_returnsNoOpInstance() {
            SoundRewardType configured = type.fromSerializedConfig(Map.of());
            Map<String, Object> serialized = configured.serializeConfig();

            assertTrue(serialized.isEmpty());
        }

        @DisplayName("invalid sound name returns no-op instance with empty map")
        @Test
        void fromSerializedConfig_invalidSound_returnsNoOpInstance() {
            Map<String, Object> config = Map.of("sound", "NOT_A_REAL_SOUND");

            SoundRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();

            assertTrue(serialized.isEmpty());
        }

        @DisplayName("sound name parsing is case-insensitive")
        @Test
        void fromSerializedConfig_caseInsensitive_parsesCorrectly() {
            Map<String, Object> config = Map.of("sound", "entity_player_levelup");

            SoundRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();

            assertFalse(serialized.isEmpty());
            assertEquals(Sound.valueOf("ENTITY_PLAYER_LEVELUP").name(), serialized.get("sound"));
        }

        @DisplayName("Number values in config are parsed as floats")
        @Test
        void fromSerializedConfig_numberValues_parsedCorrectly() {
            Map<String, Object> config = new HashMap<>();
            config.put("sound", "ENTITY_PLAYER_LEVELUP");
            config.put("volume", 2L);
            config.put("pitch", (short) 3);

            SoundRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals(2.0f, ((Number) serialized.get("volume")).floatValue(), 0.01f);
            assertEquals(3.0f, ((Number) serialized.get("pitch")).floatValue(), 0.01f);
        }

        @DisplayName("non-numeric values fall back to defaults")
        @Test
        void fromSerializedConfig_nonNumericValues_fallsBackToDefaults() {
            Map<String, Object> config = new HashMap<>();
            config.put("sound", "ENTITY_PLAYER_LEVELUP");
            config.put("volume", "not_a_number");
            config.put("pitch", "invalid");

            SoundRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals(1.0f, ((Number) serialized.get("volume")).floatValue(), 0.01f);
            assertEquals(1.0f, ((Number) serialized.get("pitch")).floatValue(), 0.01f);
        }
    }

    @Nested
    @DisplayName("Scalability")
    class ScalabilityTests {

        @DisplayName("getNumericAmount returns empty for sound rewards")
        @Test
        void getNumericAmount_returnsEmpty() {
            assertTrue(type.getNumericAmount().isEmpty());
        }

        @DisplayName("withAmountMultiplier returns same instance")
        @Test
        void withAmountMultiplier_returnsSelf() {
            SoundRewardType configured = type.fromSerializedConfig(
                    Map.of("sound", "ENTITY_PLAYER_LEVELUP"));
            assertSame(configured, configured.withAmountMultiplier(2.0));
        }
    }
}
