package us.eunoians.mcrpg.combat;

/**
 * Enum representing the derived combat classification of a session, recomputed whenever the
 * participant roster changes.
 */
public enum CombatType {

    /**
     * The session contains only mob participants.
     */
    PVE,

    /**
     * The session contains at least one player participant.
     */
    PVP
}
