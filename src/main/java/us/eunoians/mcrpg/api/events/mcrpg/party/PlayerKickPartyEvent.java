package us.eunoians.mcrpg.api.events.mcrpg.party;

import lombok.Getter;
import us.eunoians.mcrpg.party.Party;

import java.util.UUID;

public class PlayerKickPartyEvent extends PartyEvent{
  
  @Getter
  private UUID kickPlayerUUID;
  
  
  public PlayerKickPartyEvent(UUID uuid, Party party){
    super(party);
    this.kickPlayerUUID = uuid;
  }
}
