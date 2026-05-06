package us.eunoians.mcrpg.stat.impl;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.stat.instance.PlayerStatInstance;

import java.util.Optional;

/**
 * A {@link ResourcePoolPlayerStat} backed by {@link ReloadableContent} for its base value
 * and regen rate. Used for stats whose pool size and regen are configurable by server owners
 * (e.g., mana). Plain {@link ResourcePoolPlayerStat} is used for stats with hardcoded values
 * (e.g., health as a registry entry with a fixed 20-point base).
 * <p>
 * Config changes propagate automatically to all online players because
 * {@link PlayerStatInstance#getEffectiveMax()} and {@link PlayerStatInstance#tickRegen(double)}
 * delegate to the definition's live {@link #getBaseValue()} and {@link #getRegenPerSecond()}.
 */
public class ConfigurableResourcePoolPlayerStat extends ResourcePoolPlayerStat {

    private final ReloadableContent<Double> reloadableBaseValue;
    private final ReloadableContent<Double> reloadableRegenPerSecond;

    /**
     * @param key                      Unique identifier for this stat.
     * @param displayName              Human-readable name.
     * @param displaySymbol            Symbol shown in the action bar HUD.
     * @param defaultBaseValue         Compile-time fallback for base value.
     * @param defaultRegenPerSecond    Compile-time fallback for regen rate.
     * @param reloadableBaseValue      Config-backed base value source.
     * @param reloadableRegenPerSecond Config-backed regen rate source.
     */
    public ConfigurableResourcePoolPlayerStat(
            @NotNull NamespacedKey key,
            @NotNull String displayName,
            @NotNull String displaySymbol,
            double defaultBaseValue,
            double defaultRegenPerSecond,
            @NotNull ReloadableContent<Double> reloadableBaseValue,
            @NotNull ReloadableContent<Double> reloadableRegenPerSecond) {
        super(key, displayName, displaySymbol, defaultBaseValue, defaultRegenPerSecond);
        this.reloadableBaseValue = reloadableBaseValue;
        this.reloadableRegenPerSecond = reloadableRegenPerSecond;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the config-backed base value source.
     */
    @NotNull
    @Override
    public Optional<ReloadableContent<Double>> getReloadableBaseValue() {
        return Optional.of(reloadableBaseValue);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the config-backed regen rate source.
     */
    @NotNull
    @Override
    public Optional<ReloadableContent<Double>> getReloadableRegenPerSecond() {
        return Optional.of(reloadableRegenPerSecond);
    }
}
