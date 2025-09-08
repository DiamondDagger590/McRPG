package us.eunoians.mcrpg.display.impl;

import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.audience.Audience;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.builder.item.skill.SkillItemPlaceholderKeys;
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

/**
 * This experience display will display experience updates via
 * {@link Player#sendActionBar(String)}.
 */
public class ActionBarExperienceDisplay extends ExperienceDisplay {

    public ActionBarExperienceDisplay(@NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer, ExperienceDisplaySetting.ACTION_BAR);
    }

    @Override
    public void sendExperienceUpdate(@NotNull NamespacedKey skillKey) {
        McRPGPlayer mcRPGPlayer = getMcRPGPlayer();
        McRPG mcRPG = mcRPGPlayer.getPlugin();
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        var dataOptional = skillHolder.getSkillHolderData(skillKey);
        var playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        SkillRegistry skillRegistry = mcRPG.registryAccess().registry(McRPGRegistryKey.SKILL);
        Skill skill = skillRegistry.getRegisteredSkill(skillKey);
        if (dataOptional.isPresent() && playerOptional.isPresent()) {
            Audience audience = playerOptional.get();
            var skillHolderData = dataOptional.get();
            int currentLevel = skillHolderData.getCurrentLevel();
            int currentExperience = skillHolderData.getCurrentExperience();
            int experienceForNextLevel = skillHolderData.getExperienceForNextLevel();
            int remainingExperienceForNextLevel = skillHolderData.getRemainingExperienceForNextLevel();
            audience.sendActionBar(localizationManager.getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.ACTION_BAR_DISPLAY_MESSAGE, Map.of(
                    SkillItemPlaceholderKeys.SKILL.getKey(), skill.getName(mcRPGPlayer),
                    SkillItemPlaceholderKeys.LEVEL.getKey(), Integer.toString(currentLevel),
                    SkillItemPlaceholderKeys.CURRENT_EXPERIENCE.getKey(), Integer.toString(currentExperience),
                    SkillItemPlaceholderKeys.REQUIRED_EXPERIENCE_TO_LEVEL_UP.getKey(), Integer.toString(experienceForNextLevel),
                    SkillItemPlaceholderKeys.REMAINING_EXPERIENCE_TO_LEVEL_UP.getKey(), Integer.toString(remainingExperienceForNextLevel)
            )));
        }
    }

    @Override
    public void cleanDisplay() {
        // Action bars auto decay so we don't need to do anything :>
    }
}
