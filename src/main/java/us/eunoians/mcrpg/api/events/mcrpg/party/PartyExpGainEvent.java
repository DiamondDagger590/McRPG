package us.eunoians.mcrpg.api.events.mcrpg.party;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.party.Party;

public class PartyExpGainEvent extends PartyEvent{
  
  @Getter
  @Setter
  private int expGained;
  
  public PartyExpGainEvent(Party party, int expGained){
    super(party);
    this.expGained = expGained;
  }
}
