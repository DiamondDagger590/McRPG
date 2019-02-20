package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class AbilityActivateEvent extends PlayerModifiedEvent {

  @Getter
  private BaseAbility ability;
  @Getter
  private AbilityEventType eventType;

  /**
   *
   * @param ability Ability being activated
   * @param player The user
   */
  public AbilityActivateEvent(BaseAbility ability, McRPGPlayer player, AbilityEventType eventType){
    super(player);
    this.ability = ability;
    this.eventType = eventType;
  }
}
