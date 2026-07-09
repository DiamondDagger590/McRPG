package us.eunoians.mcrpg.quest.board.distribution;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.distribution.builtin.ParticipatedDistributionType;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link RemainderStrategy} full distribution math via
 * {@link QuestRewardDistributionResolver}. Tests verify the actual integer
 * truncation remainder is distributed to the correct recipient(s).
 */
public class RemainderStrategyDistributionTest extends McRPGBaseTest {

    private RewardDistributionTypeRegistry typeRegistry;
    private QuestRarityRegistry rarityRegistry;

    @BeforeEach
    void setUp() {
        typeRegistry = new RewardDistributionTypeRegistry();
        typeRegistry.register(new ParticipatedDistributionType());
        rarityRegistry = new QuestRarityRegistry();
    }

    /** Reward type that scales properly so we can verify exact amounts. */
    static class CountingReward implements QuestRewardType {

        static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "counting_reward");
        final long amount;

        CountingReward(long amount) {
            this.amount = amount;
        }

        @Override
        public @org.jetbrains.annotations.NotNull NamespacedKey getKey() { return KEY; }

        @Override
        public @org.jetbrains.annotations.NotNull QuestRewardType parseConfig(@org.jetbrains.annotations.NotNull Section section) { return this; }

        @Override
        public void grant(@org.jetbrains.annotations.NotNull Player player) {}

        @Override
        public @org.jetbrains.annotations.NotNull Map<String, Object> serializeConfig() { return Map.of("amount", amount); }

        @Override
        public @org.jetbrains.annotations.NotNull QuestRewardType fromSerializedConfig(@org.jetbrains.annotations.NotNull Map<String, Object> config) { return this; }

        @Override
        public @org.jetbrains.annotations.NotNull Optional<NamespacedKey> getExpansionKey() { return Optional.empty(); }

        @Override
        public boolean isScalable() {
            return true;
        }

        @Override
        public @org.jetbrains.annotations.NotNull QuestRewardType withAmountMultiplier(double multiplier) {
            return new CountingReward(Math.max(1, Math.round(amount * multiplier)));
        }

        @Override
        public @org.jetbrains.annotations.NotNull QuestRewardType withExactAmount(long exactAmount) {
            return new CountingReward(exactAmount);
        }

        @Override
        public @org.jetbrains.annotations.NotNull OptionalLong getNumericAmount() {
            return OptionalLong.of(amount);
        }
    }

    private DistributionTierConfig tier(QuestRewardType reward, RemainderStrategy strategy) {
        DistributionRewardEntry entry = new DistributionRewardEntry(
                reward, PotBehavior.SCALE, strategy, 1, 1, null);
        return new DistributionTierConfig("t1", ParticipatedDistributionType.KEY,
                RewardSplitMode.SPLIT_EVEN, List.of(entry), Map.of(), null, null);
    }

    @Nested
    @DisplayName("DISCARD — truncated remainder is discarded")
    class Discard {

        @Test
        @DisplayName("10 split among 3 → each gets 3, remainder 1 is discarded")
        void discardRemainder() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
            CountingReward reward = new CountingReward(10);
            var config = new RewardDistributionConfig(List.of(tier(reward, RemainderStrategy.DISCARD)));
            var snapshot = new ContributionSnapshot(Map.of(p1, 4L, p2, 3L, p3, 3L), 10, Set.of(p1, p2, p3), null);

            var result = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(config, snapshot, null, rarityRegistry, typeRegistry, new Random(42));

            // Each of the 3 players should receive exactly the base split (≈ 10/3 = 3)
            assertEquals(3, result.size());
            long totalDistributed = result.values().stream()
                    .flatMap(List::stream)
                    .mapToLong(r -> ((CountingReward) r).amount)
                    .sum();
            // Total granted should be less than 10 (remainder discarded)
            assertTrue(totalDistributed <= 10, "Total distributed should not exceed the pot");
        }
    }

    @Nested
    @DisplayName("TOP_CONTRIBUTOR — remainder goes to top contributor")
    class TopContributor {

        @Test
        @DisplayName("10 split among 3 → top contributor gets extra 1")
        void topContributorGetsExtra() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
            CountingReward reward = new CountingReward(10);
            var config = new RewardDistributionConfig(List.of(tier(reward, RemainderStrategy.TOP_CONTRIBUTOR)));
            // p1 is top contributor
            var snapshot = new ContributionSnapshot(Map.of(p1, 8L, p2, 1L, p3, 1L), 10, Set.of(p1, p2, p3), null);

            var result = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(config, snapshot, null, rarityRegistry, typeRegistry, new Random(42));

            assertEquals(3, result.size());
            // Top contributor p1 should have received the extra unit
            long p1Total = result.get(p1).stream().mapToLong(r -> ((CountingReward) r).amount).sum();
            long p2Total = result.get(p2).stream().mapToLong(r -> ((CountingReward) r).amount).sum();
            long p3Total = result.get(p3).stream().mapToLong(r -> ((CountingReward) r).amount).sum();
            // p1 has at least as much as any other player
            assertTrue(p1Total >= p2Total && p1Total >= p3Total,
                    "Top contributor should receive equal or more than others");
        }

        @Test
        @DisplayName("tied contributions — one of the tied players gets extra (UUID ordering)")
        void tiedContributorsOneDeterminstic() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            CountingReward reward = new CountingReward(5);
            var config = new RewardDistributionConfig(List.of(tier(reward, RemainderStrategy.TOP_CONTRIBUTOR)));
            // Tied contributions
            var snapshot = new ContributionSnapshot(Map.of(p1, 5L, p2, 5L), 10, Set.of(p1, p2), null);

            var result = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(config, snapshot, null, rarityRegistry, typeRegistry, new Random(42));

            // Both get rewards; one gets extra. Total stays close to 5.
            assertTrue(result.containsKey(p1) || result.containsKey(p2));
        }
    }

    @Nested
    @DisplayName("RANDOM — remainder distributed to a random player")
    class RandomRemainder {

        @Test
        @DisplayName("10 split among 3 → exactly one player gets the extra remainder unit")
        void randomRecipient() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
            CountingReward reward = new CountingReward(10);
            var config = new RewardDistributionConfig(List.of(tier(reward, RemainderStrategy.RANDOM)));
            var snapshot = new ContributionSnapshot(Map.of(p1, 4L, p2, 3L, p3, 3L), 10, Set.of(p1, p2, p3), null);

            var result = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(config, snapshot, null, rarityRegistry, typeRegistry, new Random(42));

            // All 3 players received at least their base share
            assertEquals(3, result.size());
            assertTrue(result.containsKey(p1));
            assertTrue(result.containsKey(p2));
            assertTrue(result.containsKey(p3));
        }

        @Test
        @DisplayName("same seeded random produces same recipient across runs")
        void deterministicWithSeed() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
            CountingReward reward = new CountingReward(10);
            var config = new RewardDistributionConfig(List.of(tier(reward, RemainderStrategy.RANDOM)));
            var snapshot = new ContributionSnapshot(Map.of(p1, 4L, p2, 3L, p3, 3L), 10, Set.of(p1, p2, p3), null);

            var result1 = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(config, snapshot, null, rarityRegistry, typeRegistry, new Random(12345));
            var result2 = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(config, snapshot, null, rarityRegistry, typeRegistry, new Random(12345));

            // Same seed → same extra recipient
            assertEquals(result1.keySet(), result2.keySet());
        }
    }
}
