package us.eunoians.mcrpg.quest.definition;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for {@link QuestDefinition} instances.
 * <p>
 * Quest definitions are loaded from YAML configuration via the {@link us.eunoians.mcrpg.quest.QuestManager}
 * and can also be registered programmatically by external plugins through the developer API.
 * <p>
 * This registry is the single source of truth for all quest definitions in the system.
 * It is registered globally via {@link us.eunoians.mcrpg.registry.McRPGRegistryKey#QUEST_DEFINITION}
 * and can be accessed from anywhere through {@link com.diamonddagger590.mccore.registry.RegistryAccess}.
 */
public class QuestDefinitionRegistry implements Registry<QuestDefinition> {

    private final Map<NamespacedKey, QuestDefinition> definitions = new HashMap<>();

    /**
     * Registers a quest definition. Throws if a definition with the same key is already registered.
     *
     * @param definition the quest definition to register
     * @throws IllegalStateException if a definition with the same key is already registered
     */
    public void register(@NotNull QuestDefinition definition) {
        NamespacedKey key = definition.getQuestKey();
        if (definitions.containsKey(key)) {
            throw new IllegalStateException("QuestDefinition already registered with key: " + key);
        }
        definitions.put(key, definition);
    }

    /**
     * Gets a registered quest definition by its key.
     *
     * @param key the namespaced key of the quest
     * @return the definition, or empty if not registered
     */
    @NotNull
    public Optional<QuestDefinition> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(definitions.get(key));
    }

    /**
     * Gets a registered quest definition by its key, throwing if not found.
     *
     * @param key the namespaced key of the quest
     * @return the quest definition
     * @throws IllegalArgumentException if no definition is registered with the given key
     */
    @NotNull
    public QuestDefinition getOrThrow(@NotNull NamespacedKey key) {
        QuestDefinition definition = definitions.get(key);
        if (definition == null) {
            throw new IllegalArgumentException("No QuestDefinition registered with key: " + key);
        }
        return definition;
    }

    /**
     * Checks whether a quest definition is registered with the given key.
     *
     * @param key the namespaced key to check
     * @return {@code true} if a definition is registered with that key
     */
    public boolean isRegistered(@NotNull NamespacedKey key) {
        return definitions.containsKey(key);
    }

    /**
     * Gets an immutable snapshot of all registered quest definition keys.
     *
     * @return an immutable set of registered keys
     */
    @NotNull
    public Set<NamespacedKey> getRegisteredKeys() {
        return Set.copyOf(definitions.keySet());
    }

    /**
     * Gets an immutable snapshot of all registered quest definitions.
     *
     * @return an immutable map of quest key to definition
     */
    @NotNull
    public Map<NamespacedKey, QuestDefinition> getRegisteredDefinitions() {
        return Map.copyOf(definitions);
    }

    /**
     * Gets all registered quest definitions.
     *
     * @return an unmodifiable collection of all definitions
     */
    @NotNull
    public Collection<QuestDefinition> getAll() {
        return List.copyOf(definitions.values());
    }

    @Override
    public boolean registered(@NotNull QuestDefinition questDefinition) {
        return definitions.containsKey(questDefinition.getQuestKey());
    }

    /**
     * Clears all registered definitions and bulk-registers the provided definitions.
     * This is intended for use by the config loader during reloads; external plugins
     * should use {@link #register(QuestDefinition)} instead.
     *
     * @param configDefinitions the definitions loaded from configuration files
     */
    public void replaceConfigDefinitions(@NotNull Map<NamespacedKey, QuestDefinition> configDefinitions) {
        definitions.clear();
        definitions.putAll(configDefinitions);
    }
}
