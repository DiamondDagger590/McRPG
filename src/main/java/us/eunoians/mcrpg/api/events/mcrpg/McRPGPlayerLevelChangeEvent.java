package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.skills.Skill;

public class McRPGPlayerLevelChangeEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private boolean isCancelled;

  @Getter
  private int previousLevel;
  @Getter
  private int nextLevel;
  @Getter
  private int amountOfLevelsIncreased;
  @Getter
  private Skill skillLeveled;

  public McRPGPlayerLevelChangeEvent(int previousLevel, int nextLevel, int amountOfLevelsIncreased, Skill skillLeveled){
    this.previousLevel = previousLevel;
    this.nextLevel = nextLevel;
    this.amountOfLevelsIncreased = amountOfLevelsIncreased;
    this.skillLeveled = skillLeveled;
    this.isCancelled = false;
  }

  public McRPGPlayer getMcMMOPlayer(){
    return skillLeveled.getPlayer();
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
