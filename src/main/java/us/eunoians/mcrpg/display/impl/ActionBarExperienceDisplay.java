package us.eunoians.mcrpg.display.impl;

import net.kyori.adventure.audience.Audience;
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

import java.util.UUID;

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
        McRPG mcRPG = mcRPGPlayer.getMcRPGInstance();
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        var dataOptional = skillHolder.getSkillHolderData(skillKey);
        UUID uuid = skillHolder.getUUID();
        Player player = Bukkit.getPlayer(uuid);
        SkillRegistry skillRegistry = mcRPG.getSkillRegistry();
        MiniMessage miniMessage = mcRPG.getMiniMessage();
        Skill skill = skillRegistry.getRegisteredSkill(skillKey);
        Audience audience = mcRPG.getAdventure().player(player);
        cleanDisplay();
        if (dataOptional.isPresent()) {
            var skillHolderData = dataOptional.get();
            int currentLevel = skillHolderData.getCurrentLevel();
            int currentExperience = skillHolderData.getCurrentExperience();
            int experienceForNextLevel = skillHolderData.getExperienceForNextLevel();
            Component component = miniMessage.deserialize("<gray>" + skill.getDisplayName() + "Lv.<gold>" + currentLevel + " <gray>- Needed Exp" + skill.getDisplayName() + ": <gold>" + (experienceForNextLevel - currentExperience));
            player.sendActionBar(component);
        }
    }

    @Override
    public void cleanDisplay() {
        // Action bars auto decay so we don't need to do anything :>
    }
}
