package us.eunoians.mcrpg.listener.skill;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.util.Set;
import java.util.UUID;

public interface SkillListener extends Listener {

    default void levelSkill(@NotNull UUID uuid, @NotNull Event event) {
        McRPG mcRPG = McRPG.getInstance();
        EntityManager entityManager = mcRPG.getEntityManager();
        SkillRegistry skillRegistry = mcRPG.getSkillRegistry();
        entityManager.getAbilityHolder(uuid).ifPresent(abilityHolder -> {

            if (abilityHolder instanceof SkillHolder skillHolder) {
                Set<NamespacedKey> allSkills = skillHolder.getSkills();

                allSkills.stream().map(skillRegistry::getRegisteredSkill)
                        .filter(skill -> skill.canEventLevelSkill(event))
                        .forEach(skill -> skillHolder.getSkillHolderData(skill).ifPresent(skillHolderData -> {
                            int exp = skill.calculateExperienceToGive(skillHolder, event);
                            if (exp > 0) {
                                skillHolderData.addExperience(exp);
                            }
                        }));
            }
        });
    }
}
