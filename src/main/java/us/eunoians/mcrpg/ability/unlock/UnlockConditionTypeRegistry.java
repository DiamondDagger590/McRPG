package us.eunoians.mcrpg.ability.unlock;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for {@link UnlockConditionType} implementations.
 * <p>
 * McRPG registers its built-in unlock condition types via {@code McRPGExpansion}'s
 * {@link us.eunoians.mcrpg.expansion.content.UnlockConditionTypeContentPack}. Third-party
 * expansions register their own types through the same content-pack pathway.
 */
public class UnlockConditionTypeRegistry implements Registry<UnlockConditionType> {

    private final Map<NamespacedKey, UnlockConditionType> types = new HashMap<>();

    /**
     * Registers an unlock condition type.
     *
     * @param type the type to register
     * @throws IllegalStateException if a type with the same key is already registered
     */
    public void register(@NotNull UnlockConditionType type) {
        NamespacedKey key = type.getKey();
        if (types.containsKey(key)) {
            throw new IllegalStateException("UnlockConditionType already registered with key: " + key);
        }
        types.put(key, type);
    }

    /**
     * Gets a registered unlock condition type by its key.
     *
     * @param key the namespaced key
     * @return the type, or empty if not registered
     */
    @NotNull
    public Optional<UnlockConditionType> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(types.get(key));
    }

    /**
     * Gets a registered unlock condition type by its key, throwing if not found.
     *
     * @param key the namespaced key
     * @return the type
     * @throws IllegalArgumentException if no type is registered with the given key
     */
    @NotNull
    public UnlockConditionType getOrThrow(@NotNull NamespacedKey key) {
        UnlockConditionType type = types.get(key);
        if (type == null) {
            throw new IllegalArgumentException("No UnlockConditionType registered with key: " + key);
        }
        return type;
    }

    /**
     * Checks whether a type is registered with the given key.
     *
     * @param key the namespaced key to check
     * @return {@code true} if a type is registered with that key
     */
    public boolean isRegistered(@NotNull NamespacedKey key) {
        return types.containsKey(key);
    }

    /**
     * Gets an immutable snapshot of all registered type keys.
     *
     * @return an immutable set of registered keys
     */
    @NotNull
    public Set<NamespacedKey> getRegisteredKeys() {
        return Set.copyOf(types.keySet());
    }

    @Override
    public boolean registered(@NotNull UnlockConditionType type) {
        return types.containsKey(type.getKey());
    }
}
