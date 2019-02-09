package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class AbilityActivateEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  protected boolean isCancelled;

  @Getter
  private BaseAbility ability;
  @Getter
  private McRPGPlayer user;
  @Getter
  private AbilityEventType eventType;

  /**
   *
   * @param ability Ability being activated
   * @param player The user
   */
  public AbilityActivateEvent(BaseAbility ability, McRPGPlayer player, AbilityEventType eventType){
    this.ability = ability;
    this.user = player;
    this.isCancelled = isCancelled();
    this.eventType = eventType;
  }


  @Override
  public boolean isCancelled(){
	return isCancelled;
  }

  @Override
  public void setCancelled(boolean isCancelled){
	this.isCancelled = isCancelled;
  }

  @Override
  public HandlerList getHandlers(){
	return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
