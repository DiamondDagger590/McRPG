package us.eunoians.mcrpg.types;

import lombok.Getter;

import java.util.Arrays;

public enum PartyPermissions{
  
  INVITE_PLAYERS(0, "Invite Players"),
  KICK_PLAYERS(1, "Kick Players"),
  PRIVATE_BANK(2, "Private Bank"),
  UPGRADE_PARTY(3, "Upgrade Party"),
  PVP(4, "Pvp");
  
  @Getter
  private int id;
  
  @Getter
  private String name;
  
  
  PartyPermissions(int id, String name){
    this.id = id;
    this.name = name;
  }
  
  public static PartyPermissions getPartyPermission(int id){
    return Arrays.stream(values()).filter(perm -> perm.getId() == id).findFirst().orElse(null);
  }
  
  public static PartyPermissions getPartyPermission(String name){
    return Arrays.stream(values()).filter(perm -> perm.getName().replace(" ", "").equalsIgnoreCase(name)).findFirst().orElse(null);
  }
}
