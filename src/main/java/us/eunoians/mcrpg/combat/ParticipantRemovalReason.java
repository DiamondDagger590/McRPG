package us.eunoians.mcrpg.combat;

/**
 * Enum representing the reason a participant was removed from a session's roster.
 */
public enum ParticipantRemovalReason {

    /**
     * The participant entity died.
     */
    DEATH,

    /**
     * The participant player logged out.
     */
    LOGOUT,

    /**
     * The participant entity was removed from the world (despawn, unload, plugin removal).
     */
    DESPAWN,

    /**
     * The participant's per-participant inactivity timer expired.
     */
    TIMEOUT,

    /**
     * The participant was evicted from the mob FIFO queue to make room for a newer participant.
     */
    EVICTION,

    /**
     * The participant was removed because the session itself is ending.
     */
    SESSION_END
}
