package us.eunoians.mcrpg.event.quest.chain;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainStep;

import java.util.UUID;

/**
 * Fired when a chain step's quest is retried after expiration.
 * Non-cancellable — the retry has already been initiated.
 */
public class QuestChainStepRetryEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final QuestChainDefinition chainDefinition;
    private final Player player;
    private final UUID playerUUID;
    private final QuestChainStep step;
    private final int retryNumber;
    private final int maxRetries;

    /**
     * Creates a new step-retry event.
     *
     * @param chainDefinition the chain definition
     * @param player          the player whose step is being retried, or {@code null} if offline
     * @param playerUUID      the UUID of the player whose step is being retried
     * @param step            the step being retried
     * @param retryNumber     which retry attempt this is (1-based)
     * @param maxRetries      the maximum number of retries allowed, or -1 for unlimited
     */
    public QuestChainStepRetryEvent(@NotNull QuestChainDefinition chainDefinition,
                                    @Nullable Player player,
                                    @NotNull UUID playerUUID,
                                    @NotNull QuestChainStep step,
                                    int retryNumber,
                                    int maxRetries) {
        this.chainDefinition = chainDefinition;
        this.player = player;
        this.playerUUID = playerUUID;
        this.step = step;
        this.retryNumber = retryNumber;
        this.maxRetries = maxRetries;
    }

    /**
     * Gets the chain definition.
     *
     * @return the chain definition
     */
    @NotNull
    public QuestChainDefinition getChainDefinition() {
        return chainDefinition;
    }

    /**
     * Gets the player whose step is being retried, or {@code null} if the player is offline.
     * Use {@link #getPlayerUUID()} for identity checks that must be offline-safe.
     *
     * @return the player, or {@code null} if offline
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the UUID of the player whose step is being retried.
     *
     * @return the player UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Gets the step being retried.
     *
     * @return the chain step
     */
    @NotNull
    public QuestChainStep getStep() {
        return step;
    }

    /**
     * Gets the retry attempt number (1-based).
     *
     * @return the retry number
     */
    public int getRetryNumber() {
        return retryNumber;
    }

    /**
     * Gets the maximum retry count.
     *
     * @return the max retries, or -1 for unlimited
     */
    public int getMaxRetries() {
        return maxRetries;
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
