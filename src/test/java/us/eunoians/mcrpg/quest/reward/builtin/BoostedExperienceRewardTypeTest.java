package us.eunoians.mcrpg.quest.reward.builtin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BoostedExperienceRewardTypeTest extends McRPGBaseTest {

    private BoostedExperienceRewardType type;

    @BeforeEach
    public void setup() {
        type = new BoostedExperienceRewardType();
    }

    @DisplayName("getKey returns boosted_experience key")
    @Test
    public void getKey_returnsExpectedKey() {
        assertEquals(BoostedExperienceRewardType.KEY, type.getKey());
    }

    @DisplayName("getExpansionKey returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }

    @DisplayName("fromSerializedConfig with amount 500 preserves amount")
    @Test
    public void fromSerializedConfig_preservesAmount() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 500));
        assertEquals(500, configured.getNumericAmount().orElse(0));
    }

    @DisplayName("fromSerializedConfig returns zero amount when key is missing")
    @Test
    public void fromSerializedConfig_returnsZeroWhenAmountMissing() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of());
        assertEquals(0, configured.getNumericAmount().orElse(-1));
    }

    @DisplayName("fromSerializedConfig handles Number subtype (Long)")
    @Test
    public void fromSerializedConfig_handlesLongAmount() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 300L));
        assertEquals(300, configured.getNumericAmount().orElse(0));
    }

    @DisplayName("fromSerializedConfig handles Number subtype (Double)")
    @Test
    public void fromSerializedConfig_handlesDoubleAmount() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 99.7));
        assertEquals(99, configured.getNumericAmount().orElse(0));
    }

    @DisplayName("fromSerializedConfig parses valid string amount")
    @Test
    public void fromSerializedConfig_parsesStringAmount() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", "400"));
        assertEquals(400, configured.getNumericAmount().orElse(0));
    }

    @DisplayName("fromSerializedConfig returns zero for non-numeric string")
    @Test
    public void fromSerializedConfig_returnsZeroForNonNumericString() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", "not_a_number"));
        assertEquals(0, configured.getNumericAmount().orElse(-1));
    }

    @DisplayName("fromSerializedConfig preserves localization-route")
    @Test
    public void fromSerializedConfig_preservesLocalizationRoute() {
        Map<String, Object> config = new HashMap<>();
        config.put("amount", 500);
        config.put("localization-route", "quest.reward.boosted");
        BoostedExperienceRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals("quest.reward.boosted", serialized.get("localization-route"));
    }

    @DisplayName("fromSerializedConfig preserves display label")
    @Test
    public void fromSerializedConfig_preservesDisplayLabel() {
        Map<String, Object> config = new HashMap<>();
        config.put("amount", 500);
        config.put("display", "Boosted Display");
        BoostedExperienceRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals("Boosted Display", serialized.get("display"));
    }

    @DisplayName("serializeConfig round-trips amount correctly")
    @Test
    public void serializeConfig_roundTripsAmount() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 250));
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(250, ((Number) serialized.get("amount")).intValue());
    }

    @DisplayName("serializeConfig omits display and localization-route when not set")
    @Test
    public void serializeConfig_omitsOptionalFieldsWhenNotSet() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
        Map<String, Object> serialized = configured.serializeConfig();
        assertFalse(serialized.containsKey("display"));
        assertFalse(serialized.containsKey("localization-route"));
    }

    @DisplayName("withAmountMultiplier scales amount correctly")
    @Test
    public void withAmountMultiplier_scalesAmount() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
        BoostedExperienceRewardType doubled = configured.withAmountMultiplier(2.0);
        assertEquals(200, doubled.getNumericAmount().orElse(0));
    }

    @DisplayName("withAmountMultiplier enforces minimum of 1")
    @Test
    public void withAmountMultiplier_enforcesMinimumOfOne() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 1));
        BoostedExperienceRewardType scaled = configured.withAmountMultiplier(0.001);
        assertTrue(scaled.getNumericAmount().orElse(0) >= 1);
    }

    @DisplayName("withExactAmount sets the exact amount")
    @Test
    public void withExactAmount_setsExactAmount() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
        BoostedExperienceRewardType exact = configured.withExactAmount(777);
        assertEquals(777, exact.getNumericAmount().orElse(0));
    }

    @DisplayName("withLocalizationRoute returns new instance with route set")
    @Test
    public void withLocalizationRoute_returnsNewInstanceWithRoute() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
        var route = dev.dejvokep.boostedyaml.route.Route.fromString("custom.route");
        BoostedExperienceRewardType withRoute = configured.withLocalizationRoute(route);
        Map<String, Object> serialized = withRoute.serializeConfig();
        assertEquals("custom.route", serialized.get("localization-route"));
        assertEquals(100, ((Number) serialized.get("amount")).intValue());
    }

    @DisplayName("withInlineDisplayLabel returns new instance with label set")
    @Test
    public void withInlineDisplayLabel_returnsNewInstanceWithLabel() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
        BoostedExperienceRewardType withLabel = configured.withInlineDisplayLabel("My Label");
        Map<String, Object> serialized = withLabel.serializeConfig();
        assertEquals("My Label", serialized.get("display"));
        assertEquals(100, ((Number) serialized.get("amount")).intValue());
    }

    @DisplayName("getNumericAmount returns configured amount as OptionalLong")
    @Test
    public void getNumericAmount_returnsConfiguredAmount() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 750));
        assertEquals(750L, configured.getNumericAmount().getAsLong());
    }

    @DisplayName("describeForDisplay returns formatted string with amount")
    @Test
    public void describeForDisplay_returnsFormattedString() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
        String display = configured.describeForDisplay();
        assertNotNull(display);
        assertTrue(display.contains("100"));
        assertTrue(display.contains("Boosted XP"));
    }

    @DisplayName("isScalable returns true")
    @Test
    public void isScalable_returnsTrue() {
        assertTrue(type.isScalable());
    }
}
