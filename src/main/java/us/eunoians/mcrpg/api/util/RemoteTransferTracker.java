package us.eunoians.mcrpg.api.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcrpg.McRPG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class RemoteTransferTracker {

  private static HashMap<UUID, Location> locations = new HashMap<>();

  public RemoteTransferTracker(){
	load();
  }

  public static void addLocation(UUID uuid, Location loc){
    locations.put(uuid, loc);
  }

  public static Location getLocation(UUID uuid) { return locations.getOrDefault(uuid, null);}

  public static void removeLocation(UUID uuid){
    locations.remove(uuid);
  }

  public static void removeLocation(Location loc){
    locations.values().remove(loc);
  }

  public static boolean isTracked(Location loc){
    return locations.values().contains(loc);
  }

  public static boolean isTracked(UUID uuid){
    return locations.keySet().contains(uuid);
  }

  public static ArrayList<Location> getLocations(){
    return new ArrayList<>(locations.values());
  }

  public static UUID getUUID(Location loc){
    return locations.keySet().stream().filter(uuid -> locations.get(uuid).equals(loc)).findFirst().orElse(null);
  }

  private static void save(){
    McRPG instance = McRPG.getInstance();
    locations.keySet().forEach(uuid -> {
      Location loc = locations.get(uuid);
      instance.getFileManager().getFile(FileManager.Files.LOCATIONS).set("RemoteTransfer." + uuid.toString(), locToString(loc));
	});

  	instance.getFileManager().saveFile(FileManager.Files.LOCATIONS);
  }

  private void load(){
	McRPG.getInstance().getFileManager().getFile(FileManager.Files.LOCATIONS).getConfigurationSection("RemoteTransfer").getKeys(false).stream()
		.map(UUID::fromString).forEach(uuid -> {
	  String loc = McRPG.getInstance().getFileManager().getFile(FileManager.Files.LOCATIONS).getString("RemoteTransfer." + uuid.toString());
	  locations.put(uuid, stringToLocation(loc));
	});  }

  private static String locToString(Location loc){
    new BukkitRunnable(){
      @Override
      public void run(){
        RemoteTransferTracker.save();
      }
    }.runTaskTimerAsynchronously(McRPG.getInstance(), 0, 120);
	return loc.getWorld().getName() + " , " + (int) loc.getX() + " , " + (int) loc.getY() + " , " + (int) loc.getZ();
  }

  public static Location stringToLocation(String key){
	String[] split = key.split(" , ");
	if(split.length == 4){
	  Location loc = new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), 0, 0);
	  return loc;
	}
	return null;
  }
}
