package us.eunoians.mcrpg.util;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

/**
 * Generic, reusable state machine with transition validation.
 * <p>
 * Designed for extraction into McCore. Applicable to {@code BoardOffering.State},
 * {@code QuestState}, {@code QuestStageState}, {@code QuestObjectiveState}, etc.
 *
 * @param <S> the enum type representing states
 */
public class StateMachine<S extends Enum<S>> {

    private final Map<S, Set<S>> validTransitions;
    private S currentState;

    /**
     * Creates a new state machine.
     *
     * @param initialState     the starting state
     * @param validTransitions map of state to the set of states it can transition to
     */
    public StateMachine(@NotNull S initialState,
                        @NotNull Map<S, Set<S>> validTransitions) {
        this.currentState = initialState;
        this.validTransitions = Map.copyOf(validTransitions);
    }

    /**
     * Transitions to a new state.
     *
     * @param newState the target state
     * @throws IllegalStateException if the transition is not valid from the current state
     */
    public void transitionTo(@NotNull S newState) {
        if (!canTransitionTo(newState)) {
            throw new IllegalStateException(
                    "Cannot transition from " + currentState + " to " + newState);
        }
        this.currentState = newState;
    }

    /**
     * Checks whether a transition to the given state is valid from the current state.
     *
     * @param newState the target state
     * @return {@code true} if the transition is valid
     */
    public boolean canTransitionTo(@NotNull S newState) {
        return validTransitions.getOrDefault(currentState, Set.of()).contains(newState);
    }

    /**
     * Gets the current state.
     *
     * @return the current state
     */
    @NotNull
    public S getCurrentState() {
        return currentState;
    }
}
