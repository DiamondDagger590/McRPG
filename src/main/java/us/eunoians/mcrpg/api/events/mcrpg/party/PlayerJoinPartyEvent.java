package us.eunoians.mcrpg.api.events.mcrpg.party;

import lombok.Getter;
import us.eunoians.mcrpg.party.Party;

import java.util.UUID;

public class PlayerJoinPartyEvent extends PartyEvent{
  
  @Getter
  private UUID playerJoined;
  
  public PlayerJoinPartyEvent(UUID playerJoined, Party party){
    super(party);
    this.playerJoined = playerJoined;
  }
}
