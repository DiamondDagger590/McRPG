package us.eunoians.mcrpg.combat.log;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

import java.util.Optional;
import java.util.Set;

/**
 * Abstract base for a punishment applied when a player combat logs. Each type
 * carries a {@link NamespacedKey} for serialization/registry lookup, declares
 * mutually exclusive types via {@link #getExcludes()}, and implements its
 * punishment behavior in {@link #apply(Player, CombatSession, McRPG)}.
 * <p>
 * Third-party plugins subclass this and register via
 * {@link us.eunoians.mcrpg.expansion.content.CombatLogPunishmentContentPack}.
 */
public abstract class CombatLogPunishmentType implements McRPGContent {

    private final NamespacedKey key;
    private final String configKey;
    @Nullable
    private final NamespacedKey expansionKey;

    /**
     * Constructs a new {@link CombatLogPunishmentType}.
     *
     * @param key          The unique key identifying this punishment type.
     * @param configKey    The YAML config key for this type's enabled/disabled state.
     * @param expansionKey The {@link NamespacedKey} of the owning content expansion, or
     *                     {@code null} if this type is not tied to a specific expansion.
     */
    protected CombatLogPunishmentType(@NotNull NamespacedKey key, @NotNull String configKey,
                                      @Nullable NamespacedKey expansionKey) {
        this.key = key;
        this.configKey = configKey;
        this.expansionKey = expansionKey;
    }

    /**
     * Gets the unique key identifying this punishment type.
     *
     * @return The {@link NamespacedKey}.
     */
    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    /**
     * Gets the YAML config key for this punishment type.
     *
     * @return The config key string.
     */
    @NotNull
    public String getConfigKey() {
        return configKey;
    }

    /**
     * Returns whether this punishment type is currently enabled.
     *
     * @return {@code true} if this type is enabled.
     */
    public abstract boolean isEnabled();

    /**
     * Gets the set of punishment type keys that are mutually exclusive with this
     * type. When this type is enabled, any types whose keys appear in this set
     * are automatically disabled by the enforcer before application.
     * <p>
     * Default implementation returns an empty set (no exclusions).
     *
     * @return An unmodifiable set of excluded {@link NamespacedKey}s.
     */
    @NotNull
    public Set<NamespacedKey> getExcludes() {
        return Set.of();
    }

    /**
     * Applies this punishment to the player. Called by the enforcer after
     * mutual exclusion resolution — only types that survived exclusion are applied.
     *
     * @param player  The player being punished.
     * @param session The player's active combat session.
     * @param mcRPG   The plugin instance for registry access.
     */
    public abstract void apply(@NotNull Player player, @NotNull CombatSession session,
                               @NotNull McRPG mcRPG);

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.ofNullable(expansionKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CombatLogPunishmentType other)) return false;
        return key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return key.toString();
    }
}
