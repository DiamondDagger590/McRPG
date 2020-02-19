package us.eunoians.mcrpg.types;

import lombok.Getter;

import java.util.Arrays;

public enum PartyRoles{
  
  OWNER(0),
  MOD(1),
  MEMBER(2);
  
  @Getter
  private int id;
  
  PartyRoles(int id){
    this.id = id;
  }
  
  public static PartyRoles getRoleFromId(int id){
    return Arrays.stream(values()).filter(role -> role.getId() == id).findFirst().orElse(null);
  }
}
