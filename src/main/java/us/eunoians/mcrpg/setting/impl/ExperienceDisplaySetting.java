package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.display.DisplayManager;
import us.eunoians.mcrpg.display.impl.ActionBarExperienceDisplay;
import us.eunoians.mcrpg.display.impl.BossBarExperienceDisplay;
import us.eunoians.mcrpg.display.impl.ExperienceDisplay;
import us.eunoians.mcrpg.display.impl.persistent.PersistentExperienceDisplay;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.setting.slot.ExperienceDisplaySettingSlot;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.McRPGSetting;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    public void onSettingChange(@NotNull CorePlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
        assert(player instanceof McRPGPlayer);
        McRPGPlayer mcRPGPlayer = (McRPGPlayer) player;
        DisplayManager displayManager = mcRPGPlayer.getPlugin().registryAccess()
                .registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.DISPLAY);
        // Only rebuild if they already had a display; otherwise next XP gain
        // will lazily materialise the right subclass via sendExperienceUpdate.
        if (displayManager.hasDisplay(mcRPGPlayer, ExperienceDisplay.class)) {
            ExperienceDisplay fresh = resolveSetting(mcRPGPlayer).getExperienceDisplay(mcRPGPlayer);
            displayManager.setDisplay(mcRPGPlayer, ExperienceDisplay.class, fresh);
        }
    }

    /**
     * Routes an experience update for {@code skillKey} through the player's
     * configured {@link ExperienceDisplay}, materialising or rebuilding the
     * display as needed.
     * <p>
     * Replaces the old {@code DisplayManager#sendExperienceUpdate} flow so the
     * display manager can stay a thin generic coordinator while the XP
     * lifecycle stays encapsulated with the setting that owns the display
     * type.
     *
     * @param mcRPGPlayer The player to send the update to.
     * @param skillKey    The skill that gained experience.
     */
    public static void sendExperienceUpdate(@NotNull McRPGPlayer mcRPGPlayer, @NotNull NamespacedKey skillKey) {
        McRPG mcRPG = mcRPGPlayer.getPlugin();
        boolean enabled = mcRPG.registryAccess().registry(McRPGRegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG)
                .getBoolean(MainConfigFile.DISPLAY_EXPERIENCE_UPDATES_ENABLED, false);
        if (!enabled) {
            return;
        }
        DisplayManager displayManager = mcRPG.registryAccess()
                .registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.DISPLAY);
        ExperienceDisplaySetting setting = resolveSetting(mcRPGPlayer);
        ExperienceDisplay display = displayManager.getDisplay(mcRPGPlayer, ExperienceDisplay.class).orElse(null);
        boolean rebuild = display == null || display.getSetting() != setting;
        if (!rebuild && display instanceof PersistentExperienceDisplay persistent && persistent.hasExpired()) {
            rebuild = true;
        }
        if (rebuild) {
            display = setting.getExperienceDisplay(mcRPGPlayer);
            displayManager.setDisplay(mcRPGPlayer, ExperienceDisplay.class, display);
        }
        display.sendExperienceUpdate(skillKey);
    }

    @NotNull
    private static ExperienceDisplaySetting resolveSetting(@NotNull McRPGPlayer mcRPGPlayer) {
        return mcRPGPlayer.getPlayerSetting(SETTING_KEY)
                .filter(ExperienceDisplaySetting.class::isInstance)
                .map(ExperienceDisplaySetting.class::cast)
                .orElse(FIRST_SETTING.getNodeValue());
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
