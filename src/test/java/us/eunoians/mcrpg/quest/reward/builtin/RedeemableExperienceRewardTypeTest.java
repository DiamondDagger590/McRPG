package us.eunoians.mcrpg.quest.reward.builtin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RedeemableExperienceRewardTypeTest extends McRPGBaseTest {

    private RedeemableExperienceRewardType type;

    @BeforeEach
    public void setup() {
        type = new RedeemableExperienceRewardType();
    }

    @DisplayName("getKey returns redeemable_experience key")
    @Test
    public void getKey_returnsExpectedKey() {
        assertEquals(RedeemableExperienceRewardType.KEY, type.getKey());
    }

    @DisplayName("getExpansionKey returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }

    @DisplayName("fromSerializedConfig preserves amount")
    @Test
    public void fromSerializedConfig_preservesAmount() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 300));
        assertEquals(300, configured.getNumericAmount().orElse(0));
    }

    @DisplayName("serializeConfig round-trips amount")
    @Test
    public void serializeConfig_roundTripsAmount() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 150));
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(150, ((Number) serialized.get("amount")).intValue());
    }

    @DisplayName("withAmountMultiplier scales correctly")
    @Test
    public void withAmountMultiplier_scalesAmount() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 200));
        RedeemableExperienceRewardType doubled = configured.withAmountMultiplier(1.5);
        assertEquals(300, doubled.getNumericAmount().orElse(0));
    }

    @DisplayName("withAmountMultiplier enforces minimum of 1")
    @Test
    public void withAmountMultiplier_enforcesMinimumOfOne() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 1));
        RedeemableExperienceRewardType scaled = configured.withAmountMultiplier(0.001);
        assertTrue(scaled.getNumericAmount().orElse(0) >= 1);
    }
}
