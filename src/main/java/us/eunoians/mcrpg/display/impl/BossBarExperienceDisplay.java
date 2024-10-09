package us.eunoians.mcrpg.display.impl;

import com.diamonddagger590.mccore.task.core.DelayableCoreTask;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.setting.impl.ExperienceDisplaySetting;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.util.Optional;
import java.util.UUID;

/**
 * This experience display will display an update through a {@link BossBar} for a short
 * period of time before automatically removing the displayed boss bar.
 */
public class BossBarExperienceDisplay extends ExperienceDisplay {

    protected BossBar bossBar;

    public BossBarExperienceDisplay(@NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer, ExperienceDisplaySetting.BOSS_BAR);
    }

    @Override
    public void sendExperienceUpdate(@NotNull NamespacedKey skillKey) {
        McRPGPlayer mcRPGPlayer = getMcRPGPlayer();
        McRPG mcRPG = mcRPGPlayer.getMcRPGInstance();
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        var dataOptional = skillHolder.getSkillHolderData(skillKey);
        UUID uuid = skillHolder.getUUID();
        Player player = Bukkit.getPlayer(uuid);
        if (dataOptional.isPresent() && player != null) {
            displayUpdate(skillKey, player, dataOptional.get());
            DelayableCoreTask delayableCoreTask = new DelayableCoreTask(mcRPG, 10) {

                @Override
                public void run() {
                    if (getActiveDisplay().isPresent() && getActiveDisplay().get() == bossBar) {
                        cleanDisplay();
                    }
                }
            };
            delayableCoreTask.runTask();
        }
    }

    /**
     * Displays an experience update.
     *
     * @param skillKey        The {@link NamespacedKey} to get the {@link Skill} info for.
     * @param player          The {@link Player} to display to.
     * @param skillHolderData The {@link us.eunoians.mcrpg.entity.holder.SkillHolder.SkillHolderData} containing data.
     */
    protected void displayUpdate(@NotNull NamespacedKey skillKey, @NotNull Player player, @NotNull SkillHolder.SkillHolderData skillHolderData) {
        McRPG mcRPG = getMcRPGPlayer().getMcRPGInstance();
        SkillRegistry skillRegistry = mcRPG.getSkillRegistry();
        MiniMessage miniMessage = mcRPG.getMiniMessage();
        Skill skill = skillRegistry.getRegisteredSkill(skillKey);
        Audience audience = mcRPG.getAdventure().player(player);
        cleanDisplay();
        int currentLevel = skillHolderData.getCurrentLevel();
        int currentExperience = skillHolderData.getCurrentExperience();
        int experienceForNextLevel = skillHolderData.getExperienceForNextLevel();
        Component component = miniMessage.deserialize("<gray>Lv.<gold>" + currentLevel + " <gray>- " + skill.getDisplayName() + ": <gold>" + (experienceForNextLevel - currentExperience));
        bossBar = BossBar.bossBar(component, (((float) currentExperience) / ((float) experienceForNextLevel)), BossBar.Color.WHITE, BossBar.Overlay.NOTCHED_10);
        audience.showBossBar(bossBar);
    }

    @Override
    public void cleanDisplay() {
        if (bossBar != null) {
            McRPGPlayer mcRPGPlayer = getMcRPGPlayer();
            McRPG mcRPG = mcRPGPlayer.getMcRPGInstance();
            Audience audience = mcRPG.getAdventure().player(mcRPGPlayer.getUUID());
            audience.hideBossBar(bossBar);
            bossBar = null;
        }
    }

    /**
     * Gets an {@link Optional} containing the currently displayed {@link BossBar}.
     * @return An {@link Optional} containing the currently displayed {@link BossBar}, or an empty
     * one if there is no boss bar being displayed.
     */
    @NotNull
    public Optional<BossBar> getActiveDisplay() {
        return Optional.ofNullable(bossBar);
    }
}
