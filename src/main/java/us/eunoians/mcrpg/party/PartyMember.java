package us.eunoians.mcrpg.party;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.types.PartyRoles;

import java.util.Calendar;
import java.util.UUID;

public class PartyMember{
  
  @Getter
  private UUID uuid;
  
  @Getter
  @Setter
  private PartyRoles partyRole;
  
  @Getter
  private long joinDate;
  
  public PartyMember(UUID uuid, PartyRoles partyRole, long joinDate){
    this.uuid = uuid;
    this.partyRole = partyRole;
    this.joinDate = joinDate;
  }
  
  public PartyMember(UUID uuid, int partyRole, long joinDate){
    this.uuid = uuid;
    this.partyRole = PartyRoles.getRoleFromId(partyRole);
    this.joinDate = joinDate;
  }
  
  public PartyMember(UUID uuid){
    this.uuid = uuid;
    this.partyRole = PartyRoles.MEMBER;
    this.joinDate = Calendar.getInstance().getTimeInMillis();
  }
}
