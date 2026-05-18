package us.eunoians.mcrpg.display.hud;

/**
 * Well-known priority tiers reserved by McRPG for the action bar HUD's center
 * content slots. Third parties may use any integer priority — these constants
 * describe McRPG's own usage and intentionally leave gaps for third-party
 * plugins to slot into.
 * <p>
 * Higher values win when multiple slots have active content in the same frame.
 * An expiring slot is evicted automatically, revealing the next-highest slot
 * on the following frame.
 * <p>
 * <b>Third-party gap guide:</b>
 * <ul>
 *   <li>1–9: below ambient (nearly invisible)</li>
 *   <li>11–19: between ambient and safe-zone</li>
 *   <li>21–39: between safe-zone and ability feedback</li>
 *   <li>41–98: above ability feedback but below combo — useful for custom
 *       informational overlays that should not block combo progress</li>
 *   <li>100+: above combo — reserved for emergency/override use only</li>
 * </ul>
 */
public final class CenterContentPriority {

    /**
     * Low-importance ambient feedback such as XP gain text. Always suppressed
     * while any higher-priority slot is populated.
     */
    public static final int AMBIENT_FEEDBACK = 10;

    /**
     * Safe-zone entry/exit flash messages. Overrides ambient feedback but
     * yields to ability feedback and combo state.
     */
    public static final int SAFE_ZONE_TRANSITION = 20;

    /**
     * Immediate ability feedback such as "Not Enough Mana" and "On Cooldown"
     * denial flashes. Overrides ambient and safe-zone content but yields to
     * active combo state.
     */
    public static final int ABILITY_FEEDBACK = 40;

    /**
     * Ongoing combo progress indicator (dot pattern). The highest built-in
     * priority — renders over all other McRPG content while a combo is in
     * progress. Combo feedback is the most time-sensitive information shown
     * on the action bar and must never be blocked by lower-priority content.
     */
    public static final int COMBO_STATE = 99;

    private CenterContentPriority() {}
}
