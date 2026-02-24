package us.eunoians.mcrpg.quest.board.refresh;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for {@link RefreshType} implementations.
 * <p>
 * Phase 1 registers built-in time-based types (daily, weekly). Future phases or
 * third-party plugins may register event-driven types.
 */
public class RefreshTypeRegistry implements Registry<RefreshType> {

    private final Map<NamespacedKey, RefreshType> types = new LinkedHashMap<>();

    /**
     * Registers a refresh type. Throws if a type with the same key is already registered.
     *
     * @param type the refresh type to register
     * @throws IllegalStateException if a type with the same key is already registered
     */
    public void register(@NotNull RefreshType type) {
        NamespacedKey key = type.getKey();
        if (types.containsKey(key)) {
            throw new IllegalStateException("RefreshType already registered with key: " + key);
        }
        types.put(key, type);
    }

    /**
     * Gets a registered refresh type by its key.
     *
     * @param key the namespaced key
     * @return the refresh type, or empty if not registered
     */
    @NotNull
    public Optional<RefreshType> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(types.get(key));
    }

    /**
     * Gets all registered refresh types.
     *
     * @return an unmodifiable collection of all types
     */
    @NotNull
    public Collection<RefreshType> getAll() {
        return Set.copyOf(types.values());
    }

    /**
     * Returns only time-based refresh types (polled by the rotation task).
     *
     * @return an immutable list of time-based types
     */
    @NotNull
    public List<RefreshType> getTimeBasedTypes() {
        return types.values().stream()
                .filter(RefreshType::isTimeBased)
                .toList();
    }

    @Override
    public boolean registered(@NotNull RefreshType type) {
        return types.containsKey(type.getKey());
    }
}
