package us.eunoians.mcrpg.combat.log;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import com.diamonddagger590.mccore.registry.RegistryKey;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Abstract base for a punishment applied when a player combat logs. Each type
 * carries a {@link NamespacedKey} for serialization/registry lookup, declares
 * mutually exclusive types via {@link #getExcludes()}, and implements its
 * punishment behavior in {@link #apply(Player, CombatSession, McRPG)}.
 * <p>
 * Built-in types are defined as static constants with inlined behavior.
 * Third-party plugins subclass this and register via
 * {@link us.eunoians.mcrpg.expansion.content.CombatLogPunishmentContentPack}.
 * <p>
 * Implements {@link McRPGContent} for {@code ContentPack} registration — required by the
 * {@code McRPGContentPack<T extends McRPGContent>} bound that {@code CombatLogPunishmentContentPack}
 * is built on. A {@code null} expansion key is valid for punishment types not tied to a specific
 * expansion.
 */
public abstract class CombatLogPunishmentType implements McRPGContent {

    /**
     * Kill the player on logout. Sets health to zero, triggering normal death
     * mechanics (item drops, XP loss, death message). Excludes {@link #DROP_ITEMS}
     * because death already handles item drops.
     */
    public static final CombatLogPunishmentType KILL_ON_LOGOUT = new CombatLogPunishmentType(
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "kill_on_logout"), "kill-on-logout", null) {

        @Override
        @NotNull
        public Set<NamespacedKey> getExcludes() {
            return Set.of(DROP_ITEMS.getKey());
        }

        @Override
        public void apply(@NotNull Player player, @NotNull CombatSession session,
                          @NotNull McRPG mcRPG) {
            player.setHealth(0);
        }
    };

    /**
     * Drop the player's inventory at their logout location. Mutually excluded
     * by {@link #KILL_ON_LOGOUT} — death already drops items.
     */
    public static final CombatLogPunishmentType DROP_ITEMS = new CombatLogPunishmentType(
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "drop_items"), "drop-items", null) {

        @Override
        public void apply(@NotNull Player player, @NotNull CombatSession session,
                          @NotNull McRPG mcRPG) {
            Location location = player.getLocation();
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && !item.getType().isAir()) {
                    location.getWorld().dropItemNaturally(location, item);
                }
            }
            player.getInventory().clear();
        }
    };

    /**
     * Announce the combat log to every online player and the console. Delegates to the
     * {@code McRPGLocalizationManager}'s {@code broadcastMessage(Route, Map)} so each recipient's
     * message is resolved against their own locale chain.
     */
    public static final CombatLogPunishmentType BROADCAST_MESSAGE = new CombatLogPunishmentType(
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "broadcast_message"), "broadcast-message", null) {

        @Override
        public void apply(@NotNull Player player, @NotNull CombatSession session,
                          @NotNull McRPG mcRPG) {
            var localizationManager = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION);
            Location loc = player.getLocation();
            localizationManager.broadcastMessage(LocalizationKey.COMBAT_LOG_BROADCAST, Map.of(
                    "player", player.getName(),
                    "world", loc.getWorld().getName(),
                    "x", String.valueOf((int) loc.getX()),
                    "y", String.valueOf((int) loc.getY()),
                    "z", String.valueOf((int) loc.getZ())
            ));
        }
    };

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
