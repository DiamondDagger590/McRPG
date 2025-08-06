package us.eunoians.mcrpg.skill.experience.rested;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.configuration.parser.ReloadableParser;
import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.PlayerExperienceExtras;
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
        accumulationRates.put(RestedExperienceAccumulationType.SAFE_ZONE, new ReloadableParser(fileManager.getFile(FileType.MAIN_CONFIG), MainConfigFile.SAFE_ZONE_RESTED_EXPERIENCE_ACCUMULATION_RATE));
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
    public double getRestedExperience(int timeInSeconds, @NotNull RestedExperienceAccumulationType accumulationType) {
        Parser parser = accumulationRates.get(accumulationType).getContent();
        parser.setVariable("time", timeInSeconds);
        return parser.getValue();
    }

    /**
     * Calculates and awards rested experience to the provided {@link McRPGPlayer}. If the player currently has more rested experience than
     * the current limit, then this method turns into a no-op.
     * <p>
     * This method also will override the safe zone setting if safe zone accumulation is disabled in the config.
     *
     * @param mcRPGPlayer      The {@link McRPGPlayer} to award experience to.
     * @param timeInSeconds    The amount of time in seconds to give rested experience for.
     * @param accumulationType The type of rested accumulation to use for calculating amount to award.
     */
    public void awardRestedExperience(@NotNull McRPGPlayer mcRPGPlayer, int timeInSeconds, @NotNull RestedExperienceAccumulationType accumulationType) {
        if (accumulationType == RestedExperienceAccumulationType.SAFE_ZONE && !plugin().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION)) {
            accumulationType = RestedExperienceAccumulationType.ONLINE;
        }
        awardRestedExperience(mcRPGPlayer, getRestedExperience(timeInSeconds, accumulationType));
    }

    /**
     * Awards rested experience to the provided {@link McRPGPlayer}. If the player currently has more rested experience than
     * the current limit, then this method turns into a no-op.
     *
     * @param mcRPGPlayer      The {@link McRPGPlayer} to award experience to.
     * @param restedExperience The amount of rested experience to give.
     */
    public void awardRestedExperience(@NotNull McRPGPlayer mcRPGPlayer, double restedExperience) {
        PlayerExperienceExtras playerExperienceExtras = mcRPGPlayer.getExperienceExtras();
        double currentRestedExperience = playerExperienceExtras.getRestedExperience();
        double maxAccumulation = plugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getDouble(MainConfigFile.RESTED_EXPERIENCE_MAXIMUM_ACCUMULATION);
        // If they have over the limit (theyve accumulated before and the limit got lowered or something), then leave it alone
        if (currentRestedExperience >= maxAccumulation) {
            return;
        }
        restedExperience = Math.min(playerExperienceExtras.getRestedExperience() + Math.max(0, restedExperience), maxAccumulation);
        playerExperienceExtras.setRestedExperience((int) restedExperience);
    }
}
