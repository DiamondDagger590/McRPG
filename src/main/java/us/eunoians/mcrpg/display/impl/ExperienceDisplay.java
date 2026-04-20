package us.eunoians.mcrpg.display.impl;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.setting.impl.ExperienceDisplaySetting;

/**
 * An experience display is used to visually display experience gains in a given
 * {@link us.eunoians.mcrpg.skill.Skill} to players.
 */
public abstract class ExperienceDisplay extends PlayerDisplay {

    private final ExperienceDisplaySetting setting;

    public ExperienceDisplay(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ExperienceDisplaySetting setting) {
        super(mcRPGPlayer);
        this.setting = setting;
    }

    /**
     * Gets the {@link ExperienceDisplaySetting} that defines how this display
     * is being displayed.
     *
     * @return The {@link ExperienceDisplaySetting} that defines how this display
     * is being displayed.
     */
    @NotNull
    public final ExperienceDisplaySetting getSetting() {
        return setting;
    }

    /**
     * Sends an experience update to this display for the {@link us.eunoians.mcrpg.skill.Skill} belonging
     * to the provided {@link NamespacedKey}.
     *
     * @param skillKey The {@link NamespacedKey} to update the display for.
     */
    public abstract void sendExperienceUpdate(@NotNull NamespacedKey skillKey);
}
