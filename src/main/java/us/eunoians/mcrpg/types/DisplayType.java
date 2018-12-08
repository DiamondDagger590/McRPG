package us.eunoians.mcrpg.types;

import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum DisplayType {
  BOSS_BAR("BossBar"),
  ACTION_BAR("ActionBar"),
  SCOREBOARD("Scoreboard");

  @Getter
  private String name;

  DisplayType(String name){
    this.name = name;
  }

  public static DisplayType fromString(String dType){
	return Arrays.stream(DisplayType.values()).filter(type -> type.getName().equalsIgnoreCase(dType)).findFirst().orElse(DisplayType.BOSS_BAR);
  }

  public static boolean isDisplayType(String dType){
    return Arrays.stream(DisplayType.values()).map(type -> type.getName()).collect(Collectors.toList()).contains(dType);
  }
}
