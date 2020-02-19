package us.eunoians.mcrpg.party;

import lombok.Getter;

import java.util.UUID;

public class PartyInvite{
  
  @Getter
  private UUID partyID;
  
  @Getter
  private UUID targetPlayer;
  
  public PartyInvite(UUID partyID, UUID targetPlayer){
    this.partyID = partyID;
    this.targetPlayer = targetPlayer;
  }
}
