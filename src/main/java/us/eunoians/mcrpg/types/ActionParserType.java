package us.eunoians.mcrpg.types;

import lombok.Getter;

public enum  ActionParserType {

  ATTACK("Attack"),
  BREAK("Break"),
  ABILITY_ACTIVATE("Ability Activate"),
  EXP_GAIN("Exp Gain");

  @Getter
  private String name;

  ActionParserType(String name){
    this.name = name;
  }

  public static ActionParserType fromString(String type){
    for(ActionParserType t : ActionParserType.values()){
      if(t.getName().equals(type)){
        return t;
      }
    }
    return null;
  }
}
