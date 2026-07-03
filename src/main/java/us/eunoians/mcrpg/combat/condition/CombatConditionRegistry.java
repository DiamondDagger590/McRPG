package us.eunoians.mcrpg.combat.condition;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for {@link CombatCondition} implementations.
 * <p>
 * Condition types are registered at startup via content packs and remain for the
 * lifetime of the server. Third-party plugins may register additional conditions
 * through {@link us.eunoians.mcrpg.expansion.ContentExpansion} content packs.
 */
public class CombatConditionRegistry implements Registry<CombatCondition> {

    private final Map<NamespacedKey, CombatCondition> conditions = new LinkedHashMap<>();

    /**
     * Registers a combat condition. Throws if a condition with the same key is already registered.
     *
     * @param condition the combat condition to register
     * @throws IllegalStateException if a condition with the same key is already registered
     */
    public void register(@NotNull CombatCondition condition) {
        NamespacedKey key = condition.getKey();
        if (conditions.containsKey(key)) {
            throw new IllegalStateException("CombatCondition already registered with key: " + key);
        }
        conditions.put(key, condition);
    }

    /**
     * Unregisters a combat condition by its key.
     *
     * @param key the namespaced key of the condition to unregister
     * @return an {@link Optional} containing the removed condition, or empty if no condition
     *         was registered with the given key
     */
    @NotNull
    public Optional<CombatCondition> unregister(@NotNull NamespacedKey key) {
        return Optional.ofNullable(conditions.remove(key));
    }

    /**
     * Gets a registered combat condition by its key.
     *
     * @param key the namespaced key
     * @return an {@link Optional} containing the condition, or empty if not registered
     */
    @NotNull
    public Optional<CombatCondition> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(conditions.get(key));
    }

    /**
     * Gets all registered combat conditions.
     *
     * @return an unmodifiable collection of all registered conditions
     */
    @NotNull
    public Collection<CombatCondition> getAll() {
        return Set.copyOf(conditions.values());
    }

    /**
     * Gets an immutable snapshot of all registered combat condition keys.
     *
     * @return an immutable set of registered keys
     */
    @NotNull
    public Set<NamespacedKey> getRegisteredKeys() {
        return Set.copyOf(conditions.keySet());
    }

    /**
     * Checks whether a combat condition with the given key is registered.
     *
     * @param key the namespaced key to check
     * @return {@code true} if a condition with the given key is registered, {@code false} otherwise
     */
    public boolean isRegistered(@NotNull NamespacedKey key) {
        return conditions.containsKey(key);
    }

    /**
     * Checks whether the given combat condition is registered, comparing by key.
     *
     * @param condition the combat condition to check
     * @return {@code true} if a condition with the same key is registered, {@code false} otherwise
     */
    @Override
    public boolean registered(@NotNull CombatCondition condition) {
        return conditions.containsKey(condition.getKey());
    }
}
