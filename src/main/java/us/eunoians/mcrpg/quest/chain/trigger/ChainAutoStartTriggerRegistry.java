package us.eunoians.mcrpg.quest.chain.trigger;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for all registered {@link ChainAutoStartTrigger} instances.
 * <p>
 * Follows the same standard typed registry pattern as
 * {@link us.eunoians.mcrpg.quest.source.QuestSourceRegistry}. Third-party plugins register
 * custom triggers via
 * {@link us.eunoians.mcrpg.expansion.content.ChainAutoStartTriggerContentPack}.
 */
public class ChainAutoStartTriggerRegistry implements Registry<ChainAutoStartTrigger> {

    private final Map<NamespacedKey, ChainAutoStartTrigger> triggers = new LinkedHashMap<>();

    /**
     * Registers a trigger.
     *
     * @param trigger the trigger to register
     * @throws IllegalStateException if a trigger with the same key is already registered
     */
    public void register(@NotNull ChainAutoStartTrigger trigger) {
        NamespacedKey key = trigger.getKey();
        if (triggers.containsKey(key)) {
            throw new IllegalStateException("ChainAutoStartTrigger already registered with key: " + key);
        }
        triggers.put(key, trigger);
    }

    /**
     * Returns the trigger for the given key.
     *
     * @param key the trigger key
     * @return the trigger, or empty if not registered
     */
    @NotNull
    public Optional<ChainAutoStartTrigger> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(triggers.get(key));
    }

    /**
     * Returns all registered triggers.
     *
     * @return an unmodifiable collection of all registered triggers
     */
    @NotNull
    public Collection<ChainAutoStartTrigger> allTriggers() {
        return Collections.unmodifiableCollection(triggers.values());
    }

    /**
     * Returns whether the given trigger is registered.
     *
     * @param trigger the trigger to check
     * @return {@code true} if a trigger with the same key is registered
     */
    @Override
    public boolean registered(@NotNull ChainAutoStartTrigger trigger) {
        return triggers.containsKey(trigger.getKey());
    }
}
