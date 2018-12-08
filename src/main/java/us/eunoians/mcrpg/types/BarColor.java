package us.eunoians.mcrpg.types;

import lombok.Getter;

import java.util.Arrays;

/**
 * Enum used for the player display settings
 */
public enum BarColor {
  PINK("Pink", org.bukkit.boss.BarColor.PINK),
  BLUE("Blue", org.bukkit.boss.BarColor.BLUE),
  RED("Red", org.bukkit.boss.BarColor.RED),
  GREEN("Green", org.bukkit.boss.BarColor.GREEN),
  YELLOW("Yellow", org.bukkit.boss.BarColor.YELLOW),
  PURPLE("Purple", org.bukkit.boss.BarColor.PURPLE),
  WHITE("White", org.bukkit.boss.BarColor.WHITE);


  @Getter
  String name;

  @Getter
  org.bukkit.boss.BarColor colour;

  BarColor(String name, org.bukkit.boss.BarColor colour) {
    this.name = name;
    this.colour = colour;
  }

  public static org.bukkit.boss.BarColor fromString(String color){
	return Arrays.stream(values()).filter(colour -> colour.getName().equalsIgnoreCase(color)).findFirst().orElse(BarColor.BLUE).getColour();
  }
}
