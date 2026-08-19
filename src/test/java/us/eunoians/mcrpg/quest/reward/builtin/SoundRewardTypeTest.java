package us.eunoians.mcrpg.quest.reward.builtin;

import org.bukkit.Sound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SoundRewardType")
public class SoundRewardTypeTest extends McRPGBaseTest {

    private SoundRewardType type;

    @BeforeEach
    public void setup() {
        type = new SoundRewardType();
    }

    @DisplayName("getKey returns sound key")
    @Test
    public void getKey_returnsExpectedKey() {
        assertEquals(SoundRewardType.KEY, type.getKey());
    }

    @DisplayName("getExpansionKey returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }

    @DisplayName("getNumericAmount returns empty for sound rewards")
    @Test
    public void getNumericAmount_returnsEmpty() {
        assertTrue(type.getNumericAmount().isEmpty());
    }

    @DisplayName("withAmountMultiplier returns same instance")
    @Test
    public void withAmountMultiplier_returnsSelf() {
        assertSame(type, type.withAmountMultiplier(2.0));
    }

    @DisplayName("describeForDisplay returns empty string")
    @Test
    public void describeForDisplay_returnsEmptyString() {
        assertEquals("", type.describeForDisplay());
    }

    @DisplayName("serializeConfig returns empty map for base instance with no sound")
    @Test
    public void serializeConfig_returnsEmptyMap_whenNoSound() {
        Map<String, Object> serialized = type.serializeConfig();
        assertTrue(serialized.isEmpty());
    }

    @DisplayName("fromSerializedConfig preserves sound name")
    @Test
    public void fromSerializedConfig_preservesSoundName() {
        Map<String, Object> config = Map.of(
                "sound", "UI_TOAST_CHALLENGE_COMPLETE",
                "volume", 1.0f,
                "pitch", 1.0f
        );
        SoundRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(Sound.UI_TOAST_CHALLENGE_COMPLETE.name(), serialized.get("sound"));
    }

    @DisplayName("fromSerializedConfig preserves volume and pitch")
    @Test
    public void fromSerializedConfig_preservesVolumeAndPitch() {
        Map<String, Object> config = Map.of(
                "sound", "ENTITY_EXPERIENCE_ORB_PICKUP",
                "volume", 0.5f,
                "pitch", 2.0f
        );
        SoundRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(0.5f, ((Number) serialized.get("volume")).floatValue(), 0.001f);
        assertEquals(2.0f, ((Number) serialized.get("pitch")).floatValue(), 0.001f);
    }

    @DisplayName("fromSerializedConfig uses default volume and pitch when missing")
    @Test
    public void fromSerializedConfig_usesDefaults_whenVolumeAndPitchMissing() {
        Map<String, Object> config = Map.of("sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        SoundRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(1.0f, ((Number) serialized.get("volume")).floatValue(), 0.001f);
        assertEquals(1.0f, ((Number) serialized.get("pitch")).floatValue(), 0.001f);
    }

    @DisplayName("fromSerializedConfig returns no-op instance when sound is missing")
    @Test
    public void fromSerializedConfig_returnsNoOp_whenSoundMissing() {
        Map<String, Object> config = new HashMap<>();
        config.put("volume", 1.0f);
        SoundRewardType configured = type.fromSerializedConfig(config);
        assertTrue(configured.serializeConfig().isEmpty());
    }

    @DisplayName("fromSerializedConfig returns no-op instance when sound is invalid")
    @Test
    public void fromSerializedConfig_returnsNoOp_whenSoundInvalid() {
        Map<String, Object> config = Map.of("sound", "NOT_A_REAL_SOUND_XYZZY");
        SoundRewardType configured = type.fromSerializedConfig(config);
        assertTrue(configured.serializeConfig().isEmpty());
    }

    @DisplayName("fromSerializedConfig handles case-insensitive sound names")
    @Test
    public void fromSerializedConfig_handlesCaseInsensitiveSoundNames() {
        Map<String, Object> config = Map.of("sound", "entity_experience_orb_pickup");
        SoundRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(Sound.ENTITY_EXPERIENCE_ORB_PICKUP.name(), serialized.get("sound"));
    }

    @DisplayName("fromSerializedConfig handles non-numeric volume gracefully")
    @Test
    public void fromSerializedConfig_usesDefault_whenVolumeNotNumeric() {
        Map<String, Object> config = new HashMap<>();
        config.put("sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        config.put("volume", "not-a-number");
        config.put("pitch", "bad");
        SoundRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(1.0f, ((Number) serialized.get("volume")).floatValue(), 0.001f);
        assertEquals(1.0f, ((Number) serialized.get("pitch")).floatValue(), 0.001f);
    }

    @DisplayName("fromSerializedConfig handles null volume and pitch gracefully")
    @Test
    public void fromSerializedConfig_usesDefault_whenVolumeAndPitchNull() {
        Map<String, Object> config = new HashMap<>();
        config.put("sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        config.put("volume", null);
        config.put("pitch", null);
        SoundRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(1.0f, ((Number) serialized.get("volume")).floatValue(), 0.001f);
        assertEquals(1.0f, ((Number) serialized.get("pitch")).floatValue(), 0.001f);
    }

    @DisplayName("fromSerializedConfig round-trips through serializeConfig")
    @Test
    public void fromSerializedConfig_roundTrips() {
        Map<String, Object> original = Map.of(
                "sound", "UI_TOAST_CHALLENGE_COMPLETE",
                "volume", 0.8f,
                "pitch", 1.5f
        );
        SoundRewardType first = type.fromSerializedConfig(original);
        Map<String, Object> serialized = first.serializeConfig();
        SoundRewardType second = type.fromSerializedConfig(serialized);
        Map<String, Object> reSerialized = second.serializeConfig();
        assertEquals(serialized.get("sound"), reSerialized.get("sound"));
        assertEquals(
                ((Number) serialized.get("volume")).floatValue(),
                ((Number) reSerialized.get("volume")).floatValue(),
                0.001f);
        assertEquals(
                ((Number) serialized.get("pitch")).floatValue(),
                ((Number) reSerialized.get("pitch")).floatValue(),
                0.001f);
    }

    @DisplayName("fromSerializedConfig accepts Number subtypes for volume and pitch")
    @Test
    public void fromSerializedConfig_acceptsNumberSubtypes() {
        Map<String, Object> config = Map.of(
                "sound", "ENTITY_EXPERIENCE_ORB_PICKUP",
                "volume", 1.0,
                "pitch", 2L
        );
        SoundRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(1.0f, ((Number) serialized.get("volume")).floatValue(), 0.001f);
        assertEquals(2.0f, ((Number) serialized.get("pitch")).floatValue(), 0.001f);
    }

    @DisplayName("fromSerializedConfig returns a new instance")
    @Test
    public void fromSerializedConfig_returnsNewInstance() {
        Map<String, Object> config = Map.of("sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        SoundRewardType configured = type.fromSerializedConfig(config);
        assertNotSame(type, configured);
    }

    @DisplayName("describeForDisplay returns empty string on configured instance")
    @Test
    public void describeForDisplay_returnsEmptyString_whenConfigured() {
        Map<String, Object> config = Map.of("sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        SoundRewardType configured = type.fromSerializedConfig(config);
        assertEquals("", configured.describeForDisplay());
    }

    @DisplayName("getKey is consistent between base and configured instances")
    @Test
    public void getKey_isConsistent_betweenBaseAndConfigured() {
        SoundRewardType configured = type.fromSerializedConfig(Map.of("sound", "ENTITY_EXPERIENCE_ORB_PICKUP"));
        assertEquals(type.getKey(), configured.getKey());
    }

    @DisplayName("grant is a no-op when sound is null")
    @Test
    public void grant_isNoOp_whenSoundNull() {
        var player = server.addPlayer();
        assertDoesNotThrow(() -> type.grant(player));
    }
}
