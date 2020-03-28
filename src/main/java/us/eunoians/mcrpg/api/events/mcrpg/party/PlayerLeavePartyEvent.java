package us.eunoians.mcrpg.api.events.mcrpg.party;

import lombok.Getter;
import us.eunoians.mcrpg.party.Party;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class PlayerLeavePartyEvent extends PartyEvent{
  
  @Getter
  private McRPGPlayer player;
  
  public PlayerLeavePartyEvent(McRPGPlayer mcRPGPlayer, Party party){
    super(party);
    this.player = mcRPGPlayer;
  }
}
