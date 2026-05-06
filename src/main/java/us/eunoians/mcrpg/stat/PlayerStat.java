package us.eunoians.mcrpg.stat;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Defines a player stat type that can be registered in {@link PlayerStatRegistry}.
 * <p>
 * Each stat has a unique key, display metadata, a default base value, and describes
 * whether it behaves as a resource pool (current/max, like HP and Mana) or a flat
 * value (like Defense or Attack Power).
 * <p>
 * Display names and symbols are resolved through the localization system when
 * available, falling back to the constructor-provided defaults during early startup
 * or in unit tests where localization is not initialised.
 * <p>
 * Stat definitions are shared singletons — per-player mutable state lives in
 * {@link us.eunoians.mcrpg.stat.instance.PlayerStatInstance}.
 * <p>
 * Config-backed stats override {@link #getReloadableBaseValue()} and/or
 * {@link #getReloadableRegenPerSecond()} so that config reloads propagate
 * automatically to all online players without per-player iteration.
 */
public abstract class PlayerStat implements McRPGContent {

    private final NamespacedKey key;
    private final String fallbackDisplayName;
    private final String fallbackDisplaySymbol;
    protected final double defaultBaseValue;
    protected final double defaultRegenPerSecond;

    /**
     * @param key                   Unique identifier for this stat.
     * @param fallbackDisplayName   Human-readable name used when the localization system is
     *                              unavailable (e.g., "Health", "Mana").
     * @param fallbackDisplaySymbol Symbol used when the localization system is unavailable
     *                              (e.g., "❤", "✦").
     * @param defaultBaseValue      The default base value before modifiers.
     * @param defaultRegenPerSecond The default passive regen rate per second (0 if none).
     */
    protected PlayerStat(@NotNull NamespacedKey key,
                          @NotNull String fallbackDisplayName,
                          @NotNull String fallbackDisplaySymbol,
                          double defaultBaseValue,
                          double defaultRegenPerSecond) {
        this.key = key;
        this.fallbackDisplayName = fallbackDisplayName;
        this.fallbackDisplaySymbol = fallbackDisplaySymbol;
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
     * Returns the localization {@link Route} for this stat's display name.
     * <p>
     * The default convention is {@code stat.<key>.display-name}, derived from the
     * stat's {@link NamespacedKey#getKey()}. Subclasses may override to point at a
     * different route.
     *
     * @return The route to the localised display name.
     */
    @NotNull
    public Route getDisplayNameRoute() {
        return Route.fromString("stat." + key.getKey() + ".display-name");
    }

    /**
     * Returns the localization {@link Route} for this stat's display symbol.
     * <p>
     * The default convention is {@code stat.<key>.display-symbol}, derived from the
     * stat's {@link NamespacedKey#getKey()}. Subclasses may override to point at a
     * different route.
     *
     * @return The route to the localised display symbol.
     */
    @NotNull
    public Route getDisplaySymbolRoute() {
        return Route.fromString("stat." + key.getKey() + ".display-symbol");
    }

    /**
     * Returns the human-readable display name resolved from the server's default
     * locale, falling back to the constructor-provided default if the localization
     * system is unavailable.
     *
     * @return The localised display name of this stat.
     */
    @NotNull
    public String getDisplayName() {
        return resolveLocalized(getDisplayNameRoute(), fallbackDisplayName);
    }

    /**
     * Returns the human-readable display name resolved from the given player's
     * locale, falling back to the constructor-provided default if the localization
     * system is unavailable.
     *
     * @param player The player whose locale preference is used for resolution.
     * @return The localised display name of this stat.
     */
    @NotNull
    public String getDisplayName(@NotNull McRPGPlayer player) {
        return resolveLocalized(player, getDisplayNameRoute(), fallbackDisplayName);
    }

    /**
     * Returns the display symbol resolved from the server's default locale,
     * falling back to the constructor-provided default if the localization system
     * is unavailable.
     *
     * @return The localised display symbol of this stat.
     */
    @NotNull
    public String getDisplaySymbol() {
        return resolveLocalized(getDisplaySymbolRoute(), fallbackDisplaySymbol);
    }

    /**
     * Returns the display symbol resolved from the given player's locale,
     * falling back to the constructor-provided default if the localization system
     * is unavailable.
     *
     * @param player The player whose locale preference is used for resolution.
     * @return The localised display symbol of this stat.
     */
    @NotNull
    public String getDisplaySymbol(@NotNull McRPGPlayer player) {
        return resolveLocalized(player, getDisplaySymbolRoute(), fallbackDisplaySymbol);
    }

    /**
     * @return Whether this stat is a resource pool with current/max tracking.
     */
    public abstract boolean isResourcePool();

    /**
     * Returns optional reloadable config source for the base value.
     * Default: empty (uses compile-time default). Override to back with config.
     *
     * @return The reloadable content for the base value, or empty if hardcoded.
     */
    @NotNull
    public Optional<ReloadableContent<Double>> getReloadableBaseValue() {
        return Optional.empty();
    }

    /**
     * Returns optional reloadable config source for the regen rate.
     * Default: empty (uses compile-time default). Override to back with config.
     *
     * @return The reloadable content for the regen rate, or empty if hardcoded.
     */
    @NotNull
    public Optional<ReloadableContent<Double>> getReloadableRegenPerSecond() {
        return Optional.empty();
    }

    /**
     * Returns the current base value. If a reloadable config source exists,
     * returns the live config value; otherwise returns the compile-time default.
     *
     * @return The current base value.
     */
    public double getBaseValue() {
        return getReloadableBaseValue()
                .map(ReloadableContent::getContent)
                .orElse(defaultBaseValue);
    }

    /**
     * Returns the current regen rate per second. Delegates to reloadable content if present.
     *
     * @return The current regen rate per second.
     */
    public double getRegenPerSecond() {
        return getReloadableRegenPerSecond()
                .map(ReloadableContent::getContent)
                .orElse(defaultRegenPerSecond);
    }

    /**
     * Collects any reloadable content for tracking by the
     * {@link com.diamonddagger590.mccore.configuration.ReloadableContentManager}.
     * Called by the content handler during registration.
     *
     * @return Set of reloadable content to track, empty if this stat is hardcoded.
     */
    @NotNull
    public Set<ReloadableContent<?>> getReloadableContent() {
        Set<ReloadableContent<?>> set = new HashSet<>();
        getReloadableBaseValue().ifPresent(set::add);
        getReloadableRegenPerSecond().ifPresent(set::add);
        return set;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Stat definitions are not tied to a specific content expansion — they are
     * registered via {@link PlayerStatContentPack} which carries the expansion
     * association. Returns empty by default.
     */
    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.empty();
    }

    /**
     * Attempts to resolve a localized string using the server's default locale.
     * Returns {@code fallback} if the localization system is not yet initialised,
     * if the route is not present in any loaded locale, or if the result is null.
     *
     * @param route    The localization route to resolve.
     * @param fallback The value to return when localization is unavailable.
     * @return The resolved string, or {@code fallback}.
     */
    @NotNull
    private String resolveLocalized(@NotNull Route route, @NotNull String fallback) {
        try {
            String resolved = RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION)
                    .getLocalizedMessage(route);
            return resolved != null ? resolved : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * Attempts to resolve a localized string using the given player's locale.
     * Returns {@code fallback} if the localization system is not yet initialised,
     * if the route is not present in any loaded locale, or if the result is null.
     *
     * @param player   The player whose locale preference is used for resolution.
     * @param route    The localization route to resolve.
     * @param fallback The value to return when localization is unavailable.
     * @return The resolved string, or {@code fallback}.
     */
    @NotNull
    private String resolveLocalized(@NotNull McRPGPlayer player, @NotNull Route route, @NotNull String fallback) {
        try {
            String resolved = player.getPlugin().registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION)
                    .getLocalizedMessage(player, route);
            return resolved != null ? resolved : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}
