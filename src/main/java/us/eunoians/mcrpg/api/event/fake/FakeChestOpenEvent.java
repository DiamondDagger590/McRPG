package us.eunoians.mcrpg.api.event.fake;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * This event is fired to validate if the player can open a chest or not.
 */
public class FakeChestOpenEvent extends PlayerInteractEvent {

    public FakeChestOpenEvent(Player player, Location location){
        super(player, Action.LEFT_CLICK_BLOCK, player.getItemOnCursor(), location.getBlock(), location.getBlock().getFace(location.add(1, 0, 0).getBlock()));
    }
}
