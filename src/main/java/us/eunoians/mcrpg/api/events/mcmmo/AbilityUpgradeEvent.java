package us.eunoians.mcrpg.api.events.mcmmo;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.players.McMMOPlayer;

public class AbilityUpgradeEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();

  private boolean isCancelled = false;

  @Getter
  private BaseAbility abilityUpgrading;

  @Getter
  private int currentTier;

  @Getter
  private int nextTier;

  @Getter
  private McMMOPlayer mcMMOPlayer;

  public AbilityUpgradeEvent(McMMOPlayer player, BaseAbility abilityUpgrading, int currentTier, int nextTier){
	this.mcMMOPlayer = player;
	this.abilityUpgrading = abilityUpgrading;
	this.currentTier = currentTier;
	this.nextTier = nextTier;
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
