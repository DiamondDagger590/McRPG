package us.eunoians.mcrpg.quest.board.distribution;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.board.distribution.builtin.MembershipDistributionType;
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
class QuestRewardDistributionResolverPotBehaviorTest {

    private RewardDistributionTypeRegistry typeRegistry;
    private QuestRarityRegistry rarityRegistry;
    private QuestRewardDistributionResolver resolver;

    @BeforeEach
    void setUp() {
        typeRegistry = new RewardDistributionTypeRegistry();
        typeRegistry.register(new ParticipatedDistributionType());
        typeRegistry.register(new MembershipDistributionType());
        rarityRegistry = new QuestRarityRegistry();
        resolver = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test"));
    }

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
        void resolve_givesFullRewardToAll_whenAllBehavior() {
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
        void resolve_givesToTopOnly_whenTopNSingleContributor() {
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
        void resolve_givesToTopTwo_whenTopNCountIsTwo() {
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
        void resolve_dividesEvenly_whenScaleBehavior() {
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
        void resolve_fallsBackToAll_whenScaleWithNonScalableReward() {
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
        void resolve_skipsReward_whenScaledBelowMinAmount() {
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
        void resolve_givesRemainderToTop_whenTopContributorStrategy() {
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
        void resolve_producesSameResult_whenRandomRemainderWithSameSeed() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
            var reward = new ScalableReward(10);
            var entry = new DistributionRewardEntry(reward, PotBehavior.SCALE, RemainderStrategy.RANDOM, 1, 1, null);
            var config = new RewardDistributionConfig(List.of(splitEvenTier(List.of(entry))));
            var snapshot = threePlayerSnapshot(p1, 60L, p2, 30L, p3, 10L);

            var result1 = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry, new Random(123));
            var result2 = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry, new Random(123));

            assertEquals(result1.keySet(), result2.keySet());
            for (UUID player : result1.keySet()) {
                var rewards1 = result1.get(player);
                var rewards2 = result2.get(player);
                assertEquals(rewards1.size(), rewards2.size(), "Reward count mismatch for player " + player);
                for (int i = 0; i < rewards1.size(); i++) {
                    long amount1 = ((ScalableReward) rewards1.get(i)).getAmount();
                    long amount2 = ((ScalableReward) rewards2.get(i)).getAmount();
                    assertEquals(amount1, amount2, "Reward amount mismatch for player " + player + " at index " + i);
                }
            }
        }

        @Test
        @DisplayName("DISCARD remainder does not add extra rewards")
        void resolve_noExtraRewards_whenDiscardRemainder() {
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
        void resolve_givesFullRewardToAll_whenProportionalAllBehavior() {
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
        void resolve_givesToTopOnly_whenProportionalTopN() {
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
        void resolve_distributesProportionally_whenScaleBehavior() {
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
        void resolve_fallsBackToAll_whenProportionalScaleNonScalable() {
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
        void resolve_skipsLowContributor_whenProportionalScaleBelowMin() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var reward = new ScalableReward(100);
            var entry = new DistributionRewardEntry(reward, PotBehavior.SCALE, RemainderStrategy.DISCARD, 50, 1, null);
            var config = new RewardDistributionConfig(List.of(splitProportionalTier(List.of(entry))));
            var snapshot = twoPlayerSnapshot(p1, 90L, p2, 10L);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertTrue(result.containsKey(p1));
            assertFalse(result.containsKey(p2), "Player with 10% contribution (10 reward) should be below minScaledAmount=50");
        }

        @Test
        @DisplayName("zero total contribution falls back to even split")
        void resolve_fallsBackToEvenSplit_whenProportionalZeroTotalContribution() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            var reward = new ScalableReward(1000);
            var entry = new DistributionRewardEntry(reward, PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, null);
            var tier = new DistributionTierConfig("t1", MembershipDistributionType.KEY,
                    RewardSplitMode.SPLIT_PROPORTIONAL, List.of(entry), Map.of(), null, null);
            var config = new RewardDistributionConfig(List.of(tier));
            var snapshot = new ContributionSnapshot(Map.of(p1, 0L, p2, 0L), 0, Set.of(p1, p2), null);

            var result = resolver.resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertEquals(2, result.size());
            assertEquals(500, ((ScalableReward) result.get(p1).get(0)).getAmount());
            assertEquals(500, ((ScalableReward) result.get(p2).get(0)).getAmount());
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
        void resolve_skipsTier_whenMinRarityGateNotMet() {
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
        void resolve_passesTier_whenMinRarityGateMet() {
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
        void resolve_rejectsTier_whenRequiredRarityNotMatched() {
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
        void resolve_appliesDifferentBehaviors_whenMixedPotBehaviorsInTier() {
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
        void resolve_givesFullReward_whenSinglePlayerEvenScale() {
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
        void resolve_returnsEmpty_whenNoQualifyingPlayers() {
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
