package us.eunoians.mcrpg.quest.reward.builtin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

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

    @DisplayName("serializeConfig round-trips amount correctly")
    @Test
    public void serializeConfig_roundTripsAmount() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 250));
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(250, ((Number) serialized.get("amount")).intValue());
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

    @DisplayName("getNumericAmount returns configured amount as OptionalLong")
    @Test
    public void getNumericAmount_returnsConfiguredAmount() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 750));
        assertEquals(750L, configured.getNumericAmount().getAsLong());
    }

    @DisplayName("describeForDisplay returns non-null string")
    @Test
    public void describeForDisplay_returnsNonNull() {
        BoostedExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
        assertNotNull(configured.describeForDisplay());
    }
}
