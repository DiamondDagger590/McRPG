package us.eunoians.mcrpg.quest.reward.builtin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RedeemableLevelsRewardTypeTest extends McRPGBaseTest {

    private RedeemableLevelsRewardType type;

    @BeforeEach
    public void setup() {
        type = new RedeemableLevelsRewardType();
    }

    @DisplayName("getKey returns redeemable_levels key")
    @Test
    public void getKey_returnsExpectedKey() {
        assertEquals(RedeemableLevelsRewardType.KEY, type.getKey());
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
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 5));
        assertEquals(5, configured.getNumericAmount().orElse(0));
    }

    @DisplayName("serializeConfig round-trips amount")
    @Test
    public void serializeConfig_roundTripsAmount() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 3));
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(3, ((Number) serialized.get("amount")).intValue());
    }

    @DisplayName("withAmountMultiplier scales correctly")
    @Test
    public void withAmountMultiplier_scalesAmount() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 4));
        RedeemableLevelsRewardType doubled = configured.withAmountMultiplier(2.0);
        assertEquals(8, doubled.getNumericAmount().orElse(0));
    }

    @DisplayName("withAmountMultiplier enforces minimum of 1")
    @Test
    public void withAmountMultiplier_enforcesMinimumOfOne() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 1));
        RedeemableLevelsRewardType scaled = configured.withAmountMultiplier(0.001);
        assertTrue(scaled.getNumericAmount().orElse(0) >= 1);
    }
}
