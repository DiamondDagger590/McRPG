package us.eunoians.mcrpg.quest.reward.builtin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExperienceRewardTypeTest extends McRPGBaseTest {

    private ExperienceRewardType baseType;

    @BeforeEach
    public void setup() {
        baseType = new ExperienceRewardType();
    }

    @DisplayName("Given the type, when calling getKey, then it returns the experience key")
    @Test
    public void getKey_returnsExperienceKey() {
        assertEquals(ExperienceRewardType.KEY, baseType.getKey());
    }

    @DisplayName("Given the type, when calling getExpansionKey, then it returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(baseType.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, baseType.getExpansionKey().get());
    }

    @DisplayName("Given serialized config, when round-tripping, then values are preserved")
    @Test
    public void serializeAndDeserialize_roundTripsCorrectly() {
        ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of("skill", "MINING", "amount", 500L));
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals("MINING", serialized.get("skill"));
        assertEquals(500L, ((Number) serialized.get("amount")).longValue());
    }

    @DisplayName("Given empty config, when deserializing, then defaults are used")
    @Test
    public void fromSerializedConfig_usesDefaults_whenEmpty() {
        ExperienceRewardType configured = baseType.fromSerializedConfig(Map.of());
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals("", serialized.get("skill"));
        assertEquals(0L, ((Number) serialized.get("amount")).longValue());
    }
}
