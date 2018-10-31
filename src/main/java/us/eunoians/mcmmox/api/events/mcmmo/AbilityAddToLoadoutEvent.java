package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class AbilityAddToLoadoutEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private boolean isCancelled = false;
  @Getter
  private BaseAbility abilityToAdd;

  @Getter
  private McMMOPlayer mcMMOPlayer;

  public AbilityAddToLoadoutEvent(McMMOPlayer player, BaseAbility abilityToAdd){
	this.mcMMOPlayer = player;
	this.abilityToAdd = abilityToAdd;
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
