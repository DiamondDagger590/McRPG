package us.eunoians.mcrpg.api.util.artifacts;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;

import java.util.HashMap;
import java.util.Map;

public class ArtifactManager{
  
  @Getter
  private static ArtifactManager instance;
  
  private McRPG mcRPG;
  
  private Map<String, Double> spawnChances = new HashMap<>();
  
  public ArtifactManager(McRPG mcRPG){
    this.mcRPG = mcRPG;
  }
  
  private void init(){
    FileConfiguration artifactFile = mcRPG.getFileManager().getFile(FileManager.Files.ARTIFACT_FILE);
    if(artifactFile.getBoolean("BrewingArtifacts.Enabled")){
      double chanceOfSpawning = artifactFile.getDouble("BrewingArtifacts.ChanceOfSpawning");
      spawnChances.put("BrewingArtifacts", chanceOfSpawning);
    }
  }
  
  public void reload(){
    spawnChances.clear();
    init();
  }
  
  public boolean isArtifactTypeValid(String type){
    return spawnChances.containsKey(type);
  }
  
  public double getArtifactTypeChance(String type){
    return spawnChances.get(type);
  }
}
