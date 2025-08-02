package us.eunoians.mcrpg.skill.experience.context;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;

/**
 * A {@link SkillExperienceContext} that contains the {@link EntityDamageByEntityEvent}
 * to be processed for {@link us.eunoians.mcrpg.skill.experience.modifier.ExperienceModifier}s.
 */
public class EntityDamageContext extends SkillExperienceContext<EntityDamageByEntityEvent> {

    public EntityDamageContext(@NotNull SkillHolder skillHolder, @NotNull Skill skill, int baseExperience, @NotNull EntityDamageByEntityEvent event) {
        super(skillHolder, skill, baseExperience, event);
    }
}
