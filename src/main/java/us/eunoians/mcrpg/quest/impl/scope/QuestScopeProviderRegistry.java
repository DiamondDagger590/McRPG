package us.eunoians.mcrpg.quest.impl.scope;

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
 * Global, append-only registry for {@link QuestScopeProvider} implementations.
 * <p>
 * McRPG registers built-in scope providers (single player, land, permission) during bootstrap.
 * External plugins can register their own scope types to extend the quest system's
 * participation model.
 * <p>
 * Registered via {@link us.eunoians.mcrpg.registry.McRPGRegistryKey#QUEST_SCOPE_PROVIDER}
 * and accessible from anywhere through {@link com.diamonddagger590.mccore.registry.RegistryAccess}.
 */
public class QuestScopeProviderRegistry implements Registry<QuestScopeProvider<?>> {

    private final Map<NamespacedKey, QuestScopeProvider<?>> providers = new HashMap<>();

    /**
     * Registers a scope provider. Throws if a provider with the same key is already registered.
     *
     * @param provider the scope provider to register
     * @throws IllegalStateException if a provider with the same key is already registered
     */
    public void register(@NotNull QuestScopeProvider<?> provider) {
        NamespacedKey key = provider.getKey();
        if (providers.containsKey(key)) {
            throw new IllegalStateException("QuestScopeProvider already registered with key: " + key);
        }
        providers.put(key, provider);
    }

    /**
     * Gets a registered scope provider by its key.
     *
     * @param key the namespaced key
     * @return the scope provider, or empty if not registered
     */
    @NotNull
    public Optional<QuestScopeProvider<?>> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(providers.get(key));
    }

    /**
     * Gets a registered scope provider by its key, throwing if not found.
     *
     * @param key the namespaced key
     * @return the scope provider
     * @throws IllegalArgumentException if no provider is registered with the given key
     */
    @NotNull
    public QuestScopeProvider<?> getOrThrow(@NotNull NamespacedKey key) {
        QuestScopeProvider<?> provider = providers.get(key);
        if (provider == null) {
            throw new IllegalArgumentException("No QuestScopeProvider registered with key: " + key);
        }
        return provider;
    }

    /**
     * Checks whether a scope provider is registered with the given key.
     *
     * @param key the namespaced key to check
     * @return {@code true} if a provider is registered with that key
     */
    public boolean isRegistered(@NotNull NamespacedKey key) {
        return providers.containsKey(key);
    }

    /**
     * Gets an immutable snapshot of all registered scope provider keys.
     *
     * @return an immutable set of registered keys
     */
    @NotNull
    public Set<NamespacedKey> getRegisteredKeys() {
        return Set.copyOf(providers.keySet());
    }

    /**
     * Gets an immutable snapshot of all registered scope providers.
     *
     * @return an unmodifiable collection of all registered providers
     */
    @NotNull
    public Collection<QuestScopeProvider<?>> getRegisteredProviders() {
        return List.copyOf(providers.values());
    }

    @Override
    public boolean registered(@NotNull QuestScopeProvider<?> provider) {
        return providers.containsKey(provider.getKey());
    }
}
