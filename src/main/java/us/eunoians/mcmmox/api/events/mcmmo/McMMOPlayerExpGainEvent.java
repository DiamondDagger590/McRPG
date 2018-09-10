package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.skills.Skill;
import us.eunoians.mcmmox.types.GainReason;

public class McMMOPlayerExpGainEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private boolean isCancelled;

  @Getter
  private int expGained;
  @Getter
  private Skill skillGained;
  @Getter
  private GainReason gainType;

  public McMMOPlayerExpGainEvent(int expGained, Skill skillGained, GainReason gainType){
    this.expGained = expGained;
    this.skillGained = skillGained;
    this.gainType = gainType;
    this.isCancelled = false;
  }

  public McMMOPlayer getMcMMOPlayer(){
    return skillGained.getPlayer();
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
