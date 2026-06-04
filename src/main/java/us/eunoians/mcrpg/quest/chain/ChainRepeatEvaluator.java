package us.eunoians.mcrpg.quest.chain;

import com.diamonddagger590.mccore.util.TimeProvider;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Optional;

/**
 * Evaluates whether a chain in a terminal state is eligible for repeat re-start
 * based on the chain definition's repeat mode, cooldown, and completion limits.
 * <p>
 * Stateless collaborator owned by {@link QuestChainManager}. Extracted to keep
 * repeat logic testable without needing a full manager.
 */
public class ChainRepeatEvaluator {

    private final TimeProvider timeProvider;

    /**
     * Constructs a new evaluator.
     *
     * @param timeProvider the time provider for cooldown calculations
     */
    public ChainRepeatEvaluator(@NotNull TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * Determines whether a chain can be re-started for a player based on
     * the chain's repeat mode and the player's chain state.
     *
     * @param definition the chain definition
     * @param state      the player's current chain state (must be terminal)
     * @return {@code true} if the chain can be re-started
     */
    public boolean canRepeat(@NotNull QuestChainDefinition definition,
                             @NotNull QuestChainPlayerState state) {
        if (!state.getState().isTerminal() || !state.getState().isRepeatEligible()) {
            return false;
        }

        return switch (definition.getRepeatMode()) {
            case ONCE -> false;
            case UNLIMITED -> true;
            case COOLDOWN -> isCooldownMet(definition, state);
            case LIMITED -> isUnderLimit(definition, state);
            case COOLDOWN_LIMITED -> isCooldownMet(definition, state)
                    && isUnderLimit(definition, state);
        };
    }

    /**
     * Returns the remaining cooldown duration, or empty if no cooldown applies
     * or the cooldown has elapsed.
     *
     * @param definition the chain definition
     * @param state      the player's chain state
     * @return remaining cooldown, or empty
     */
    @NotNull
    public Optional<Duration> getCooldownRemaining(@NotNull QuestChainDefinition definition,
                                                   @NotNull QuestChainPlayerState state) {
        Optional<Duration> cooldownOpt = definition.getRepeatCooldown();
        if (cooldownOpt.isEmpty()) {
            return Optional.empty();
        }
        Duration cooldown = cooldownOpt.get();
        return state.getLastCompletedAt().flatMap(lastCompleted -> {
            Duration elapsed = Duration.between(lastCompleted, timeProvider.now());
            Duration remaining = cooldown.minus(elapsed);
            return remaining.isNegative() ? Optional.empty() : Optional.of(remaining);
        });
    }

    /**
     * Checks whether the cooldown period has elapsed since the last completion.
     *
     * @param definition the chain definition
     * @param state      the player's chain state
     * @return {@code true} if no cooldown is configured or the cooldown has elapsed
     */
    private boolean isCooldownMet(@NotNull QuestChainDefinition definition,
                                  @NotNull QuestChainPlayerState state) {
        Optional<Duration> cooldownOpt = definition.getRepeatCooldown();
        if (cooldownOpt.isEmpty()) {
            return true;
        }
        Duration cooldown = cooldownOpt.get();
        return state.getLastCompletedAt()
                .map(last -> Duration.between(last, timeProvider.now()).compareTo(cooldown) >= 0)
                .orElse(true);
    }

    /**
     * Checks whether the player's completion count is below the chain's max.
     *
     * @param definition the chain definition
     * @param state      the player's chain state
     * @return {@code true} if the player has not reached the maximum completions
     */
    private boolean isUnderLimit(@NotNull QuestChainDefinition definition,
                                 @NotNull QuestChainPlayerState state) {
        int max = definition.getMaxCompletions();
        return max < 0 || state.getCompletionCount() < max;
    }
}
