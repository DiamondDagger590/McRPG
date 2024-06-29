package us.eunoians.mcrpg.listener.ability;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.UUID;

/**
 * This listener handles activating/readying abilities from {@link PlayerToggleSneakEvent}s
 */
public class OnSneakAbilityListener implements AbilityListener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent playerToggleSneakEvent) {
        UUID uuid = playerToggleSneakEvent.getPlayer().getUniqueId();
        activateAbilities(uuid, playerToggleSneakEvent);
        readyAbilities(uuid, playerToggleSneakEvent);
    }
}
