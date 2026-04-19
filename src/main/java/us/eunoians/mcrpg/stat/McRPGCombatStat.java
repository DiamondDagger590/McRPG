package us.eunoians.mcrpg.stat;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.util.McRPGMethods;

/**
 * Constants class defining the built-in McRPG combat stat definitions.
 * <p>
 * Follows the same pattern as {@link us.eunoians.mcrpg.statistic.McRPGStatistic}
 * for gameplay statistics.
 */
public final class McRPGCombatStat {

    private McRPGCombatStat() {}

    private static final String NAMESPACE = McRPGMethods.getMcRPGNamespace();

    /** Key for the Health stat. */
    @SuppressWarnings("deprecation")
    public static final NamespacedKey HEALTH_KEY = new NamespacedKey(NAMESPACE, "health");

    /** Key for the Mana stat. */
    @SuppressWarnings("deprecation")
    public static final NamespacedKey MANA_KEY = new NamespacedKey(NAMESPACE, "mana");

    /**
     * Health — resource pool displayed on the left side of the action bar.
     * In the PoC, current HP is derived from vanilla health as a scaled display value.
     * No regen (HP regen is handled by a separate future system).
     */
    public static final CombatStat HEALTH = new ResourcePoolCombatStat(
            HEALTH_KEY, "Health", "❤", 200, 0
    );

    /**
     * Mana — resource pool displayed on the right side of the action bar.
     * Consumed by combo abilities, regenerates passively.
     * Base values come from config; these are compile-time defaults.
     */
    public static final CombatStat MANA = new ResourcePoolCombatStat(
            MANA_KEY, "Mana", "✦", 220, 5
    );

    /**
     * Registers all built-in combat stats into the provided registry.
     *
     * @param registry The registry to populate.
     */
    public static void registerAll(@NotNull CombatStatRegistry registry) {
        registry.register(HEALTH);
        registry.register(MANA);
    }
}
