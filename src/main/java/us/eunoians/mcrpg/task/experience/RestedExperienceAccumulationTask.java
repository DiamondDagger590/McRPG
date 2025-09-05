package us.eunoians.mcrpg.task.experience;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.CancelableCoreTask;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.experience.rested.RestedExperienceAccumulationType;
import us.eunoians.mcrpg.skill.experience.rested.RestedExperienceManager;
import us.eunoians.mcrpg.skill.experience.rested.RestedExperienceOnlineAccumulationSetting;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This task runs in the background and will periodically award experience
 */
public final class RestedExperienceAccumulationTask extends CancelableCoreTask {

    private Set<UUID> playersLastUpdated;
    private RestedExperienceOnlineAccumulationSetting onlineAccumulationSetting;

    public RestedExperienceAccumulationTask(@NotNull McRPG mcRPG, double taskDelay, double taskFrequency) {
        super(mcRPG, taskDelay, taskFrequency);
        this.playersLastUpdated = new HashSet<>();
        this.onlineAccumulationSetting = RestedExperienceOnlineAccumulationSetting.fromString(RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.MAIN_CONFIG)
                .getString(MainConfigFile.RESTED_EXPERIENCE_ALLOW_ONLINE_ACCUMULATION))
                .orElse(RestedExperienceOnlineAccumulationSetting.DISABLED);
    }

    @NotNull
    @Override
    public McRPG getPlugin() {
        return (McRPG) super.getPlugin();
    }

    @Override
    protected void onCancel() {
        playersLastUpdated.clear();
    }

    @Override
    protected void onDelayComplete() {

    }

    @Override
    protected void onIntervalStart() {

    }

    @Override
    protected void onIntervalComplete() {
        Set<UUID> currentPlayers = new HashSet<>();
        RestedExperienceManager restedExperienceManager = getPlugin().registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.RESTED_EXPERIENCE);
        // Ensure we allow online accumulation
        if (onlineAccumulationSetting != RestedExperienceOnlineAccumulationSetting.DISABLED) {
            // Check all players
            for (CorePlayer corePlayer : getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getAllPlayers()) {
                currentPlayers.add(corePlayer.getUUID());
                // If the player was online last time, then we can award experience
                if (playersLastUpdated.contains(corePlayer.getUUID()) && corePlayer instanceof McRPGPlayer mcRPGPlayer) {
                    // If player is afk and afk rested experience accumulation is disabled, then skip.
                    if (mcRPGPlayer.isAfk() && getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG)
                            .getBoolean(MainConfigFile.DISABLE_AFK_RESTED_EXPERIENCE_ACCUMULATION)) {
                        continue;
                    }
                    RestedExperienceAccumulationType accumulationType = RestedExperienceAccumulationType.ONLINE;
                    // ENABLED and SAFE_ZONE_ONLY support safe zones so we can first check for
                    // safe zone accumulation
                    if (mcRPGPlayer.isStandingInSafeZone()) {
                        accumulationType = RestedExperienceAccumulationType.ONLINE_SAFE_ZONE;
                    }
                    // If normal online accumulation isn't enabled, and we aren't in a safe zone, then we aren't going to award anything.
                    else if (onlineAccumulationSetting != RestedExperienceOnlineAccumulationSetting.ENABLED) {
                        continue;
                    }
                    restedExperienceManager.awardRestedExperience(mcRPGPlayer, (int) taskFrequency, accumulationType, false);
                }
            }
            playersLastUpdated = currentPlayers;
        }
    }

    @Override
    protected void onIntervalPause() {

    }

    @Override
    protected void onIntervalResume() {

    }


}
