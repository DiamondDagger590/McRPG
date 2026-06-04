package us.eunoians.mcrpg.event.quest.chain;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private final Player player;

    /**
     * Creates a new cascade start event.
     *
     * @param chainKey   the chain key for which the cascade is starting
     * @param playerUUID the UUID of the player entering the cascade
     * @param player     the Bukkit player, or {@code null} if the player is not online
     *                   (e.g. when the cascade is initiated from an advance path where
     *                   only the UUID is known)
     */
    public CascadeStartEvent(@NotNull NamespacedKey chainKey, @NotNull UUID playerUUID,
                             @Nullable Player player) {
        this.chainKey = chainKey;
        this.playerUUID = playerUUID;
        this.player = player;
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

    /**
     * Gets the Bukkit player, or {@code null} if the player is not online.
     *
     * @return the player, or {@code null}
     */
    @Nullable
    public Player getPlayer() {
        return player;
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
