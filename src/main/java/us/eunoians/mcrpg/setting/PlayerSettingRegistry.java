package us.eunoians.mcrpg.setting;

import com.google.common.collect.ImmutableSet;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PlayerSettingRegistry {

    private final Map<NamespacedKey, PlayerSetting> settings;

    public PlayerSettingRegistry() {
        settings = new HashMap<>();
    }

    public void registerSetting(@NotNull PlayerSetting playerSetting) {
        settings.put(playerSetting.getSettingKey(), playerSetting.getFirstSetting().getNodeValue());
    }

    public boolean isSettingRegistered(@NotNull NamespacedKey key) {
        return settings.containsKey(key);
    }

    @NotNull
    public Optional<PlayerSetting> getSetting(@NotNull NamespacedKey key) {
        return Optional.ofNullable(settings.get(key));
    }

    @NotNull
    public Set<NamespacedKey> getSettingKeys() {
        return settings.keySet();
    }

    @NotNull
    public Set<PlayerSetting> getSettings() {
        return ImmutableSet.copyOf(settings.values());
    }
}
