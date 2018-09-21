package us.eunoians.mcmmox.types;

import lombok.Getter;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.util.Parser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    FileManager.Files file  = Arrays.stream(FileManager.Files.values()).filter(f -> f.getFileName().contains(name)).findFirst().orElse(FileManager.Files.SWORDS_CONFIG);
    return new Parser(Mcmmox.getInstance().getFileManager().getFile(file).getString("ExpEquation"));
  }

  public boolean isEnabled(){
    FileManager.Files file  = Arrays.stream(FileManager.Files.values()).filter(f -> f.getFileName().contains(name)).findFirst().orElse(FileManager.Files.SWORDS_CONFIG);
    return Mcmmox.getInstance().getFileManager().getFile(file).getBoolean(name + "Enabled");
  }

  public List<String> getEnabledAbilities(){
    FileManager.Files file  = Arrays.stream(FileManager.Files.values()).filter(f -> f.getFileName().contains(name)).findFirst().orElse(FileManager.Files.SWORDS_CONFIG);
    return Mcmmox.getInstance().getFileManager().getFile(file).getConfigurationSection("EnabledAbilities").getKeys(false)
        .stream().filter(ability -> file.getFile().getBoolean("EnabledAbilities." + ability)).collect(Collectors.toList());
  }

  public static Skills fromString(String skill){
    return Arrays.stream(Skills.values()).filter(type -> type.getName().equalsIgnoreCase(skill)).findFirst().orElse(SWORDS);
  }

  public static boolean isSkill(String skill){
    return Arrays.stream(Skills.values()).map(type -> type.getName()).collect(Collectors.toList()).contains(skill);
  }

}
