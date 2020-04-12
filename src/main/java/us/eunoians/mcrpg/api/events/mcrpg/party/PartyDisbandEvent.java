package us.eunoians.mcrpg.api.events.mcrpg.party;

import us.eunoians.mcrpg.party.Party;

public class PartyDisbandEvent extends PartyEvent{
  
  public PartyDisbandEvent(Party party){
    super(party);
  }
}
