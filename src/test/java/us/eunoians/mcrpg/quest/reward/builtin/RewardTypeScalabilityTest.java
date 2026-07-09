package us.eunoians.mcrpg.quest.reward.builtin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies every built-in scalable reward type honours the {@link QuestRewardType#isScalable()} /
 * {@link QuestRewardType#withExactAmount(long)} contract the distribution resolver relies on. If a
 * type accidentally inherited the {@code isScalable() == false} default, the resolver would silently
 * fall back to granting unscaled rewards.
 */
public class RewardTypeScalabilityTest extends McRPGBaseTest {

    static Stream<Arguments> scalableRewardTypes() {
        return Stream.of(
                Arguments.of("experience", (Supplier<QuestRewardType>) ExperienceRewardType::new),
                Arguments.of("item", (Supplier<QuestRewardType>) ItemRewardType::new),
                Arguments.of("boosted_experience", (Supplier<QuestRewardType>) BoostedExperienceRewardType::new),
                Arguments.of("redeemable_experience", (Supplier<QuestRewardType>) RedeemableExperienceRewardType::new),
                Arguments.of("redeemable_levels", (Supplier<QuestRewardType>) RedeemableLevelsRewardType::new),
                Arguments.of("scalable_command", (Supplier<QuestRewardType>) ScalableCommandRewardType::new));
    }

    @ParameterizedTest(name = "{0} is scalable")
    @MethodSource("scalableRewardTypes")
    @DisplayName("built-in scalable reward types report isScalable() == true")
    void isScalable_returnsTrue(String name, Supplier<QuestRewardType> factory) {
        assertTrue(factory.get().isScalable(), name + " must be scalable");
    }

    @ParameterizedTest(name = "{0} withExactAmount sets the exact amount")
    @MethodSource("scalableRewardTypes")
    @DisplayName("withExactAmount produces a reward carrying the exact numeric amount")
    void withExactAmount_setsExactNumericAmount(String name, Supplier<QuestRewardType> factory) {
        QuestRewardType scaled = factory.get().withExactAmount(7);
        assertTrue(scaled.getNumericAmount().isPresent(), name + " must expose a numeric amount");
        assertEquals(7L, scaled.getNumericAmount().getAsLong(), name + " must carry the exact amount");
    }

    @ParameterizedTest(name = "{0} withExactAmount(1) yields amount 1")
    @MethodSource("scalableRewardTypes")
    @DisplayName("withExactAmount(1) — the single-unit remainder share — yields amount 1")
    void withExactAmount_singleUnit_yieldsOne(String name, Supplier<QuestRewardType> factory) {
        assertEquals(1L, factory.get().withExactAmount(1).getNumericAmount().getAsLong(), name);
    }
}
