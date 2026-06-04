package us.eunoians.mcrpg.quest.chain.availability;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for {@link WindowBoundaryType} implementations. Accessed via
 * {@link us.eunoians.mcrpg.registry.McRPGRegistryKey#WINDOW_BOUNDARY_TYPE}.
 */
public class WindowBoundaryTypeRegistry implements Registry<WindowBoundaryType> {

    private final Map<NamespacedKey, WindowBoundaryType> types = new LinkedHashMap<>();

    /**
     * Registers a boundary type.
     *
     * @param type the boundary type to register
     */
    @Override
    public void register(@NotNull WindowBoundaryType type) {
        types.put(type.getKey(), type);
    }

    /**
     * {@inheritDoc}
     *
     * @param type the boundary type to check
     * @return {@code true} if a type with the same key is registered
     */
    @Override
    public boolean registered(@NotNull WindowBoundaryType type) {
        return types.containsKey(type.getKey());
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
}
