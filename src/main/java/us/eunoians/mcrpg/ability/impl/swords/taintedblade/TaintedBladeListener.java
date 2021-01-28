package us.eunoians.mcrpg.ability.impl.swords.taintedblade;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.player.McRPGPlayer;

/**
 * Handles activation of {@link TaintedBlade}
 *
 * @author DiamondDagger590
 */
public class TaintedBladeListener implements Listener {

    private static final NamespacedKey TAINTED_BLADE_KEY = McRPG.getNamespacedKey("tainted_blade");

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleDamage(EntityDamageByEntityEvent entityDamageByEntityEvent){
        if(entityDamageByEntityEvent.getDamager() instanceof LivingEntity && entityDamageByEntityEvent.getEntity() instanceof LivingEntity &&
                AbilityHolder.isAbilityHolder((LivingEntity) entityDamageByEntityEvent.getDamager())) {

            LivingEntity damager = (LivingEntity) entityDamageByEntityEvent.getDamager();
            AbilityHolder abilityHolder = damager instanceof Player ? new AbilityHolder((Player) damager) : AbilityHolder.getFromEntity(damager);

            if(abilityHolder instanceof McRPGPlayer ? abilityHolder.getAbilityFromLoadout(TAINTED_BLADE_KEY) != null : abilityHolder.hasAbility(TAINTED_BLADE_KEY)) {

                TaintedBlade taintedBlade = (TaintedBlade) abilityHolder.getAbility(TAINTED_BLADE_KEY);

                if (taintedBlade.isToggled() && taintedBlade.isReady()) {
                    taintedBlade.activate(abilityHolder, entityDamageByEntityEvent);
                }
            }
        }
    }
}
