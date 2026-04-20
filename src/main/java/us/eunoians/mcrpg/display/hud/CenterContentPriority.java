package us.eunoians.mcrpg.display.hud;

/**
 * Well-known priority tiers reserved by McRPG for the action bar HUD's center
 * content slots. Third parties may use any integer priority — these constants
 * describe McRPG's own usage and leave headroom on both ends for overrides.
 * <p>
 * Higher values win when multiple slots have active content in the same frame.
 * An expiring slot is evicted automatically, revealing the next-highest slot
 * on the following frame.
 */
public final class CenterContentPriority {

    /**
     * Low-importance ambient feedback such as XP gain text. Always suppressed
     * while any higher-priority slot is populated.
     */
    public static final int AMBIENT_FEEDBACK = 1;

    /**
     * Safe-zone entry/exit flash messages. Overrides XP but yields to combo
     * state and combat feedback.
     */
    public static final int SAFE_ZONE_TRANSITION = 2;

    /**
     * Ongoing combo progress indicator (dot pattern). Renders while a combo is
     * in progress; yields only to immediate combat feedback.
     */
    public static final int COMBO_STATE = 3;

    /**
     * Immediate combat feedback such as cooldown countdowns and "Not Enough
     * Mana" errors. Takes precedence over every other built-in tier.
     */
    public static final int ABILITY_FEEDBACK = 4;

    private CenterContentPriority() {}
}
