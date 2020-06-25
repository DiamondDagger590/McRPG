package us.eunoians.mcrpg.api.util.blood;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BloodManager{
  
  @Getter
  private static BloodManager instance;
  
  private McRPG mcRPG;
  @Getter
  private double normalSpawnChance;
  @Getter
  private double bleedingSpawnChance;
  @Getter
  private boolean ignoreNaturalMobs;
  
  private Map<BloodType, Double> spawnChances = new HashMap<>();
  
  public BloodManager(McRPG mcRPG){
    this.mcRPG = mcRPG;
    init();
    instance = this;
  }
  
  private void init(){
    FileConfiguration bloodFile = mcRPG.getFileManager().getFile(FileManager.Files.BLOOD_FILE);
    if(bloodFile.getBoolean("CrystallizedBlood.Enabled")){
      normalSpawnChance = bloodFile.getDouble("BrewingArtifacts.ChanceOfSpawning");
      spawnChances.put("BrewingArtifacts", chanceOfSpawning);
    }
  }
  
  public void reload(){
    spawnChances.clear();
    init();
  }
  
  public enum BloodType{
    TOOL("Tool"),
    WEAPON("Weapon"),
    ARMOR("Armor"),
    CURSE("Curse");
    
    @Getter
    private String id;
    
    BloodType(String id){
      this.id = id;
    }
    
    public static BloodType getFromID(String id){
      return Arrays.stream(values()).filter(bloodType -> bloodType.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }
  }
  
  
}
