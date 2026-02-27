package us.eunoians.mcrpg.quest.board.distribution;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PotBehaviorDistributionTest {

    private static final NamespacedKey REWARD_KEY = NamespacedKey.fromString("mcrpg:experience");

    private QuestRewardType scalableReward(long amount) {
        QuestRewardType reward = mock(QuestRewardType.class);
        when(reward.getKey()).thenReturn(REWARD_KEY);
        when(reward.getNumericAmount()).thenReturn(OptionalLong.of(amount));
        return reward;
    }

    private QuestRewardType nonScalableReward() {
        QuestRewardType reward = mock(QuestRewardType.class);
        when(reward.getKey()).thenReturn(REWARD_KEY);
        when(reward.getNumericAmount()).thenReturn(OptionalLong.empty());
        return reward;
    }

    @Nested
    @DisplayName("PotBehavior enum values")
    class PotBehaviorValues {

        @Test
        @DisplayName("SCALE behavior for proportional distribution")
        void scaleBehavior() {
            DistributionRewardEntry entry = new DistributionRewardEntry(
                    scalableReward(100), PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, null);
            assertEquals(PotBehavior.SCALE, entry.potBehavior());
        }

        @Test
        @DisplayName("ALL gives full unscaled reward to all qualifying players")
        void allBehavior() {
            DistributionRewardEntry entry = new DistributionRewardEntry(
                    nonScalableReward(), PotBehavior.ALL, RemainderStrategy.DISCARD, 1, 1, null);
            assertEquals(PotBehavior.ALL, entry.potBehavior());
        }

        @Test
        @DisplayName("TOP_N with top-count 1 gives to single player")
        void topOnePlayer() {
            DistributionRewardEntry entry = new DistributionRewardEntry(
                    nonScalableReward(), PotBehavior.TOP_N, RemainderStrategy.DISCARD, 1, 1, null);
            assertEquals(PotBehavior.TOP_N, entry.potBehavior());
            assertEquals(1, entry.topCount());
        }

        @Test
        @DisplayName("TOP_N with top-count 3")
        void topThree() {
            DistributionRewardEntry entry = new DistributionRewardEntry(
                    nonScalableReward(), PotBehavior.TOP_N, RemainderStrategy.DISCARD, 1, 3, null);
            assertEquals(3, entry.topCount());
        }
    }

    @Nested
    @DisplayName("RemainderStrategy enum values")
    class RemainderStrategyTests {

        @Test
        @DisplayName("DISCARD remainder strategy")
        void discardRemainder() {
            DistributionRewardEntry entry = new DistributionRewardEntry(
                    scalableReward(10), PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, null);
            assertEquals(RemainderStrategy.DISCARD, entry.remainderStrategy());
        }

        @Test
        @DisplayName("TOP_CONTRIBUTOR remainder strategy")
        void topContributorRemainder() {
            DistributionRewardEntry entry = new DistributionRewardEntry(
                    scalableReward(10), PotBehavior.SCALE, RemainderStrategy.TOP_CONTRIBUTOR, 1, 1, null);
            assertEquals(RemainderStrategy.TOP_CONTRIBUTOR, entry.remainderStrategy());
        }

        @Test
        @DisplayName("RANDOM remainder strategy")
        void randomRemainder() {
            DistributionRewardEntry entry = new DistributionRewardEntry(
                    scalableReward(10), PotBehavior.SCALE, RemainderStrategy.RANDOM, 1, 1, null);
            assertEquals(RemainderStrategy.RANDOM, entry.remainderStrategy());
        }
    }

    @Nested
    @DisplayName("MinScaledAmount")
    class MinScaledAmountTests {

        @Test
        @DisplayName("min-scaled-amount defaults to 1")
        void defaultMinScaled() {
            DistributionRewardEntry entry = new DistributionRewardEntry(
                    scalableReward(1000), PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, null);
            assertEquals(1, entry.minScaledAmount());
        }

        @Test
        @DisplayName("custom min-scaled-amount preserved")
        void customMinScaled() {
            DistributionRewardEntry entry = new DistributionRewardEntry(
                    scalableReward(1000), PotBehavior.SCALE, RemainderStrategy.DISCARD, 10, 1, null);
            assertEquals(10, entry.minScaledAmount());
        }
    }

    @Nested
    @DisplayName("DistributionRewardEntry record")
    class DistributionRewardEntryTests {

        @Test
        @DisplayName("reward accessor returns correct reward")
        void rewardAccessor() {
            QuestRewardType reward = scalableReward(50);
            DistributionRewardEntry entry = new DistributionRewardEntry(
                    reward, PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, null);
            assertSame(reward, entry.reward());
        }

        @Test
        @DisplayName("null fallback is allowed")
        void nullFallback() {
            DistributionRewardEntry entry = new DistributionRewardEntry(
                    nonScalableReward(), PotBehavior.ALL, RemainderStrategy.DISCARD, 1, 1, null);
            assertNull(entry.fallback());
        }
    }
}
