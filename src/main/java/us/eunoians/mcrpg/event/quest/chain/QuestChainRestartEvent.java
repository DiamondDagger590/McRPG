package us.eunoians.mcrpg.event.quest.chain;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;

import java.util.UUID;

/**
 * Fired when a chain is re-started from step 1. Occurs either from repeat-mode
 * re-evaluation or from the {@code restart-chain} on-quest-expire behavior.
 * Non-cancellable — the state transition has already been applied.
 */
public class QuestChainRestartEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final QuestChainDefinition chainDefinition;
    private final Player player;
    private final UUID playerUUID;
    private final RestartReason reason;

    /**
     * Describes why a chain was restarted.
     */
    public enum RestartReason {
        /** The chain's repeat mode allowed a repeat re-start. */
        REPEAT_MODE,
        /** A step quest expired with {@code restart-chain} on-quest-expire behavior. */
        QUEST_EXPIRE_RESTART_CHAIN
    }

    /**
     * Creates a new chain-restart event.
     *
     * @param chainDefinition the chain definition
     * @param player          the player whose chain was restarted, or {@code null} if offline
     * @param playerUUID      the UUID of the player whose chain was restarted
     * @param reason          the reason for the restart
     */
    public QuestChainRestartEvent(@NotNull QuestChainDefinition chainDefinition,
                                  @Nullable Player player,
                                  @NotNull UUID playerUUID,
                                  @NotNull RestartReason reason) {
        this.chainDefinition = chainDefinition;
        this.player = player;
        this.playerUUID = playerUUID;
        this.reason = reason;
    }

    /**
     * Gets the chain definition that was restarted.
     *
     * @return the chain definition
     */
    @NotNull
    public QuestChainDefinition getChainDefinition() {
        return chainDefinition;
    }

    /**
     * Gets the player whose chain was restarted, or {@code null} if the player is offline.
     * Use {@link #getPlayerUUID()} for identity checks that must be offline-safe.
     *
     * @return the player, or {@code null} if offline
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the UUID of the player whose chain was restarted.
     *
     * @return the player UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Gets the reason for the restart.
     *
     * @return the restart reason
     */
    @NotNull
    public RestartReason getReason() {
        return reason;
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
