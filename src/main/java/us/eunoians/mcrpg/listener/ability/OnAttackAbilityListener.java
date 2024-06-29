package us.eunoians.mcrpg.listener.ability;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

/**
 * This listener allows for readying or activating abilities from
 * {@link EntityDamageByEntityEvent}.
 */
public class OnAttackAbilityListener implements AbilityListener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleOnAttackAbilities(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        UUID uuid = entityDamageByEntityEvent.getDamager().getUniqueId();
        activateAbilities(uuid, entityDamageByEntityEvent);
        readyAbilities(uuid, entityDamageByEntityEvent);
    }
}
