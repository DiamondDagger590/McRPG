package us.eunoians.mcrpg.quest.chain.availability;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for {@link WindowBoundaryType} implementations. Accessed via
 * {@link us.eunoians.mcrpg.registry.McRPGRegistryKey#WINDOW_BOUNDARY_TYPE}.
 */
public class WindowBoundaryTypeRegistry {

    private final Map<NamespacedKey, WindowBoundaryType> types = new LinkedHashMap<>();

    /**
     * Registers a boundary type.
     *
     * @param type the boundary type to register
     */
    public void register(@NotNull WindowBoundaryType type) {
        types.put(type.getKey(), type);
    }

    /**
     * Gets a boundary type by key.
     *
     * @param key the boundary type key
     * @return the boundary type, or empty if not registered
     */
    @NotNull
    public Optional<WindowBoundaryType> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(types.get(key));
    }

    /**
     * Checks whether a boundary type is registered.
     *
     * @param key the boundary type key
     * @return {@code true} if registered
     */
    public boolean registered(@NotNull NamespacedKey key) {
        return types.containsKey(key);
    }
}
