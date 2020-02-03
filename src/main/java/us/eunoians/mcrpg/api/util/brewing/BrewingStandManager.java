package us.eunoians.mcrpg.api.util.brewing;

import lombok.Getter;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.BrewingStand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.brewing.standmeta.BrewingStandWrapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BrewingStandManager {

  private Map<String, FileWrapper> chunkToSaveFile = new HashMap<>();
  private Map<String, Map<Location, BrewingStandWrapper>> brewingStandWrapperMap = new HashMap<>();

  public BrewingStandWrapper initNewBrewingStand(BrewingStand brewingStand){
    Location location = brewingStand.getLocation();
    String chunkKey = Methods.chunkToLoc(location.getChunk());
    if(!chunkToSaveFile.containsKey(chunkKey)){
      File folder = new File(McRPG.getInstance().getDataFolder(), "brewing_storage");
      if(!folder.exists()){
        folder.mkdir();
      }
      File file = new File(folder, chunkKey + ".yml");
      FileWrapper fileWrapper = new FileWrapper(file);
      chunkToSaveFile.put(chunkKey, fileWrapper);
    }
    BrewingStandWrapper brewingStandWrapper = new BrewingStandWrapper(brewingStand, chunkToSaveFile.get(chunkKey).getFileConfiguration());
    if(brewingStandWrapperMap.containsKey(chunkKey)){
      brewingStandWrapperMap.get(chunkKey).put(location, brewingStandWrapper);
    }
    else{
      HashMap<Location, BrewingStandWrapper> newMap = new HashMap<>();
      newMap.put(location, brewingStandWrapper);
      brewingStandWrapperMap.put(chunkKey, newMap);
    }
    brewingStandWrapper.saveToFile();
    save(location);
    return brewingStandWrapper;
  }

  public void unloadChunk(Chunk chunk){
    String chunkKey = Methods.chunkToLoc(chunk);
    if(brewingStandWrapperMap.containsKey(chunkKey)){
      for(Location loc : brewingStandWrapperMap.get(chunkKey).keySet()){
        BrewingStandWrapper brewingStandWrapper = brewingStandWrapperMap.get(chunkKey).get(loc);
        brewingStandWrapper.finishBrew();
        brewingStandWrapper.saveToFile();
      }
    }
    brewingStandWrapperMap.remove(chunkKey);
    chunkToSaveFile.remove(chunkKey);
  }
  
  public void breakBrewingStand(Location location){
    String chunkKey = Methods.chunkToLoc(location.getChunk());
    if(brewingStandWrapperMap.containsKey(chunkKey) && brewingStandWrapperMap.get(chunkKey).containsKey(location)){
      brewingStandWrapperMap.get(chunkKey).get(location).deleteData();
      String locationKey = Methods.locToString(location);
      chunkToSaveFile.get(chunkKey).getFileConfiguration().set("BrewingStands." + locationKey, null);
      brewingStandWrapperMap.get(chunkKey).remove(location);
      if(brewingStandWrapperMap.get(chunkKey).size() == 0){
        brewingStandWrapperMap.remove(chunkKey);
        chunkToSaveFile.get(chunkKey).getFile().delete();
        chunkToSaveFile.remove(chunkKey);
      }
    }
  }

  public boolean isBrewingStandLoaded(BrewingStand brewingStand){
    Location location = brewingStand.getLocation();
    String chunkKey = Methods.chunkToLoc(location.getChunk());
    return brewingStandWrapperMap.containsKey(chunkKey) && brewingStandWrapperMap.get(chunkKey).containsKey(location);
  }

  public BrewingStandWrapper getBrewingStandWrapper(BrewingStand brewingStand){
    Location location = brewingStand.getLocation();
    String chunkKey = Methods.chunkToLoc(location.getChunk());
    return brewingStandWrapperMap.get(chunkKey).get(location);
  }

  public void save(Location location){
    String chunkKey = Methods.chunkToLoc(location.getChunk());
    if(chunkToSaveFile.containsKey(chunkKey)){
      chunkToSaveFile.get(chunkKey).save();
    }
  }

  public void save(Chunk chunk){
    String chunkKey = Methods.chunkToLoc(chunk);
    if(chunkToSaveFile.containsKey(chunkKey)){
      chunkToSaveFile.get(chunkKey).save();
    }
  }

  public void shutDown(){
    for(FileWrapper fileWrapper : chunkToSaveFile.values()){
      fileWrapper.save();
    }
  }
  private class FileWrapper {

    @Getter
    private File file;
    @Getter
    private FileConfiguration fileConfiguration;

    public FileWrapper(File file){
      this.file = file;
      this.fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public void save(){
      try{
        fileConfiguration.save(file);
      } catch(IOException e){
        e.printStackTrace();
      }
    }
  }
}