package us.eunoians.mcrpg.skill.experience.rested;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.configuration.common.ReloadableParser;
import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import net.kyori.adventure.audience.Audience;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.PlayerExperienceExtras;
import us.eunoians.mcrpg.event.entity.player.PlayerAwardedRestedExperienceEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;

/**
 * This manager handles calculating and awarding rested experience for players.
 */
public class RestedExperienceManager extends Manager<McRPG> {

    private final Map<RestedExperienceAccumulationType, ReloadableParser> accumulationRates;

    public RestedExperienceManager(@NotNull McRPG plugin) {
        super(plugin);
        this.accumulationRates = new HashMap<>();
        FileManager fileManager = plugin.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        accumulationRates.put(RestedExperienceAccumulationType.ONLINE, new ReloadableParser(fileManager.getFile(FileType.MAIN_CONFIG), MainConfigFile.ONLINE_RESTED_EXPERIENCE_ACCUMULATION_RATE));
        accumulationRates.put(RestedExperienceAccumulationType.OFFLINE, new ReloadableParser(fileManager.getFile(FileType.MAIN_CONFIG), MainConfigFile.OFFLINE_RESTED_EXPERIENCE_ACCUMULATION_RATE));
        accumulationRates.put(RestedExperienceAccumulationType.ONLINE_SAFE_ZONE, new ReloadableParser(fileManager.getFile(FileType.MAIN_CONFIG), MainConfigFile.ONLINE_SAFE_ZONE_RESTED_EXPERIENCE_ACCUMULATION_RATE));
        accumulationRates.put(RestedExperienceAccumulationType.OFFLINE_SAFE_ZONE, new ReloadableParser(fileManager.getFile(FileType.MAIN_CONFIG), MainConfigFile.OFFLINE_SAFE_ZONE_RESTED_EXPERIENCE_ACCUMULATION_RATE));
        ReloadableContentManager reloadableContentManager = plugin.registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.RELOADABLE_CONTENT);
        accumulationRates.values().forEach(reloadableContentManager::trackReloadableContent);
    }

    /**
     * Gets the amount of rested experience that should be awarded for the period of time provided. The amount
     * may differ based on if the safe zone or normal equations are used.
     *
     * @param timeInSeconds    The amount of time in seconds to give rested experience for.
     * @param accumulationType The type of accumulation rate to get the rested experience from.
     * @return The amount of rested experience that should be awarded for the period of time provided.
     */
    public float getRestedExperience(int timeInSeconds, @NotNull RestedExperienceAccumulationType accumulationType) {
        Parser parser = accumulationRates.get(accumulationType).getContent();
        parser.setVariable("time", timeInSeconds);
        return (float) parser.getValue();
    }

    /**
     * Calculates and awards rested experience to the provided {@link McRPGPlayer}. If the player currently has more rested experience than
     * the current limit, then this method turns into a no-op.
     * <p>
     * This method also will override the safe zone setting if safe zone accumulation is disabled in the config.
     *
     * @param mcRPGPlayer               The {@link McRPGPlayer} to award experience to.
     * @param timeInSeconds             The amount of time in seconds to give rested experience for.
     * @param accumulationType          The type of rested accumulation to use for calculating amount to award.
     * @param notifyPlayerOfOfflineGain If the player should be notified of their experience gain.
     */
    public void awardRestedExperience(@NotNull McRPGPlayer mcRPGPlayer, int timeInSeconds, @NotNull RestedExperienceAccumulationType accumulationType, boolean notifyPlayerOfOfflineGain) {
        if (accumulationType == RestedExperienceAccumulationType.ONLINE_SAFE_ZONE && !plugin().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION)) {
            accumulationType = RestedExperienceAccumulationType.ONLINE;
        } else if (accumulationType == RestedExperienceAccumulationType.OFFLINE_SAFE_ZONE && !plugin().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION)) {
            accumulationType = RestedExperienceAccumulationType.OFFLINE;
        }
        awardRestedExperience(mcRPGPlayer, getRestedExperience(timeInSeconds, accumulationType), notifyPlayerOfOfflineGain);
    }

    /**
     * Awards rested experience to the provided {@link McRPGPlayer}. If the player currently has more rested experience than
     * the current limit, then this method turns into a no-op.
     *
     * @param mcRPGPlayer               The {@link McRPGPlayer} to award experience to.
     * @param restedExperience          The amount of rested experience to give.
     * @param notifyPlayerOfOfflineGain If the player should be notified of their experience gain.
     */
    public void awardRestedExperience(@NotNull McRPGPlayer mcRPGPlayer, float restedExperience, boolean notifyPlayerOfOfflineGain) {
        if (restedExperience == 0) {
            return;
        }
        PlayerExperienceExtras playerExperienceExtras = mcRPGPlayer.getExperienceExtras();
        McRPGLocalizationManager localizationManager = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Audience audience = mcRPGPlayer.getAsBukkitPlayer().get();
        double currentRestedExperience = playerExperienceExtras.getRestedExperience();
        float maxAccumulation = plugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getFloat(MainConfigFile.RESTED_EXPERIENCE_MAXIMUM_ACCUMULATION);
        // If they have over the limit (they've accumulated before and the limit got lowered or something), then leave it alone
        if (currentRestedExperience >= maxAccumulation) {
            // If they have just logged in, let them know they are at the experience cap
            if (notifyPlayerOfOfflineGain) {
                audience.sendMessage(localizationManager.getLocalizedMessageAsComponent(audience, LocalizationKey.MAXIMUM_RESTED_EXPERIENCE_REACHED_MESSAGE));
            }
            return;
        }
        PlayerAwardedRestedExperienceEvent playerAwardedRestedExperienceEvent = new PlayerAwardedRestedExperienceEvent(mcRPGPlayer, restedExperience, maxAccumulation);
        Bukkit.getPluginManager().callEvent(playerAwardedRestedExperienceEvent);
        if (playerAwardedRestedExperienceEvent.isCancelled()) {
            return;
        }
        float finalExperienceCount = Math.min(playerExperienceExtras.getRestedExperience() + Math.max(0, playerAwardedRestedExperienceEvent.getRestedExperience()), maxAccumulation);
        float accumulatedExperience = finalExperienceCount - playerExperienceExtras.getRestedExperience();
        if (notifyPlayerOfOfflineGain && accumulatedExperience > 0) {
            audience.sendMessage(localizationManager.getLocalizedMessageAsComponent(audience, LocalizationKey.OFFLINE_RESTED_EXPERIENCE_AWARDED_MESSAGE,
                    Map.of("rested-experience-gained", Float.toString(accumulatedExperience))));
        }
        playerExperienceExtras.setRestedExperience(finalExperienceCount);
    }

    /**
     * This function only exists to be used by unit tests to reset the internals
     * of this manager.
     */
    @API(status = API.Status.INTERNAL)
    private static void reset() {
        RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.RESTED_EXPERIENCE).accumulationRates.clear();
    }
}
