package us.eunoians.mcrpg.quest.reward;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestRewardTypeDefaultMethodsTest extends McRPGBaseTest {

    private static final NamespacedKey REWARD_KEY = new NamespacedKey("test", "mock_reward");
    private static final NamespacedKey EXPANSION_KEY = new NamespacedKey("test", "mock_expansion");

    private MockQuestRewardType rewardType;

    @BeforeEach
    void setUp() {
        rewardType = new MockQuestRewardType(REWARD_KEY, EXPANSION_KEY);
    }

    @Nested
    @DisplayName("withAmountMultiplier")
    class WithAmountMultiplier {

        @Test
        @DisplayName("returns this by default")
        void returnsThis_byDefault() {
            assertSame(rewardType, rewardType.withAmountMultiplier(0.5));
        }
    }

    @Nested
    @DisplayName("getNumericAmount")
    class GetNumericAmount {

        @Test
        @DisplayName("returns empty by default")
        void returnsEmpty_byDefault() {
            OptionalLong result = rewardType.getNumericAmount();
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("isScalable")
    class IsScalable {

        @Test
        @DisplayName("returns false by default")
        void returnsFalse_byDefault() {
            assertFalse(rewardType.isScalable());
        }
    }

    @Nested
    @DisplayName("withExactAmount")
    class WithExactAmount {

        @Test
        @DisplayName("returns this by default")
        void returnsThis_byDefault() {
            assertSame(rewardType, rewardType.withExactAmount(5));
        }
    }

    @Nested
    @DisplayName("describeForDisplay (no-arg)")
    class DescribeForDisplayNoArg {

        @Test
        @DisplayName("returns type name from key with underscores replaced by spaces")
        void returnsTypeName_fromKey() {
            assertEquals("mock reward", rewardType.describeForDisplay());
        }

        @Test
        @DisplayName("returns formatted name for single-word key")
        void returnsFormattedName_singleWord() {
            MockQuestRewardType singleWord = new MockQuestRewardType(
                    new NamespacedKey("test", "experience"),
                    EXPANSION_KEY
            );
            assertEquals("experience", singleWord.describeForDisplay());
        }

        @Test
        @DisplayName("includes numeric amount when getNumericAmount returns value")
        void includesNumericAmount_whenPresent() {
            QuestRewardType withAmount = new NumericMockRewardType(
                    new NamespacedKey("test", "gold_coins"),
                    EXPANSION_KEY,
                    100
            );
            assertEquals("100 gold coins", withAmount.describeForDisplay());
        }
    }

    @Nested
    @DisplayName("describeForDisplay (McRPGPlayer)")
    class DescribeForDisplayWithPlayer {

        @Test
        @DisplayName("delegates to no-arg describeForDisplay by default")
        void delegatesToNoArgByDefault() {
            assertEquals(rewardType.describeForDisplay(), rewardType.describeForDisplay());
        }
    }

    @Nested
    @DisplayName("withLocalizationRoute")
    class WithLocalizationRoute {

        @Test
        @DisplayName("returns this by default")
        void returnsThis_byDefault() {
            Route route = Route.fromString("test.path");
            assertSame(rewardType, rewardType.withLocalizationRoute(route));
        }
    }

    @Nested
    @DisplayName("withInlineDisplayLabel")
    class WithInlineDisplayLabel {

        @Test
        @DisplayName("returns this by default")
        void returnsThis_byDefault() {
            assertSame(rewardType, rewardType.withInlineDisplayLabel("Some Label"));
        }
    }

    /**
     * A reward type that returns a numeric amount, for testing the describeForDisplay branch.
     */
    private static class NumericMockRewardType extends MockQuestRewardType {

        private final long amount;

        NumericMockRewardType(NamespacedKey key, NamespacedKey expansionKey, long amount) {
            super(key, expansionKey);
            this.amount = amount;
        }

        @Override
        public OptionalLong getNumericAmount() {
            return OptionalLong.of(amount);
        }
    }
}
