package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class AbilityUpgradeEvent extends PlayerModifiedEvent {

  @Getter
  private BaseAbility abilityUpgrading;

  @Getter
  private int currentTier;

  @Getter
  private int nextTier;

  public AbilityUpgradeEvent(McRPGPlayer player, BaseAbility abilityUpgrading, int currentTier, int nextTier){
	super(player);
	this.abilityUpgrading = abilityUpgrading;
	this.currentTier = currentTier;
	this.nextTier = nextTier;
  }
}