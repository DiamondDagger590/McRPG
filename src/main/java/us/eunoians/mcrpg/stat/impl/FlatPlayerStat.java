package us.eunoians.mcrpg.stat.impl;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.stat.PlayerStat;

/**
 * A {@link PlayerStat} that represents a flat numerical value with no current/max tracking.
 * Defense and Attack Power are examples: they have an effective value computed from base +
 * modifiers, but no depletable pool.
 */
public class FlatPlayerStat extends PlayerStat {

    /**
     * @param key              Unique identifier for this stat.
     * @param displayName      Human-readable name.
     * @param displaySymbol    Symbol shown in the action bar HUD.
     * @param defaultBaseValue The default base value before modifiers.
     */
    public FlatPlayerStat(@NotNull NamespacedKey key,
                           @NotNull String displayName,
                           @NotNull String displaySymbol,
                           double defaultBaseValue) {
        super(key, displayName, displaySymbol, defaultBaseValue, 0);
    }

    @Override
    public boolean isResourcePool() {
        return false;
    }
}
