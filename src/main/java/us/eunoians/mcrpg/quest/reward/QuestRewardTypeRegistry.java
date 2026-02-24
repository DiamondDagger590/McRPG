package us.eunoians.mcrpg.quest.reward;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for {@link QuestRewardType} implementations.
 * <p>
 * McRPG registers built-in reward types (experience, items, commands) during bootstrap.
 * External plugins can register their own types to extend the reward system.
 * <p>
 * This registry is append-only — types cannot be unregistered once added.
 */
public class QuestRewardTypeRegistry implements Registry<QuestRewardType> {

    private final Map<NamespacedKey, QuestRewardType> types = new HashMap<>();

    /**
     * Registers a reward type. Throws if a type with the same key is already registered.
     *
     * @param type the reward type to register
     * @throws IllegalStateException if a type with the same key is already registered
     */
    public void register(@NotNull QuestRewardType type) {
        NamespacedKey key = type.getKey();
        if (types.containsKey(key)) {
            throw new IllegalStateException("QuestRewardType already registered with key: " + key);
        }
        types.put(key, type);
    }

    /**
     * Gets a registered reward type by its key.
     *
     * @param key the namespaced key
     * @return the reward type, or empty if not registered
     */
    @NotNull
    public Optional<QuestRewardType> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(types.get(key));
    }

    /**
     * Gets a registered reward type by its key, throwing if not found.
     *
     * @param key the namespaced key
     * @return the reward type
     * @throws IllegalArgumentException if no type is registered with the given key
     */
    @NotNull
    public QuestRewardType getOrThrow(@NotNull NamespacedKey key) {
        QuestRewardType type = types.get(key);
        if (type == null) {
            throw new IllegalArgumentException("No QuestRewardType registered with key: " + key);
        }
        return type;
    }

    /**
     * Checks whether a reward type is registered with the given key.
     *
     * @param key the namespaced key to check
     * @return {@code true} if a type is registered with that key
     */
    public boolean isRegistered(@NotNull NamespacedKey key) {
        return types.containsKey(key);
    }

    /**
     * Gets an immutable snapshot of all registered reward type keys.
     *
     * @return an immutable set of registered keys
     */
    @NotNull
    public Set<NamespacedKey> getRegisteredKeys() {
        return Set.copyOf(types.keySet());
    }

    @Override
    public boolean registered(@NotNull QuestRewardType type) {
        return types.containsKey(type.getKey());
    }
}
