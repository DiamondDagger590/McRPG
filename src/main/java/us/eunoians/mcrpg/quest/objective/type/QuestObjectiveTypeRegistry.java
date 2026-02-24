package us.eunoians.mcrpg.quest.objective.type;

import org.jetbrains.annotations.Nullable;
import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for {@link QuestObjectiveType} implementations.
 * <p>
 * McRPG registers built-in objective types (block break, mob kill, etc.) during bootstrap.
 * External plugins can register their own types to extend the quest system.
 */
public class QuestObjectiveTypeRegistry implements Registry<QuestObjectiveType> {

    private final Map<NamespacedKey, QuestObjectiveType> types = new HashMap<>();

    /**
     * Registers an objective type. Logs a warning and does not overwrite if a type
     * with the same key is already registered.
     *
     * @param type the objective type to register
     * @throws IllegalStateException if a type with the same key is already registered
     */
    public void register(@NotNull QuestObjectiveType type) {
        NamespacedKey key = type.getKey();
        if (types.containsKey(key)) {
            throw new IllegalStateException("QuestObjectiveType already registered with key: " + key);
        }
        types.put(key, type);
    }

    /**
     * Gets a registered objective type by its key.
     *
     * @param key the namespaced key
     * @return the objective type, or empty if not registered
     */
    @NotNull
    public Optional<QuestObjectiveType> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(types.get(key));
    }

    /**
     * Gets a registered objective type by its key, throwing if not found.
     *
     * @param key the namespaced key
     * @return the objective type
     * @throws IllegalArgumentException if no type is registered with the given key
     */
    @NotNull
    public QuestObjectiveType getOrThrow(@NotNull NamespacedKey key) {
        QuestObjectiveType type = types.get(key);
        if (type == null) {
            throw new IllegalArgumentException("No QuestObjectiveType registered with key: " + key);
        }
        return type;
    }

    /**
     * Checks whether an objective type is registered with the given key.
     *
     * @param key the namespaced key to check
     * @return {@code true} if a type is registered with that key
     */
    public boolean isRegistered(@NotNull NamespacedKey key) {
        return types.containsKey(key);
    }

    /**
     * Gets an immutable snapshot of all registered objective type keys.
     *
     * @return an immutable set of registered keys
     */
    @NotNull
    public Set<NamespacedKey> getRegisteredKeys() {
        return Set.copyOf(types.keySet());
    }

    @Override
    public boolean registered(@NotNull QuestObjectiveType type) {
        return types.containsKey(type.getKey());
    }
}
