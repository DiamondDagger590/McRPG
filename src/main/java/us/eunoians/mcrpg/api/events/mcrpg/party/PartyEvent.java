package us.eunoians.mcrpg.api.events.mcrpg.party;

import lombok.Getter;
import us.eunoians.mcrpg.api.events.mcrpg.McRPGEvent;
import us.eunoians.mcrpg.party.Party;

public abstract class PartyEvent extends McRPGEvent{
  
  @Getter
  private Party party;
  
  public PartyEvent(Party party){
    this.party = party;
  }
}
