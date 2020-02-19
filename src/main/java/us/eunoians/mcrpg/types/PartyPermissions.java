package us.eunoians.mcrpg.types;

import lombok.Getter;

import java.util.Arrays;

public enum PartyPermissions{
  
  INVITE_PLAYERS(0),
  KICK_PLAYERS(1),
  PRIVATE_BANK(2),
  UPGRADE_PARTY(3),
  PVP(4);
  
  
  @Getter
  private int id;
  
  
  PartyPermissions(int id){
    this.id = id;
  }
  
  public static PartyPermissions getPartyPermission(int id){
    return Arrays.stream(values()).filter(perm -> perm.getId() == id).findFirst().orElse(null);
  }
}
