package us.eunoians.mcrpg.quest.chain;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mutable per-player state for a single quest chain. Loaded eagerly at join from
 * {@link us.eunoians.mcrpg.database.table.quest.chain.QuestChainStateDAO} and mutated by
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

    /**
     * Represents a step completion that has been recorded in memory but may not yet be
     * persisted to the completion log. Tracked here so that synchronous flush at logout
     * can replay entries whose async write was cancelled.
     *
     * @param questKey         the quest key that was completed
     * @param completedAt      the completion timestamp
     * @param completionNumber which chain completion run this belongs to (1-based)
     */
    public record PendingAdvancement(@NotNull NamespacedKey questKey, @NotNull Instant completedAt, int completionNumber) {}

    private final NamespacedKey chainKey;
    private NamespacedKey currentQuestKey;
    private QuestChainState state;
    private int completionCount;
    private Instant lastCompletedAt;
    private boolean conditionsPending;
    private final AtomicInteger dirtyVersion = new AtomicInteger(0);
    private final List<PendingAdvancement> pendingAdvancements = new ArrayList<>();

    /**
     * Constructs a player chain state from database values. Nullable parameters are
     * stored internally and exposed as {@link Optional} via getters.
     *
     * @param chainKey        the chain definition key
     * @param currentQuestKey the current step's quest key ({@code null} for terminal states)
     * @param state           the chain state
     * @param completionCount the number of times this chain has been completed
     * @param lastCompletedAt the last completion timestamp ({@code null} if never)
     */
    public QuestChainPlayerState(@NotNull NamespacedKey chainKey,
                                 @Nullable NamespacedKey currentQuestKey,
                                 @NotNull QuestChainState state,
                                 int completionCount,
                                 @Nullable Instant lastCompletedAt) {
        this.chainKey = chainKey;
        this.currentQuestKey = currentQuestKey;
        this.state = state;
        this.completionCount = completionCount;
        this.lastCompletedAt = lastCompletedAt;
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
        QuestChainPlayerState state = new QuestChainPlayerState(chainKey, firstQuestKey, QuestChainState.ACTIVE, 0, null);
        state.markDirty();
        return state;
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
     * Gets the last completion timestamp.
     *
     * @return the last completion time, or empty if never completed
     */
    @NotNull
    public Optional<Instant> getLastCompletedAt() {
        return Optional.ofNullable(lastCompletedAt);
    }

    /**
     * Returns the current dirty version. A value of 0 means the state is clean.
     * Each mutation increments this counter. Used by persistence to snapshot the
     * version before writing and conditionally clear only if no new mutations
     * occurred since the snapshot.
     *
     * @return the current dirty version
     */
    public int getDirtyVersion() {
        return dirtyVersion.get();
    }

    /**
     * Returns whether this state has unsaved mutations.
     *
     * @return {@code true} if the state is dirty
     */
    public boolean isDirty() {
        return dirtyVersion.get() > 0;
    }

    /**
     * Clears the dirty flag only if no mutations have occurred since the given
     * snapshot version. Returns whether the clear succeeded.
     *
     * @param snapshotVersion the version captured at persistence snapshot time
     * @return {@code true} if dirty was cleared; {@code false} if a newer mutation occurred
     */
    public boolean clearDirtyIfCurrent(int snapshotVersion) {
        return dirtyVersion.compareAndSet(snapshotVersion, 0);
    }

    /**
     * Unconditionally resets the dirty version to 0. Used only by authoritative
     * synchronous flush operations where the caller has exclusive access to this
     * state (e.g. logout flush on the DB thread after prepareForFlush gates
     * all async writes).
     */
    public void clearDirty() {
        dirtyVersion.set(0);
    }

    /**
     * Explicitly marks this state as dirty. Used for initial creation via
     * {@link #newActive(NamespacedKey, NamespacedKey)} where no mutation method
     * runs but the state must be persisted at logout.
     */
    public void markDirty() {
        dirtyVersion.incrementAndGet();
    }

    /**
     * Records a step advancement that needs to be written to the completion log, and marks
     * this state dirty so the synchronous flush at logout picks it up even when the advancement
     * is recorded outside of a normal state mutation (e.g. the failure path in
     * {@link QuestChainManager#advanceChain}).
     * <p>
     * The async write path snapshots these entries without clearing them. If the async write
     * fails or is skipped, the entries remain here for the synchronous flush at logout, which
     * clears them only after a successful transaction.
     *
     * @param questKey         the completed quest key
     * @param completedAt      the completion timestamp
     * @param completionNumber which chain completion run this belongs to (1-based)
     */
    public void recordAdvancement(@NotNull NamespacedKey questKey, @NotNull Instant completedAt, int completionNumber) {
        pendingAdvancements.add(new PendingAdvancement(questKey, completedAt, completionNumber));
        dirtyVersion.incrementAndGet();
    }

    /**
     * Returns an unmodifiable view of the pending advancement entries that have not
     * yet been cleared. Used by persistence to snapshot advancement data before writing.
     *
     * @return unmodifiable list of pending advancements
     */
    @NotNull
    public List<PendingAdvancement> getPendingAdvancements() {
        return Collections.unmodifiableList(pendingAdvancements);
    }

    /**
     * Clears all pending advancement entries. Called after a successful synchronous flush has
     * persisted them to the completion log.
     */
    public void clearPendingAdvancements() {
        pendingAdvancements.clear();
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
     * Returns whether this chain step is waiting for start conditions to be met.
     *
     * @return {@code true} if conditions are pending
     */
    public boolean isConditionsPending() {
        return conditionsPending;
    }

    /**
     * Sets the conditions-pending flag. When {@code true}, the chain is ACTIVE at a step
     * whose start conditions have not yet been met — the step's quest has not been started.
     *
     * @param pending whether conditions are pending
     */
    public void setConditionsPending(boolean pending) {
        this.conditionsPending = pending;
        dirtyVersion.incrementAndGet();
    }

    /**
     * Advances to the next step in the chain. Sets the current quest key to the provided
     * next step key and marks the state as dirty.
     *
     * @param nextQuestKey the next step's quest key
     */
    public void advance(@NotNull NamespacedKey nextQuestKey) {
        this.currentQuestKey = nextQuestKey;
        dirtyVersion.incrementAndGet();
    }

    /**
     * Marks the chain as completed, increments the completion count, and records
     * the completion timestamp.
     *
     * @param completedAt the completion timestamp
     */
    public void complete(@NotNull Instant completedAt) {
        this.state = QuestChainState.COMPLETED;
        this.currentQuestKey = null;
        this.completionCount++;
        this.lastCompletedAt = completedAt;
        dirtyVersion.incrementAndGet();
    }

    /**
     * Marks the chain as abandoned and nulls the current quest key. Terminal for
     * {@code ONCE} chains; repeat-eligible for non-{@code ONCE} chains (re-start
     * governed by repeat mode evaluation).
     */
    public void abandon() {
        this.state = QuestChainState.ABANDONED;
        this.currentQuestKey = null;
        dirtyVersion.incrementAndGet();
    }

    /**
     * Marks the chain as failed and nulls the current quest key.
     */
    public void fail() {
        this.state = QuestChainState.FAILED;
        this.currentQuestKey = null;
        dirtyVersion.incrementAndGet();
    }

    /**
     * Marks the chain as expired and nulls the current quest key.
     */
    public void expire() {
        this.state = QuestChainState.EXPIRED;
        this.currentQuestKey = null;
        dirtyVersion.incrementAndGet();
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
        dirtyVersion.incrementAndGet();
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
        dirtyVersion.incrementAndGet();
    }
}
