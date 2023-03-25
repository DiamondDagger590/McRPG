package us.eunoians.mcrpg.ability.component;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.AbilityHolderTracker;

/**
 * This interface is used for abilities that activate whenever one {@link Entity}
 * attacks another.
 */
public interface OnAttackAbility extends DamageableAbility {

    //TODO javadoc
    public boolean affectsEntity(@NotNull Entity entity);

    public default boolean shouldActivateOnAttack(@NotNull EntityDamageByEntityEvent entityDamageByEntityEvent){

        AbilityHolderTracker entityManager;

        Entity damager = entityDamageByEntityEvent.getDamager();
        Entity damaged = entityDamageByEntityEvent.getEntity();

        if(!affectsEntity(damaged)){
            return false;
        }


        return false;
    }
}
