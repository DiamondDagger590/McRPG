package us.eunoians.mcrpg.event.combat;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.CombatParticipant;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.CombatType;

import java.util.Collection;
import java.util.Collections;

/**
 * Fired when a player disconnects with an active combat session and the server's
 * combat log mode matches the session's combat type. Cancelling this event
 * exempts the player entirely — no punishments are evaluated or applied.
 */
public class PlayerCombatLogEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final CombatSession session;
    private final CombatType combatType;
    private final Collection<CombatParticipant> participants;
    private boolean cancelled;

    /**
     * Constructs a new {@link PlayerCombatLogEvent}.
     *
     * @param player       The player who is combat logging.
     * @param session      The player's active combat session.
     * @param combatType   The derived combat type of the session at logout time.
     * @param participants The participant roster at logout time.
     */
    public PlayerCombatLogEvent(@NotNull Player player, @NotNull CombatSession session,
                                @NotNull CombatType combatType,
                                @NotNull Collection<CombatParticipant> participants) {
        this.player = player;
        this.session = session;
        this.combatType = combatType;
        this.participants = Collections.unmodifiableCollection(participants);
        this.cancelled = false;
    }

    /**
     * Gets the player who is combat logging.
     *
     * @return The {@link Player}.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the player's active combat session.
     *
     * @return The {@link CombatSession}.
     */
    @NotNull
    public CombatSession getSession() {
        return session;
    }

    /**
     * Gets the derived combat type of the session at logout time.
     *
     * @return The {@link CombatType}.
     */
    @NotNull
    public CombatType getCombatType() {
        return combatType;
    }

    /**
     * Gets the participant roster at logout time.
     *
     * @return An unmodifiable collection of {@link CombatParticipant}s.
     */
    @NotNull
    public Collection<CombatParticipant> getParticipants() {
        return participants;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * Gets the static handler list for this event type.
     *
     * @return The {@link HandlerList}.
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
