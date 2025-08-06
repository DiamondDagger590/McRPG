package us.eunoians.mcrpg.task.experience;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.CancellableCoreTask;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.experience.rested.RestedExperienceAccumulationType;
import us.eunoians.mcrpg.skill.experience.rested.RestedExperienceManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * This task runs in the background and will periodically award experience
 */
public final class RestedExperienceAccumulationTask extends CancellableCoreTask {

    private static final ReloadableContent<OnlineAccumulationType> ONLINE_ACCUMULATION_TYPE_RELOADABLE_CONTENT = new ReloadableContent<>(RegistryAccess.registryAccess()
            .registry(RegistryKey.MANAGER)
            .manager(McRPGManagerKey.FILE)
            .getFile(FileType.MAIN_CONFIG), MainConfigFile.RESTED_EXPERIENCE_ALLOW_ONLINE_ACCUMULATION,
            (yamlDocument, route) -> OnlineAccumulationType.fromString(yamlDocument.getString(route)).orElse(OnlineAccumulationType.DISABLED));

    static {
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.RELOADABLE_CONTENT).trackReloadableContent(ONLINE_ACCUMULATION_TYPE_RELOADABLE_CONTENT);
    }
    private Set<UUID> playersLastUpdated;

    public RestedExperienceAccumulationTask(@NotNull McRPG mcRPG, double taskDelay, double taskFrequency) {
        super(mcRPG, taskDelay, taskFrequency);
        this.playersLastUpdated = new HashSet<>();
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
        double duration = getTaskFrequency();
        // Ensure we allow online accumulation
        if (ONLINE_ACCUMULATION_TYPE_RELOADABLE_CONTENT.getContent() != OnlineAccumulationType.DISABLED) {
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
                    var playerOptional = mcRPGPlayer.getAsBukkitPlayer();
                    if (playerOptional.isPresent()) {
                        Player player = playerOptional.get();
                        RestedExperienceAccumulationType accumulationType = RestedExperienceAccumulationType.ONLINE;
                        boolean inSafeZone = false;
                        // ENABLED and SAFE_ZONE_ONLY support safe zones so we can first check for
                        // safe zone accumulation
                        if (mcRPGPlayer.isStandingInSafeZone()
                                && getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.SAFE_ZONE).isPlayerInSafeZone(player)
                                && getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG)
                                .getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION)) {
                            inSafeZone = true;
                            accumulationType = RestedExperienceAccumulationType.SAFE_ZONE;
                        }
                        // If normal online accumulation isn't enabled and we aren't in a safe zone, then we aren't going to award anything.
                        else if (ONLINE_ACCUMULATION_TYPE_RELOADABLE_CONTENT.getContent() != OnlineAccumulationType.ENABLED) {
                            continue;
                        }
                        restedExperienceManager.awardRestedExperience(mcRPGPlayer, (int) taskDelay, accumulationType);
                        mcRPGPlayer.setStandingInSafeZone(inSafeZone);
                    }
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

    private enum OnlineAccumulationType {
        ENABLED,
        SAFE_ZONE_ONLY,
        DISABLED;

        public static Optional<OnlineAccumulationType> fromString(@NotNull String string) {
            return Arrays.stream(values()).filter(type -> type.toString().equalsIgnoreCase(string)).findFirst();
        }
    }
}
