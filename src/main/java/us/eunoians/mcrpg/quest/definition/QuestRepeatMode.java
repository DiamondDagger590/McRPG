package us.eunoians.mcrpg.quest.definition;

/**
 * Defines how a quest may be repeated by a player once completed.
 */
public enum QuestRepeatMode {

    /**
     * The quest can only be completed once per player, ever.
     */
    ONCE,

    /**
     * The quest can be completed any number of times with no restrictions.
     */
    REPEATABLE,

    /**
     * The quest can be repeated after a configured cooldown period elapses
     * since the player's last completion.
     */
    COOLDOWN,

    /**
     * The quest can be repeated up to a configured maximum number of times
     * per player.
     */
    LIMITED
}
