package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.display.DisplayManager;
import us.eunoians.mcrpg.display.impl.ActionBarExperienceDisplay;
import us.eunoians.mcrpg.display.impl.BossBarExperienceDisplay;
import us.eunoians.mcrpg.display.impl.ExperienceDisplay;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.setting.ExperienceDisplaySettingSlot;
import us.eunoians.mcrpg.setting.McRPGSetting;
import us.eunoians.mcrpg.setting.PlayerSetting;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A player setting that allows players to configure how they want
 * experience updates to be displayed.
 */
public enum ExperienceDisplaySetting implements McRPGSetting {

    /**
     * This setting allows players to view experience updates via {@link org.bukkit.boss.BossBar}
     */
    BOSS_BAR(BossBarExperienceDisplay::new),
    /**
     * This setting allows players to view experience updates via {@link org.bukkit.entity.Player#sendActionBar(String)}
     */
    ACTION_BAR(ActionBarExperienceDisplay::new),
    ;

    private static final LinkedNode<ExperienceDisplaySetting> FIRST_SETTING = new LinkedNode<>(BOSS_BAR);
    private static final Map<ExperienceDisplaySetting, LinkedNode<ExperienceDisplaySetting>> SETTINGS = new HashMap<>();
    public static final NamespacedKey SETTING_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "experience-display-setting");

    static {
        SETTINGS.put(FIRST_SETTING.getNodeValue(), FIRST_SETTING);
        LinkedNode<ExperienceDisplaySetting> prev = FIRST_SETTING;
        for (ExperienceDisplaySetting setting : values()) {
            if (setting != FIRST_SETTING.getNodeValue()) {
                LinkedNode<ExperienceDisplaySetting> next = new LinkedNode<>(setting);
                prev.setNext(next);
                prev = next;
                SETTINGS.put(setting, prev);
            }
        }
        prev.setNext(FIRST_SETTING);
    }

    private final ExperienceDisplayFunction experienceDisplayFunction;

    ExperienceDisplaySetting(@NotNull ExperienceDisplayFunction experienceDisplayFunction) {
        this.experienceDisplayFunction = experienceDisplayFunction;
    }

    @NotNull
    @Override
    public NamespacedKey getSettingKey() {
        return SETTING_KEY;
    }

    @NotNull
    @Override
    public LinkedNode<ExperienceDisplaySetting> getFirstSetting() {
        return FIRST_SETTING;
    }

    @NotNull
    @Override
    public LinkedNode<ExperienceDisplaySetting> getNextSetting() {
        return SETTINGS.get(this).getNextNode();
    }

    @NotNull
    @Override
    public ExperienceDisplaySettingSlot getSettingSlot(@NotNull McRPGPlayer mcRPGPlayer) {
        return new ExperienceDisplaySettingSlot(mcRPGPlayer, this);
    }

    @Override
    public void onSettingChange(@NotNull McRPGPlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
        McRPG mcRPG = player.getMcRPGInstance();
        DisplayManager displayManager = mcRPG.getDisplayManager();
        UUID uuid = player.getUUID();
        if (displayManager.hasActiveDisplay(uuid)) {
            displayManager.createDisplay(player);
        }
    }

    @NotNull
    @Override
    public Optional<ExperienceDisplaySetting> fromString(@NotNull String setting) {
        return Arrays.stream(values()).filter(experienceDisplaySetting -> experienceDisplaySetting.toString().equalsIgnoreCase(setting)).findFirst();
    }

    /**
     * Gets an {@link ExperienceDisplay} specific to this display setting.
     *
     * @param mcRPGPlayer The {@link McRPGPlayer} to get the display for.
     * @return An {@link ExperienceDisplay} for the given {@link McRPGPlayer}.
     */
    @NotNull
    public ExperienceDisplay getExperienceDisplay(@NotNull McRPGPlayer mcRPGPlayer) {
        return experienceDisplayFunction.createExperienceDisplay(mcRPGPlayer);
    }

    /**
     * A functional interface used for creating {@link ExperienceDisplay}s.
     */
    private interface ExperienceDisplayFunction {

        /**
         * Creates an {@link ExperienceDisplay} specific to this display setting.
         *
         * @param mcRPGPlayer The {@link McRPGPlayer} to get the display for.
         * @return An {@link ExperienceDisplay} for the given {@link McRPGPlayer}.
         */
        @NotNull
        ExperienceDisplay createExperienceDisplay(@NotNull McRPGPlayer mcRPGPlayer);
    }
}
