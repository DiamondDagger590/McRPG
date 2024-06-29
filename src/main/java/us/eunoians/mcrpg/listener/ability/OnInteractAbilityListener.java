package us.eunoians.mcrpg.listener.ability;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

/**
 * This listener handles activating/readying abilities from {@link PlayerInteractEvent}s and
 * {@link PlayerInteractEntityEvent}s.
 */
public class OnInteractAbilityListener implements AbilityListener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleOnInteract(PlayerInteractEvent playerInteractEvent) {
        UUID uuid = playerInteractEvent.getPlayer().getUniqueId();
        activateAbilities(uuid, playerInteractEvent);
        readyAbilities(uuid, playerInteractEvent);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleOnInteractEntity(PlayerInteractEntityEvent playerInteractEvent) {
        UUID uuid = playerInteractEvent.getPlayer().getUniqueId();
        activateAbilities(uuid, playerInteractEvent);
        readyAbilities(uuid, playerInteractEvent);
    }
}
