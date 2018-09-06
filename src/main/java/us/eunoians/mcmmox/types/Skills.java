package us.eunoians.mcmmox.types;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.util.Parser;

import java.io.File;
import java.util.Arrays;

/*
An enum that stores a type of every skill
 */
public enum Skills {
  SWORDS("Swords"),
  MINING("Mining"),
  AXES("Axes"),
  ARCHERY("Archery"),
  REPAIR("Repair");

  @Getter
  private String name;

  Skills(String name){
	this.name = name;
  }

  public Parser getExpEquation(){
    FileManager.Files file = Arrays.stream(FileManager.Files.values()).filter(f -> f.getFileName().contains(name)).findFirst().orElse(FileManager.Files.SWORDS_CONFIG);
    return new Parser(file.getFile().getString("ExpEquation"));
  }

}
