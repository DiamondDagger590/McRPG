package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class AbilityActivateEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  protected boolean isCancelled;

  @Getter
  private BaseAbility ability;
  @Getter
  private McMMOPlayer user;

  /**
   *
   * @param ability Ability being activated
   * @param player The user
   */
  public AbilityActivateEvent(BaseAbility ability, McMMOPlayer player){
    this.ability = ability;
    this.user = player;
    this.isCancelled = isCancelled();
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
