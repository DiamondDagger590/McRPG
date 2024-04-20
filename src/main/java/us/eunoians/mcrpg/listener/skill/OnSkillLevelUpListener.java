package us.eunoians.mcrpg.listener.skill;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.UnlockableAbility;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.api.event.ability.AbilityUnlockEvent;
import us.eunoians.mcrpg.api.event.skill.PostSkillGainExpEvent;
import us.eunoians.mcrpg.api.event.skill.SkillGainLevelEvent;
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;

import java.util.UUID;

/**
 * This listener is in charge of handling ability unlocks and ability point distributions
 */
public class OnSkillLevelUpListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleLevelUp(SkillGainLevelEvent skillGainLevelEvent) {
        SkillHolder skillHolder = skillGainLevelEvent.getSkillHolder();
        UUID uuid = skillHolder.getUUID();
        int levels = skillGainLevelEvent.getLevels();
        Skill skill = McRPG.getInstance().getSkillRegistry().getRegisteredSkill(skillGainLevelEvent.getSkillKey());
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        Audience player = McRPG.getInstance().getAdventure().player(uuid);
        player.sendMessage(miniMessage.deserialize(String.format("<green>You have gone up <gold>%d levels<green> in <gold>%s<green>.", levels, skill.getDisplayName())));
    }

    @EventHandler
    public void handleLevelUp(PostSkillGainExpEvent postSkillGainExpEvent) {
        SkillHolder skillHolder = postSkillGainExpEvent.getSkillHolder();
        UUID uuid = skillHolder.getUUID();
        Skill skill = McRPG.getInstance().getSkillRegistry().getRegisteredSkill(postSkillGainExpEvent.getSkillKey());
        var skillHolderDataOptional = skillHolder.getSkillHolderData(skill);
        // Check to see if we need to unlock any abilities
        if (skillHolderDataOptional.isPresent()) {
            var skillHolderData = skillHolderDataOptional.get();
            AbilityRegistry abilityRegistry = McRPG.getInstance().getAbilityRegistry();
            // Check all abilities for the skill
            for (NamespacedKey abilityKey : abilityRegistry.getAbilitiesBelongingToSkill(skill)) {
                Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
                // If the ability is unlockable and the holder's skill level is higher than or equal to unlock level
                if (ability instanceof UnlockableAbility unlockableAbility && unlockableAbility.getUnlockLevel() <= skillHolderData.getCurrentLevel()) {
                    var abilityDataOptional = skillHolder.getAbilityData(abilityKey);
                    if (abilityDataOptional.isPresent()) {
                        AbilityData abilityData = abilityDataOptional.get();
                        var attributeOptional = abilityData.getAbilityAttribute(AbilityAttributeManager.ABILITY_UNLOCKED_ATTRIBUTE);
                        if (attributeOptional.isPresent()) {
                            AbilityUnlockedAttribute attribute = (AbilityUnlockedAttribute) attributeOptional.get();
                            // If the ability isn't unlocked, unlock it after we call the event
                            if (!attribute.getContent()) {
                                AbilityUnlockEvent abilityUnlockEvent = new AbilityUnlockEvent(unlockableAbility);
                                Bukkit.getPluginManager().callEvent(abilityUnlockEvent);
                                abilityData.updateAttribute(attribute, true);
                                //Save the updated attribute
                                SkillDAO.savePlayerSkillData(McRPG.getInstance().getDatabaseManager().getDatabase().getConnection(), skillHolder).exceptionally(throwable -> {
                                    throwable.printStackTrace();
                                    return null;
                                });
                                MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
                                Audience player = McRPG.getInstance().getAdventure().player(uuid);
                                player.sendMessage(miniMessage.deserialize(String.format("<green>You have unlocked a new ability! <gold>%s<green> is now available for use.", unlockableAbility.getDisplayName())));
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handlePostExperienceGain(PostSkillGainExpEvent skillGainExpEvent) {
        SkillHolder skillHolder = skillGainExpEvent.getSkillHolder();
        Skill skill = McRPG.getInstance().getSkillRegistry().getRegisteredSkill(skillGainExpEvent.getSkillKey());

        if(Bukkit.getEntity(skillHolder.getUUID()) instanceof Player player && player.isOnline()) {
            McRPG.getInstance().getDisplayManager().sendExperienceUpdate(skillHolder, skill.getSkillKey());
        }
    }
}
