package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import us.eunoians.mcrpg.players.McRPGPlayer;

public abstract class PlayerModifiedEvent extends McRPGEvent {

  @Getter
  private McRPGPlayer mcRPGPlayer;

  public PlayerModifiedEvent(McRPGPlayer mcRPGPlayer){
    this.mcRPGPlayer = mcRPGPlayer;
  }
}
