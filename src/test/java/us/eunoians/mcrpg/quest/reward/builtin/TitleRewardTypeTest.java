package us.eunoians.mcrpg.quest.reward.builtin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TitleRewardType")
public class TitleRewardTypeTest extends McRPGBaseTest {

    private TitleRewardType type;

    @BeforeEach
    public void setup() {
        type = new TitleRewardType();
    }

    @DisplayName("getKey returns title_message key")
    @Test
    public void getKey_returnsExpectedKey() {
        assertEquals(TitleRewardType.KEY, type.getKey());
    }

    @DisplayName("getExpansionKey returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }

    @DisplayName("getNumericAmount returns empty for title rewards")
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

    @DisplayName("serializeConfig includes all fields with defaults for base instance")
    @Test
    public void serializeConfig_includesAllDefaultFields() {
        Map<String, Object> serialized = type.serializeConfig();
        assertEquals("", serialized.get("title"));
        assertEquals("", serialized.get("subtitle"));
        assertEquals(10, serialized.get("fade-in"));
        assertEquals(70, serialized.get("stay"));
        assertEquals(20, serialized.get("fade-out"));
    }

    @DisplayName("fromSerializedConfig preserves title and subtitle")
    @Test
    public void fromSerializedConfig_preservesTitleAndSubtitle() {
        Map<String, Object> config = Map.of(
                "title", "<primary>Quest Complete!",
                "subtitle", "<body>Well done."
        );
        TitleRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals("<primary>Quest Complete!", serialized.get("title"));
        assertEquals("<body>Well done.", serialized.get("subtitle"));
    }

    @DisplayName("fromSerializedConfig preserves timing values")
    @Test
    public void fromSerializedConfig_preservesTimingValues() {
        Map<String, Object> config = Map.of(
                "title", "Hello",
                "subtitle", "",
                "fade-in", 5,
                "stay", 100,
                "fade-out", 30
        );
        TitleRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(5, serialized.get("fade-in"));
        assertEquals(100, serialized.get("stay"));
        assertEquals(30, serialized.get("fade-out"));
    }

    @DisplayName("fromSerializedConfig uses defaults for missing timing fields")
    @Test
    public void fromSerializedConfig_usesDefaults_whenTimingFieldsMissing() {
        Map<String, Object> config = Map.of("title", "Test");
        TitleRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(10, serialized.get("fade-in"));
        assertEquals(70, serialized.get("stay"));
        assertEquals(20, serialized.get("fade-out"));
    }

    @DisplayName("fromSerializedConfig uses empty string for missing title and subtitle")
    @Test
    public void fromSerializedConfig_usesEmptyString_whenTitleAndSubtitleMissing() {
        Map<String, Object> config = Map.of("fade-in", 5);
        TitleRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals("", serialized.get("title"));
        assertEquals("", serialized.get("subtitle"));
    }

    @DisplayName("fromSerializedConfig handles non-numeric timing gracefully")
    @Test
    public void fromSerializedConfig_usesDefault_whenTimingNotNumeric() {
        Map<String, Object> config = new HashMap<>();
        config.put("title", "Test");
        config.put("fade-in", "not-a-number");
        config.put("stay", "bad");
        config.put("fade-out", "invalid");
        TitleRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(10, serialized.get("fade-in"));
        assertEquals(70, serialized.get("stay"));
        assertEquals(20, serialized.get("fade-out"));
    }

    @DisplayName("fromSerializedConfig handles null timing values gracefully")
    @Test
    public void fromSerializedConfig_usesDefault_whenTimingNull() {
        Map<String, Object> config = new HashMap<>();
        config.put("title", "Test");
        config.put("fade-in", null);
        config.put("stay", null);
        config.put("fade-out", null);
        TitleRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(10, serialized.get("fade-in"));
        assertEquals(70, serialized.get("stay"));
        assertEquals(20, serialized.get("fade-out"));
    }

    @DisplayName("fromSerializedConfig round-trips through serializeConfig")
    @Test
    public void fromSerializedConfig_roundTrips() {
        Map<String, Object> original = Map.of(
                "title", "<primary>Level Up!",
                "subtitle", "<body>You reached level 10",
                "fade-in", 15,
                "stay", 80,
                "fade-out", 25
        );
        TitleRewardType first = type.fromSerializedConfig(original);
        Map<String, Object> serialized = first.serializeConfig();
        TitleRewardType second = type.fromSerializedConfig(serialized);
        Map<String, Object> reSerialized = second.serializeConfig();
        assertEquals(serialized, reSerialized);
    }

    @DisplayName("fromSerializedConfig accepts Number subtypes for timing")
    @Test
    public void fromSerializedConfig_acceptsNumberSubtypes() {
        Map<String, Object> config = Map.of(
                "title", "Test",
                "fade-in", 5L,
                "stay", 100.0,
                "fade-out", (short) 30
        );
        TitleRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(5, serialized.get("fade-in"));
        assertEquals(100, serialized.get("stay"));
        assertEquals(30, serialized.get("fade-out"));
    }

    @DisplayName("fromSerializedConfig returns a new instance")
    @Test
    public void fromSerializedConfig_returnsNewInstance() {
        Map<String, Object> config = Map.of("title", "New");
        TitleRewardType configured = type.fromSerializedConfig(config);
        assertNotSame(type, configured);
    }

    @DisplayName("describeForDisplay returns empty string on configured instance")
    @Test
    public void describeForDisplay_returnsEmptyString_whenConfigured() {
        Map<String, Object> config = Map.of("title", "<primary>Quest Complete!", "subtitle", "<body>Well done.");
        TitleRewardType configured = type.fromSerializedConfig(config);
        assertEquals("", configured.describeForDisplay());
    }

    @DisplayName("getKey is consistent between base and configured instances")
    @Test
    public void getKey_isConsistent_betweenBaseAndConfigured() {
        TitleRewardType configured = type.fromSerializedConfig(Map.of("title", "Test"));
        assertEquals(type.getKey(), configured.getKey());
    }
}
