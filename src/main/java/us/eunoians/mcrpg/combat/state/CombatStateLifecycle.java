package us.eunoians.mcrpg.combat.state;

/**
 * Enum representing the lifecycle scope of a combat state type. Determines what happens to the state
 * value when a session ends.
 */
public enum CombatStateLifecycle {

    /**
     * State is cleared when the session ends. Default. Used for transient combat mechanics
     * like ability stacks that only exist during active combat.
     */
    SESSION,

    /**
     * State survives session boundaries. The combat tracker preserves the value and re-attaches
     * it to the entity's next session. Backed by the persistent state DAO. Used for cross-combat
     * tracking like "times entered combat today."
     */
    PERSISTENT
}
