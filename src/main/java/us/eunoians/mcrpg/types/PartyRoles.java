package us.eunoians.mcrpg.types;

import lombok.Getter;

import java.util.Arrays;

public enum PartyRoles{
  
  OWNER(0, "Owner"),
  MOD(1, "Mod"),
  MEMBER(2, "Member");
  
  @Getter
  private int id;
  
  @Getter
  private String name;
  
  PartyRoles(int id, String name){
    this.id = id;
    this.name = name;
  }
  
  public static PartyRoles getRoleFromId(int id){
    return Arrays.stream(values()).filter(role -> role.getId() == id).findFirst().orElse(null);
  }
  
  public static PartyRoles getRoleFromName(String name){
    return Arrays.stream(values()).filter(role -> role.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
  }
}
