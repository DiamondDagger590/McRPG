package us.eunoians.mcrpg.listener.ability;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import us.eunoians.mcrpg.event.ability.swords.BleedActivateEvent;

/**
 * This listener handles activating any abilities that rely on {@link BleedActivateEvent}
 * for activation
 */
public class OnBleedActivateListener implements AbilityListener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleBleedActivate(BleedActivateEvent bleedActivateEvent) {
        activateAbilities(bleedActivateEvent.getAbilityHolder().getUUID(), bleedActivateEvent);
    }
}
