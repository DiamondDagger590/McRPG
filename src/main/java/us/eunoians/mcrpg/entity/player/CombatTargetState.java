package us.eunoians.mcrpg.entity.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Session-only, per-player state tracking the last entity attacked by the player.
 * Used by Phase Shift to determine the teleport target. Not persisted — resets on
 * logout, death, or world change.
 */
public class CombatTargetState {

    private UUID lastAttackedEntityUUID;
    private long lastAttackTimestamp;

    /**
     * Records an attack against the given entity.
     *
     * @param entityUUID The UUID of the attacked entity.
     * @param timestamp  The system time in milliseconds when the attack occurred.
     */
    public void recordAttack(@NotNull UUID entityUUID, long timestamp) {
        this.lastAttackedEntityUUID = entityUUID;
        this.lastAttackTimestamp = timestamp;
    }

    /**
     * Gets the UUID of the last entity attacked by this player.
     *
     * @return The UUID, or null if no attack has been recorded.
     */
    @Nullable
    public UUID getLastAttackedEntityUUID() {
        return lastAttackedEntityUUID;
    }

    /**
     * Gets the timestamp of the last recorded attack.
     *
     * @return The system time in milliseconds.
     */
    public long getLastAttackTimestamp() {
        return lastAttackTimestamp;
    }

    /**
     * Checks whether the player has attacked a target within the given time window.
     *
     * @param currentTime  The current system time in milliseconds.
     * @param windowMillis The maximum age of the attack in milliseconds.
     * @return True if a target was attacked within the window.
     */
    public boolean hasRecentTarget(long currentTime, long windowMillis) {
        return lastAttackedEntityUUID != null
                && (currentTime - lastAttackTimestamp) <= windowMillis;
    }

    /**
     * Clears all tracked combat state.
     */
    public void clear() {
        lastAttackedEntityUUID = null;
        lastAttackTimestamp = 0;
    }
}
