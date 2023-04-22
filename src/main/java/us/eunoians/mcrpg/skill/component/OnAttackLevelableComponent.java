package us.eunoians.mcrpg.skill.component;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

public interface OnAttackLevelableComponent extends EventLevelableComponent {

    /**
     * Checks to see if this ability component can affect the provided
     * {@link Entity}.
     *
     * @param entity The {@link Entity} to check
     * @return {@code true} if the provided {@link Entity} is affected by
     * this ability component
     */
    public boolean affectsEntity(@NotNull Entity entity);

    @Override
    default boolean shouldGiveExperience(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        if (event instanceof EntityDamageByEntityEvent entityDamageByEntityEvent) {
            Entity damager = entityDamageByEntityEvent.getDamager();
            Entity damaged = entityDamageByEntityEvent.getEntity();
            return damager.getUniqueId().equals(abilityHolder.getUUID()) && affectsEntity(damaged);
        }
        return false;
    }
}
