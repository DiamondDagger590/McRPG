package us.eunoians.mcrpg.display;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.display.impl.ExperienceDisplay;
import us.eunoians.mcrpg.display.impl.persistent.PersistentExperienceDisplay;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.setting.impl.ExperienceDisplaySetting;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The manager for managing {@link ExperienceDisplay}s for players.
 */
public class DisplayManager {

    private final McRPG mcRPG;
    private final Map<UUID, ExperienceDisplay> activeDisplays;

    public DisplayManager(@NotNull McRPG plugin) {
        this.mcRPG = plugin;
        this.activeDisplays = new HashMap<>();
    }

    /**
     * Creates and updates the active {@link ExperienceDisplay} for the provided
     * {@link McRPGPlayer}.
     *
     * @param mcRPGPlayer The {@link McRPGPlayer} to create a new {@link ExperienceDisplay} for.
     */
    public void createDisplay(@NotNull McRPGPlayer mcRPGPlayer) {
        // Clean up an existing display if it exists
        UUID uuid = mcRPGPlayer.getUUID();
        if (hasActiveDisplay(uuid)) {
            removeDisplay(uuid);
        }
        var playerSettingOptional = mcRPGPlayer.getPlayerSetting(ExperienceDisplaySetting.SETTING_KEY);
        if (playerSettingOptional.isPresent() && playerSettingOptional.get() instanceof ExperienceDisplaySetting experienceDisplaySetting) {
            ExperienceDisplay experienceDisplay = experienceDisplaySetting.getExperienceDisplay(mcRPGPlayer);
            activeDisplays.put(mcRPGPlayer.getUUID(), experienceDisplay);
        }
    }

    /**
     * Checks to see if the provided {@link UUID} has an active {@link ExperienceDisplay},
     *
     * @param uuid The {@link UUID} to check.
     * @return {@code true} if the provided {@link UUID} has an active {@link ExperienceDisplay}.
     */
    public boolean hasActiveDisplay(@NotNull UUID uuid) {
        return activeDisplays.containsKey(uuid);
    }

    /**
     * Gets an {@link Optional} containing the {@link ExperienceDisplay} for the provided
     * {@link UUID}.
     *
     * @param uuid The {@link UUID} to get the {@link ExperienceDisplay} for.
     * @return An {@link Optional} containing the {@link ExperienceDisplay} for the provided {@link UUID},
     * or an empty on if {@link #hasActiveDisplay(UUID)} returns {@code false}.
     */
    @NotNull
    public Optional<ExperienceDisplay> getActiveDisplay(@NotNull UUID uuid) {
        return Optional.ofNullable(activeDisplays.get(uuid));
    }

    /**
     * Sends a visual update of the current experience state of the {@link us.eunoians.mcrpg.skill.Skill} belonging to
     * the provided {@link NamespacedKey} for the given {@link McRPGPlayer}.
     *
     * @param mcRPGPlayer The {@link McRPGPlayer} to update the display for.
     * @param skillKey    The {@link NamespacedKey} to get the {@link us.eunoians.mcrpg.skill.Skill} information for the display.
     */
    public void sendExperienceUpdate(@NotNull McRPGPlayer mcRPGPlayer, @NotNull NamespacedKey skillKey) {
        UUID uuid = mcRPGPlayer.getUUID();

        // If they don't have an active display, set one
        if (!hasActiveDisplay(uuid)) {
            createDisplay(mcRPGPlayer);
        }
        ExperienceDisplay experienceDisplay = activeDisplays.get(uuid);
        // Check if it is a persistent display and if the time has expired on it, create a new one
        if (experienceDisplay instanceof PersistentExperienceDisplay persistentExperienceDisplay && persistentExperienceDisplay.hasExpired()) {
            createDisplay(mcRPGPlayer);
            experienceDisplay = activeDisplays.get(uuid);
        }
        experienceDisplay.sendExperienceUpdate(skillKey);
    }

    /**
     * Removes and cleans the active display for the provided {@link UUID}.
     *
     * @param uuid The {@link UUID} to remove the display for.
     */
    public void removeDisplay(@NotNull UUID uuid) {
        if (activeDisplays.containsKey(uuid)) {
            activeDisplays.remove(uuid).cleanDisplay();
        }
    }
}
