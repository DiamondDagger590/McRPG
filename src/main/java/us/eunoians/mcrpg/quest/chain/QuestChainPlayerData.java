package us.eunoians.mcrpg.quest.chain;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Container holding all {@link QuestChainPlayerState} instances for a single player.
 * Loaded eagerly from the database at join time and stored on
 * {@link us.eunoians.mcrpg.entity.player.McRPGPlayer}.
 * <p>
 * Maintains a reverse index ({@code questKeyToChainKey}) from each ACTIVE chain's
 * {@code currentQuestKey} to its {@code chainKey}. This enables O(1) lookup when
 * a quest completes and the chain system needs to find which chain (if any) owns
 * that quest. The index is rebuilt by {@link #rebuildQuestKeyIndex()} which is called
 * by {@link QuestChainManager#advanceChain} after updating {@code currentQuestKey},
 * and internally by {@link #putChainState} and {@link #removeChainState}.
 * <p>
 * Eviction: chain states are removed on player logout (the player object is discarded)
 * and reloaded at next login via the async join pipeline. No unbounded growth occurs
 * since the map is bounded by the number of chains a player participates in.
 */
public class QuestChainPlayerData {

    private final Map<NamespacedKey, QuestChainPlayerState> chainStates;
    private final Map<NamespacedKey, NamespacedKey> questKeyToChainKey;

    public QuestChainPlayerData() {
        this.chainStates = new ConcurrentHashMap<>();
        this.questKeyToChainKey = new HashMap<>();
    }

    /**
     * Gets the chain state for the given chain key.
     *
     * @param chainKey the chain definition key
     * @return the chain state, or empty if no state exists for this chain
     */
    @NotNull
    public Optional<QuestChainPlayerState> getChainState(@NotNull NamespacedKey chainKey) {
        return Optional.ofNullable(chainStates.get(chainKey));
    }

    /**
     * Gets all chain states with {@code ACTIVE} state.
     *
     * @return list of active chain states
     */
    @NotNull
    public List<QuestChainPlayerState> getActiveChains() {
        return chainStates.values().stream()
                .filter(QuestChainPlayerState::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Gets all chain states regardless of state.
     *
     * @return all chain states
     */
    @NotNull
    public Collection<QuestChainPlayerState> getAllStates() {
        return chainStates.values();
    }

    /**
     * Gets all chain states that have unsaved mutations.
     *
     * @return dirty chain states
     */
    @NotNull
    public List<QuestChainPlayerState> getDirtyStates() {
        return chainStates.values().stream()
                .filter(QuestChainPlayerState::isDirty)
                .collect(Collectors.toList());
    }

    /**
     * Adds or replaces a chain state and incrementally updates the quest key index.
     *
     * @param state the chain state to add
     */
    public void putChainState(@NotNull QuestChainPlayerState state) {
        chainStates.put(state.getChainKey(), state);
        updateQuestKeyIndex(state);
    }

    /**
     * Inserts all states from the list and rebuilds the quest key index once at the end.
     * Use this during the login load pipeline to avoid O(N²) repeated rebuilds.
     *
     * @param states the list of chain states to insert
     */
    public void putChainStateBatch(@NotNull List<QuestChainPlayerState> states) {
        for (QuestChainPlayerState state : states) {
            chainStates.put(state.getChainKey(), state);
        }
        rebuildQuestKeyIndex();
    }

    /**
     * Removes a chain state and incrementally updates the quest key index.
     *
     * @param chainKey the chain key to remove
     */
    public void removeChainState(@NotNull NamespacedKey chainKey) {
        QuestChainPlayerState removed = chainStates.remove(chainKey);
        if (removed != null) {
            removed.getCurrentQuestKey().ifPresent(questKeyToChainKey::remove);
        }
    }

    /**
     * Incrementally updates the {@code questKeyToChainKey} reverse index for a single
     * changed chain state. Removes any old entry for this chain and adds the new current
     * quest key if the state is still ACTIVE. Prefer this over {@link #rebuildQuestKeyIndex()}
     * for single-state mutations.
     *
     * @param state the chain state that was just mutated
     */
    public void updateQuestKeyIndex(@NotNull QuestChainPlayerState state) {
        questKeyToChainKey.values().removeIf(chainKey -> chainKey.equals(state.getChainKey()));
        if (state.isActive()) {
            state.getCurrentQuestKey().ifPresent(questKey ->
                    questKeyToChainKey.put(questKey, state.getChainKey()));
        }
    }

    /**
     * Returns the chain key that currently owns the given quest key,
     * or empty if no ACTIVE chain has this quest as its current step.
     * O(1) via the {@code questKeyToChainKey} reverse index.
     *
     * @param questKey the quest definition key to look up
     * @return the chain key, or empty
     */
    @NotNull
    public Optional<NamespacedKey> getChainKeyForCurrentQuest(@NotNull NamespacedKey questKey) {
        return Optional.ofNullable(questKeyToChainKey.get(questKey));
    }

    /**
     * Rebuilds the {@code questKeyToChainKey} reverse index by iterating all ACTIVE
     * chain states and mapping their {@code currentQuestKey} to their {@code chainKey}.
     * Called after any operation that changes a chain state's {@code currentQuestKey}
     * (advance, re-resolution, put, remove).
     */
    public void rebuildQuestKeyIndex() {
        questKeyToChainKey.clear();
        for (QuestChainPlayerState state : chainStates.values()) {
            if (state.isActive()) {
                state.getCurrentQuestKey().ifPresent(questKey ->
                        questKeyToChainKey.put(questKey, state.getChainKey()));
            }
        }
    }
}
