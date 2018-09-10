package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.eunoians.mcmmox.Abilities.BaseAbility;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class AbilityActivateEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  protected boolean isCancelled;

  @Getter
  private BaseAbility ability;
  @Getter
  private McMMOPlayer player;

  public AbilityActivateEvent(BaseAbility ability, McMMOPlayer player){
    this.ability = ability;
    this.player = player;
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
