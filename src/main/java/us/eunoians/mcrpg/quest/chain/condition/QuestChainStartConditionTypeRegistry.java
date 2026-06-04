package us.eunoians.mcrpg.quest.chain.condition;

import com.diamonddagger590.mccore.registry.Registry;
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
public class QuestChainStartConditionTypeRegistry implements Registry<QuestChainStartConditionType> {

    private final Map<NamespacedKey, QuestChainStartConditionType> types = new LinkedHashMap<>();

    /**
     * Registers a condition type. If a type with the same key is already registered,
     * it is silently replaced.
     *
     * @param type the condition type to register
     */
    @Override
    public void register(@NotNull QuestChainStartConditionType type) {
        types.put(type.getKey(), type);
    }

    /**
     * {@inheritDoc}
     *
     * @param type the condition type to check
     * @return {@code true} if a type with the same key is registered
     */
    @Override
    public boolean registered(@NotNull QuestChainStartConditionType type) {
        return types.containsKey(type.getKey());
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
     * Returns all registered condition types.
     *
     * @return an unmodifiable collection of all registered condition types
     */
    @NotNull
    public Collection<QuestChainStartConditionType> getAll() {
        return Collections.unmodifiableCollection(types.values());
    }
}
