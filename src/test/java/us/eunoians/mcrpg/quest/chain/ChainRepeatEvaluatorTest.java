package us.eunoians.mcrpg.quest.chain;

import com.diamonddagger590.mccore.util.TimeProvider;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChainRepeatEvaluatorTest {

    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("mcrpg", "test_chain");
    private static final NamespacedKey SOURCE_KEY = new NamespacedKey("mcrpg", "test_source");
    private static final NamespacedKey TRIGGER_KEY = new NamespacedKey("mcrpg", "login");
    private static final NamespacedKey QUEST_KEY = new NamespacedKey("mcrpg", "test_quest");
    private static final Instant NOW = Instant.parse("2026-06-04T12:00:00Z");

    private ChainRepeatEvaluator evaluator(Instant now) {
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);
        return new ChainRepeatEvaluator(new TimeProvider(clock));
    }

    private QuestChainDefinition definition(QuestChainRepeatMode mode, Duration cooldown, int maxCompletions) {
        var builder = new QuestChainDefinition.Builder(
                CHAIN_KEY, SOURCE_KEY, TRIGGER_KEY,
                List.of(QuestChainStep.simple(QUEST_KEY)))
                .repeatMode(mode)
                .maxCompletions(maxCompletions);
        if (cooldown != null) {
            builder.repeatCooldown(cooldown);
        }
        return builder.build();
    }

    private QuestChainPlayerState completedState(int completionCount, Instant lastCompletedAt) {
        return new QuestChainPlayerState(CHAIN_KEY, null, QuestChainState.COMPLETED,
                completionCount, lastCompletedAt);
    }

    private QuestChainPlayerState abandonedState() {
        return new QuestChainPlayerState(CHAIN_KEY, null, QuestChainState.ABANDONED, 0, null);
    }

    private QuestChainPlayerState activeState() {
        return QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_KEY);
    }

    @Nested
    @DisplayName("ONCE mode")
    class OnceMode {

        @Test
        @DisplayName("Given ONCE mode and COMPLETED state, When canRepeat is called, Then returns false")
        void canRepeat_returnsFalse_whenOnceAndCompleted() {
            ChainRepeatEvaluator eval = evaluator(NOW);
            assertFalse(eval.canRepeat(definition(QuestChainRepeatMode.ONCE, null, -1), completedState(1, NOW)));
        }

        @Test
        @DisplayName("Given ONCE mode and ABANDONED state, When canRepeat is called, Then returns false")
        void canRepeat_returnsFalse_whenOnceAndAbandoned() {
            ChainRepeatEvaluator eval = evaluator(NOW);
            assertFalse(eval.canRepeat(definition(QuestChainRepeatMode.ONCE, null, -1), abandonedState()));
        }
    }

    @Nested
    @DisplayName("UNLIMITED mode")
    class UnlimitedMode {

        @Test
        @DisplayName("Given UNLIMITED mode and COMPLETED state, When canRepeat is called, Then returns true")
        void canRepeat_returnsTrue_whenUnlimitedAndCompleted() {
            ChainRepeatEvaluator eval = evaluator(NOW);
            assertTrue(eval.canRepeat(definition(QuestChainRepeatMode.UNLIMITED, null, -1), completedState(100, NOW)));
        }

        @Test
        @DisplayName("Given UNLIMITED mode and ABANDONED state, When canRepeat is called, Then returns true")
        void canRepeat_returnsTrue_whenUnlimitedAndAbandoned() {
            ChainRepeatEvaluator eval = evaluator(NOW);
            assertTrue(eval.canRepeat(definition(QuestChainRepeatMode.UNLIMITED, null, -1), abandonedState()));
        }
    }

    @Nested
    @DisplayName("COOLDOWN mode")
    class CooldownMode {

        @Test
        @DisplayName("Given COOLDOWN mode and cooldown has elapsed, When canRepeat is called, Then returns true")
        void canRepeat_returnsTrue_whenCooldownElapsed() {
            ChainRepeatEvaluator eval = evaluator(NOW);
            Instant completedAt = NOW.minus(Duration.ofHours(25));
            assertTrue(eval.canRepeat(
                    definition(QuestChainRepeatMode.COOLDOWN, Duration.ofHours(24), -1),
                    completedState(1, completedAt)));
        }

        @Test
        @DisplayName("Given COOLDOWN mode and still on cooldown, When canRepeat is called, Then returns false")
        void canRepeat_returnsFalse_whenStillOnCooldown() {
            ChainRepeatEvaluator eval = evaluator(NOW);
            Instant completedAt = NOW.minus(Duration.ofHours(12));
            assertFalse(eval.canRepeat(
                    definition(QuestChainRepeatMode.COOLDOWN, Duration.ofHours(24), -1),
                    completedState(1, completedAt)));
        }

        @Test
        @DisplayName("Given COOLDOWN mode and no previous completion, When canRepeat is called, Then returns true")
        void canRepeat_returnsTrue_whenNoPreviousCompletion() {
            ChainRepeatEvaluator eval = evaluator(NOW);
            QuestChainPlayerState state = new QuestChainPlayerState(
                    CHAIN_KEY, null, QuestChainState.ABANDONED, 0, null);
            assertTrue(eval.canRepeat(
                    definition(QuestChainRepeatMode.COOLDOWN, Duration.ofHours(24), -1), state));
        }
    }

    @Nested
    @DisplayName("LIMITED mode")
    class LimitedMode {

        @Test
        @DisplayName("Given LIMITED mode and under limit, When canRepeat is called, Then returns true")
        void canRepeat_returnsTrue_whenUnderLimit() {
            ChainRepeatEvaluator eval = evaluator(NOW);
            assertTrue(eval.canRepeat(
                    definition(QuestChainRepeatMode.LIMITED, null, 5),
                    completedState(3, NOW)));
        }

        @Test
        @DisplayName("Given LIMITED mode and at limit, When canRepeat is called, Then returns false")
        void canRepeat_returnsFalse_whenAtLimit() {
            ChainRepeatEvaluator eval = evaluator(NOW);
            assertFalse(eval.canRepeat(
                    definition(QuestChainRepeatMode.LIMITED, null, 5),
                    completedState(5, NOW)));
        }
    }

    @Nested
    @DisplayName("COOLDOWN_LIMITED mode")
    class CooldownLimitedMode {

        @Test
        @DisplayName("Given COOLDOWN_LIMITED with both conditions met, When canRepeat is called, Then returns true")
        void canRepeat_returnsTrue_whenBothConditionsMet() {
            ChainRepeatEvaluator eval = evaluator(NOW);
            Instant completedAt = NOW.minus(Duration.ofHours(25));
            assertTrue(eval.canRepeat(
                    definition(QuestChainRepeatMode.COOLDOWN_LIMITED, Duration.ofHours(24), 5),
                    completedState(3, completedAt)));
        }

        @Test
        @DisplayName("Given COOLDOWN_LIMITED with cooldown not met, When canRepeat is called, Then returns false")
        void canRepeat_returnsFalse_whenCooldownNotMet() {
            ChainRepeatEvaluator eval = evaluator(NOW);
            Instant completedAt = NOW.minus(Duration.ofHours(12));
            assertFalse(eval.canRepeat(
                    definition(QuestChainRepeatMode.COOLDOWN_LIMITED, Duration.ofHours(24), 5),
                    completedState(3, completedAt)));
        }

        @Test
        @DisplayName("Given COOLDOWN_LIMITED with limit reached, When canRepeat is called, Then returns false")
        void canRepeat_returnsFalse_whenLimitReached() {
            ChainRepeatEvaluator eval = evaluator(NOW);
            Instant completedAt = NOW.minus(Duration.ofHours(25));
            assertFalse(eval.canRepeat(
                    definition(QuestChainRepeatMode.COOLDOWN_LIMITED, Duration.ofHours(24), 5),
                    completedState(5, completedAt)));
        }
    }

    @Test
    @DisplayName("Given ACTIVE state, When canRepeat is called, Then returns false")
    void canRepeat_returnsFalse_whenActiveState() {
        ChainRepeatEvaluator eval = evaluator(NOW);
        assertFalse(eval.canRepeat(definition(QuestChainRepeatMode.UNLIMITED, null, -1), activeState()));
    }

    @Nested
    @DisplayName("getCooldownRemaining")
    class CooldownRemaining {

        @Test
        @DisplayName("Given no cooldown configured, When getCooldownRemaining is called, Then returns empty")
        void getCooldownRemaining_returnsEmpty_whenNoCooldownConfigured() {
            ChainRepeatEvaluator eval = evaluator(NOW);
            assertEquals(Optional.empty(), eval.getCooldownRemaining(
                    definition(QuestChainRepeatMode.UNLIMITED, null, -1),
                    completedState(1, NOW)));
        }

        @Test
        @DisplayName("Given cooldown not yet elapsed, When getCooldownRemaining is called, Then returns remaining duration")
        void getCooldownRemaining_returnsRemaining_whenNotElapsed() {
            ChainRepeatEvaluator eval = evaluator(NOW);
            Instant completedAt = NOW.minus(Duration.ofHours(12));
            Optional<Duration> remaining = eval.getCooldownRemaining(
                    definition(QuestChainRepeatMode.COOLDOWN, Duration.ofHours(24), -1),
                    completedState(1, completedAt));
            assertTrue(remaining.isPresent());
            assertEquals(Duration.ofHours(12), remaining.get());
        }

        @Test
        @DisplayName("Given cooldown has elapsed, When getCooldownRemaining is called, Then returns empty")
        void getCooldownRemaining_returnsEmpty_whenElapsed() {
            ChainRepeatEvaluator eval = evaluator(NOW);
            Instant completedAt = NOW.minus(Duration.ofHours(25));
            Optional<Duration> remaining = eval.getCooldownRemaining(
                    definition(QuestChainRepeatMode.COOLDOWN, Duration.ofHours(24), -1),
                    completedState(1, completedAt));
            assertTrue(remaining.isEmpty());
        }
    }
}
