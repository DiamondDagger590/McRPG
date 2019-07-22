package us.eunoians.mcrpg.api.util.books;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.types.Skills;

import java.util.*;

public class BookManager {

    @Getter
    private static BookManager instance;
    private McRPG plugin;

    @Getter
    private List<String> enabledUnlockEvents;
    @Getter
    private List<String> enabledUpgradeEvents;
    @Getter
    private Set<String> unlockExcluded;
    @Getter
    private Set<String> upgradeExcluded;

    @Getter
    private double defaultUpgradeChance;
    @Getter
    private double defaultUnlockChance;

    @Getter
    private Map<String, Map<Material, Double>> materialChances;
    @Getter
    private Map<String, Map<EntityType, Double>> entityChances;
    @Getter
    private Map<String, Map<Skills, Double>> expChances;



    public BookManager(McRPG plugin){
        instance = this;
        this.plugin = plugin;
        FileConfiguration upgradeFile = plugin.getFileManager().getFile(FileManager.Files.UPGRADE_BOOKS);
        FileConfiguration unlockFile = plugin.getFileManager().getFile(FileManager.Files.UNLOCK_BOOKS);

        defaultUnlockChance = unlockFile.getDouble("DropChances.Default");
        defaultUpgradeChance = upgradeFile.getDouble("DropChances.Default");

        enabledUnlockEvents = unlockFile.getStringList("DropChances.DropEvents");
        enabledUpgradeEvents = upgradeFile.getStringList("DropChances.DropEvents");

        unlockExcluded = new HashSet<>(unlockFile.getStringList("DropChances.Excluded"));
        upgradeExcluded = new HashSet<>(upgradeFile.getStringList("DropChances.Excluded"));

        materialChances = new HashMap<>();
        entityChances = new HashMap<>();
        expChances = new HashMap<>();

        Map<Material, Double> unlockMaterialChances = new HashMap<>();
        Map<EntityType, Double> unlockEntityChances = new HashMap<>();
        Map<Skills, Double> unlockExpChances = new HashMap<>();

        for(String s : unlockFile.getStringList("DropChances.SpecialCases")){
            String[] data = s.split(":");
            if(Material.getMaterial(data[0]) != null){
                unlockMaterialChances.put(Material.getMaterial(data[0]), Double.parseDouble(data[1]));
            }
            else if(EntityType.fromName(data[0]) != null){
                unlockEntityChances.put(EntityType.fromName(data[0]), Double.parseDouble(data[1]));
            }
            else if(Skills.isSkill(data[0])){
                unlockExpChances.put(Skills.fromString(data[0]), Double.parseDouble(data[1]));
            }
        }

        materialChances.put("Unlock", unlockMaterialChances);
        entityChances.put("Unlock", unlockEntityChances);
        expChances.put("Unlock", unlockExpChances);

        Map<Material, Double> upgradeMaterialChances = new HashMap<>();
        Map<EntityType, Double> upgradeEntityChances = new HashMap<>();
        Map<Skills, Double> upgradeExpChances = new HashMap<>();

        for(String s : upgradeFile.getStringList("DropChances.SpecialCases")){
            String[] data = s.split(":");
            if(Material.getMaterial(data[0]) != null){
                upgradeMaterialChances.put(Material.getMaterial(data[0]), Double.parseDouble(data[1]));
            }
            else if(EntityType.fromName(data[0]) != null){
                upgradeEntityChances.put(EntityType.fromName(data[0]), Double.parseDouble(data[1]));
            }
            else if(Skills.isSkill(data[0])){
                upgradeExpChances.put(Skills.fromString(data[0]), Double.parseDouble(data[1]));
            }
        }

        materialChances.put("Upgrade", upgradeMaterialChances);
        entityChances.put("Upgrade", upgradeEntityChances);
        expChances.put("Upgrade", upgradeExpChances);
    }

    public void reload(){
        FileConfiguration upgradeFile = plugin.getFileManager().getFile(FileManager.Files.UPGRADE_BOOKS);
        FileConfiguration unlockFile = plugin.getFileManager().getFile(FileManager.Files.UNLOCK_BOOKS);

        defaultUnlockChance = unlockFile.getDouble("DropChances.Default");
        defaultUpgradeChance = upgradeFile.getDouble("DropChances.Default");

        enabledUnlockEvents = unlockFile.getStringList("DropChances.DropEvents");
        enabledUpgradeEvents = upgradeFile.getStringList("DropChances.DropEvents");

        unlockExcluded = new HashSet<>(unlockFile.getStringList("DropChances.Excluded"));
        upgradeExcluded = new HashSet<>(upgradeFile.getStringList("DropChances.Excluded"));

        materialChances = new HashMap<>();
        entityChances = new HashMap<>();
        expChances = new HashMap<>();

        Map<Material, Double> unlockMaterialChances = new HashMap<>();
        Map<EntityType, Double> unlockEntityChances = new HashMap<>();
        Map<Skills, Double> unlockExpChances = new HashMap<>();

        for(String s : unlockFile.getStringList("DropChances.SpecialCases")){
            String[] data = s.split(":");
            if(Material.getMaterial(data[0]) != null){
                unlockMaterialChances.put(Material.getMaterial(data[0]), Double.parseDouble(data[1]));
            }
            else if(EntityType.fromName(data[0]) != null){
                unlockEntityChances.put(EntityType.fromName(data[0]), Double.parseDouble(data[1]));
            }
            else if(Skills.isSkill(data[0])){
                unlockExpChances.put(Skills.fromString(data[0]), Double.parseDouble(data[1]));
            }
        }

        materialChances.put("Unlock", unlockMaterialChances);
        entityChances.put("Unlock", unlockEntityChances);
        expChances.put("Unlock", unlockExpChances);

        Map<Material, Double> upgradeMaterialChances = new HashMap<>();
        Map<EntityType, Double> upgradeEntityChances = new HashMap<>();
        Map<Skills, Double> upgradeExpChances = new HashMap<>();

        for(String s : upgradeFile.getStringList("DropChances.SpecialCases")){
            String[] data = s.split(":");
            if(Material.getMaterial(data[0]) != null){
                upgradeMaterialChances.put(Material.getMaterial(data[0]), Double.parseDouble(data[1]));
            }
            else if(EntityType.fromName(data[0]) != null){
                upgradeEntityChances.put(EntityType.fromName(data[0]), Double.parseDouble(data[1]));
            }
            else if(Skills.isSkill(data[0])){
                upgradeExpChances.put(Skills.fromString(data[0]), Double.parseDouble(data[1]));
            }
        }

        materialChances.put("Upgrade", upgradeMaterialChances);
        entityChances.put("Upgrade", upgradeEntityChances);
        expChances.put("Upgrade", upgradeExpChances);
    }

}
