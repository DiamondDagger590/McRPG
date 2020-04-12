package us.eunoians.mcrpg.api.events.mcrpg.party;

import us.eunoians.mcrpg.party.Party;

public class PartyCreateEvent extends PartyEvent{
  
  public PartyCreateEvent(Party party){
    super(party);
  }
}
