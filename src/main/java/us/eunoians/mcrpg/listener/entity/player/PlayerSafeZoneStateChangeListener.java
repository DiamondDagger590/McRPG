package us.eunoians.mcrpg.listener.entity.player;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.audience.Audience;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.entity.player.PlayerSafeZoneStateChangeEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.experience.rested.RestedExperienceOnlineAccumulationSetting;

/**
 * This listener handles listening to when a player enters or leaves a "safe zone"
 * for McRPG.
 */
public class PlayerSafeZoneStateChangeListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSafeZoneStateChange(PlayerSafeZoneStateChangeEvent playerSafeZoneStateChangeEvent) {
        boolean safeZoneAllowed = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE)
                .getFile(FileType.MAIN_CONFIG).getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION);
        // Only send the update if safe zone accumulation is allowed
        if (safeZoneAllowed) {
            PlayerSafeZoneStateChangeEvent.SafeZoneStateChangeType changeType = playerSafeZoneStateChangeEvent.getSafeZoneStateChangeType();
            McRPGPlayer mcRPGPlayer = playerSafeZoneStateChangeEvent.getMcRPGPlayer();
            McRPGLocalizationManager localizationManager = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
            Audience audience = mcRPGPlayer.getAsBukkitPlayer().get();
            RestedExperienceOnlineAccumulationSetting restedExperienceOnlineAccumulationSetting = RestedExperienceOnlineAccumulationSetting.getCurrentSetting()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid rested experience setting was provided. Please reach out to a developer to resolve."));
            Route localizationRoute = restedExperienceOnlineAccumulationSetting != RestedExperienceOnlineAccumulationSetting.DISABLED
                    ? getOnlineMessage(changeType) : getOfflineMessage(changeType);
            audience.sendActionBar(localizationManager.getLocalizedMessageAsComponent(audience, localizationRoute));
        }
    }

    /**
     * Get the {@link Route} of the message to send to the player if safe zone accumulation is
     * allowed while they are online.
     *
     * @param safeZoneStateChangeType The action being performed that caused the state change.
     * @return The {@link Route} of the message to send to the player.
     */
    @NotNull
    private Route getOnlineMessage(@NotNull PlayerSafeZoneStateChangeEvent.SafeZoneStateChangeType safeZoneStateChangeType) {
        return safeZoneStateChangeType == PlayerSafeZoneStateChangeEvent.SafeZoneStateChangeType.ENTERED
                ? LocalizationKey.ENTERING_SAFE_ZONE_ONLINE_ACCUMULATION_MESSAGE : LocalizationKey.LEAVING_SAFE_ZONE_ONLINE_ACCUMULATION_MESSAGE;
    }

    /**
     * Get the {@link Route} of the message to send to the player if safe zone accumulation is
     * allowed while they are offline.
     *
     * @param safeZoneStateChangeType The action being performed that caused the state change.
     * @return The {@link Route} of the message to send to the player.
     */
    @NotNull
    private Route getOfflineMessage(@NotNull PlayerSafeZoneStateChangeEvent.SafeZoneStateChangeType safeZoneStateChangeType) {
        return safeZoneStateChangeType == PlayerSafeZoneStateChangeEvent.SafeZoneStateChangeType.ENTERED
                ? LocalizationKey.ENTERING_SAFE_ZONE_OFFLINE_ACCUMULATION_MESSAGE : LocalizationKey.LEAVING_SAFE_ZONE_OFFLINE_ACCUMULATION_MESSAGE;
    }
}
