package us.eunoians.mcrpg.stat;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a combat stat type that can be registered in {@link CombatStatRegistry}.
 * <p>
 * Each stat has a unique key, display metadata, a default base value, and describes
 * whether it behaves as a resource pool (current/max, like HP and Mana) or a flat
 * value (like Defense or Attack Power).
 * <p>
 * Stat definitions are shared singletons — per-player mutable state lives in
 * {@link CombatStatInstance}.
 */
public abstract class CombatStat {

    private final NamespacedKey key;
    private final String displayName;
    private final String displaySymbol;
    private final double defaultBaseValue;
    private final double defaultRegenPerSecond;

    /**
     * @param key                  Unique identifier for this stat.
     * @param displayName          Human-readable name (e.g., "Health", "Mana").
     * @param displaySymbol        Symbol shown in the action bar (e.g., "❤", "✦").
     * @param defaultBaseValue     The default base value before modifiers.
     * @param defaultRegenPerSecond The default passive regen rate per second (0 if none).
     */
    protected CombatStat(@NotNull NamespacedKey key,
                          @NotNull String displayName,
                          @NotNull String displaySymbol,
                          double defaultBaseValue,
                          double defaultRegenPerSecond) {
        this.key = key;
        this.displayName = displayName;
        this.displaySymbol = displaySymbol;
        this.defaultBaseValue = defaultBaseValue;
        this.defaultRegenPerSecond = defaultRegenPerSecond;
    }

    /**
     * @return The unique key identifying this stat.
     */
    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    /**
     * @return The human-readable display name of this stat.
     */
    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return The symbol used in the action bar HUD display.
     */
    @NotNull
    public String getDisplaySymbol() {
        return displaySymbol;
    }

    /**
     * @return Whether this stat is a resource pool with current/max tracking.
     */
    public abstract boolean isResourcePool();

    /**
     * @return The default base value for this stat before any modifiers.
     */
    public double getDefaultBaseValue() {
        return defaultBaseValue;
    }

    /**
     * @return The default passive regeneration rate per second.
     */
    public double getDefaultRegenPerSecond() {
        return defaultRegenPerSecond;
    }
}
