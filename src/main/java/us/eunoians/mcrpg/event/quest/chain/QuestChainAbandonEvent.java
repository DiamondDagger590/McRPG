package us.eunoians.mcrpg.event.quest.chain;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainManager;

import java.util.UUID;

/**
 * Fired when a quest chain is abandoned for a player. This can occur from any of:
 * <ul>
 *   <li>The player cancelled a step quest while the chain was active
 *       ({@link QuestChainManager#handleQuestCancelled})</li>
 *   <li>An admin force-abandoned the chain via a command</li>
 *   <li>The player disabled the tutorial via the confirmation GUI, which calls
 *       {@link QuestChainManager#abandonChain(java.util.UUID, org.bukkit.NamespacedKey)}
 *       directly with the chain key</li>
 * </ul>
 * <p>
 * The {@code player} field may be {@code null} when the abandonment occurs while the player
 * is offline (e.g., via an admin command targeting an offline player). The {@code playerUUID}
 * field is always non-null and should be preferred for identity checks.
 * <p>
 * Non-cancellable: the chain state transition has already been applied before this event fires.
 * External plugins may use this event for analytics, cleanup, or side-effect logic only.
 */
public class QuestChainAbandonEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final QuestChainDefinition chainDefinition;
    private final Player player;
    private final UUID playerUUID;

    /**
     * Creates a new chain-abandon event.
     *
     * @param chainDefinition the definition of the abandoned chain
     * @param player          the player whose chain was abandoned, or {@code null} if offline
     * @param playerUUID      the UUID of the player whose chain was abandoned
     */
    public QuestChainAbandonEvent(@NotNull QuestChainDefinition chainDefinition,
                                  @Nullable Player player,
                                  @NotNull UUID playerUUID) {
        this.chainDefinition = chainDefinition;
        this.player = player;
        this.playerUUID = playerUUID;
    }

    /**
     * Gets the definition of the abandoned chain.
     *
     * @return the chain definition
     */
    @NotNull
    public QuestChainDefinition getChainDefinition() {
        return chainDefinition;
    }

    /**
     * Gets the player whose chain was abandoned, or {@code null} if the player is offline.
     * Use {@link #getPlayerUUID()} for identity checks that must be offline-safe.
     *
     * @return the player, or {@code null} if offline
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the UUID of the player whose chain was abandoned.
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
