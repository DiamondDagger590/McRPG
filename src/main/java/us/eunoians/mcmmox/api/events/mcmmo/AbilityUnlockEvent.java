package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class AbilityUnlockEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private boolean isCancelled = false;
  @Getter
  private BaseAbility abilityToUnlock;

  @Getter
  private McMMOPlayer mcMMOPlayer;

  public AbilityUnlockEvent(McMMOPlayer player, BaseAbility abilityToUnlock){
	this.mcMMOPlayer = player;
	this.abilityToUnlock = abilityToUnlock;
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
