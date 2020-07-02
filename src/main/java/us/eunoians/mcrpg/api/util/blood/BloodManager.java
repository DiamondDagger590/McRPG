package us.eunoians.mcrpg.api.util.blood;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class BloodManager{
  
  @Getter
  private static BloodManager instance;
  
  private McRPG mcRPG;
  
  @Getter
  private boolean enabled;
  @Getter
  private double normalSpawnChance;
  @Getter
  private double bleedingSpawnChance;
  @Getter
  private boolean ignoreNaturalMobs;
  
  private Map<BloodType, Double> spawnChances = new HashMap<>();
  private Map<BloodType, BloodWrapper> bloodEffects = new HashMap<>();
  private Set<UUID> playersUnderCurse = new HashSet<>();
  
  public BloodManager(McRPG mcRPG){
    this.mcRPG = mcRPG;
    init();
    instance = this;
  }
  
  private void init(){
    FileConfiguration bloodFile = mcRPG.getFileManager().getFile(FileManager.Files.BLOOD_FILE);
    enabled = bloodFile.getBoolean("CrystallizedBlood.Enabled");
    if(enabled){
      normalSpawnChance = bloodFile.getDouble("CrystallizedBlood.SpawnChances.Normal");
      bleedingSpawnChance = bloodFile.getDouble("CrystallizedBlood.SpawnChances.Bleeding");
      ignoreNaturalMobs = bloodFile.getBoolean("CrystallizedBlood.SpawnChances.IgnoreNonNaturalMobs");
      for(String bloodTypeValue : bloodFile.getConfigurationSection("CrystallizedBlood.Types").getKeys(false)){
        BloodType bloodType = BloodType.getFromID(bloodTypeValue);
        if(bloodType != null){
          
          int lowEnd = 0;
          int highEnd = 0;
          double shatterChance = 0d;
          
          String key = "CrystallizedBlood.Types." + bloodTypeValue + ".";
          
          spawnChances.put(bloodType, bloodFile.getDouble(key + "SpawnChance"));
          
          if(bloodFile.contains(key + "ExpMultiplierRange")){
            List<Integer> values = Arrays.stream(bloodFile.getString(key + "ExpMultiplierRange").split("-")).map(Integer::parseInt).collect(Collectors.toList());
            lowEnd = values.get(0);
            highEnd = values.size() > 1 ? values.get(1) : lowEnd;
          }
          if(bloodFile.contains(key + "ItemShatterChanceRange")){
            shatterChance = bloodFile.getDouble(key + "ItemShatterChanceRange");
          }
          
          if(bloodFile.contains(key + "Duration")){
            BloodWrapper bloodWrapper = new BloodWrapper(bloodFile.getInt(key + "Duration"));
            bloodEffects.put(bloodType, bloodWrapper);
            continue;
          }
          else{
            BloodWrapper bloodWrapper = new BloodWrapper(lowEnd, highEnd, shatterChance);
            bloodEffects.put(bloodType, bloodWrapper);
            continue;
          }
        }
      }
    }
  }
  
  public void reload(){
    bloodEffects.clear();
    init();
  }
  
  public BloodWrapper getBloodWrapper(BloodType bloodType){
    return bloodEffects.getOrDefault(bloodType, null);
  }
  
  public double getIndividualSpawnChance(BloodType bloodType){
    return spawnChances.getOrDefault(bloodType, 0d);
  }
  
  public boolean isPlayerUnderCurse(UUID uuid){
    return playersUnderCurse.contains(uuid);
  }
  
  public void setPlayerUnderCurse(UUID uuid, int duration){
    playersUnderCurse.add(uuid);
    new BukkitRunnable(){
      @Override
      public void run(){
        playersUnderCurse.remove(uuid);
        if(Bukkit.getOfflinePlayer(uuid).isOnline()){
          Player player = Bukkit.getPlayer(uuid);
          player.sendMessage(Methods.color(player, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Blood.NoLongerCursed")));
        }
      }
    }.runTaskLater(McRPG.getInstance(), duration * 20);
  }
  
  public enum BloodType{
    TOOL("Tools", new HashSet<>(Arrays.asList("PICKAXE", "AXE", "SHOVEL", "HOE"))),
    WEAPON("Weapons", new HashSet<>(Arrays.asList("SWORD", "AXE"))),
    ARMOR("Armor", new HashSet<>(Arrays.asList("HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"))),
    CURSE("BloodCurse", new HashSet<>(Arrays.asList("REDSTONE")));
    
    @Getter
    private String id;
    
    @Getter
    private Set<String> applicableMaterialSuffix;
    
    BloodType(String id, Set<String> applicableMaterialSuffix){
      this.id = id;
      this.applicableMaterialSuffix = applicableMaterialSuffix;
    }
    
    public static BloodType getFromID(String id){
      return Arrays.stream(values()).filter(bloodType -> bloodType.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }
    
    public boolean isMaterialApplicable(Material material){
      for(String s : material.name().split("_")){
        if(applicableMaterialSuffix.contains(s)){
          return true;
        }
      }
      return false;
    }
  }
  
  
}
