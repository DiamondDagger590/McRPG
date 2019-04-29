package us.eunoians.mcrpg.types;

import lombok.Getter;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.util.Parser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/*
An enum that stores a type of every skill
 */
public enum Skills {
  ARCHERY("Archery", DefaultAbilities.DAZE),
  HERBALISM("Herbalism", DefaultAbilities.TOO_MANY_PLANTS),
  MINING("Mining", DefaultAbilities.DOUBLE_DROP),
  SWORDS("Swords", DefaultAbilities.BLEED),
  UNARMED("Unarmed", DefaultAbilities.STICKY_FINGERS),
  WOODCUTTING("Woodcutting", DefaultAbilities.EXTRA_LUMBER),
  FITNESS("Fitness", DefaultAbilities.ROLL);

  @Getter
  private String name;

  @Getter
  private DefaultAbilities defaultAbility;

  Skills(String name, DefaultAbilities defaultAbility){
	this.name = name;
	this.defaultAbility = defaultAbility;
  }

  public Parser getExpEquation(){
	FileManager.Files file = Arrays.stream(FileManager.Files.values()).filter(f -> f.getFileName().contains(name.toLowerCase())).findFirst().orElse(FileManager.Files.SWORDS_CONFIG);
	return new Parser(McRPG.getInstance().getFileManager().getFile(file).getString("ExpEquation"));
  }

  public String getDisplayName(){
      return McRPG.getInstance().getLangFile().getString("SkillNames." + this.name);
  }

  public boolean isEnabled(){
	FileManager.Files file = Arrays.stream(FileManager.Files.values()).filter(f -> f.getFileName().contains(name.toLowerCase())).findFirst().orElse(FileManager.Files.SWORDS_CONFIG);
	return McRPG.getInstance().getFileManager().getFile(file).getBoolean(name + "Enabled");
  }

  public List<String> getEnabledAbilities(){
	FileManager.Files file = Arrays.stream(FileManager.Files.values()).filter(f -> f.getFileName().contains(name.toLowerCase())).findFirst().orElse(FileManager.Files.SWORDS_CONFIG);
	return McRPG.getInstance().getFileManager().getFile(file).getConfigurationSection("EnabledAbilities").getKeys(false)
		.stream().filter(ability -> file.getFile().getBoolean("EnabledAbilities." + ability)).collect(Collectors.toList());
  }

  public static Skills fromString(String skill){
	return Arrays.stream(Skills.values()).filter(type -> type.getDisplayName().equalsIgnoreCase(skill) || type.getName().equalsIgnoreCase(skill)).findAny().orElse(null);
  }

  public static boolean isSkill(String skill){
	return Arrays.stream(Skills.values()).map(type -> type.getName().toLowerCase()).collect(Collectors.toList()).contains(skill.toLowerCase()) || Arrays.stream(Skills.values()).map(type -> type.getDisplayName().toLowerCase()).collect(Collectors.toList()).contains(skill.toLowerCase());
  }

}
