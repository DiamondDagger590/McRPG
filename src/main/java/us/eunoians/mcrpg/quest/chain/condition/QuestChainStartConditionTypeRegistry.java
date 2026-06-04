package us.eunoians.mcrpg.quest.chain.condition;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for {@link QuestChainStartConditionType} implementations. Maps
 * {@link NamespacedKey} to the corresponding condition type factory.
 * <p>
 * Third-party plugins register custom condition types via
 * {@link us.eunoians.mcrpg.expansion.content.QuestChainStartConditionContentPack}.
 *
 * @see QuestChainStartConditionType
 */
public class QuestChainStartConditionTypeRegistry {

    private final Map<NamespacedKey, QuestChainStartConditionType> types = new LinkedHashMap<>();

    /**
     * Registers a condition type. If a type with the same key is already registered,
     * it is silently replaced.
     *
     * @param type the condition type to register
     */
    public void register(@NotNull QuestChainStartConditionType type) {
        types.put(type.getKey(), type);
    }

    /**
     * Returns the condition type registered under the given key.
     *
     * @param key the condition type key
     * @return the condition type, or empty if not registered
     */
    @NotNull
    public Optional<QuestChainStartConditionType> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(types.get(key));
    }

    /**
     * Checks whether a condition type is registered under the given key.
     *
     * @param key the condition type key
     * @return {@code true} if a type with the given key is registered
     */
    public boolean registered(@NotNull NamespacedKey key) {
        return types.containsKey(key);
    }

    /**
     * Returns all registered condition types.
     *
     * @return an unmodifiable collection of all registered condition types
     */
    @NotNull
    public Collection<QuestChainStartConditionType> getAll() {
        return Collections.unmodifiableCollection(types.values());
    }
}
