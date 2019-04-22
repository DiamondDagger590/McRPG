package us.eunoians.mcrpg.util.worldguard;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;

import java.util.HashMap;

public class WGSupportManager {

  @Getter
  private HashMap<World, HashMap<String, WGRegion>> regionManager = new HashMap<>();


  public WGSupportManager(McRPG plugin){
    FileConfiguration config = plugin.getFileManager().getFile(FileManager.Files.WORLDGUARD_CONFIG);

    for(String s : config.getConfigurationSection("WorldGuardSupport").getKeys(false)){
      if(s.equals("TestRegion")){
        continue;
      }
      String key = "WorldGuardSupport." + s + ".";
      World w = Bukkit.getWorld(config.getString(key + "World"));
      String regionName = config.getString(key + "RegionName");
      WGRegion region = new WGRegion(key);
      if(w != null){
        if(regionManager.containsKey(w)){
          regionManager.get(w).put(regionName, region);
        }
        else{
          HashMap<String, WGRegion> map = new HashMap<>();
          map.put(regionName, region);
          regionManager.put(w, map);
        }
      }
    }
  }

  public boolean isWorldTracker(World w){
    return regionManager.containsKey(w);
  }
}