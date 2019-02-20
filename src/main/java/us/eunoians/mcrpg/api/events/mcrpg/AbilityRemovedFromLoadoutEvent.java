package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class AbilityRemovedFromLoadoutEvent extends PlayerModifiedEvent {

  @Getter
  private BaseAbility abilityToRemove;

  public AbilityRemovedFromLoadoutEvent(McRPGPlayer player, BaseAbility abilityToRemove){
	super(player);
	this.abilityToRemove = abilityToRemove;
  }
}