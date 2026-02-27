package us.eunoians.mcrpg.quest.board.template.condition;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for {@link TemplateCondition} implementations.
 * <p>
 * Condition types are registered at startup via content packs and remain for the
 * lifetime of the server. Not cleared on reload.
 */
public class TemplateConditionRegistry implements Registry<TemplateCondition> {

    private final Map<NamespacedKey, TemplateCondition> conditions = new LinkedHashMap<>();

    /**
     * Registers a condition type. Throws if a type with the same key is already registered.
     *
     * @param condition the condition type to register
     * @throws IllegalStateException if a condition with the same key is already registered
     */
    public void register(@NotNull TemplateCondition condition) {
        NamespacedKey key = condition.getKey();
        if (conditions.containsKey(key)) {
            throw new IllegalStateException("TemplateCondition already registered with key: " + key);
        }
        conditions.put(key, condition);
    }

    /**
     * Gets a registered condition type by its key.
     *
     * @param key the namespaced key
     * @return the condition type, or empty if not registered
     */
    @NotNull
    public Optional<TemplateCondition> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(conditions.get(key));
    }

    /**
     * Gets all registered condition types.
     *
     * @return an unmodifiable collection of all registered types
     */
    @NotNull
    public Collection<TemplateCondition> getAll() {
        return Set.copyOf(conditions.values());
    }

    /**
     * Gets an immutable snapshot of all registered condition type keys.
     *
     * @return an immutable set of registered keys
     */
    @NotNull
    public Set<NamespacedKey> getRegisteredKeys() {
        return Set.copyOf(conditions.keySet());
    }

    @Override
    public boolean registered(@NotNull TemplateCondition condition) {
        return conditions.containsKey(condition.getKey());
    }
}
