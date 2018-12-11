package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class AbilityUnlockEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private boolean isCancelled = false;
  @Getter
  private BaseAbility abilityToUnlock;

  @Getter
  private McRPGPlayer mcRPGPlayer;

  public AbilityUnlockEvent(McRPGPlayer player, BaseAbility abilityToUnlock){
	this.mcRPGPlayer = player;
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
