package us.eunoians.mcrpg.event.quest;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fired when a cascade begins for a player — a new {@link us.eunoians.mcrpg.quest.chain.CascadeContext}
 * has been created and the root chain start or advance is about to be delegated.
 * <p>
 * Non-cancellable: the cascade has already been initiated before this event fires.
 * External plugins may use this event for logging, UI suppression, or other side-effect logic.
 */
public class CascadeStartEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final NamespacedKey chainKey;
    private final UUID playerUUID;

    /**
     * Creates a new cascade start event.
     *
     * @param chainKey   the chain key for which the cascade is starting
     * @param playerUUID the UUID of the player entering the cascade
     */
    public CascadeStartEvent(@NotNull NamespacedKey chainKey, @NotNull UUID playerUUID) {
        this.chainKey = chainKey;
        this.playerUUID = playerUUID;
    }

    /**
     * Gets the chain key for which the cascade is starting.
     *
     * @return the chain key
     */
    @NotNull
    public NamespacedKey getChainKey() {
        return chainKey;
    }

    /**
     * Gets the UUID of the player entering the cascade.
     *
     * @return the player UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
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
