package us.eunoians.mcrpg.types;

import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum LevelCommandType{
  ARCHERY("Archery"),
  AXES("Axes"),
  EXCAVATION("Excavation"),
  FISHING("Fishing"),
  FITNESS("Fitness"),
  HERBALISM("Herbalism"),
  MINING("Mining"),
  POWER("Power"),
  SORCERY("Sorcery"),
  SWORDS("Swords"),
  UNARMED("Unarmed"),
  WOODCUTTING("Woodcutting"),
  TAMING("Taming");
  
  @Getter
  private final String name;
  
  LevelCommandType(String name){
    this.name = name;
  }
  
  public static LevelCommandType fromString(String commandType){
    return Arrays.stream(LevelCommandType.values()).filter(type -> type.getName().equalsIgnoreCase(commandType)).findAny().orElse(null);
  }
  
  public static boolean isCommandType(String commandType){
    return Arrays.stream(LevelCommandType.values()).map(type -> type.getName().toLowerCase()).collect(Collectors.toList()).contains(commandType.toLowerCase());
  }
}
