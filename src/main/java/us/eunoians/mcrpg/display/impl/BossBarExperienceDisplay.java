package us.eunoians.mcrpg.display.impl;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.DelayableCoreTask;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.builder.item.skill.SkillItemPlaceholderKeys;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.impl.ExperienceDisplaySetting;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This experience display will display an update through a {@link BossBar} for a short
 * period of time before automatically removing the displayed boss bar.
 */
public class BossBarExperienceDisplay extends ExperienceDisplay {

    int taskId;
    protected BossBar bossBar;

    public BossBarExperienceDisplay(@NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer, ExperienceDisplaySetting.BOSS_BAR);
    }

    @Override
    public void sendExperienceUpdate(@NotNull NamespacedKey skillKey) {
        McRPGPlayer mcRPGPlayer = getMcRPGPlayer();
        McRPG mcRPG = mcRPGPlayer.getPlugin();
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        var dataOptional = skillHolder.getSkillHolderData(skillKey);
        UUID uuid = skillHolder.getUUID();
        Player player = Bukkit.getPlayer(uuid);
        if (dataOptional.isPresent() && player != null) {
            displayUpdate(skillKey, getMcRPGPlayer(), dataOptional.get());
            DelayableCoreTask delayableCoreTask = new DelayableCoreTask(mcRPG, mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getInt(MainConfigFile.EXPERIENCE_BOSS_BAR_DISPLAY_DURATION, 3)) {

                @Override
                public void run() {
                    if (getActiveDisplay().isPresent() && taskId == this.getBukkitTaskId()) {
                        cleanDisplay();
                    }
                }
            };
            delayableCoreTask.runTask();
            taskId = delayableCoreTask.getBukkitTaskId();
        }
    }

    /**
     * Displays an experience update.
     *
     * @param skillKey        The {@link NamespacedKey} to get the {@link Skill} info for.
     * @param mcRPGPlayer     The {@link McRPGPlayer} to display to.
     * @param skillHolderData The {@link SkillHolder.SkillHolderData} containing data.
     */
    protected void displayUpdate(@NotNull NamespacedKey skillKey, @NotNull McRPGPlayer mcRPGPlayer, @NotNull SkillHolder.SkillHolderData skillHolderData) {
        var audienceOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (audienceOptional.isEmpty()) {
            return;
        }
        Audience audience = audienceOptional.get();
        McRPG mcRPG = getMcRPGPlayer().getPlugin();
        YamlDocument mainConfig = mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG);
        McRPGLocalizationManager localizationManager = mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        SkillRegistry skillRegistry = mcRPG.registryAccess().registry(McRPGRegistryKey.SKILL);
        Skill skill = skillRegistry.getRegisteredSkill(skillKey);
        cleanDisplay();
        int currentLevel = skillHolderData.getCurrentLevel();
        int currentExperience = skillHolderData.getCurrentExperience();
        int experienceForNextLevel = skillHolderData.getExperienceForNextLevel();
        int remainingExperienceForNextLevel = skillHolderData.getRemainingExperienceForNextLevel();
        Component component = localizationManager.getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.BOSS_BAR_DISPLAY_MESSAGE, Map.of(
                SkillItemPlaceholderKeys.SKILL.getKey(), skill.getName(mcRPGPlayer),
                SkillItemPlaceholderKeys.LEVEL.getKey(), Integer.toString(currentLevel),
                SkillItemPlaceholderKeys.CURRENT_EXPERIENCE.getKey(), Integer.toString(currentExperience),
                SkillItemPlaceholderKeys.REQUIRED_EXPERIENCE_TO_LEVEL_UP.getKey(), Integer.toString(experienceForNextLevel),
                SkillItemPlaceholderKeys.REMAINING_EXPERIENCE_TO_LEVEL_UP.getKey(), Integer.toString(remainingExperienceForNextLevel)));
        bossBar = BossBar.bossBar(component, (((float) currentExperience) / ((float) experienceForNextLevel)),
                BossBar.Color.valueOf(mainConfig.getString(MainConfigFile.EXPERIENCE_BOSS_BAR_DISPLAY_COLOR, "PURPLE")),
                BossBar.Overlay.valueOf(mainConfig.getString(MainConfigFile.EXPERIENCE_BOSS_BAR_STYLE, "NOTCHED_10")));
        audience.showBossBar(bossBar);
    }

    @Override
    public void cleanDisplay() {
        if (bossBar != null) {
            McRPGPlayer mcRPGPlayer = getMcRPGPlayer();
            mcRPGPlayer.getAsBukkitPlayer().ifPresent(audience -> audience.hideBossBar(bossBar));
            bossBar = null;
        }
    }

    /**
     * Gets an {@link Optional} containing the currently displayed {@link BossBar}.
     *
     * @return An {@link Optional} containing the currently displayed {@link BossBar}, or an empty
     * one if there is no boss bar being displayed.
     */
    @NotNull
    public Optional<BossBar> getActiveDisplay() {
        return Optional.ofNullable(bossBar);
    }
}
