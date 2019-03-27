package us.eunoians.mcrpg.types;

import lombok.Getter;

public enum RedeemType {
  EXP("Exp"),
  LEVEL("Level");

  @Getter
  String name;

  RedeemType(String name){
    this.name = name;
  }
}
