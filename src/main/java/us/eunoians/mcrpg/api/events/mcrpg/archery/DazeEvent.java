package us.eunoians.mcrpg.api.events.mcrpg.archery;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.archery.Daze;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class DazeEvent extends AbilityActivateEvent{

  @Getter
  @Setter
  private boolean forcePlayerLookup;

  @Getter
  @Setter
  private int blindnessDuration;

  @Getter
  @Setter
  private int nauseaDuration;

  public DazeEvent(McRPGPlayer mcRPGPlayer, Daze daze, boolean forcePlayerLookup, int blindnessDuration, int nauseaDuration){
    super(daze, mcRPGPlayer, AbilityEventType.COMBAT);
    this.forcePlayerLookup = forcePlayerLookup;
    this.blindnessDuration = blindnessDuration;
    this.nauseaDuration = nauseaDuration;
  }
}
