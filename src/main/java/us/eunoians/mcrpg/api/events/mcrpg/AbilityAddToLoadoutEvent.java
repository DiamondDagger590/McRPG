package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class AbilityAddToLoadoutEvent extends PlayerModifiedEvent {

  @Getter
  private BaseAbility abilityToAdd;

  public AbilityAddToLoadoutEvent(McRPGPlayer player, BaseAbility abilityToAdd) {
    super(player);
    this.abilityToAdd = abilityToAdd;
  }
}