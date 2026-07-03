package us.eunoians.mcrpg.combat;

import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.util.UUID;

/**
 * Mutable data class representing a single participant in a combat session. The
 * {@code lastInteractionMillis} field is updated on every damage interaction between
 * the session owner and this participant.
 */
public class CombatParticipant {

    private final UUID uuid;
    private final ParticipantType participantType;
    private final CustomEntityWrapper entityWrapper;
    private long lastInteractionMillis;

    /**
     * Constructs a new {@link CombatParticipant}.
     *
     * @param uuid                 The UUID of the participant entity.
     * @param participantType      Whether this participant is a {@link ParticipantType#PLAYER} or {@link ParticipantType#MOB}.
     * @param entityWrapper        The {@link CustomEntityWrapper} identifying the participant's entity type.
     * @param lastInteractionMillis The timestamp (epoch millis) of the most recent interaction.
     */
    public CombatParticipant(@NotNull UUID uuid, @NotNull ParticipantType participantType,
                             @NotNull CustomEntityWrapper entityWrapper, long lastInteractionMillis) {
        this.uuid = uuid;
        this.participantType = participantType;
        this.entityWrapper = entityWrapper;
        this.lastInteractionMillis = lastInteractionMillis;
    }

    /**
     * Gets the UUID of the participant entity.
     *
     * @return The UUID of the participant entity.
     */
    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Gets the {@link ParticipantType} of this participant.
     *
     * @return The {@link ParticipantType} of this participant.
     */
    @NotNull
    public ParticipantType getParticipantType() {
        return participantType;
    }

    /**
     * Gets the {@link CustomEntityWrapper} identifying this participant's entity type.
     * Supports both vanilla entity types and custom entities from plugins like MythicMobs.
     *
     * @return The {@link CustomEntityWrapper} for this participant.
     */
    @NotNull
    public CustomEntityWrapper getEntityWrapper() {
        return entityWrapper;
    }

    /**
     * Gets the timestamp of the most recent interaction between this participant
     * and the session owner.
     *
     * @return Epoch milliseconds of the last interaction.
     */
    public long getLastInteractionMillis() {
        return lastInteractionMillis;
    }

    /**
     * Updates the timestamp of the most recent interaction.
     *
     * @param lastInteractionMillis Epoch milliseconds of the new interaction.
     */
    public void setLastInteractionMillis(long lastInteractionMillis) {
        this.lastInteractionMillis = lastInteractionMillis;
    }

    /**
     * Checks whether this participant has exceeded the given inactivity timeout.
     *
     * @param timeoutMillis The inactivity timeout threshold in milliseconds.
     * @return {@code true} if this participant has been inactive longer than the timeout.
     */
    public boolean isTimedOut(long timeoutMillis) {
        long currentTimeMillis = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
        return (currentTimeMillis - lastInteractionMillis) >= timeoutMillis;
    }
}
