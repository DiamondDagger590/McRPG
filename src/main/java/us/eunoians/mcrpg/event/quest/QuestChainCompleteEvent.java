package us.eunoians.mcrpg.event.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;

import java.util.UUID;

/**
 * Fired when all steps in a quest chain have been completed by a player. This event is
 * fired after the chain state has been marked {@code COMPLETED} — it is post-commit and
 * non-cancellable.
 * <p>
 * The {@code completionNumber} reflects how many times this player has now completed the
 * chain (1 on first completion, 2 on second, etc.), allowing repeat-mode handling and
 * analytics to use a single event type.
 * <p>
 * The {@code player} field may be {@code null} when completion occurs while the player
 * is offline (e.g., via a shared-scope quest completing for an offline participant). The
 * {@code playerUUID} field is always non-null and should be preferred for identity checks.
 */
public class QuestChainCompleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final QuestChainDefinition chainDefinition;
    @Nullable
    private final Player player;
    private final UUID playerUUID;
    private final int completionNumber;

    /**
     * Creates a new chain-complete event.
     *
     * @param chainDefinition  the definition of the completed chain
     * @param player           the player who completed the chain, or {@code null} if offline
     * @param playerUUID       the UUID of the player who completed the chain
     * @param completionNumber how many times the player has now completed this chain
     */
    public QuestChainCompleteEvent(@NotNull QuestChainDefinition chainDefinition,
                                   @Nullable Player player,
                                   @NotNull UUID playerUUID,
                                   int completionNumber) {
        this.chainDefinition = chainDefinition;
        this.player = player;
        this.playerUUID = playerUUID;
        this.completionNumber = completionNumber;
    }

    /**
     * Gets the definition of the completed chain.
     *
     * @return the chain definition
     */
    @NotNull
    public QuestChainDefinition getChainDefinition() {
        return chainDefinition;
    }

    /**
     * Gets the player who completed the chain, or {@code null} if the player is offline.
     * Use {@link #getPlayerUUID()} for identity checks that must be offline-safe.
     *
     * @return the player, or {@code null} if offline
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the UUID of the player who completed the chain.
     *
     * @return the player UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Gets how many times the player has now completed this chain.
     *
     * @return the completion number (≥ 1)
     */
    public int getCompletionNumber() {
        return completionNumber;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
