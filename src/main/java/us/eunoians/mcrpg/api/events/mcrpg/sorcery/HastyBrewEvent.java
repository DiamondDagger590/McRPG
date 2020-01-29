package us.eunoians.mcrpg.api.events.mcrpg.sorcery;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.sorcery.HastyBrew;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

/**
 * This is called every time the potion brew duration needs to be calculated after a player has interacted with a brewing stand.
 * As such it may be called frequently for seemingly no reason for internal purposes.
 */
public class HastyBrewEvent extends AbilityActivateEvent{
  
  @Getter
  @Setter
  private double brewDurationBoost;
  
  public HastyBrewEvent(McRPGPlayer player, HastyBrew hastyBrew, double brewDurationBoost){
    super(hastyBrew, player, AbilityEventType.RECREATIONAL);
    this.brewDurationBoost = brewDurationBoost;
  }
}
