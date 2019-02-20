package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class AbilityUnlockEvent extends PlayerModifiedEvent {

  @Getter
  private BaseAbility abilityToUnlock;

  public AbilityUnlockEvent(McRPGPlayer player, BaseAbility abilityToUnlock){
    super(player);
	this.abilityToUnlock = abilityToUnlock;
  }
}