package us.eunoians.mcrpg.api.events.mcrpg.sorcery;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.sorcery.ManaAffinity;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class ManaAffinityEvent extends AbilityActivateEvent {
  
  @Getter
  @Setter
  private double discoveryChanceIncrease;
  
  public ManaAffinityEvent(McRPGPlayer player, ManaAffinity manaAffinity, double discoveryChanceIncrease){
    super(manaAffinity, player, AbilityEventType.RECREATIONAL);
    this.discoveryChanceIncrease = discoveryChanceIncrease;
  }
}
