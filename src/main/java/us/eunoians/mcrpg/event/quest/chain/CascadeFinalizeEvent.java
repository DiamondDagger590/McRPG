package us.eunoians.mcrpg.event.quest.chain;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Fired after a cascade finalizes for a player — after the batch summary has been sent
 * and deferred messages for the final step have been delivered. This event provides a
 * snapshot of the cascade outcome.
 * <p>
 * The {@code player} field may be {@code null} if the player disconnected mid-cascade.
 * In that case the cascade was cleaned up but no messages were delivered.
 * <p>
 * Non-cancellable: the cascade has already been finalized before this event fires.
 * External plugins may use this event for analytics, achievement checks, or UI updates.
 */
public class CascadeFinalizeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final NamespacedKey chainKey;
    private final UUID playerUUID;
    private final Player player;
    private final List<CascadeCompletedStep> autoCompletedSteps;
    private final NamespacedKey lastStartedQuestKey;
    private final CascadeOutcome outcome;

    /**
     * Creates a new cascade finalize event.
     *
     * @param chainKey            the chain key for which the cascade occurred
     * @param playerUUID          the UUID of the player
     * @param player              the Bukkit player, or {@code null} if offline
     * @param autoCompletedSteps  the list of steps that auto-completed during the cascade
     * @param lastStartedQuestKey the quest key of the final step started, or {@code null}
     * @param outcome             the outcome of the cascade
     */
    public CascadeFinalizeEvent(@NotNull NamespacedKey chainKey,
                                @NotNull UUID playerUUID,
                                @Nullable Player player,
                                @NotNull List<CascadeCompletedStep> autoCompletedSteps,
                                @Nullable NamespacedKey lastStartedQuestKey,
                                @NotNull CascadeOutcome outcome) {
        this.chainKey = chainKey;
        this.playerUUID = playerUUID;
        this.player = player;
        this.autoCompletedSteps = Collections.unmodifiableList(autoCompletedSteps);
        this.lastStartedQuestKey = lastStartedQuestKey;
        this.outcome = outcome;
    }

    /**
     * Gets the chain key for which the cascade occurred.
     *
     * @return the chain key
     */
    @NotNull
    public NamespacedKey getChainKey() {
        return chainKey;
    }

    /**
     * Gets the UUID of the player.
     *
     * @return the player UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Gets the Bukkit player, or {@code null} if the player disconnected mid-cascade.
     *
     * @return the player, or {@code null}
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the list of steps that auto-completed during the cascade. Empty if no
     * steps auto-completed (i.e., this was a normal single-step cascade context
     * with no recursive advancement).
     *
     * @return unmodifiable list of auto-completed steps
     */
    @NotNull
    public List<CascadeCompletedStep> getAutoCompletedSteps() {
        return autoCompletedSteps;
    }

    /**
     * Returns {@code true} if at least one step auto-completed during the cascade.
     *
     * @return {@code true} if steps auto-completed
     */
    public boolean hadAutoCompletedSteps() {
        return !autoCompletedSteps.isEmpty();
    }

    /**
     * Gets the quest key of the last step started during the cascade.
     *
     * @return the last started quest key, or empty if no step was started
     */
    @NotNull
    public Optional<NamespacedKey> getLastStartedQuestKey() {
        return Optional.ofNullable(lastStartedQuestKey);
    }

    /**
     * Gets the outcome of this cascade — whether it completed successfully, was
     * halted by the depth limit, or failed due to an internal error.
     *
     * @return the cascade outcome
     */
    @NotNull
    public CascadeOutcome getOutcome() {
        return outcome;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Returns the handler list for this event type.
     *
     * @return the handler list
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
