package us.eunoians.mcrpg.display.impl;

import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKeys;
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
        MiniMessage miniMessage = mcRPG.getMiniMessage();
        Skill skill = skillRegistry.getRegisteredSkill(skillKey);
        if (dataOptional.isPresent() && playerOptional.isPresent()) {
            cleanDisplay();
            Audience audience = mcRPG.getAdventure().player(playerOptional.get());
            var skillHolderData = dataOptional.get();
            int currentLevel = skillHolderData.getCurrentLevel();
            int currentExperience = skillHolderData.getCurrentExperience();
            int experienceForNextLevel = skillHolderData.getExperienceForNextLevel();
            audience.sendActionBar(localizationManager.getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKeys.ACTION_BAR_DISPLAY_MESSAGE, Map.of(
                    "skill", skill.getDisplayName(mcRPGPlayer),
                    "level", Integer.toString(currentLevel),
                    "current-experience", Integer.toString(currentExperience),
                    "required-experience-for-next-level", Integer.toString(experienceForNextLevel),
                    "remaining-experience-for-next-level", Integer.toString(experienceForNextLevel, currentExperience))
            ));
        }
    }

    @Override
    public void cleanDisplay() {
        // Action bars auto decay so we don't need to do anything :>
    }
}
