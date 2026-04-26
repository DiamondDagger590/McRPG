package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

import java.util.Map;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    @DisplayName("withLocalizationRoute returns a new instance with the route set")
    void withLocalizationRoute_returnsNewInstanceWithRoute() {
        ScalableCommandRewardType base = new ScalableCommandRewardType()
                .fromSerializedConfig(Map.of("command", "give {player} diamond {amount}", "base-amount", 5L));
        Route route = Route.fromString("quests.mcrpg.my_quest.rewards.diamond_reward");
        ScalableCommandRewardType withRoute = (ScalableCommandRewardType) base.withLocalizationRoute(route);

        assertNotSame(base, withRoute);
        assertEquals("quests.mcrpg.my_quest.rewards.diamond_reward",
                withRoute.serializeConfig().get("localization-route"));
    }

    @Test
    @DisplayName("localization route is absent in serialized config when not set")
    void serializeConfig_withoutRoute_omitsLocalizationRouteKey() {
        ScalableCommandRewardType base = new ScalableCommandRewardType()
                .fromSerializedConfig(Map.of("command", "give {player} diamond {amount}", "base-amount", 5L));
        assertNull(base.serializeConfig().get("localization-route"));
    }

    @Test
    @DisplayName("localization route round-trips through serialize/deserialize")
    void localizationRoute_roundTrips() {
        Map<String, Object> config = Map.of(
                "command", "give {player} diamond {amount}",
                "base-amount", 10L,
                "localization-route", "quests.mcrpg.my_quest.rewards.diamond_reward"
        );
        ScalableCommandRewardType restored = new ScalableCommandRewardType().fromSerializedConfig(config);
        assertEquals("quests.mcrpg.my_quest.rewards.diamond_reward",
                restored.serializeConfig().get("localization-route"));
    }

    @Test
    @DisplayName("withAmountMultiplier preserves the localization route on the scaled instance")
    void withAmountMultiplier_preservesLocalizationRoute() {
        Route route = Route.fromString("quests.mcrpg.my_quest.rewards.diamond_reward");
        ScalableCommandRewardType base = (ScalableCommandRewardType) new ScalableCommandRewardType()
                .fromSerializedConfig(Map.of("command", "give {player} diamond {amount}", "base-amount", 10L))
                .withLocalizationRoute(route);
        QuestRewardType scaled = base.withAmountMultiplier(0.5);
        assertNotNull(scaled.serializeConfig().get("localization-route"));
        assertEquals("quests.mcrpg.my_quest.rewards.diamond_reward",
                scaled.serializeConfig().get("localization-route"));
    }
}
