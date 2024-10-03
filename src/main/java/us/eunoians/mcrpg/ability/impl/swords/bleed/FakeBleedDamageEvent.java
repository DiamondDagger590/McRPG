package us.eunoians.mcrpg.ability.impl.swords.bleed;

import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This event is used to check bleed damage since it is a DOT and we want to respect plugins that have safe zones
 */
public class FakeBleedDamageEvent extends EntityDamageEvent {

    public FakeBleedDamageEvent(@NotNull Entity damagee, @NotNull DamageCause cause, double damage) {
        super(damagee, cause, DamageSource.builder(DamageType.GENERIC).build(), damage);

        if (getDamage(DamageModifier.ARMOR) > 0) { //Will throw a fit for things like zombies if not ><
            setDamage(DamageModifier.ARMOR, 0);
        }
    }
}
