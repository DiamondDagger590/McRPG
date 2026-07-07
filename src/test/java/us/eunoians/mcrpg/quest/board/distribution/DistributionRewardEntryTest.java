package us.eunoians.mcrpg.quest.board.distribution;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.template.condition.ConditionContext;
import us.eunoians.mcrpg.quest.board.template.condition.RewardFallback;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DistributionRewardEntryTest extends McRPGBaseTest {

    @Nested
    @DisplayName("Canonical constructor validation")
    class CanonicalConstructorValidation {

        @Test
        @DisplayName("negative minScaledAmount throws IllegalArgumentException")
        void constructor_negativeMinScaledAmount_throwsIllegalArgument() {
            QuestRewardType reward = mock(QuestRewardType.class);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new DistributionRewardEntry(reward, PotBehavior.SCALE,
                            RemainderStrategy.DISCARD, -1, 1, null));
            assertEquals("minScaledAmount must be >= 0, got: -1", ex.getMessage());
        }

        @Test
        @DisplayName("zero topCount throws IllegalArgumentException")
        void constructor_zeroTopCount_throwsIllegalArgument() {
            QuestRewardType reward = mock(QuestRewardType.class);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new DistributionRewardEntry(reward, PotBehavior.SCALE,
                            RemainderStrategy.DISCARD, 0, 0, null));
            assertEquals("topCount must be >= 1, got: 0", ex.getMessage());
        }

        @Test
        @DisplayName("negative topCount throws IllegalArgumentException")
        void constructor_negativeTopCount_throwsIllegalArgument() {
            QuestRewardType reward = mock(QuestRewardType.class);

            assertThrows(IllegalArgumentException.class,
                    () -> new DistributionRewardEntry(reward, PotBehavior.SCALE,
                            RemainderStrategy.DISCARD, 0, -5, null));
        }

        @Test
        @DisplayName("zero minScaledAmount and one topCount is valid")
        void constructor_zeroMinAndOneTop_succeeds() {
            QuestRewardType reward = mock(QuestRewardType.class);

            DistributionRewardEntry entry = new DistributionRewardEntry(reward,
                    PotBehavior.SCALE, RemainderStrategy.DISCARD, 0, 1, null);

            assertEquals(0, entry.minScaledAmount());
            assertEquals(1, entry.topCount());
        }

        @Test
        @DisplayName("valid parameters preserve all fields")
        void constructor_validParams_preservesAllFields() {
            QuestRewardType reward = mock(QuestRewardType.class);
            RewardFallback fallback = mock(RewardFallback.class);

            DistributionRewardEntry entry = new DistributionRewardEntry(reward,
                    PotBehavior.TOP_N, RemainderStrategy.TOP_CONTRIBUTOR, 5, 3, fallback);

            assertSame(reward, entry.reward());
            assertEquals(PotBehavior.TOP_N, entry.potBehavior());
            assertEquals(RemainderStrategy.TOP_CONTRIBUTOR, entry.remainderStrategy());
            assertEquals(5, entry.minScaledAmount());
            assertEquals(3, entry.topCount());
            assertSame(fallback, entry.fallback());
        }
    }

    @Nested
    @DisplayName("Convenience constructor")
    class ConvenienceConstructor {

        @Test
        @DisplayName("sets default potBehavior to SCALE")
        void convenienceConstructor_defaultPotBehavior_isScale() {
            QuestRewardType reward = mock(QuestRewardType.class);

            DistributionRewardEntry entry = new DistributionRewardEntry(reward);

            assertEquals(PotBehavior.SCALE, entry.potBehavior());
        }

        @Test
        @DisplayName("sets default remainderStrategy to DISCARD")
        void convenienceConstructor_defaultRemainderStrategy_isDiscard() {
            QuestRewardType reward = mock(QuestRewardType.class);

            DistributionRewardEntry entry = new DistributionRewardEntry(reward);

            assertEquals(RemainderStrategy.DISCARD, entry.remainderStrategy());
        }

        @Test
        @DisplayName("sets default minScaledAmount to 1")
        void convenienceConstructor_defaultMinScaledAmount_isOne() {
            QuestRewardType reward = mock(QuestRewardType.class);

            DistributionRewardEntry entry = new DistributionRewardEntry(reward);

            assertEquals(1, entry.minScaledAmount());
        }

        @Test
        @DisplayName("sets default topCount to 1")
        void convenienceConstructor_defaultTopCount_isOne() {
            QuestRewardType reward = mock(QuestRewardType.class);

            DistributionRewardEntry entry = new DistributionRewardEntry(reward);

            assertEquals(1, entry.topCount());
        }

        @Test
        @DisplayName("sets default fallback to null")
        void convenienceConstructor_defaultFallback_isNull() {
            QuestRewardType reward = mock(QuestRewardType.class);

            DistributionRewardEntry entry = new DistributionRewardEntry(reward);

            assertNull(entry.fallback());
        }

        @Test
        @DisplayName("preserves the reward reference")
        void convenienceConstructor_preservesReward() {
            QuestRewardType reward = mock(QuestRewardType.class);

            DistributionRewardEntry entry = new DistributionRewardEntry(reward);

            assertSame(reward, entry.reward());
        }
    }

    @Nested
    @DisplayName("resolveForPlayer")
    class ResolveForPlayer {

        @Test
        @DisplayName("returns primary reward when fallback is null")
        void resolveForPlayer_nullFallback_returnsPrimary() {
            QuestRewardType reward = mock(QuestRewardType.class);
            DistributionRewardEntry entry = new DistributionRewardEntry(reward);

            ConditionContext context = ConditionContext.forRewardGrant(
                    UUID.randomUUID(), null, null);

            assertSame(reward, entry.resolveForPlayer(context));
        }

        @Test
        @DisplayName("delegates to fallback when fallback is present and condition is true")
        void resolveForPlayer_fallbackConditionTrue_returnsFallbackReward() {
            QuestRewardType primary = mock(QuestRewardType.class);
            QuestRewardType fallbackReward = mock(QuestRewardType.class);
            TemplateCondition condition = mock(TemplateCondition.class);
            ConditionContext context = ConditionContext.forRewardGrant(
                    UUID.randomUUID(), null, null);

            when(condition.evaluate(context)).thenReturn(true);

            RewardFallback fallback = new RewardFallback(condition, fallbackReward);
            DistributionRewardEntry entry = new DistributionRewardEntry(primary,
                    PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, fallback);

            assertSame(fallbackReward, entry.resolveForPlayer(context));
        }

        @Test
        @DisplayName("returns primary when fallback is present but condition is false")
        void resolveForPlayer_fallbackConditionFalse_returnsPrimary() {
            QuestRewardType primary = mock(QuestRewardType.class);
            QuestRewardType fallbackReward = mock(QuestRewardType.class);
            TemplateCondition condition = mock(TemplateCondition.class);
            ConditionContext context = ConditionContext.forRewardGrant(
                    UUID.randomUUID(), null, null);

            when(condition.evaluate(context)).thenReturn(false);

            RewardFallback fallback = new RewardFallback(condition, fallbackReward);
            DistributionRewardEntry entry = new DistributionRewardEntry(primary,
                    PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, fallback);

            assertSame(primary, entry.resolveForPlayer(context));
        }
    }
}
