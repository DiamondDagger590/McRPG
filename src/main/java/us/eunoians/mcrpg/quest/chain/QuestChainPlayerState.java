package us.eunoians.mcrpg.quest.chain;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Mutable per-player state for a single quest chain. Loaded eagerly at join from
 * {@link us.eunoians.mcrpg.database.table.quest.QuestChainStateDAO} and mutated by
 * {@link QuestChainManager}.
 * <p>
 * When a chain reaches a terminal state ({@code COMPLETED}, {@code ABANDONED},
 * {@code FAILED}, {@code EXPIRED}), {@code currentQuestKey} is set to {@code null}.
 * This is semantically correct — there is no "current" quest in a terminal state.
 * <p>
 * The {@code dirty} flag indicates that this state has been mutated since last persistence.
 * Only cleared on a successful database write.
 */
public class QuestChainPlayerState {

    private final NamespacedKey chainKey;
    private NamespacedKey currentQuestKey;
    private QuestChainState state;
    private int completionCount;
    private Long lastCompletedAt;
    private volatile boolean dirty;

    /**
     * Constructs a player chain state from database values. Nullable parameters are
     * stored internally and exposed as {@link Optional} via getters.
     *
     * @param chainKey        the chain definition key
     * @param currentQuestKey the current step's quest key ({@code null} for terminal states)
     * @param state           the chain state
     * @param completionCount the number of times this chain has been completed
     * @param lastCompletedAt the last completion timestamp in epoch millis ({@code null} if never)
     */
    public QuestChainPlayerState(@NotNull NamespacedKey chainKey,
                                 @Nullable NamespacedKey currentQuestKey,
                                 @NotNull QuestChainState state,
                                 int completionCount,
                                 @Nullable Long lastCompletedAt) {
        this.chainKey = chainKey;
        this.currentQuestKey = currentQuestKey;
        this.state = state;
        this.completionCount = completionCount;
        this.lastCompletedAt = lastCompletedAt;
        this.dirty = false;
    }

    /**
     * Creates a new ACTIVE state for starting a chain at the given first step.
     *
     * @param chainKey      the chain definition key
     * @param firstQuestKey the first step's quest key
     * @return a new active state
     */
    @NotNull
    public static QuestChainPlayerState newActive(@NotNull NamespacedKey chainKey,
                                                  @NotNull NamespacedKey firstQuestKey) {
        return new QuestChainPlayerState(chainKey, firstQuestKey, QuestChainState.ACTIVE, 0, null);
    }

    /**
     * Gets the chain definition key.
     *
     * @return the chain key
     */
    @NotNull
    public NamespacedKey getChainKey() {
        return chainKey;
    }

    /**
     * Gets the current step's quest key. Empty for terminal states where there is no active step.
     *
     * @return the current quest key, or empty
     */
    @NotNull
    public Optional<NamespacedKey> getCurrentQuestKey() {
        return Optional.ofNullable(currentQuestKey);
    }

    /**
     * Gets the current chain state.
     *
     * @return the chain state
     */
    @NotNull
    public QuestChainState getState() {
        return state;
    }

    /**
     * Gets the number of times this chain has been completed.
     *
     * @return the completion count
     */
    public int getCompletionCount() {
        return completionCount;
    }

    /**
     * Gets the last completion timestamp in epoch millis.
     *
     * @return the last completion time, or empty if never completed
     */
    @NotNull
    public Optional<Long> getLastCompletedAt() {
        return Optional.ofNullable(lastCompletedAt);
    }

    /**
     * Returns whether this state has unsaved mutations.
     *
     * @return {@code true} if the state is dirty
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Clears the dirty flag after a successful database write.
     */
    public void clearDirty() {
        this.dirty = false;
    }

    /**
     * Returns whether the chain is currently in the {@code ACTIVE} state.
     *
     * @return {@code true} if the chain is active
     */
    public boolean isActive() {
        return state == QuestChainState.ACTIVE;
    }

    /**
     * Advances to the next step in the chain. Sets the current quest key to the provided
     * next step key and marks the state as dirty.
     *
     * @param nextQuestKey the next step's quest key
     */
    public void advance(@NotNull NamespacedKey nextQuestKey) {
        this.currentQuestKey = nextQuestKey;
        this.dirty = true;
    }

    /**
     * Marks the chain as completed, increments the completion count, and records
     * the completion timestamp.
     *
     * @param completedAt the completion timestamp in epoch millis
     */
    public void complete(long completedAt) {
        this.state = QuestChainState.COMPLETED;
        this.currentQuestKey = null;
        this.completionCount++;
        this.lastCompletedAt = completedAt;
        this.dirty = true;
    }

    /**
     * Marks the chain as abandoned and nulls the current quest key. Terminal for
     * {@code ONCE} chains; repeat-eligible for non-{@code ONCE} chains (re-start
     * governed by repeat mode evaluation).
     */
    public void abandon() {
        this.state = QuestChainState.ABANDONED;
        this.currentQuestKey = null;
        this.dirty = true;
    }

    /**
     * Marks the chain as failed and nulls the current quest key.
     */
    public void fail() {
        this.state = QuestChainState.FAILED;
        this.currentQuestKey = null;
        this.dirty = true;
    }

    /**
     * Marks the chain as expired and nulls the current quest key.
     */
    public void expire() {
        this.state = QuestChainState.EXPIRED;
        this.currentQuestKey = null;
        this.dirty = true;
    }

    /**
     * Resets the chain to a specific step, restoring the ACTIVE state.
     * Used for restart or re-resolution operations.
     *
     * @param questKey the quest key to reset to
     */
    public void resetToStep(@NotNull NamespacedKey questKey) {
        this.state = QuestChainState.ACTIVE;
        this.currentQuestKey = questKey;
        this.dirty = true;
    }

    /**
     * Fully resets the chain state — clears completion count and last completed timestamp,
     * and sets state to ACTIVE at the given first step. Used by the hard-reset admin command.
     *
     * @param firstQuestKey the first step's quest key
     */
    public void hardReset(@NotNull NamespacedKey firstQuestKey) {
        this.state = QuestChainState.ACTIVE;
        this.currentQuestKey = firstQuestKey;
        this.completionCount = 0;
        this.lastCompletedAt = null;
        this.dirty = true;
    }
}
