package us.eunoians.mcrpg.quest.chain;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.quest.CascadeCompletedStep;
import us.eunoians.mcrpg.quest.definition.OnStartMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Transient per-player state tracking a cascade of auto-completed chain steps
 * within a single tick. Lifetime:
 * <ol>
 *   <li>Created by {@link CascadeOrchestrator} before delegating to the chain manager
 *       at the root cascade site (either {@code tryStartChain} or {@code advanceChain})</li>
 *   <li>Populated by recursive {@code advanceChain} calls (recording auto-completed steps)
 *       and by {@link us.eunoians.mcrpg.listener.quest.QuestStartMessageListener} (deferring
 *       on-start messages)</li>
 *   <li>Finalized by {@link CascadeOrchestrator#finalizeCascade} after the root
 *       delegation returns — sends batch summary and delivers
 *       the final (non-auto-completed) step's deferred messages</li>
 *   <li>Removed from the orchestrator's {@code activeCascades} map</li>
 * </ol>
 * <p>
 * Not thread-safe — all access is on the main Bukkit thread within a single tick.
 */
public class CascadeContext {

    private final NamespacedKey chainKey;
    private final List<CascadeCompletedStep> autoCompletedSteps;
    private final Map<NamespacedKey, List<OnStartMessage>> deferredMessages;
    private NamespacedKey lastStartedQuestKey;

    /**
     * Creates a new cascade context for the given chain.
     *
     * @param chainKey the chain being cascaded
     */
    public CascadeContext(@NotNull NamespacedKey chainKey) {
        this.chainKey = chainKey;
        this.autoCompletedSteps = new ArrayList<>();
        this.deferredMessages = new LinkedHashMap<>();
    }

    /**
     * Returns the key of the chain this cascade belongs to.
     *
     * @return the chain key
     */
    @NotNull
    public NamespacedKey getChainKey() {
        return chainKey;
    }

    /**
     * Records that a step auto-completed during this cascade. Steps are stored
     * in encounter order for the batch summary.
     *
     * @param questKey    the quest definition key of the completed step
     * @param displayName the resolved display name for the batch summary
     */
    public void recordAutoCompletedStep(@NotNull NamespacedKey questKey, @NotNull String displayName) {
        autoCompletedSteps.add(new CascadeCompletedStep(questKey, displayName));
    }

    /**
     * Defers on-start messages for a quest that is starting during a cascade.
     * Messages are stored keyed by quest key so {@link CascadeOrchestrator#finalizeCascade}
     * can selectively deliver only the final step's messages.
     *
     * @param questKey the quest whose messages are being deferred
     * @param messages the on-start messages to defer
     */
    public void deferMessages(@NotNull NamespacedKey questKey, @NotNull List<OnStartMessage> messages) {
        deferredMessages.put(questKey, new ArrayList<>(messages));
    }

    /**
     * Tracks the most recently started quest key. Updated each time a new step
     * quest is started during the cascade. Used by {@code finalizeCascade} to identify
     * which step's deferred messages should be delivered (the final non-auto-completed step).
     *
     * @param questKey the quest key being started
     */
    public void setLastStartedQuestKey(@NotNull NamespacedKey questKey) {
        this.lastStartedQuestKey = questKey;
    }

    /**
     * Returns the most recently started quest key, or empty if no quest
     * has been started during this cascade yet.
     *
     * @return the last started quest key, or empty
     */
    @NotNull
    public Optional<NamespacedKey> getLastStartedQuestKey() {
        return Optional.ofNullable(lastStartedQuestKey);
    }

    /**
     * Returns an unmodifiable view of all auto-completed steps recorded during
     * this cascade, in the order they completed.
     *
     * @return unmodifiable list of auto-completed steps
     */
    @NotNull
    public List<CascadeCompletedStep> getAutoCompletedSteps() {
        return Collections.unmodifiableList(autoCompletedSteps);
    }

    /**
     * Returns an unmodifiable view of the deferred on-start messages for the given
     * quest key, or an empty list if no messages were deferred for that key.
     *
     * @param questKey the quest definition key to look up
     * @return unmodifiable list of deferred messages, or an empty list
     */
    @NotNull
    public List<OnStartMessage> getDeferredMessagesFor(@NotNull NamespacedKey questKey) {
        List<OnStartMessage> messages = deferredMessages.get(questKey);
        return messages != null ? Collections.unmodifiableList(messages) : List.of();
    }

    /**
     * Returns {@code true} if at least one step auto-completed during this cascade.
     *
     * @return {@code true} if auto-completed steps were recorded
     */
    public boolean hasAutoCompletedSteps() {
        return !autoCompletedSteps.isEmpty();
    }

}
