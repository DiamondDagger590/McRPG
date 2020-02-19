package us.eunoians.mcrpg.party;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.types.PartyPermissions;
import us.eunoians.mcrpg.types.PartyRoles;

import java.util.*;

public class Party{
  
  private Map<PartyPermissions, PartyRoles> partyPermissions = new HashMap<>();
  private Map<UUID, PartyMember> partyMembers = new HashMap<>();
  @Getter
  @Setter
  private String name;
  
  @Getter
  private UUID partyID;
  
  public Party(UUID uuid, String partyName){
    partyID = UUID.randomUUID();
    PartyMember owner = new PartyMember(uuid);
    owner.setPartyRole(PartyRoles.OWNER);
    partyMembers.put(uuid, owner);
    initPerms();
  }
  
  public Party(Map<UUID, PartyMember> partyMembers, Map<PartyPermissions, PartyRoles> partyPermissions, String partyName, UUID partyID){
    this.partyMembers = partyMembers;
    this.partyPermissions = partyPermissions;
    this.name = partyName;
    this.partyID = partyID;
  }
  
  
  private void initPerms(){
    partyPermissions.put(PartyPermissions.UPGRADE_PARTY, PartyRoles.OWNER);
    partyPermissions.put(PartyPermissions.PVP, PartyRoles.MEMBER);
    partyPermissions.put(PartyPermissions.PRIVATE_BANK, PartyRoles.MOD);
    partyPermissions.put(PartyPermissions.KICK_PLAYERS, PartyRoles.MOD);
    partyPermissions.put(PartyPermissions.INVITE_PLAYERS, PartyRoles.MOD);
  }
}
