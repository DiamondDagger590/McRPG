package us.eunoians.mcrpg.quest.chain;

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

/**
 * Registry holding all loaded {@link QuestChainDefinition} instances.
 * Uses the same clear-and-replace pattern as
 * {@link us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry} for reloads.
 * <p>
 * Maintains a secondary {@code triggerIndex} mapping auto-start trigger keys to the chain
 * definitions that use them. Both indexes are rebuilt from scratch on reload when
 * {@link #clear()} is called followed by re-registration.
 */
public class QuestChainRegistry implements Registry<QuestChainDefinition> {

    private final Map<NamespacedKey, QuestChainDefinition> chains = new HashMap<>();
    private final Map<NamespacedKey, List<QuestChainDefinition>> triggerIndex = new HashMap<>();

    /**
     * Registers a chain definition. Also adds the chain to the {@code triggerIndex} under its
     * auto-start trigger key.
     *
     * @param definition the chain definition to register
     * @throws IllegalStateException if a definition with the same key is already registered
     */
    public void register(@NotNull QuestChainDefinition definition) {
        NamespacedKey chainKey = definition.getChainKey();
        if (chains.containsKey(chainKey)) {
            throw new IllegalStateException("Chain definition already registered with key: " + chainKey);
        }
        chains.put(chainKey, definition);
        triggerIndex.computeIfAbsent(definition.getAutoStartTriggerKey(), k -> new ArrayList<>()).add(definition);
    }

    /**
     * Returns the chain definition for the given key.
     *
     * @param chainKey the chain key
     * @return the definition, or empty if not registered
     */
    @NotNull
    public Optional<QuestChainDefinition> get(@NotNull NamespacedKey chainKey) {
        return Optional.ofNullable(chains.get(chainKey));
    }

    /**
     * Returns all chain definitions whose auto-start trigger matches the given key. O(1) lookup
     * via the pre-built {@code triggerIndex}.
     *
     * @param triggerKey the trigger key to filter by
     * @return chains using this trigger, or an empty list if none
     */
    @NotNull
    public List<QuestChainDefinition> getChainsForTrigger(@NotNull NamespacedKey triggerKey) {
        return Collections.unmodifiableList(triggerIndex.getOrDefault(triggerKey, List.of()));
    }

    /**
     * Returns all registered chain definitions.
     *
     * @return all chain definitions
     */
    @NotNull
    public Collection<QuestChainDefinition> allChains() {
        return Collections.unmodifiableCollection(chains.values());
    }

    /**
     * Returns whether the given chain definition is already registered.
     *
     * @param definition the chain definition to check
     * @return {@code true} if a definition with the same key is registered
     */
    @Override
    public boolean registered(@NotNull QuestChainDefinition definition) {
        return chains.containsKey(definition.getChainKey());
    }

    /**
     * Clears all registered definitions and the trigger index. Used during reload so
     * both the main {@code chains} map and the {@code triggerIndex} are reset before
     * re-registration.
     */
    public void clear() {
        chains.clear();
        triggerIndex.clear();
    }
}
