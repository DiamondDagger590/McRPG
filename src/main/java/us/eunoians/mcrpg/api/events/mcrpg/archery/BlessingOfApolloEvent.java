package us.eunoians.mcrpg.api.events.mcrpg.archery;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.archery.BlessingOfApollo;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class BlessingOfApolloEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int fireResDuration;

  @Getter
  @Setter
  private int igniteDuration;

  @Getter
  @Setter
  private int cooldown;

  public BlessingOfApolloEvent(McRPGPlayer mcRPGPlayer, BlessingOfApollo blessingOfApollo, int cooldown, int fireResDuration, int igniteDuration){
    super(blessingOfApollo, mcRPGPlayer, AbilityEventType.COMBAT);
    this.cooldown = cooldown;
    this.fireResDuration = fireResDuration;
    this.igniteDuration = igniteDuration;
  }
}
