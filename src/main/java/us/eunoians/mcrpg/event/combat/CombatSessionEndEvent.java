package us.eunoians.mcrpg.event.combat;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.CombatParticipant;
import us.eunoians.mcrpg.combat.CombatSessionEndReason;
import us.eunoians.mcrpg.combat.CombatType;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Fired when a combat session ends. Not cancellable — informational only. Carries the final
 * participant roster, the derived combat type, the end reason, and the total session duration.
 */
public class CombatSessionEndEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID entityUUID;
    private final CombatSessionEndReason reason;
    private final Collection<CombatParticipant> finalParticipants;
    private final CombatType finalCombatType;
    private final long durationMillis;

    /**
     * Constructs a new {@link CombatSessionEndEvent}.
     *
     * @param entityUUID        The UUID of the entity whose session ended.
     * @param reason            The reason the session ended.
     * @param finalParticipants The participant roster at the time of session end.
     * @param finalCombatType   The derived combat type at the time of session end.
     * @param durationMillis    The total session duration in milliseconds.
     */
    public CombatSessionEndEvent(@NotNull UUID entityUUID,
                                  @NotNull CombatSessionEndReason reason,
                                  @NotNull Collection<CombatParticipant> finalParticipants,
                                  @NotNull CombatType finalCombatType,
                                  long durationMillis) {
        this.entityUUID = entityUUID;
        this.reason = reason;
        this.finalParticipants = finalParticipants;
        this.finalCombatType = finalCombatType;
        this.durationMillis = durationMillis;
    }

    /**
     * Gets the UUID of the entity whose session ended.
     *
     * @return The entity UUID.
     */
    @NotNull
    public UUID getEntityUUID() {
        return entityUUID;
    }

    /**
     * Gets the reason the session ended.
     *
     * @return The {@link CombatSessionEndReason}.
     */
    @NotNull
    public CombatSessionEndReason getReason() {
        return reason;
    }

    /**
     * Gets the participant roster at the time the session ended.
     *
     * @return An unmodifiable {@link Collection} of final {@link CombatParticipant}s.
     */
    @NotNull
    public Collection<CombatParticipant> getFinalParticipants() {
        return Collections.unmodifiableCollection(finalParticipants);
    }

    /**
     * Gets the derived combat type at the time the session ended.
     *
     * @return The final {@link CombatType}.
     */
    @NotNull
    public CombatType getFinalCombatType() {
        return finalCombatType;
    }

    /**
     * Gets the total session duration in milliseconds.
     *
     * @return The duration in milliseconds.
     */
    public long getDurationMillis() {
        return durationMillis;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Required by Bukkit to locate the handler list for this event class when
     * a listener registers for {@link CombatSessionEndEvent}.
     *
     * @return The shared {@link HandlerList} for this event.
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
