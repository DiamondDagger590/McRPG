package us.eunoians.mcrpg.api.events.mcrpg.party;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.party.Party;

public class PartyLevelUpEvent extends PartyEvent{
  
  @Getter
  private int previousLevel;
  
  @Getter
  @Setter
  private int newLevel;
  
  public PartyLevelUpEvent(Party party, int levelPrevious, int newLevel){
    super(party);
    this.previousLevel = levelPrevious;
    this.newLevel = newLevel;
  }
}
