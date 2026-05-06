package us.eunoians.mcrpg.stat.impl;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.stat.PlayerStat;

/**
 * A {@link PlayerStat} that behaves as a resource pool with a current value and a maximum.
 * HP and Mana are the canonical examples: they deplete through use or damage and regenerate
 * over time.
 * <p>
 * For config-backed base/regen values, use {@link ConfigurableResourcePoolPlayerStat} instead.
 */
public class ResourcePoolPlayerStat extends PlayerStat {

    /**
     * @param key                   Unique identifier for this stat.
     * @param displayName           Human-readable name.
     * @param displaySymbol         Symbol shown in the action bar HUD.
     * @param defaultBaseValue      The default maximum value before modifiers.
     * @param defaultRegenPerSecond Passive regen rate per second (0 for stats like HP
     *                              where regen is handled by a separate system).
     */
    public ResourcePoolPlayerStat(@NotNull NamespacedKey key,
                                   @NotNull String displayName,
                                   @NotNull String displaySymbol,
                                   double defaultBaseValue,
                                   double defaultRegenPerSecond) {
        super(key, displayName, displaySymbol, defaultBaseValue, defaultRegenPerSecond);
    }

    @Override
    public boolean isResourcePool() {
        return true;
    }
}
