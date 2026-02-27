package us.eunoians.mcrpg.quest.reward.builtin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

import java.util.Map;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.*;

class ScalableCommandRewardTypeTest {

    @Test
    @DisplayName("getNumericAmount returns base amount")
    void numericAmountReturnsBase() {
        ScalableCommandRewardType reward = new ScalableCommandRewardType()
                .fromSerializedConfig(Map.of("command", "give {player} diamond {amount}", "base-amount", 10L));
        OptionalLong amount = reward.getNumericAmount();
        assertTrue(amount.isPresent());
        assertEquals(10L, amount.getAsLong());
    }

    @Test
    @DisplayName("withAmountMultiplier scales amount correctly")
    void withAmountMultiplierScales() {
        ScalableCommandRewardType base = new ScalableCommandRewardType()
                .fromSerializedConfig(Map.of("command", "give {player} diamond {amount}", "base-amount", 10L));
        QuestRewardType scaled = base.withAmountMultiplier(0.5);
        assertEquals(5L, scaled.getNumericAmount().orElse(-1));
    }

    @Test
    @DisplayName("withAmountMultiplier enforces minimum of 1")
    void withAmountMultiplierMinimum() {
        ScalableCommandRewardType base = new ScalableCommandRewardType()
                .fromSerializedConfig(Map.of("command", "give {player} diamond {amount}", "base-amount", 10L));
        QuestRewardType scaled = base.withAmountMultiplier(0.01);
        assertEquals(1L, scaled.getNumericAmount().orElse(-1));
    }

    @Test
    @DisplayName("serializeConfig round-trips correctly")
    void serializeRoundTrip() {
        ScalableCommandRewardType original = new ScalableCommandRewardType()
                .fromSerializedConfig(Map.of("command", "eco give {player} {amount}", "base-amount", 100L));
        Map<String, Object> serialized = original.serializeConfig();
        ScalableCommandRewardType restored = new ScalableCommandRewardType()
                .fromSerializedConfig(serialized);
        assertEquals(100L, restored.getNumericAmount().orElse(-1));
        assertEquals(serialized, restored.serializeConfig());
    }

    @Test
    @DisplayName("default constructor creates empty instance")
    void defaultConstructor() {
        ScalableCommandRewardType empty = new ScalableCommandRewardType();
        assertEquals(0L, empty.getNumericAmount().orElse(-1));
    }
}
