package us.eunoians.mcrpg.quest.source;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for {@link QuestSource} implementations.
 * <p>
 * Sources are registered programmatically via content packs (no config-file source).
 * This registry is append-only and not cleared on reload.
 */
public class QuestSourceRegistry implements Registry<QuestSource> {

    private final Map<NamespacedKey, QuestSource> sources = new LinkedHashMap<>();

    /**
     * Registers a quest source. Throws if a source with the same key is already registered.
     *
     * @param source the quest source to register
     * @throws IllegalStateException if a source with the same key is already registered
     */
    public void register(@NotNull QuestSource source) {
        NamespacedKey key = source.getKey();
        if (sources.containsKey(key)) {
            throw new IllegalStateException("QuestSource already registered with key: " + key);
        }
        sources.put(key, source);
    }

    /**
     * Gets a registered quest source by its key.
     *
     * @param key the namespaced key
     * @return the quest source, or empty if not registered
     */
    @NotNull
    public Optional<QuestSource> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(sources.get(key));
    }

    /**
     * Gets all registered quest sources.
     *
     * @return an unmodifiable collection of all sources
     */
    @NotNull
    public Collection<QuestSource> getAll() {
        return Set.copyOf(sources.values());
    }

    @Override
    public boolean registered(@NotNull QuestSource source) {
        return sources.containsKey(source.getKey());
    }
}
