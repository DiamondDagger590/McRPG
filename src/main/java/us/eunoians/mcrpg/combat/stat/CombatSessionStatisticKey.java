package us.eunoians.mcrpg.combat.stat;

import org.bukkit.NamespacedKey;
import us.eunoians.mcrpg.util.McRPGMethods;

/**
 * Constants for the built-in per-session statistic keys. These keys are used within
 * {@link CombatSessionStatistics} and are distinct from the cumulative {@code McRPGStatistic}
 * keys (though they share the same namespace for natural mapping).
 */
public final class CombatSessionStatisticKey {

    private CombatSessionStatisticKey() { }

    /** Total damage dealt during this session. Type: DOUBLE. */
    public static final NamespacedKey DAMAGE_DEALT = McRPGMethods.parseNamespacedKey("damage_dealt");

    /** Total damage taken during this session. Type: DOUBLE. */
    public static final NamespacedKey DAMAGE_TAKEN = McRPGMethods.parseNamespacedKey("damage_taken");

    /** Total healing applied to other entities during this session. Type: DOUBLE. */
    public static final NamespacedKey HEALING_DEALT = McRPGMethods.parseNamespacedKey("healing_dealt");

    /** Total healing received during this session, from any source (explicit heal attribution,
     *  vanilla regen, saturation, beacons, etc.). Type: DOUBLE. */
    public static final NamespacedKey HEALING_RECEIVED = McRPGMethods.parseNamespacedKey("healing_received");

    /** Attack count during this session. Type: LONG. */
    public static final NamespacedKey HITS_LANDED = McRPGMethods.parseNamespacedKey("hits_landed");

    /** Times hit during this session. Type: LONG. */
    public static final NamespacedKey HITS_RECEIVED = McRPGMethods.parseNamespacedKey("hits_received");

    /** Entities killed during this session. Type: LONG. */
    public static final NamespacedKey KILLS = McRPGMethods.parseNamespacedKey("kills");

    /** Session duration in seconds. Type: DOUBLE. Computed and written at snapshot time. */
    public static final NamespacedKey SESSION_DURATION = McRPGMethods.parseNamespacedKey("session_duration");
}
