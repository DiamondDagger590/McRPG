package us.eunoians.mcrpg.combat.state;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for {@link CombatStateType} definitions. Accessed via
 * {@link us.eunoians.mcrpg.registry.McRPGRegistryKey#COMBAT_STATE_TYPE}.
 */
public class CombatStateTypeRegistry implements Registry<CombatStateType<?>> {

    private final Map<NamespacedKey, CombatStateType<?>> stateTypes = new HashMap<>();

    /**
     * Registers a state type. Throws if a type with the same key is already registered.
     *
     * @param stateType The state type to register.
     * @throws IllegalStateException if a type with the same key is already registered.
     */
    public void register(@NotNull CombatStateType<?> stateType) {
        if (stateTypes.containsKey(stateType.getKey())) {
            throw new IllegalStateException("CombatStateType already registered: " + stateType.getKey());
        }
        stateTypes.put(stateType.getKey(), stateType);
    }

    /**
     * Unregisters a state type by key.
     *
     * @param key The key of the state type to unregister.
     * @return An {@link Optional} containing the removed type, or empty if not found.
     */
    @NotNull
    public Optional<CombatStateType<?>> unregister(@NotNull NamespacedKey key) {
        return Optional.ofNullable(stateTypes.remove(key));
    }

    /**
     * Gets a state type by key.
     *
     * @param key The key to look up.
     * @return An {@link Optional} containing the type, or empty if not found.
     */
    @NotNull
    public Optional<CombatStateType<?>> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(stateTypes.get(key));
    }

    /**
     * Gets all registered state types.
     *
     * @return An unmodifiable {@link Collection} of all registered types.
     */
    @NotNull
    public Collection<CombatStateType<?>> getAll() {
        return Collections.unmodifiableCollection(stateTypes.values());
    }

    /**
     * Gets all registered state types with {@link CombatStateLifecycle#PERSISTENT} scope.
     * Used by the manager during session start/end for load/save lifecycle hooks.
     *
     * @return A {@link List} of persistent state types.
     */
    @NotNull
    public List<CombatStateType<?>> getPersistentTypes() {
        List<CombatStateType<?>> persistent = new ArrayList<>();
        for (CombatStateType<?> type : stateTypes.values()) {
            if (type.isPersistent()) {
                persistent.add(type);
            }
        }
        return persistent;
    }

    /**
     * Gets an immutable snapshot of all registered state type keys. A snapshot (rather than a live
     * view) so callers can safely iterate and call {@link #unregister(NamespacedKey)} in the same
     * loop without a {@link java.util.ConcurrentModificationException} — matching
     * {@link us.eunoians.mcrpg.combat.condition.CombatConditionRegistry#getRegisteredKeys()}.
     *
     * @return An immutable {@link Set} of registered keys.
     */
    @NotNull
    public Set<NamespacedKey> getRegisteredKeys() {
        return Set.copyOf(stateTypes.keySet());
    }

    /**
     * Checks whether a state type is registered with the given key.
     *
     * @param key The key to check.
     * @return {@code true} if a type is registered with that key.
     */
    public boolean isRegistered(@NotNull NamespacedKey key) {
        return stateTypes.containsKey(key);
    }

    /**
     * Checks whether the given state type is registered, comparing by key.
     *
     * @param stateType The state type to check.
     * @return {@code true} if a type with the same key is registered, {@code false} otherwise.
     */
    @Override
    public boolean registered(@NotNull CombatStateType<?> stateType) {
        return stateTypes.containsKey(stateType.getKey());
    }
}
