package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Sound;
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

@DisplayName("SoundRewardType")
public class SoundRewardTypeTest extends McRPGBaseTest {

    private SoundRewardType baseType;

    @BeforeEach
    public void setup() {
        baseType = new SoundRewardType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @Test
        @DisplayName("getKey returns mcrpg:sound")
        public void getKey_returnsSoundKey() {
            assertEquals(SoundRewardType.KEY, baseType.getKey());
        }

        @Test
        @DisplayName("key namespace is mcrpg")
        public void getKey_namespaceIsMcrpg() {
            assertEquals("mcrpg", baseType.getKey().getNamespace());
        }

        @Test
        @DisplayName("key value is sound")
        public void getKey_valueIsSound() {
            assertEquals("sound", baseType.getKey().getKey());
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
            SoundRewardType configured = baseType.fromSerializedConfig(
                    Map.of("sound", "ENTITY_PLAYER_LEVELUP", "volume", 0.5f, "pitch", 1.2f));
            assertEquals("", configured.describeForDisplay());
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("base instance serializes to empty map")
        public void serializeConfig_baseInstance_returnsEmptyMap() {
            Map<String, Object> serialized = baseType.serializeConfig();
            assertTrue(serialized.isEmpty());
        }

        @Test
        @DisplayName("configured instance serializes all fields")
        public void serializeConfig_configuredInstance_containsAllFields() {
            SoundRewardType configured = baseType.fromSerializedConfig(
                    Map.of("sound", "ENTITY_PLAYER_LEVELUP", "volume", 0.8f, "pitch", 1.5f));
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals(Sound.valueOf("ENTITY_PLAYER_LEVELUP").name(), serialized.get("sound"));
            assertEquals(0.8f, ((Number) serialized.get("volume")).floatValue(), 0.001f);
            assertEquals(1.5f, ((Number) serialized.get("pitch")).floatValue(), 0.001f);
        }
    }

    @Nested
    @DisplayName("fromSerializedConfig")
    class FromSerializedConfig {

        @Test
        @DisplayName("round-trips sound, volume, and pitch")
        public void fromSerializedConfig_roundTripsCorrectly() {
            Map<String, Object> config = Map.of(
                    "sound", "UI_TOAST_CHALLENGE_COMPLETE",
                    "volume", 0.7f,
                    "pitch", 1.3f);
            SoundRewardType configured = baseType.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals(Sound.valueOf("UI_TOAST_CHALLENGE_COMPLETE").name(), serialized.get("sound"));
            assertEquals(0.7f, ((Number) serialized.get("volume")).floatValue(), 0.001f);
            assertEquals(1.3f, ((Number) serialized.get("pitch")).floatValue(), 0.001f);
        }

        @Test
        @DisplayName("missing sound key returns base instance with empty serialization")
        public void fromSerializedConfig_missingSound_returnsBaseInstance() {
            SoundRewardType configured = baseType.fromSerializedConfig(Map.of("volume", 1.0f));
            assertTrue(configured.serializeConfig().isEmpty());
        }

        @Test
        @DisplayName("invalid sound name returns base instance with empty serialization")
        public void fromSerializedConfig_invalidSound_returnsBaseInstance() {
            SoundRewardType configured = baseType.fromSerializedConfig(
                    Map.of("sound", "NOT_A_REAL_SOUND_NAME"));
            assertTrue(configured.serializeConfig().isEmpty());
        }

        @Test
        @DisplayName("missing volume defaults to 1.0")
        public void fromSerializedConfig_missingVolume_defaultsToOne() {
            SoundRewardType configured = baseType.fromSerializedConfig(
                    Map.of("sound", "ENTITY_PLAYER_LEVELUP"));
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals(1.0f, ((Number) serialized.get("volume")).floatValue(), 0.001f);
        }

        @Test
        @DisplayName("missing pitch defaults to 1.0")
        public void fromSerializedConfig_missingPitch_defaultsToOne() {
            SoundRewardType configured = baseType.fromSerializedConfig(
                    Map.of("sound", "ENTITY_PLAYER_LEVELUP"));
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals(1.0f, ((Number) serialized.get("pitch")).floatValue(), 0.001f);
        }

        @Test
        @DisplayName("non-numeric volume falls back to default")
        public void fromSerializedConfig_nonNumericVolume_fallsBackToDefault() {
            Map<String, Object> config = new HashMap<>();
            config.put("sound", "ENTITY_PLAYER_LEVELUP");
            config.put("volume", "not_a_number");
            config.put("pitch", 1.0f);

            SoundRewardType configured = baseType.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals(1.0f, ((Number) serialized.get("volume")).floatValue(), 0.001f);
        }

        @Test
        @DisplayName("non-numeric pitch falls back to default")
        public void fromSerializedConfig_nonNumericPitch_fallsBackToDefault() {
            Map<String, Object> config = new HashMap<>();
            config.put("sound", "ENTITY_PLAYER_LEVELUP");
            config.put("volume", 1.0f);
            config.put("pitch", "invalid");

            SoundRewardType configured = baseType.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals(1.0f, ((Number) serialized.get("pitch")).floatValue(), 0.001f);
        }

        @Test
        @DisplayName("sound name is case-insensitive")
        public void fromSerializedConfig_caseInsensitiveSound() {
            SoundRewardType configured = baseType.fromSerializedConfig(
                    Map.of("sound", "entity_player_levelup"));
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals(Sound.valueOf("ENTITY_PLAYER_LEVELUP").name(), serialized.get("sound"));
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("parses valid sound with default volume and pitch")
        public void parseConfig_validSound_usesDefaults() {
            Section section = mock(Section.class);
            when(section.getString("sound")).thenReturn("ENTITY_PLAYER_LEVELUP");
            when(section.get("volume")).thenReturn(null);
            when(section.get("pitch")).thenReturn(null);

            SoundRewardType parsed = baseType.parseConfig(section);
            Map<String, Object> serialized = parsed.serializeConfig();

            assertEquals(Sound.valueOf("ENTITY_PLAYER_LEVELUP").name(), serialized.get("sound"));
            assertEquals(1.0f, ((Number) serialized.get("volume")).floatValue(), 0.001f);
            assertEquals(1.0f, ((Number) serialized.get("pitch")).floatValue(), 0.001f);
        }

        @Test
        @DisplayName("parses custom volume and pitch")
        public void parseConfig_customVolumeAndPitch() {
            Section section = mock(Section.class);
            when(section.getString("sound")).thenReturn("BLOCK_NOTE_BLOCK_PLING");
            when(section.get("volume")).thenReturn(0.5);
            when(section.get("pitch")).thenReturn(2.0);

            SoundRewardType parsed = baseType.parseConfig(section);
            Map<String, Object> serialized = parsed.serializeConfig();

            assertEquals(0.5f, ((Number) serialized.get("volume")).floatValue(), 0.001f);
            assertEquals(2.0f, ((Number) serialized.get("pitch")).floatValue(), 0.001f);
        }

        @Test
        @DisplayName("returns new instance")
        public void parseConfig_returnsNewInstance() {
            Section section = mock(Section.class);
            when(section.getString("sound")).thenReturn("ENTITY_PLAYER_LEVELUP");
            when(section.get("volume")).thenReturn(null);
            when(section.get("pitch")).thenReturn(null);

            SoundRewardType parsed = baseType.parseConfig(section);
            assertNotSame(baseType, parsed);
        }

        @Test
        @DisplayName("null sound name returns base instance")
        public void parseConfig_nullSound_returnsBaseInstance() {
            Section section = mock(Section.class);
            when(section.getString("sound")).thenReturn(null);

            SoundRewardType parsed = baseType.parseConfig(section);
            assertTrue(parsed.serializeConfig().isEmpty());
        }

        @Test
        @DisplayName("invalid sound name returns base instance")
        public void parseConfig_invalidSound_returnsBaseInstance() {
            Section section = mock(Section.class);
            when(section.getString("sound")).thenReturn("TOTALLY_FAKE_SOUND");

            SoundRewardType parsed = baseType.parseConfig(section);
            assertTrue(parsed.serializeConfig().isEmpty());
        }
    }

    @Nested
    @DisplayName("grant")
    class Grant {

        @Test
        @DisplayName("base instance grant is a no-op")
        public void grant_baseInstance_isNoOp() {
            PlayerMock player = server.addPlayer();
            assertDoesNotThrow(() -> baseType.grant(player));
        }

        @Test
        @DisplayName("configured instance plays sound to player")
        public void grant_configuredInstance_playsSound() {
            SoundRewardType configured = baseType.fromSerializedConfig(
                    Map.of("sound", "ENTITY_PLAYER_LEVELUP", "volume", 1.0f, "pitch", 1.0f));
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
