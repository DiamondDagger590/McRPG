package us.eunoians.mcrpg.quest.board.distribution;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for {@link RewardDistributionType} implementations.
 * <p>
 * Not cleared on reload — types are registered at startup via content packs
 * and remain for the lifetime of the server.
 */
public class RewardDistributionTypeRegistry implements Registry<RewardDistributionType> {

    private final Map<NamespacedKey, RewardDistributionType> types = new HashMap<>();

    /**
     * Registers a distribution type. Throws if a type with the same key is already registered.
     *
     * @param type the distribution type to register
     * @throws IllegalStateException if a type with the same key is already registered
     */
    public void register(@NotNull RewardDistributionType type) {
        NamespacedKey key = type.getKey();
        if (types.containsKey(key)) {
            throw new IllegalStateException("RewardDistributionType already registered with key: " + key);
        }
        types.put(key, type);
    }

    /**
     * Gets a registered distribution type by its key.
     *
     * @param key the namespaced key
     * @return the distribution type, or empty if not registered
     */
    @NotNull
    public Optional<RewardDistributionType> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(types.get(key));
    }

    /**
     * Gets all registered distribution types.
     *
     * @return an unmodifiable collection of all registered types
     */
    @NotNull
    public Collection<RewardDistributionType> getAll() {
        return Set.copyOf(types.values());
    }

    /**
     * Gets an immutable snapshot of all registered distribution type keys.
     *
     * @return an immutable set of registered keys
     */
    @NotNull
    public Set<NamespacedKey> getRegisteredKeys() {
        return Set.copyOf(types.keySet());
    }

    @Override
    public boolean registered(@NotNull RewardDistributionType type) {
        return types.containsKey(type.getKey());
    }
}
