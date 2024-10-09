package us.eunoians.mcrpg.setting;

import com.google.common.collect.ImmutableSet;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A registry for all {@link PlayerSetting}s.
 * <p>
 * While multiple player setting objects may share the same {@link NamespacedKey},
 * each {@link NamespacedKey} is given the responsibility of owning the chain of
 * {@link com.diamonddagger590.mccore.util.LinkedNode}s. This registry provides an easy way
 * to get the first setting for a given node chain.
 */
public class PlayerSettingRegistry {

    private final Map<NamespacedKey, PlayerSetting> settings;

    public PlayerSettingRegistry() {
        settings = new HashMap<>();
    }

    /**
     * Registers the provided {@link PlayerSetting} to be used.
     *
     * @param playerSetting The {@link PlayerSetting} to register.
     */
    public void registerSetting(@NotNull PlayerSetting playerSetting) {
        settings.put(playerSetting.getSettingKey(), playerSetting.getFirstSetting().getNodeValue());
    }

    /**
     * Checks to see if the provided {@link NamespacedKey} has a registered {@link PlayerSetting}.
     *
     * @param key The {@link NamespacedKey} to check.
     * @return {@code true} if the provided {@link NamespacedKey} has a registered {@link PlayerSetting}.
     */
    public boolean isSettingRegistered(@NotNull NamespacedKey key) {
        return settings.containsKey(key);
    }

    /**
     * Gets an {@link Optional} containing the {@link PlayerSetting} that belongs to the
     * provided {@link NamespacedKey}.
     *
     * @param key The {@link NamespacedKey} to get the {@link Optional} for.
     * @return An {@link Optional} containing the {@link PlayerSetting} that belongs to the
     * provided {@link NamespacedKey}, or an empty optional if no matches are found.
     */
    @NotNull
    public Optional<PlayerSetting> getSetting(@NotNull NamespacedKey key) {
        return Optional.ofNullable(settings.get(key));
    }

    /**
     * Gets an {@link ImmutableSet} of all {@link NamespacedKey}s registered.
     *
     * @return An {@link ImmutableSet} of all {@link NamespacedKey}s registered.
     */
    @NotNull
    public Set<NamespacedKey> getSettingKeys() {
        return ImmutableSet.copyOf(settings.keySet());
    }

    /**
     * Gets an {@link ImmutableSet} of all {@link PlayerSetting}s registered.
     *
     * @return An {@link ImmutableSet} of all {@link PlayerSetting}s registered.
     */
    @NotNull
    public Set<PlayerSetting> getSettings() {
        return ImmutableSet.copyOf(settings.values());
    }
}
