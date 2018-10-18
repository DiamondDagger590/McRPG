package us.eunoians.mcmmox.api.events.mcmmo;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class FakeChestOpenEvent extends PlayerInteractEvent {

  public FakeChestOpenEvent(Player p, Location location){
    super(p, Action.LEFT_CLICK_BLOCK, p.getItemInHand(), location.getBlock(), location.getBlock().getFace(location.add(1, 0, 0).getBlock()));
  }

  @Override
  public HandlerList getHandlers(){
	return null;
  }
}
