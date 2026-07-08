package us.eunoians.mcrpg.quest.board.distribution;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.board.distribution.builtin.ParticipatedDistributionType;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("QuestRewardDistributionResolver — PotBehavior and remainder strategies")
class QuestRewardDistributionResolverPotBehaviorTest extends McRPGBaseTest {

    private RewardDistributionTypeRegistry typeRegistry;
    private QuestRarityRegistry rarityRegistry;
    private QuestRewardDistributionResolver resolver;

    @BeforeEach
    void setUp() {
        typeRegistry = new RewardDistributionTypeRegistry();
        typeRegistry.register(new ParticipatedDistributionType());
        rarityRegistry = new QuestRarityRegistry();
        resolver = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test"));
    }

    /**
     * Scalable test reward that tracks its amount and supports withAmountMultiplier.
     */
    static final class ScalableReward implements QuestRewardType {

        static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "scalable_test");
        private final long amount;

        ScalableReward(long amount) {
            this.amount = amount;
        }

        long getAmount() {
            return amount;
        }

        @Override
        public @NotNull NamespacedKey getKey() {
            return KEY;
        }

        @Override
        public @NotNull QuestRewardType parseConfig(@NotNull Section section) {
            return this;
        }

        @Override
        public void grant(@NotNull Player player) {
        }

        @Override
        public @NotNull Map<String, Object> serializeConfig() {
            return Map.of("amount", amount);
        }

        @Override
        public @NotNull QuestRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
            return this;
        }

        @Override
        public @NotNull Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }

        @Override
        public @NotNull QuestRewardType withAmountMultiplier(double multiplier) {
            return new ScalableReward(Math.max(1, Math.round(amount * multiplier)));
        }

        @Override
        public @NotNull OptionalLong getNumericAmount() {
            return OptionalLong.of(amount);
        }
    }

    /**
     * Non-scalable test reward that returns itself from withAmountMultiplier.
     */
    static final class NonScalableReward implements QuestRewardType {

        static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "non_scalable_test");

        @Override
        public @NotNull NamespacedKey getKey() {
            return KEY;
        }

        @Override
        public @NotNull QuestRewardType parseConfig(@NotNull Section section) {
            return this;
        }

        @Override
        public void grant(@NotNull Player player) {
        }

        @Override
        public @NotNull Map<String, Object> serializeConfig() {
            return Map.of();
        }

        @Override
        public @NotNull QuestRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
            return this;
        }

        @Override
        public @NotNull Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }
    }

    private DistributionTierConfig splitEvenTier(List<DistributionRewardEntry> entries) {
        return new DistributionTierConfig("t1", ParticipatedDistributionType.KEY,
                RewardSplitMode.SPLIT_EVEN, entries, Map.of(), null, null);
    }

    private DistributionTierConfig splitProportionalTier(List<DistributionRewardEntry> entries) {
        return new DistributionTierConfig("t1", ParticipatedDistributionType.KEY,
                RewardSplitMode.SPLIT_PROPORTIONAL, entries, Map.of(), null, null);
    }

    private ContributionSnapshot twoPlayerSnapshot(UUID p1, long c1, UUID p2, long c2) {
        return new ContributionSnapshot(Map.of(p1, c1, p2, c2), c1 + c2, Set.of(p1, p2), null);
    }

    private ContributionSnapshot threePlayerSnapshot(UUID p1, long c1, UUID p2, long c2, UUID p3, long c3) {
        return new ContributionSnapshot(Map.of(p1, c1, p2, c2, p3, c3), c1 + c2 + c3, Set.of(p1, p2, p3), null);
    }

    @Nested
    @DisplayName("SPLIT_EVEN with PotBehavior.ALL")
    class SplitEvenAll {

        @Test
        @DisplayName("ALL gives unscaled reward to every qualifying player")
        void allBehavior_givesFullRewardToEachPlayer() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var reward = new ScalableReward(1000);
            var entry = new DistributionRewardEntry(reward, PotBehavior.ALL, RemainderStrategy.DISCARD, 1, 1, null);
            var config = new RewardDistributionConfig(List.of(splitEvenTier(List.of(entry))));
            var snapshot = twoPlayerSnapshot(p1, 60L, p2, 40L);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertEquals(2, result.size());
            assertEquals(1000, ((ScalableReward) result.get(p1).get(0)).getAmount());
            assertEquals(1000, ((ScalableReward) result.get(p2).get(0)).getAmount());
        }
    }

    @Nested
    @DisplayName("SPLIT_EVEN with PotBehavior.TOP_N")
    class SplitEvenTopN {

        @Test
        @DisplayName("TOP_N with topCount=1 gives reward only to top contributor")
        void topN_singleTopContributor() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var reward = new ScalableReward(1000);
            var entry = new DistributionRewardEntry(reward, PotBehavior.TOP_N, RemainderStrategy.DISCARD, 1, 1, null);
            var config = new RewardDistributionConfig(List.of(splitEvenTier(List.of(entry))));
            var snapshot = twoPlayerSnapshot(p1, 80L, p2, 20L);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertTrue(result.containsKey(p1));
            assertFalse(result.containsKey(p2));
            assertEquals(1000, ((ScalableReward) result.get(p1).get(0)).getAmount());
        }

        @Test
        @DisplayName("TOP_N with topCount=2 gives reward to top two contributors")
        void topN_topTwoContributors() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
            var reward = new ScalableReward(500);
            var entry = new DistributionRewardEntry(reward, PotBehavior.TOP_N, RemainderStrategy.DISCARD, 1, 2, null);
            var config = new RewardDistributionConfig(List.of(splitEvenTier(List.of(entry))));
            var snapshot = threePlayerSnapshot(p1, 50L, p2, 30L, p3, 20L);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertTrue(result.containsKey(p1));
            assertTrue(result.containsKey(p2));
            assertFalse(result.containsKey(p3));
        }
    }

    @Nested
    @DisplayName("SPLIT_EVEN with PotBehavior.SCALE")
    class SplitEvenScale {

        @Test
        @DisplayName("SCALE divides reward evenly among players")
        void scale_dividesEvenly() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var reward = new ScalableReward(1000);
            var entry = new DistributionRewardEntry(reward, PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, null);
            var config = new RewardDistributionConfig(List.of(splitEvenTier(List.of(entry))));
            var snapshot = twoPlayerSnapshot(p1, 60L, p2, 40L);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertEquals(2, result.size());
            assertEquals(500, ((ScalableReward) result.get(p1).get(0)).getAmount());
            assertEquals(500, ((ScalableReward) result.get(p2).get(0)).getAmount());
        }

        @Test
        @DisplayName("SCALE with non-scalable reward warns and gives unscaled to all")
        void scale_nonScalableReward_fallsBackToAll() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var reward = new NonScalableReward();
            var entry = new DistributionRewardEntry(reward, PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, null);
            var config = new RewardDistributionConfig(List.of(splitEvenTier(List.of(entry))));
            var snapshot = twoPlayerSnapshot(p1, 60L, p2, 40L);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertEquals(2, result.size());
            assertTrue(result.get(p1).get(0) instanceof NonScalableReward);
            assertTrue(result.get(p2).get(0) instanceof NonScalableReward);
        }

        @Test
        @DisplayName("SCALE skips when scaled amount falls below minScaledAmount")
        void scale_belowMinScaledAmount_skipsReward() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var reward = new ScalableReward(10);
            var entry = new DistributionRewardEntry(reward, PotBehavior.SCALE, RemainderStrategy.DISCARD, 100, 1, null);
            var config = new RewardDistributionConfig(List.of(splitEvenTier(List.of(entry))));
            var snapshot = twoPlayerSnapshot(p1, 60L, p2, 40L);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Remainder strategies")
    class RemainderStrategies {

        @Test
        @DisplayName("TOP_CONTRIBUTOR remainder goes to highest contributor")
        void topContributorRemainder_goesToHighestContributor() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
            var reward = new ScalableReward(10);
            var entry = new DistributionRewardEntry(reward, PotBehavior.SCALE, RemainderStrategy.TOP_CONTRIBUTOR, 1, 1, null);
            var config = new RewardDistributionConfig(List.of(splitEvenTier(List.of(entry))));
            var snapshot = threePlayerSnapshot(p1, 60L, p2, 30L, p3, 10L);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry, new Random(42));

            assertTrue(result.containsKey(p1));
            long totalForP1 = result.get(p1).stream()
                    .mapToLong(r -> ((ScalableReward) r).getAmount())
                    .sum();
            assertTrue(totalForP1 > 3, "Top contributor should receive base share plus remainder");
        }

        @Test
        @DisplayName("RANDOM remainder is distributed deterministically with seeded Random")
        void randomRemainder_deterministicWithSeed() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
            var reward = new ScalableReward(10);
            var entry = new DistributionRewardEntry(reward, PotBehavior.SCALE, RemainderStrategy.RANDOM, 1, 1, null);
            var config = new RewardDistributionConfig(List.of(splitEvenTier(List.of(entry))));
            var snapshot = threePlayerSnapshot(p1, 60L, p2, 30L, p3, 10L);

            var result1 = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry, new Random(123));
            var result2 = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry, new Random(123));

            assertEquals(result1.size(), result2.size());
        }

        @Test
        @DisplayName("DISCARD remainder does not add extra rewards")
        void discardRemainder_noExtraRewards() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
            var reward = new ScalableReward(10);
            var entry = new DistributionRewardEntry(reward, PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, null);
            var config = new RewardDistributionConfig(List.of(splitEvenTier(List.of(entry))));
            var snapshot = threePlayerSnapshot(p1, 60L, p2, 30L, p3, 10L);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            for (var rewards : result.values()) {
                assertEquals(1, rewards.size(), "Each player should receive exactly one reward with DISCARD");
            }
        }
    }

    @Nested
    @DisplayName("SPLIT_PROPORTIONAL with PotBehavior.ALL")
    class ProportionalAll {

        @Test
        @DisplayName("ALL gives unscaled reward to every qualifying player regardless of contribution")
        void proportionalAll_fullRewardToAll() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var reward = new ScalableReward(1000);
            var entry = new DistributionRewardEntry(reward, PotBehavior.ALL, RemainderStrategy.DISCARD, 1, 1, null);
            var config = new RewardDistributionConfig(List.of(splitProportionalTier(List.of(entry))));
            var snapshot = twoPlayerSnapshot(p1, 75L, p2, 25L);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertEquals(2, result.size());
            assertEquals(1000, ((ScalableReward) result.get(p1).get(0)).getAmount());
            assertEquals(1000, ((ScalableReward) result.get(p2).get(0)).getAmount());
        }
    }

    @Nested
    @DisplayName("SPLIT_PROPORTIONAL with PotBehavior.TOP_N")
    class ProportionalTopN {

        @Test
        @DisplayName("TOP_N gives unscaled reward to top contributor only")
        void proportionalTopN_topOnly() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var reward = new ScalableReward(1000);
            var entry = new DistributionRewardEntry(reward, PotBehavior.TOP_N, RemainderStrategy.DISCARD, 1, 1, null);
            var config = new RewardDistributionConfig(List.of(splitProportionalTier(List.of(entry))));
            var snapshot = twoPlayerSnapshot(p1, 80L, p2, 20L);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertTrue(result.containsKey(p1));
            assertFalse(result.containsKey(p2));
            assertEquals(1000, ((ScalableReward) result.get(p1).get(0)).getAmount());
        }
    }

    @Nested
    @DisplayName("SPLIT_PROPORTIONAL with PotBehavior.SCALE")
    class ProportionalScale {

        @Test
        @DisplayName("SCALE distributes proportionally by contribution")
        void proportionalScale_distributedByContribution() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var reward = new ScalableReward(1000);
            var entry = new DistributionRewardEntry(reward, PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, null);
            var config = new RewardDistributionConfig(List.of(splitProportionalTier(List.of(entry))));
            var snapshot = twoPlayerSnapshot(p1, 75L, p2, 25L);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertEquals(2, result.size());
            assertEquals(750, ((ScalableReward) result.get(p1).get(0)).getAmount());
            assertEquals(250, ((ScalableReward) result.get(p2).get(0)).getAmount());
        }

        @Test
        @DisplayName("SCALE with non-scalable reward in proportional gives unscaled to all")
        void proportionalScale_nonScalable_fallsBackToAll() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var reward = new NonScalableReward();
            var entry = new DistributionRewardEntry(reward, PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, null);
            var config = new RewardDistributionConfig(List.of(splitProportionalTier(List.of(entry))));
            var snapshot = twoPlayerSnapshot(p1, 75L, p2, 25L);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertEquals(2, result.size());
            assertTrue(result.get(p1).get(0) instanceof NonScalableReward);
            assertTrue(result.get(p2).get(0) instanceof NonScalableReward);
        }

        @Test
        @DisplayName("SCALE with proportional skips player when below minScaledAmount")
        void proportionalScale_belowMin_skipsPlayer() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var reward = new ScalableReward(100);
            var entry = new DistributionRewardEntry(reward, PotBehavior.SCALE, RemainderStrategy.DISCARD, 50, 1, null);
            var config = new RewardDistributionConfig(List.of(splitProportionalTier(List.of(entry))));
            var snapshot = twoPlayerSnapshot(p1, 90L, p2, 10L);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertTrue(result.containsKey(p1));
            assertFalse(result.containsKey(p2), "Player with 10% contribution (10 reward) should be below minScaledAmount=50");
        }
    }

    @Nested
    @DisplayName("Rarity gate integration")
    class RarityGate {

        private NamespacedKey commonKey;
        private NamespacedKey rareKey;

        @BeforeEach
        void setUpRarities() {
            String ns = McRPGMethods.getMcRPGNamespace();
            commonKey = new NamespacedKey(ns, "common");
            rareKey = new NamespacedKey(ns, "rare");
            rarityRegistry.register(new QuestRarity(commonKey, 60, 1.0, 1.0, McRPGExpansion.EXPANSION_KEY));
            rarityRegistry.register(new QuestRarity(rareKey, 10, 1.5, 1.5, McRPGExpansion.EXPANSION_KEY));
        }

        @Test
        @DisplayName("tier with min-rarity RARE skips COMMON quest")
        void minRarityGate_skipsTierForLowerRarity() {
            UUID p1 = UUID.randomUUID();
            var reward = new ScalableReward(1000);
            var tier = new DistributionTierConfig("t1", ParticipatedDistributionType.KEY,
                    RewardSplitMode.INDIVIDUAL, List.of(reward), Map.of(), rareKey, null, true);
            var config = new RewardDistributionConfig(List.of(tier));
            var snapshot = new ContributionSnapshot(Map.of(p1, 100L), 100, Set.of(p1), null);

            var result = resolver.resolve(config, snapshot, commonKey, rarityRegistry, typeRegistry);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("tier with min-rarity RARE passes for RARE quest")
        void minRarityGate_passesTierForMatchingRarity() {
            UUID p1 = UUID.randomUUID();
            var reward = new ScalableReward(1000);
            var tier = new DistributionTierConfig("t1", ParticipatedDistributionType.KEY,
                    RewardSplitMode.INDIVIDUAL, List.of(reward), Map.of(), rareKey, null, true);
            var config = new RewardDistributionConfig(List.of(tier));
            var snapshot = new ContributionSnapshot(Map.of(p1, 100L), 100, Set.of(p1), null);

            var result = resolver.resolve(config, snapshot, rareKey, rarityRegistry, typeRegistry);

            assertEquals(1, result.size());
            assertTrue(result.containsKey(p1));
        }

        @Test
        @DisplayName("tier with required-rarity RARE rejects non-matching quest")
        void requiredRarityGate_rejectsNonMatching() {
            UUID p1 = UUID.randomUUID();
            var reward = new ScalableReward(1000);
            var tier = new DistributionTierConfig("t1", ParticipatedDistributionType.KEY,
                    RewardSplitMode.INDIVIDUAL, List.of(reward), Map.of(), null, rareKey, true);
            var config = new RewardDistributionConfig(List.of(tier));
            var snapshot = new ContributionSnapshot(Map.of(p1, 100L), 100, Set.of(p1), null);

            var result = resolver.resolve(config, snapshot, commonKey, rarityRegistry, typeRegistry);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Mixed reward entries")
    class MixedEntries {

        @Test
        @DisplayName("tier with multiple entries applies different pot behaviors per entry")
        void mixedPotBehaviors_inSameTier() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var scalableReward = new ScalableReward(1000);
            var participationReward = new ScalableReward(50);

            var scaleEntry = new DistributionRewardEntry(
                    scalableReward, PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, null);
            var allEntry = new DistributionRewardEntry(
                    participationReward, PotBehavior.ALL, RemainderStrategy.DISCARD, 1, 1, null);
            var config = new RewardDistributionConfig(List.of(splitEvenTier(List.of(scaleEntry, allEntry))));
            var snapshot = twoPlayerSnapshot(p1, 60L, p2, 40L);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertEquals(2, result.size());
            assertEquals(2, result.get(p1).size());
            assertEquals(2, result.get(p2).size());

            assertEquals(500, ((ScalableReward) result.get(p1).get(0)).getAmount());
            assertEquals(50, ((ScalableReward) result.get(p1).get(1)).getAmount());
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("single player with SPLIT_EVEN SCALE receives full reward")
        void singlePlayer_splitEvenScale_fullReward() {
            UUID p1 = UUID.randomUUID();
            var reward = new ScalableReward(1000);
            var entry = new DistributionRewardEntry(reward, PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, null);
            var config = new RewardDistributionConfig(List.of(splitEvenTier(List.of(entry))));
            var snapshot = new ContributionSnapshot(Map.of(p1, 100L), 100, Set.of(p1), null);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertEquals(1, result.size());
            assertEquals(1000, ((ScalableReward) result.get(p1).get(0)).getAmount());
        }

        @Test
        @DisplayName("no qualifying players returns empty result")
        void noQualifyingPlayers_emptyResult() {
            UUID p1 = UUID.randomUUID();
            var reward = new ScalableReward(1000);
            var entry = new DistributionRewardEntry(reward, PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, null);
            var config = new RewardDistributionConfig(List.of(splitEvenTier(List.of(entry))));
            var snapshot = new ContributionSnapshot(Map.of(p1, 0L), 0, Set.of(p1), null);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertTrue(result.isEmpty());
        }
    }
}
