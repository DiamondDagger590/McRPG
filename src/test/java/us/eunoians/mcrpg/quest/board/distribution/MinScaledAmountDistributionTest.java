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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@code minScaledAmount} guard in {@link QuestRewardDistributionResolver}.
 * Verifies that rewards whose scaled share falls below the minimum threshold are
 * skipped, preventing pot-overrun when many players qualify for a small reward pool.
 */
public class MinScaledAmountDistributionTest extends McRPGBaseTest {

    private RewardDistributionTypeRegistry typeRegistry;
    private QuestRarityRegistry rarityRegistry;

    @BeforeEach
    void setUp() {
        typeRegistry = new RewardDistributionTypeRegistry();
        typeRegistry.register(new ParticipatedDistributionType());
        rarityRegistry = new QuestRarityRegistry();
    }

    static class ScalableReward implements QuestRewardType {

        static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "scalable_reward");
        final long amount;

        ScalableReward(long amount) { this.amount = amount; }

        @Override
        public @org.jetbrains.annotations.NotNull NamespacedKey getKey() { return KEY; }

        @Override
        public @org.jetbrains.annotations.NotNull QuestRewardType parseConfig(@org.jetbrains.annotations.NotNull Section section) { return this; }

        @Override
        public void grant(@org.jetbrains.annotations.NotNull Player player) {}

        @Override
        public @org.jetbrains.annotations.NotNull Map<String, Object> serializeConfig() { return Map.of(); }

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
            return new ScalableReward(Math.round(amount * multiplier));
        }

        @Override
        public @org.jetbrains.annotations.NotNull QuestRewardType withExactAmount(long exactAmount) {
            return new ScalableReward(exactAmount);
        }

        @Override
        public @org.jetbrains.annotations.NotNull OptionalLong getNumericAmount() {
            return OptionalLong.of(amount);
        }
    }

    private DistributionTierConfig tierWithMinScaled(QuestRewardType reward, int minScaledAmount) {
        DistributionRewardEntry entry = new DistributionRewardEntry(
                reward, PotBehavior.SCALE, RemainderStrategy.DISCARD, minScaledAmount, 1, null);
        return new DistributionTierConfig("t1", ParticipatedDistributionType.KEY,
                RewardSplitMode.SPLIT_EVEN, List.of(entry), Map.of(), null, null);
    }

    @Nested
    @DisplayName("min-scaled-amount prevents reward granting when share too small")
    class MinScaledAmountGuard {

        @Test
        @DisplayName("pot of 1000 split 20 ways ≥ min-scaled-amount 1 → all receive reward")
        void largePotPassesMinScaled() {
            List<UUID> players = Stream.generate(UUID::randomUUID).limit(20)
                    .toList();
            Map<UUID, Long> contributions = new HashMap<>();
            players.forEach(p -> contributions.put(p, 1L));
            Set<UUID> playerSet = new HashSet<>(players);

            ScalableReward reward = new ScalableReward(1000);
            var config = new RewardDistributionConfig(List.of(tierWithMinScaled(reward, 1)));
            var snapshot = new ContributionSnapshot(contributions, 20, playerSet, null);

            var result = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            // 1000 / 20 = 50 per player, well above min of 1 → all should receive
            assertFalse(result.isEmpty(), "All players should receive rewards when share exceeds min");
            assertTrue(result.size() == 20);
        }

        @Test
        @DisplayName("pot of 5 split 20 ways → min-scaled-amount 0 prevents pot-overrun")
        void smallPotWithMinScaledZeroPreventsOverrun() {
            List<UUID> players = Stream.generate(UUID::randomUUID).limit(20)
                    .toList();
            Map<UUID, Long> contributions = new HashMap<>();
            players.forEach(p -> contributions.put(p, 1L));
            Set<UUID> playerSet = new HashSet<>(players);

            ScalableReward reward = new ScalableReward(5);
            var config = new RewardDistributionConfig(List.of(tierWithMinScaled(reward, 0)));
            var snapshot = new ContributionSnapshot(contributions, 20, playerSet, null);

            var result = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            // 5 / 20 rounds to 0 → with minScaledAmount=0, players whose share = 0 are skipped
            // Total granted should not exceed the original pot
            long totalGranted = result.values().stream()
                    .flatMap(List::stream)
                    .mapToLong(r -> ((ScalableReward) r).amount)
                    .sum();
            assertTrue(totalGranted <= 5, "Total granted (" + totalGranted + ") should not exceed pot of 5");
        }

        @Test
        @DisplayName("min-scaled-amount 10 skips players whose share is below 10")
        void minScaledAmountFiltersSmallShares() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            // Pot of 3 split 2 ways → each gets ~1 or 2, below min of 10
            ScalableReward reward = new ScalableReward(3);
            var config = new RewardDistributionConfig(List.of(tierWithMinScaled(reward, 10)));
            var snapshot = new ContributionSnapshot(Map.of(p1, 1L, p2, 2L), 3, Set.of(p1, p2), null);

            var result = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            // Each player's share (≈1) is below min-scaled-amount 10 → reward skipped
            assertTrue(result.isEmpty() || result.values().stream()
                    .allMatch(List::isEmpty),
                    "No rewards should be granted when scaled amount is below min-scaled-amount");
        }
    }

    @Nested
    @DisplayName("DistributionRewardEntry validation")
    class EntryValidation {

        @Test
        @DisplayName("negative minScaledAmount throws IllegalArgumentException")
        void negativeMinScaledThrows() {
            ScalableReward reward = new ScalableReward(100);
            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                    new DistributionRewardEntry(reward, PotBehavior.SCALE, RemainderStrategy.DISCARD, -1, 1, null));
        }

        @Test
        @DisplayName("topCount below 1 throws IllegalArgumentException")
        void topCountBelowOneThrows() {
            ScalableReward reward = new ScalableReward(100);
            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                    new DistributionRewardEntry(reward, PotBehavior.TOP_N, RemainderStrategy.DISCARD, 1, 0, null));
        }
    }
}
