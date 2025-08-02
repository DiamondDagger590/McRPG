package us.eunoians.mcrpg.listener.ability;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import us.eunoians.mcrpg.event.ability.mining.ExtraOreActivateEvent;

import java.util.UUID;

/**
 * This listener handles activating any abilities that rely on the {@link ExtraOreActivateEvent}.
 */
public class OnExtraOreActivateListener implements AbilityListener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleExtraOreActivate(ExtraOreActivateEvent extraOreActivateEvent) {
        UUID uuid = extraOreActivateEvent.getAbilityHolder().getUUID();
        activateAbilities(uuid, extraOreActivateEvent);
    }
}
