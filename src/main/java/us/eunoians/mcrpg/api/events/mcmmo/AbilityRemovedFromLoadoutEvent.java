package us.eunoians.mcrpg.api.events.mcmmo;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.players.McMMOPlayer;

public class AbilityRemovedFromLoadoutEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private boolean isCancelled = false;

  @Getter
  private BaseAbility abilityToRemove;

  @Getter
  private McMMOPlayer mcMMOPlayer;

  public AbilityRemovedFromLoadoutEvent(McMMOPlayer player, BaseAbility abilityToRemove){
	this.mcMMOPlayer = player;
	this.abilityToRemove = abilityToRemove;
  }

  @Override
  public boolean isCancelled(){
	return isCancelled;
  }

  @Override
  public void setCancelled(boolean cancelled){
	isCancelled = cancelled;
  }

  @Override
  public HandlerList getHandlers(){
	return handlers;
  }
  public static HandlerList getHandlerList() {
	return handlers;
  }


}
