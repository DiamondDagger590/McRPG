package us.eunoians.mcrpg.types;

import lombok.Getter;

/*
Simply a way to track whether an ability is active or passive
 */
public enum AbilityType {
  ACTIVE("Active"),
  PASSIVE("Passive");

  @Getter
  private String name;

  AbilityType(String name) {
    this.name = name;
  }
}

