package us.eunoians.mcrpg.listener.skill;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.AbilityHolderTracker;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.util.Set;

public class OnAttackLevelListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleOnAttackAbilities(EntityDamageByEntityEvent entityDamageByEntityEvent) {

        McRPG mcRPG = McRPG.getInstance();
        AbilityHolderTracker abilityHolderTracker = mcRPG.getEntityManager();
        SkillRegistry skillRegistry = mcRPG.getSkillRegistry();

        Entity damager = entityDamageByEntityEvent.getDamager();
        Entity damaged = entityDamageByEntityEvent.getEntity();

        abilityHolderTracker.getAbilityHolder(damager.getUniqueId()).ifPresent(damagerAbilityHolder -> {

            if (damagerAbilityHolder instanceof SkillHolder damagerSkillHolder) {
                Set<NamespacedKey> allSkills = damagerSkillHolder.getSkills();

                allSkills.stream().map(skillRegistry::getRegisteredSkill)
                        .filter(skill -> skill.canEventLevelSkill(entityDamageByEntityEvent))
                        .forEach(skill -> damagerSkillHolder.getSkillHolderData(skill).ifPresent(skillHolderData -> {
                            int exp = skill.calculateExperienceToGive(damagerSkillHolder, entityDamageByEntityEvent);
                            if (exp > 0) {
                                skillHolderData.addExperience(exp);
                            }
                        }));
            }
        });
    }
}
