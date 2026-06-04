package us.eunoians.mcrpg.event.quest.chain;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;

import java.util.UUID;

/**
 * Fired when a quest chain fails for a player — currently triggered when a step quest expires
 * and the chain's {@code on-quest-expire} behavior is {@code fail-chain}.
 * <p>
 * The {@code player} field may be {@code null} when the failure occurs while the player is
 * offline (e.g., via an admin command targeting an offline player). The {@code playerUUID}
 * field is always non-null and should be preferred for identity checks.
 * <p>
 * Non-cancellable: the chain state transition has already been applied before this event fires.
 * External plugins may use this event for analytics, cleanup, or side-effect logic only.
 */
public class QuestChainFailEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final QuestChainDefinition chainDefinition;
    private final Player player;
    private final UUID playerUUID;

    /**
     * Creates a new chain-fail event.
     *
     * @param chainDefinition the definition of the failed chain
     * @param player          the player whose chain failed, or {@code null} if offline
     * @param playerUUID      the UUID of the player whose chain failed
     */
    public QuestChainFailEvent(@NotNull QuestChainDefinition chainDefinition,
                               @Nullable Player player,
                               @NotNull UUID playerUUID) {
        this.chainDefinition = chainDefinition;
        this.player = player;
        this.playerUUID = playerUUID;
    }

    /**
     * Gets the definition of the failed chain.
     *
     * @return the chain definition
     */
    @NotNull
    public QuestChainDefinition getChainDefinition() {
        return chainDefinition;
    }

    /**
     * Gets the player whose chain failed, or {@code null} if the player is offline.
     * Use {@link #getPlayerUUID()} for identity checks that must be offline-safe.
     *
     * @return the player, or {@code null} if offline
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the UUID of the player whose chain failed.
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

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
