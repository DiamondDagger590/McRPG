package us.eunoians.mcrpg.types;

import lombok.Getter;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.skills.*;
import us.eunoians.mcrpg.util.Parser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static us.eunoians.mcrpg.types.DefaultAbilities.*;
import static us.eunoians.mcrpg.types.UnlockedAbilities.*;

/*
An enum that stores a type of every skill
 */
public enum Skills {
  ARCHERY("Archery", Archery.class, DAZE, COMBO, PUNCTURE, TIPPED_ARROWS, BLESSING_OF_APOLLO, BLESSING_OF_ARTEMIS, CURSE_OF_HADES),
  AXES("Axes", Axes.class, SHRED, HEAVY_STRIKE, BLOOD_FRENZY, SHARPER_AXE, WHIRLWIND_STRIKE, ARES_BLESSING, CRIPPLING_BLOW),
  EXCAVATION("Excavation", Excavation.class, EXTRACTION, BURIED_TREASURE, LARGER_SPADE, MANA_DEPOSIT, HAND_DIGGING, FRENZY_DIG, PANS_SHRINE),
  FISHING("Fishing", Fishing.class, GREAT_ROD, POSEIDONS_FAVOR, MAGIC_TOUCH, SEA_GODS_BLESSING, SUNKEN_ARMORY, SHAKE, SUPER_ROD),
  FITNESS("Fitness", Fitness.class, ROLL, THICK_SKIN, BULLET_PROOF, DODGE, IRON_MUSCLES, RUNNERS_DIET, DIVINE_ESCAPE),
  HERBALISM("Herbalism", Herbalism.class, TOO_MANY_PLANTS, REPLANTING, FARMERS_DIET, DIAMOND_FLOWERS, MASS_HARVEST, PANS_BLESSING, NATURES_WRATH),
  MINING("Mining", Mining.class, DOUBLE_DROP, RICHER_ORES, ITS_A_TRIPLE, REMOTE_TRANSFER, SUPER_BREAKER, BLAST_MINING, ORE_SCANNER),
  SORCERY("Sorcery", Sorcery.class, HASTY_BREW, CIRCES_RECIPES, POTION_AFFINITY, MANA_AFFINITY, CIRCES_PROTECTION, HADES_DOMAIN, CIRCES_SHRINE),
  SWORDS("Swords", Swords.class, BLEED, DEEPER_WOUND, BLEED_PLUS, VAMPIRE, SERRATED_STRIKES, RAGE_SPIKE, TAINTED_BLADE),
  TAMING("Taming", Taming.class, GORE, DIVINE_FUR, SHARPENED_FANGS, LINKED_FANGS, COMRADERY, PETAS_WRATH, FURY_OF_CERBERUS),
  UNARMED("Unarmed", Unarmed.class, STICKY_FINGERS, TIGHTER_GRIP, DISARM, IRON_ARM, BERSERK, SMITING_FIST, DENSE_IMPACT),
  WOODCUTTING("Woodcutting", Woodcutting.class, EXTRA_LUMBER, HEAVY_SWING, NYMPHS_VITALITY, DRYADS_GIFT, HESPERIDES_APPLES, TEMPORAL_HARVEST, DEMETERS_SHRINE);

  @Getter
  private String name;

  @Getter
  private Class<? extends Skill> clazz;

  @Getter
  private DefaultAbilities defaultAbility;

  @Getter
  private UnlockedAbilities[] unlockedAbilities;

  Skills(String name, Class<? extends Skill> clazz, DefaultAbilities defaultAbility, UnlockedAbilities... unlockedAbilities){
    this.name = name;
    this.clazz = clazz;
    this.defaultAbility = defaultAbility;
    this.unlockedAbilities = unlockedAbilities;
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

  public List<GenericAbility> getAllAbilities() {
    return Stream.concat(Stream.of(defaultAbility), Arrays.stream(unlockedAbilities)).collect(Collectors.toList());
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

  public boolean usePerm(){
    FileManager.Files file = Arrays.stream(FileManager.Files.values()).filter(f -> f.getFileName().contains(name.toLowerCase())).findFirst().orElse(FileManager.Files.SWORDS_CONFIG);
    return McRPG.getInstance().getFileManager().getFile(file).getBoolean("RequirePermission", false);
  }

  public int getMaxLevel(){
    FileManager.Files file = Arrays.stream(FileManager.Files.values()).filter(f -> f.getFileName().contains(name.toLowerCase())).findFirst().orElse(FileManager.Files.SWORDS_CONFIG);
    return file.getFile().getInt("MaxLevel");
  }

}
