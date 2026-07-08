package us.eunoians.mcrpg.event.quest.chain;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;

import java.util.Optional;
import java.util.UUID;

/**
 * Fired before a chain transitions to {@link us.eunoians.mcrpg.quest.chain.QuestChainState#EXPIRED}
 * because its availability window closed. Cancellable — cancelling prevents the
 * expiration for this check interval, enabling third-party plugins to implement
 * exemptions (e.g., premium event passes, staff overrides).
 */
public class QuestChainExpireEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final QuestChainDefinition chainDefinition;
    private final UUID playerUUID;
    private final Player player;
    private boolean cancelled;

    /**
     * Creates a new chain-expire event.
     *
     * @param chainDefinition the chain definition being expired
     * @param playerUUID      the UUID of the affected player
     * @param player          the Bukkit player, or {@code null} if offline
     */
    public QuestChainExpireEvent(@NotNull QuestChainDefinition chainDefinition,
                                 @NotNull UUID playerUUID,
                                 @Nullable Player player) {
        this.chainDefinition = chainDefinition;
        this.playerUUID = playerUUID;
        this.player = player;
    }

    /**
     * Gets the chain definition being expired.
     *
     * @return the chain definition
     */
    @NotNull
    public QuestChainDefinition getChainDefinition() {
        return chainDefinition;
    }

    /**
     * Gets the UUID of the affected player.
     *
     * @return the player UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Gets the Bukkit player, if online.
     *
     * @return the player, or empty if offline
     */
    @NotNull
    public Optional<Player> getPlayer() {
        return Optional.ofNullable(player);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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
