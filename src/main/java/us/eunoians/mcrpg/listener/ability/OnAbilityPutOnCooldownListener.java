package us.eunoians.mcrpg.listener.ability;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.api.event.ability.AbilityPutOnCooldownEvent;

/**
 * This listener automatically starts the cooldown expire timer whenever
 * an {@link us.eunoians.mcrpg.entity.holder.AbilityHolder} gets put on cooldown.
 */
public class OnAbilityPutOnCooldownListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAbilityPutOnCooldown(AbilityPutOnCooldownEvent event) {
        event.getAbilityHolder().startCooldownExpireNotificationTimer(event.getAbility(), event.getCooldown());
    }
}
