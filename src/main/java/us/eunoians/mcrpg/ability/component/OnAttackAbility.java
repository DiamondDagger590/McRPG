package us.eunoians.mcrpg.ability.component;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This interface is used for abilities that activate whenever one {@link Entity}
 * attacks another.
 */
public interface OnAttackAbility extends DamageableAbility {

    public boolean affectsEntity(@NotNull Entity entity);

    public default boolean shouldActivateOnAttack(@NotNull EntityDamageByEntityEvent entityDamageByEntityEvent){

        Entity damager = entityDamageByEntityEvent.getDamager();
        Entity damaged = entityDamageByEntityEvent.getEntity();

        if(!affectsEntity(damaged)){
            return false;
        }


        return false;
    }
}
