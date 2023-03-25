package us.eunoians.mcrpg.ability.component;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

//TODO javadoc
public interface OnAttackedAbility extends DamageableAbility {

    public boolean affectedByEntity(@NotNull Entity entity);

    public void onEntityAttacked(@NotNull EntityDamageByEntityEvent entityDamageByEntityEvent);
}
