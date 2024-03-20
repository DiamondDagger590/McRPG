package us.eunoians.mcrpg.skill.component;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;

/**
 * An {@link EventLevelableComponent} that awards experience whenever a {@link SkillHolder}
 * is the attacker for an {@link EntityDamageByEntityEvent}.
 */
public interface OnAttackLevelableComponent extends EventLevelableComponent {

    /**
     * Checks to see if this ability component can affect the provided
     * {@link Entity}.
     *
     * @param entity The {@link Entity} to check
     * @return {@code true} if the provided {@link Entity} is affected by
     * this ability component
     */
    boolean affectsEntity(@NotNull Entity entity);

    @Override
    default boolean shouldGiveExperience(@NotNull SkillHolder skillHolder, @NotNull Event event) {
        if (event instanceof EntityDamageByEntityEvent entityDamageByEntityEvent) {
            Entity damager = entityDamageByEntityEvent.getDamager();
            Entity damaged = entityDamageByEntityEvent.getEntity();
            return damager.getUniqueId().equals(skillHolder.getUUID()) && affectsEntity(damaged);
        }
        return false;
    }
}
