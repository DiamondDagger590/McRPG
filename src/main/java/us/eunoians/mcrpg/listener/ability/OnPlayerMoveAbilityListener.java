package us.eunoians.mcrpg.listener.ability;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

public class OnPlayerMoveAbilityListener implements AbilityListener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleMove(PlayerMoveEvent playerMoveEvent) {
        activateAbilities(playerMoveEvent.getPlayer().getUniqueId(), playerMoveEvent);
    }
}
