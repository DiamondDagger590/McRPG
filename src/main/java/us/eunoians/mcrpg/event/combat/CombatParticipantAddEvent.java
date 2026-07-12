package us.eunoians.mcrpg.event.combat;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.CombatParticipant;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.CombatType;

/**
 * Fired when a new participant is about to join an existing combat session. Cancellable — cancelling
 * prevents the participant from being added and leaves the session's activity timer untouched, so a
 * listener that consistently cancels adds (e.g. excluding pets or NPCs from combat) will not keep the
 * owner's session alive indefinitely.
 */
public class CombatParticipantAddEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final CombatSession session;
    private final CombatParticipant newParticipant;
    private final CombatType previousCombatType;
    private final CombatType newCombatType;
    private boolean cancelled;

    /**
     * Constructs a new {@link CombatParticipantAddEvent}.
     *
     * @param session            The session the participant is joining.
     * @param newParticipant     The participant being added.
     * @param previousCombatType The session's combat type before the addition.
     * @param newCombatType      The session's combat type after the addition.
     */
    public CombatParticipantAddEvent(@NotNull CombatSession session,
                                     @NotNull CombatParticipant newParticipant,
                                     @NotNull CombatType previousCombatType,
                                     @NotNull CombatType newCombatType) {
        this.session = session;
        this.newParticipant = newParticipant;
        this.previousCombatType = previousCombatType;
        this.newCombatType = newCombatType;
    }

    /**
     * Gets the session the participant is joining.
     *
     * @return The {@link CombatSession}.
     */
    @NotNull
    public CombatSession getSession() {
        return session;
    }

    /**
     * Gets the participant being added.
     *
     * @return The new {@link CombatParticipant}.
     */
    @NotNull
    public CombatParticipant getNewParticipant() {
        return newParticipant;
    }

    /**
     * Gets the session's {@link CombatType} before this participant was added.
     *
     * @return The previous combat type.
     */
    @NotNull
    public CombatType getPreviousCombatType() {
        return previousCombatType;
    }

    /**
     * Gets the session's {@link CombatType} after this participant is added.
     *
     * @return The new combat type.
     */
    @NotNull
    public CombatType getNewCombatType() {
        return newCombatType;
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

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
