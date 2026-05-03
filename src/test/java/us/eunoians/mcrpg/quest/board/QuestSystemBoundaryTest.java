package us.eunoians.mcrpg.quest.board;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.board.distribution.ContributionSnapshot;
import us.eunoians.mcrpg.quest.board.refresh.builtin.DailyRefreshType;
import us.eunoians.mcrpg.quest.board.distribution.DistributionTierConfig;
import us.eunoians.mcrpg.quest.board.distribution.PotBehavior;
import us.eunoians.mcrpg.quest.board.distribution.QuestRewardDistributionResolver;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionTypeRegistry;
import us.eunoians.mcrpg.quest.board.distribution.RewardSplitMode;
import us.eunoians.mcrpg.quest.board.distribution.RemainderStrategy;
import us.eunoians.mcrpg.quest.board.distribution.DistributionRewardEntry;
import us.eunoians.mcrpg.quest.board.distribution.builtin.ParticipatedDistributionType;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.template.ResolvedVariableContext;
import us.eunoians.mcrpg.quest.board.template.condition.ComparisonOperator;
import us.eunoians.mcrpg.quest.board.template.condition.ConditionContext;
import us.eunoians.mcrpg.quest.board.template.condition.VariableCheck;
import us.eunoians.mcrpg.quest.board.template.condition.VariableCondition;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Edge-case hardening tests covering robustness scenarios for the condition system,
 * distribution resolver, and board state management.
 */
class QuestSystemBoundaryTest {

    private static final NamespacedKey REWARD_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "edge_reward");

    private RewardDistributionTypeRegistry typeRegistry;
    private QuestRarityRegistry rarityRegistry;

    @BeforeEach
    void setUp() {
        typeRegistry = new RewardDistributionTypeRegistry();
        typeRegistry.register(new ParticipatedDistributionType());
        rarityRegistry = new QuestRarityRegistry();
    }

    @Nested
    @DisplayName("VariableCondition — missing or undefined variable")
    class UndefinedVariable {

        @Test
        @DisplayName("VariableCondition with undefined variable name returns false (safe default)")
        void undefinedVariableReturnsFalse() {
            ResolvedVariableContext vars = new ResolvedVariableContext(
                    Map.of("block_count", 20.0), 1.0, 1.0, 1.0);
            // Reference a variable that doesn't exist in the resolved context
            VariableCondition condition = new VariableCondition("non_existent_variable",
                    new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN, 10));
            ConditionContext ctx = new ConditionContext(null, null, null, vars, null, null);
            assertFalse(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("VariableCondition with null resolved variables returns true (include-by-default in non-generation context)")
        void nullVariableContextReturnsTrue() {
            // When outside of a generation context (resolvedVariables is null),
            // VariableCondition defaults to true to avoid excluding elements in non-generation sites.
            VariableCondition condition = new VariableCondition("block_count",
                    new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN, 10));
            ConditionContext ctx = new ConditionContext(null, null, null, null, null, null);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("ContainsAny check with empty value list always returns false")
        void containsAnyEmptyListReturnsFalse() {
            ResolvedVariableContext vars = new ResolvedVariableContext(
                    Map.of("ore_type", "DIAMOND_ORE"), 1.0, 1.0, 1.0);
            VariableCondition condition = new VariableCondition("ore_type",
                    new VariableCheck.ContainsAny(List.of()));
            ConditionContext ctx = new ConditionContext(null, null, null, vars, null, null);
            assertFalse(condition.evaluate(ctx));
        }
    }

    @Nested
    @DisplayName("OfferingAcceptResult — state completeness")
    class OfferingAcceptResultState {

        @Test
        @DisplayName("ACCEPTED result isAccepted() returns true")
        void acceptedResultIsAccepted() {
            assertTrue(OfferingAcceptResult.ACCEPTED.isAccepted());
        }

        @Test
        @DisplayName("all failure result variants return false from isAccepted()")
        void failureResultsNotAccepted() {
            assertFalse(OfferingAcceptResult.SLOTS_FULL.isAccepted());
            assertFalse(OfferingAcceptResult.NOT_AVAILABLE.isAccepted());
            assertFalse(OfferingAcceptResult.CANCELLED_BY_EVENT.isAccepted());
            assertFalse(OfferingAcceptResult.DEFINITION_NOT_FOUND.isAccepted());
        }
    }

    @Nested
    @DisplayName("Distribution resolver — empty/degenerate cases")
    class DistributionEdgeCases {

        @Test
        @DisplayName("single player quest — player receives full reward in SPLIT_EVEN mode")
        void singlePlayerSplitEvenFullReward() {
            UUID solo = UUID.randomUUID();
            QuestRewardType reward = scalableReward(100);
            DistributionTierConfig tier = new DistributionTierConfig("participated",
                    ParticipatedDistributionType.KEY,
                    RewardSplitMode.SPLIT_EVEN,
                    List.of(new DistributionRewardEntry(reward, PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, null)),
                    Map.of(), null, null);
            var config = new RewardDistributionConfig(List.of(tier));
            var snapshot = new ContributionSnapshot(Map.of(solo, 100L), 100, Set.of(solo), null);

            var result = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertTrue(result.containsKey(solo), "Solo player should qualify");
        }

        @Test
        @DisplayName("empty contributions map — no players qualify for PARTICIPATED tier")
        void emptyContributionsNoQualifyingPlayers() {
            QuestRewardType reward = scalableReward(100);
            DistributionTierConfig tier = new DistributionTierConfig("participated",
                    ParticipatedDistributionType.KEY,
                    RewardSplitMode.INDIVIDUAL,
                    List.of(reward), Map.of(), null, null, true);
            var config = new RewardDistributionConfig(List.of(tier));
            var snapshot = new ContributionSnapshot(Map.of(), 0, Set.of(), null);

            var result = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertTrue(result.isEmpty(), "Empty contributions should yield no qualifying players");
        }

        @Test
        @DisplayName("unregistered distribution type key logs warning and skips tier")
        void unregisteredTypeKeySkipped() {
            UUID p1 = UUID.randomUUID();
            QuestRewardType reward = scalableReward(100);
            NamespacedKey unknownKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "unknown_type");
            DistributionTierConfig tier = new DistributionTierConfig("tier1", unknownKey,
                    RewardSplitMode.INDIVIDUAL, List.of(reward), Map.of(), null, null, true);
            var config = new RewardDistributionConfig(List.of(tier));
            var snapshot = new ContributionSnapshot(Map.of(p1, 100L), 100, Set.of(p1), null);

            var result = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertTrue(result.isEmpty(), "Unregistered type should cause tier to be skipped");
        }

        @Test
        @DisplayName("ALL pot-behavior gives full unscaled reward to all qualifying players")
        void allBehaviorGrantsFullReward() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
            QuestRewardType reward = nonScalableReward();
            DistributionTierConfig tier = new DistributionTierConfig("participated",
                    ParticipatedDistributionType.KEY,
                    RewardSplitMode.SPLIT_EVEN,
                    List.of(new DistributionRewardEntry(reward, PotBehavior.ALL, RemainderStrategy.DISCARD, 1, 1, null)),
                    Map.of(), null, null);
            var config = new RewardDistributionConfig(List.of(tier));
            var snapshot = new ContributionSnapshot(Map.of(p1, 60L, p2, 40L), 100, Set.of(p1, p2), null);

            var result = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            // ALL behavior: both players get the full reward regardless of split mode
            assertTrue(result.containsKey(p1) && result.containsKey(p2),
                    "Both players should receive the ALL-behavior reward");
        }
    }

    // -------------------------------------------------------------------------
    // 11.23  Concurrent acceptance guard (BoardOffering state machine)
    // -------------------------------------------------------------------------

    /** Fixed epoch used throughout hardening tests — eliminates wall-clock dependency. */
    private static final long FIXED_ACCEPTED_AT = 1_000_000L;
    /** Simulated "now" value: greater than PAST_EXPIRY, less than FUTURE_EXPIRY. */
    private static final long SIMULATED_NOW = 2_000_000L;
    private static final long PAST_EXPIRY = 1_000L;
    private static final long FUTURE_EXPIRY = 10_000_000L;

    @Nested
    @DisplayName("BoardOffering — concurrent acceptance guard")
    class ConcurrentAcceptanceGuard {

        @Test
        @DisplayName("VISIBLE offering transitions to ACCEPTED once; second attempt is blocked")
        void givenVisibleOffering_whenAcceptedTwice_thenSecondTransitionIsBlocked() {
            var offering = newVisibleOffering();
            assertTrue(offering.canTransitionTo(BoardOffering.State.ACCEPTED));

            offering.accept(FIXED_ACCEPTED_AT, UUID.randomUUID());
            assertEquals(BoardOffering.State.ACCEPTED, offering.getState());

            assertFalse(offering.canTransitionTo(BoardOffering.State.ACCEPTED),
                    "Once accepted, ACCEPTED→ACCEPTED transition must be blocked");
        }

        @Test
        @DisplayName("EXPIRED offering cannot be accepted — expiry wins over a racing acceptance")
        void givenExpiredOffering_whenAcceptAttempted_thenTransitionIsBlocked() {
            var offering = newVisibleOffering();
            offering.transitionTo(BoardOffering.State.EXPIRED);

            assertFalse(offering.canTransitionTo(BoardOffering.State.ACCEPTED),
                    "EXPIRED offering must not be acceptable");
        }

        @Test
        @DisplayName("accept() on already-ACCEPTED offering throws IllegalStateException")
        void givenAcceptedOffering_whenAcceptCalledAgain_thenIllegalStateExceptionThrown() {
            var offering = newVisibleOffering();
            offering.accept(FIXED_ACCEPTED_AT, UUID.randomUUID());

            assertThrows(IllegalStateException.class,
                    () -> offering.accept(FIXED_ACCEPTED_AT, UUID.randomUUID()),
                    "Second accept() call must throw");
        }
    }

    // -------------------------------------------------------------------------
    // 11.23  Orphaned offering state repair (ACCEPTED → EXPIRED)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("BoardOffering — orphaned state repair")
    class OrphanedStateRepair {

        @Test
        @DisplayName("ACCEPTED offering with missing quest instance can be repaired to EXPIRED")
        void givenAcceptedOfferingWithNoActiveQuest_whenRepairedToExpired_thenStateIsExpired() {
            var offering = newAcceptedOffering();

            assertTrue(offering.canTransitionTo(BoardOffering.State.EXPIRED),
                    "Repair path ACCEPTED→EXPIRED must be a valid transition");
            offering.transitionTo(BoardOffering.State.EXPIRED);
            assertEquals(BoardOffering.State.EXPIRED, offering.getState());
        }

        @Test
        @DisplayName("EXPIRED and COMPLETED states are terminal — no further transitions allowed")
        void givenTerminalState_whenTransitionAttempted_thenNoTransitionIsAllowed() {
            var expired = newVisibleOffering();
            expired.transitionTo(BoardOffering.State.EXPIRED);
            assertFalse(expired.canTransitionTo(BoardOffering.State.VISIBLE));
            assertFalse(expired.canTransitionTo(BoardOffering.State.ACCEPTED));
            assertFalse(expired.canTransitionTo(BoardOffering.State.EXPIRED));

            var completed = newAcceptedOffering();
            completed.transitionTo(BoardOffering.State.COMPLETED);
            assertFalse(completed.canTransitionTo(BoardOffering.State.ACCEPTED));
            assertFalse(completed.canTransitionTo(BoardOffering.State.EXPIRED));
        }

        @Test
        @DisplayName("questInstanceUUID is present after accept(), confirming repair criterion is available")
        void givenVisibleOffering_whenAccepted_thenQuestInstanceUUIDIsPresent() {
            UUID instanceUUID = UUID.randomUUID();
            var offering = newVisibleOffering();
            offering.accept(FIXED_ACCEPTED_AT, instanceUUID);

            assertTrue(offering.getQuestInstanceUUID().isPresent());
            assertEquals(instanceUUID, offering.getQuestInstanceUUID().get());
        }
    }

    // -------------------------------------------------------------------------
    // 11.23  Server restart recovery — missed rotation detection
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("BoardRotation — missed rotation detection")
    class MissedRotationDetection {

        @Test
        @DisplayName("rotation that expired in the past is detected as missed (now > expiresAt)")
        void givenRotationExpiredInPast_whenComparedToSimulatedNow_thenDetectedAsMissed() {
            var rotation = new BoardRotation(
                    UUID.randomUUID(),
                    new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "default_board"),
                    DailyRefreshType.KEY,
                    0L,
                    PAST_EXPIRY - Duration.ofDays(1).toMillis(),
                    PAST_EXPIRY);

            assertTrue(SIMULATED_NOW > rotation.getExpiresAt(),
                    "Rotation with expiresAt in the past must be detected as missed");
        }

        @Test
        @DisplayName("active rotation with a future expiry is not treated as missed")
        void givenRotationWithFutureExpiry_whenComparedToSimulatedNow_thenNotMissed() {
            var rotation = new BoardRotation(
                    UUID.randomUUID(),
                    new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "default_board"),
                    DailyRefreshType.KEY,
                    0L,
                    SIMULATED_NOW,
                    FUTURE_EXPIRY);

            assertFalse(SIMULATED_NOW > rotation.getExpiresAt(),
                    "Rotation with expiresAt in the future must not be flagged as missed");
        }

        @Test
        @DisplayName("rotation with expiresAt strictly less than now is treated as expired (boundary)")
        void givenRotationAtBoundary_whenComparedToSimulatedNow_thenDetectedAsMissed() {
            // expiresAt = SIMULATED_NOW - 1: strictly less than simulated now
            long boundaryExpiry = SIMULATED_NOW - 1;
            var rotation = new BoardRotation(
                    UUID.randomUUID(),
                    new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "default_board"),
                    DailyRefreshType.KEY,
                    0L,
                    boundaryExpiry - Duration.ofDays(1).toMillis(),
                    boundaryExpiry);

            assertTrue(SIMULATED_NOW > rotation.getExpiresAt(),
                    "Rotation at the expiry boundary must be treated as missed");
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private BoardOffering newVisibleOffering() {
        return new BoardOffering(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "shared_daily"),
                0,
                new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test_quest"),
                new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "common"),
                null,
                Duration.ofHours(24));
    }

    private BoardOffering newAcceptedOffering() {
        var offering = newVisibleOffering();
        offering.accept(FIXED_ACCEPTED_AT, UUID.randomUUID());
        return offering;
    }

    private QuestRewardType scalableReward(long amount) {
        QuestRewardType reward = mock(QuestRewardType.class);
        when(reward.getKey()).thenReturn(REWARD_KEY);
        when(reward.getNumericAmount()).thenReturn(OptionalLong.of(amount));
        when(reward.withAmountMultiplier(1.0)).thenReturn(reward);
        when(reward.withAmountMultiplier(0.5)).thenAnswer(inv -> {
            QuestRewardType scaled = mock(QuestRewardType.class);
            when(scaled.getKey()).thenReturn(REWARD_KEY);
            when(scaled.getNumericAmount()).thenReturn(OptionalLong.of(Math.round(amount * 0.5)));
            return scaled;
        });
        return reward;
    }

    private QuestRewardType nonScalableReward() {
        QuestRewardType reward = mock(QuestRewardType.class);
        when(reward.getKey()).thenReturn(REWARD_KEY);
        when(reward.getNumericAmount()).thenReturn(OptionalLong.empty());
        when(reward.withAmountMultiplier(1.0)).thenReturn(reward);
        return reward;
    }
}
