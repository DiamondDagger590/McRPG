package us.eunoians.mcrpg.quest.board.scope;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registry for {@link ScopedBoardAdapter} instances.
 * <p>
 * Adapters are registered at startup by plugin hooks and are not reloaded.
 * Keyed by the adapter's {@link ScopedBoardAdapter#getScopeProviderKey()}.
 */
public class ScopedBoardAdapterRegistry implements Registry<ScopedBoardAdapter> {

    private final Map<NamespacedKey, ScopedBoardAdapter> adapters = new LinkedHashMap<>();

    /**
     * Registers an adapter. If an adapter with the same scope provider key already exists,
     * it is replaced.
     *
     * @param adapter the adapter to register
     */
    public void register(@NotNull ScopedBoardAdapter adapter) {
        adapters.put(adapter.getScopeProviderKey(), adapter);
    }

    /**
     * Gets a registered adapter by its scope provider key.
     *
     * @param scopeProviderKey the scope provider key
     * @return the adapter, or empty if not registered
     */
    @NotNull
    public Optional<ScopedBoardAdapter> get(@NotNull NamespacedKey scopeProviderKey) {
        return Optional.ofNullable(adapters.get(scopeProviderKey));
    }

    /**
     * Gets all registered adapters.
     *
     * @return an unmodifiable collection of all adapters
     */
    @NotNull
    public Collection<ScopedBoardAdapter> getAll() {
        return Set.copyOf(adapters.values());
    }

    @Override
    public boolean registered(@NotNull ScopedBoardAdapter adapter) {
        return adapters.containsKey(adapter.getScopeProviderKey());
    }
}
