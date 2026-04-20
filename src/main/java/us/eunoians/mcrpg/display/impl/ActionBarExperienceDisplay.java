package us.eunoians.mcrpg.display.impl;

import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.builder.item.skill.SkillItemPlaceholderKeys;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.display.hud.ActionBarHudDisplay;
import us.eunoians.mcrpg.display.hud.CenterContentPriority;
import us.eunoians.mcrpg.display.hud.content.TimedCenterContent;
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
 * {@link ExperienceDisplay} that surfaces XP gain messages via the action bar.
 * <p>
 * Instead of writing to {@link Player#sendActionBar(Component)} directly — which
 * would race the HP/mana HUD and momentarily blank it — this implementation
 * pushes the rendered message into the player's shared
 * {@link ActionBarHudDisplay} at
 * {@link CenterContentPriority#AMBIENT_FEEDBACK}. The HUD's priority resolver
 * then transparently defers to higher-priority content (safe-zone messages,
 * combo dots, cooldown countdowns) without the XP message stomping them.
 */
public class ActionBarExperienceDisplay extends ExperienceDisplay {

    /**
     * How many ticks an XP message remains visible before the ambient slot is
     * released back to combo dots or whatever lower-priority content exists.
     */
    private static final long DISPLAY_DURATION_TICKS = 40L;

    public ActionBarExperienceDisplay(@NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer, ExperienceDisplaySetting.ACTION_BAR);
    }

    @Override
    public void sendExperienceUpdate(@NotNull NamespacedKey skillKey) {
        McRPGPlayer mcRPGPlayer = getMcRPGPlayer();
        McRPG mcRPG = mcRPGPlayer.getPlugin();
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        var dataOptional = skillHolder.getSkillHolderData(skillKey);
        if (dataOptional.isEmpty()) {
            return;
        }
        McRPGLocalizationManager localizationManager = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        SkillRegistry skillRegistry = mcRPG.registryAccess().registry(McRPGRegistryKey.SKILL);
        Skill skill = skillRegistry.getRegisteredSkill(skillKey);
        var skillHolderData = dataOptional.get();
        Component message = localizationManager.getLocalizedMessageAsComponent(mcRPGPlayer,
                LocalizationKey.ACTION_BAR_DISPLAY_MESSAGE, Map.of(
                        SkillItemPlaceholderKeys.SKILL.getKey(), skill.getName(mcRPGPlayer),
                        SkillItemPlaceholderKeys.LEVEL.getKey(), Integer.toString(skillHolderData.getCurrentLevel()),
                        SkillItemPlaceholderKeys.CURRENT_EXPERIENCE.getKey(), Integer.toString(skillHolderData.getCurrentExperience()),
                        SkillItemPlaceholderKeys.REQUIRED_EXPERIENCE_TO_LEVEL_UP.getKey(), Integer.toString(skillHolderData.getExperienceForNextLevel()),
                        SkillItemPlaceholderKeys.REMAINING_EXPERIENCE_TO_LEVEL_UP.getKey(), Integer.toString(skillHolderData.getRemainingExperienceForNextLevel())
                ));

        ActionBarHudDisplay hud = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DISPLAY)
                .getOrCreateActionBarHud(mcRPGPlayer);
        long expiryTick = Bukkit.getCurrentTick() + DISPLAY_DURATION_TICKS;
        hud.setSlot(CenterContentPriority.AMBIENT_FEEDBACK, new TimedCenterContent(message, expiryTick));
    }

    @Override
    public void cleanDisplay() {
        // Ambient slot self-expires; nothing owned by this display needs tearing down.
    }
}
