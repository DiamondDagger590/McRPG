package us.eunoians.mcrpg.event.combat;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.CombatParticipant;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.CombatType;
import us.eunoians.mcrpg.combat.ParticipantRemovalReason;

/**
 * Fired when a participant is removed from a combat session. Not cancellable — informational only.
 */
public class CombatParticipantRemoveEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final CombatSession session;
    private final CombatParticipant removedParticipant;
    private final ParticipantRemovalReason reason;
    private final CombatType previousCombatType;
    private final CombatType newCombatType;

    /**
     * Constructs a new {@link CombatParticipantRemoveEvent}.
     *
     * @param session             The session the participant was removed from.
     * @param removedParticipant  The removed participant.
     * @param reason              The reason for removal.
     * @param previousCombatType  The session's combat type before the removal.
     * @param newCombatType       The session's combat type after the removal.
     */
    public CombatParticipantRemoveEvent(@NotNull CombatSession session,
                                        @NotNull CombatParticipant removedParticipant,
                                        @NotNull ParticipantRemovalReason reason,
                                        @NotNull CombatType previousCombatType,
                                        @NotNull CombatType newCombatType) {
        this.session = session;
        this.removedParticipant = removedParticipant;
        this.reason = reason;
        this.previousCombatType = previousCombatType;
        this.newCombatType = newCombatType;
    }

    /**
     * Gets the session the participant was removed from.
     *
     * @return The {@link CombatSession}.
     */
    @NotNull
    public CombatSession getSession() {
        return session;
    }

    /**
     * Gets the participant that was removed.
     *
     * @return The removed {@link CombatParticipant}.
     */
    @NotNull
    public CombatParticipant getRemovedParticipant() {
        return removedParticipant;
    }

    /**
     * Gets the reason for removal.
     *
     * @return The {@link ParticipantRemovalReason}.
     */
    @NotNull
    public ParticipantRemovalReason getReason() {
        return reason;
    }

    /**
     * Gets the session's {@link CombatType} before this participant was removed.
     *
     * @return The previous combat type.
     */
    @NotNull
    public CombatType getPreviousCombatType() {
        return previousCombatType;
    }

    /**
     * Gets the session's {@link CombatType} after this participant was removed.
     *
     * @return The new combat type.
     */
    @NotNull
    public CombatType getNewCombatType() {
        return newCombatType;
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
