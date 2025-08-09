package us.eunoians.mcrpg.skill.experience.rested;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Arrays;
import java.util.Optional;

/**
 * This enum represents all the different settings for
 * accumulating rested experience whilst the player is online.
 */
public enum RestedExperienceOnlineAccumulationSetting {
    ENABLED,
    SAFE_ZONE_ONLY,
    DISABLED;

    /**
     * Gets the accumulation setting instance matching the provided string.
     *
     * @param type The string to get the corresponding enum value from
     * @return An {@link Optional} containing an accumulation type if there were matches.
     */
    @NotNull
    public static Optional<RestedExperienceOnlineAccumulationSetting> fromString(@NotNull String type) {
        return Arrays.stream(values()).filter(accumulationType -> accumulationType.toString()
                .equalsIgnoreCase(type)).findFirst();
    }

    /**
     * Gets the current setting from the config file.
     *
     * @return An {@link Optional} containing the accumulation setting if the config was setup correctly.
     */
    @NotNull
    public static Optional<RestedExperienceOnlineAccumulationSetting> getCurrentSetting() {
        return fromString(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG)
                .getString(MainConfigFile.RESTED_EXPERIENCE_ALLOW_ONLINE_ACCUMULATION));
    }
}

