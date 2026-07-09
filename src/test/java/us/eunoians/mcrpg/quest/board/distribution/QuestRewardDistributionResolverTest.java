package us.eunoians.mcrpg.quest.board.distribution;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.distribution.builtin.ParticipatedDistributionType;
import us.eunoians.mcrpg.quest.board.distribution.builtin.TopPlayersDistributionType;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestRewardDistributionResolverTest extends McRPGBaseTest {

    private RewardDistributionTypeRegistry typeRegistry;
    private QuestRarityRegistry rarityRegistry;
    private QuestRewardDistributionResolver resolver;

    @BeforeEach
    void setUp() {
        typeRegistry = new RewardDistributionTypeRegistry();
        typeRegistry.register(new TopPlayersDistributionType());
        typeRegistry.register(new ParticipatedDistributionType());
        rarityRegistry = new QuestRarityRegistry();
        resolver = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test"));
    }

    /**
     * Simple test reward type that tracks its amount for split-mode verification.
     */
    static class TestRewardType implements QuestRewardType {

        static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test_reward");
        private final long amount;

        TestRewardType(long amount) {
            this.amount = amount;
        }

        long getAmount() {
            return amount;
        }

        @Override
        public @org.jetbrains.annotations.NotNull NamespacedKey getKey() {
            return KEY;
        }

        @Override
        public @org.jetbrains.annotations.NotNull QuestRewardType parseConfig(@org.jetbrains.annotations.NotNull Section section) {
            return this;
        }

        @Override
        public void grant(@org.jetbrains.annotations.NotNull Player player) {
        }

        @Override
        public @org.jetbrains.annotations.NotNull Map<String, Object> serializeConfig() {
            return Map.of("amount", amount);
        }

        @Override
        public @org.jetbrains.annotations.NotNull QuestRewardType fromSerializedConfig(@org.jetbrains.annotations.NotNull Map<String, Object> config) {
            return this;
        }

        @Override
        public @org.jetbrains.annotations.NotNull Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }

        @Override
        public boolean isScalable() {
            return true;
        }

        @Override
        public @org.jetbrains.annotations.NotNull QuestRewardType withAmountMultiplier(double multiplier) {
            return new TestRewardType(Math.max(1, (long) (amount * multiplier)));
        }

        @Override
        public @org.jetbrains.annotations.NotNull QuestRewardType withExactAmount(long exactAmount) {
            return new TestRewardType(exactAmount);
        }

        @Override
        public @org.jetbrains.annotations.NotNull java.util.OptionalLong getNumericAmount() {
            return java.util.OptionalLong.of(amount);
        }
    }

    @Nested
    @DisplayName("INDIVIDUAL split mode")
    class IndividualSplit {

        @DisplayName("each qualifying player receives full rewards")
        @Test
        void eachPlayerFullRewards() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var reward = new TestRewardType(1000);
            var tier = new DistributionTierConfig("t1", ParticipatedDistributionType.KEY,
                    RewardSplitMode.INDIVIDUAL, List.of(reward), Map.of(), null, null, true);
            var config = new RewardDistributionConfig(List.of(tier));
            var snapshot = new ContributionSnapshot(Map.of(p1, 60L, p2, 40L), 100, Set.of(p1, p2), null);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertEquals(2, result.size());
            assertTrue(result.containsKey(p1));
            assertTrue(result.containsKey(p2));
            assertEquals(1000, ((TestRewardType) result.get(p1).get(0)).getAmount());
            assertEquals(1000, ((TestRewardType) result.get(p2).get(0)).getAmount());
        }
    }

    @Nested
    @DisplayName("SPLIT_EVEN split mode")
    class SplitEven {

        @DisplayName("pot evenly divided among qualifying players")
        @Test
        void potEvenlyDivided() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var reward = new TestRewardType(1000);
            var tier = new DistributionTierConfig("t1", ParticipatedDistributionType.KEY,
                    RewardSplitMode.SPLIT_EVEN, List.of(reward), Map.of(), null, null, true);
            var config = new RewardDistributionConfig(List.of(tier));
            var snapshot = new ContributionSnapshot(Map.of(p1, 60L, p2, 40L), 100, Set.of(p1, p2), null);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertEquals(2, result.size());
            assertEquals(500, ((TestRewardType) result.get(p1).get(0)).getAmount());
            assertEquals(500, ((TestRewardType) result.get(p2).get(0)).getAmount());
        }

        @DisplayName("single qualifying player gets the full pot")
        @Test
        void singlePlayerFullPot() {
            UUID p1 = UUID.randomUUID();
            var reward = new TestRewardType(1000);
            var tier = new DistributionTierConfig("t1", ParticipatedDistributionType.KEY,
                    RewardSplitMode.SPLIT_EVEN, List.of(reward), Map.of(), null, null, true);
            var config = new RewardDistributionConfig(List.of(tier));
            var snapshot = new ContributionSnapshot(Map.of(p1, 100L), 100, Set.of(p1), null);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertEquals(1, result.size());
            assertEquals(1000, ((TestRewardType) result.get(p1).get(0)).getAmount());
        }
    }

    @Nested
    @DisplayName("SPLIT_PROPORTIONAL split mode")
    class SplitProportional {

        @DisplayName("pot divided proportionally by contribution")
        @Test
        void potDividedProportionally() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var reward = new TestRewardType(1000);
            var tier = new DistributionTierConfig("t1", ParticipatedDistributionType.KEY,
                    RewardSplitMode.SPLIT_PROPORTIONAL, List.of(reward), Map.of(), null, null, true);
            var config = new RewardDistributionConfig(List.of(tier));
            var snapshot = new ContributionSnapshot(Map.of(p1, 75L, p2, 25L), 100, Set.of(p1, p2), null);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertEquals(2, result.size());
            assertEquals(750, ((TestRewardType) result.get(p1).get(0)).getAmount());
            assertEquals(250, ((TestRewardType) result.get(p2).get(0)).getAmount());
        }

        @DisplayName("zero total contribution falls back to SPLIT_EVEN")
        @Test
        void zeroContributionFallsBackToEven() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var reward = new TestRewardType(1000);
            var tier = new DistributionTierConfig("t1", ParticipatedDistributionType.KEY,
                    RewardSplitMode.SPLIT_PROPORTIONAL, List.of(reward), Map.of(), null, null, true);
            var config = new RewardDistributionConfig(List.of(tier));
            // Both players have contribution but 0 in snapshot among qualifying
            var snapshot = new ContributionSnapshot(Map.of(p1, 0L, p2, 0L), 0, Set.of(p1, p2), null);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            // Participated type requires > 0, so no players qualify
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Multi-tier stacking")
    class MultiTier {

        @DisplayName("rewards from multiple tiers stack for same player")
        @Test
        void rewardsFromMultipleTiersStack() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var highReward = new TestRewardType(500);
            var lowReward = new TestRewardType(100);
            var topTier = new DistributionTierConfig("top", TopPlayersDistributionType.KEY,
                    RewardSplitMode.INDIVIDUAL, List.of(highReward),
                    Map.of(DistributionTierConfig.PARAM_TOP_PLAYER_COUNT, 1), null, null, true);
            var allTier = new DistributionTierConfig("all", ParticipatedDistributionType.KEY,
                    RewardSplitMode.INDIVIDUAL, List.of(lowReward), Map.of(), null, null, true);
            var config = new RewardDistributionConfig(List.of(topTier, allTier));
            var snapshot = new ContributionSnapshot(Map.of(p1, 80L, p2, 20L), 100, Set.of(p1, p2), null);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertEquals(2, result.size());
            assertEquals(2, result.get(p1).size());
            assertEquals(1, result.get(p2).size());
        }
    }

    @DisplayName("unregistered distribution type key logs warning and skips tier")
    @Test
    void unregisteredTypeSkipped() {
        UUID p1 = UUID.randomUUID();
        var reward = new TestRewardType(100);
        var unknownKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "nonexistent");
        var tier = new DistributionTierConfig("t1", unknownKey,
                RewardSplitMode.INDIVIDUAL, List.of(reward), Map.of(), null, null, true);
        var config = new RewardDistributionConfig(List.of(tier));
        var snapshot = new ContributionSnapshot(Map.of(p1, 100L), 100, Set.of(p1), null);

        var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);
        assertTrue(result.isEmpty());
    }

    @DisplayName("empty distribution config returns empty result")
    @Test
    void emptyConfigReturnsEmpty() {
        UUID p1 = UUID.randomUUID();
        var config = new RewardDistributionConfig(List.of());
        var snapshot = new ContributionSnapshot(Map.of(p1, 100L), 100, Set.of(p1), null);

        var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);
        assertTrue(result.isEmpty());
    }

    @Nested
    @DisplayName("Remainder distribution")
    class RemainderDistribution {

        private RewardDistributionConfig configWith(@NotNull RemainderStrategy strategy) {
            var entry = new DistributionRewardEntry(new TestRewardType(10), PotBehavior.SCALE, strategy, 1, 1, null);
            var tier = new DistributionTierConfig("t1", ParticipatedDistributionType.KEY,
                    RewardSplitMode.SPLIT_EVEN, List.of(entry), Map.of(), null, null);
            return new RewardDistributionConfig(List.of(tier));
        }

        private long grantedTotal(@NotNull Map<UUID, List<QuestRewardType>> result) {
            return result.values().stream()
                    .flatMap(List::stream)
                    .mapToLong(reward -> ((TestRewardType) reward).getAmount())
                    .sum();
        }

        @Test
        @DisplayName("TOP_CONTRIBUTOR routes the truncation remainder so granted total equals the pot")
        void topContributor_grantsFullPot_whenSplitTruncates() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID(), p4 = UUID.randomUUID();
            var config = configWith(RemainderStrategy.TOP_CONTRIBUTOR);
            var snapshot = new ContributionSnapshot(
                    Map.of(p1, 40L, p2, 30L, p3, 20L, p4, 10L), 100, Set.of(p1, p2, p3, p4), null);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            // 10 XP / 4 players truncates to 2 each (8 granted); the leftover 2 goes to the top contributor.
            assertEquals(10, grantedTotal(result));
            assertEquals(2, result.get(p1).size());
            assertEquals(1, result.get(p2).size());
        }

        @Test
        @DisplayName("DISCARD drops the truncation remainder")
        void discard_dropsRemainder() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID(), p4 = UUID.randomUUID();
            var config = configWith(RemainderStrategy.DISCARD);
            var snapshot = new ContributionSnapshot(
                    Map.of(p1, 40L, p2, 30L, p3, 20L, p4, 10L), 100, Set.of(p1, p2, p3, p4), null);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            // 2 each, remainder 2 discarded.
            assertEquals(8, grantedTotal(result));
        }

        @Test
        @DisplayName("RANDOM distributes the whole pot as single-unit remainder shares")
        void random_grantsFullPot() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID(), p4 = UUID.randomUUID();
            var config = configWith(RemainderStrategy.RANDOM);
            var snapshot = new ContributionSnapshot(
                    Map.of(p1, 40L, p2, 30L, p3, 20L, p4, 10L), 100, Set.of(p1, p2, p3, p4), null);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry, new java.util.Random(0));

            // 2 each (8) plus two 1-unit remainder shares == 10.
            assertEquals(10, grantedTotal(result));
        }
    }
}
