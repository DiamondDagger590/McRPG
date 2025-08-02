package us.eunoians.mcrpg.listener.skill;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.experience.context.EntityDamageContext;
import us.eunoians.mcrpg.skill.experience.context.SkillExperienceContext;

import java.util.Optional;

/**
 * This listener handles {@link Skill}s that gain experience from the {@link EntityDamageByEntityEvent}.
 */
public class OnAttackLevelListener implements SkillListener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleOnAttackAbilities(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        levelSkill(entityDamageByEntityEvent.getDamager().getUniqueId(), entityDamageByEntityEvent);
    }

    @NotNull
    @Override
    public Optional<SkillExperienceContext<?>> getEventContext(@NotNull SkillHolder skillHolder, @NotNull Skill skill, int experience, @NotNull Event event) {
        return event instanceof EntityDamageByEntityEvent entityDamageByEntityEvent ? Optional.of(new EntityDamageContext(skillHolder, skill, experience, entityDamageByEntityEvent)) : Optional.empty();
    }
}
