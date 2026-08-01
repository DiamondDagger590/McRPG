package us.eunoians.mcrpg.combat;

/**
 * Enum representing the reason a combat session ended.
 */
public enum CombatSessionEndReason {

    /**
     * The session's inactivity timer expired with no new combat events.
     */
    TIMEOUT,

    /**
     * The session owner died.
     */
    DEATH,

    /**
     * The session owner logged out.
     */
    LOGOUT,

    /**
     * All participants in the session are dead, despawned, or otherwise invalid.
     */
    ALL_PARTICIPANTS_GONE,

    /**
     * The session was ended programmatically via the API.
     */
    PLUGIN
}
