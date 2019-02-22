package us.eunoians.mcrpg.util.worldguard;

import lombok.Getter;
import org.bukkit.World;

import java.util.HashMap;

public class WGSupportManager {

  @Getter
  private HashMap<World, HashMap<String, WGRegion>> regionManager = new HashMap<>();


}
