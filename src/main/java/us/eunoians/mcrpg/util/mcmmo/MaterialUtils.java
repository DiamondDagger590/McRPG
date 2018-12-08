package us.eunoians.mcrpg.util.mcmmo;

import org.bukkit.Material;

/**
 * This code is not mine. It is from the original McMMO allowed for use by their license.
 * All credit goes to the original authors as I have only changed a little to suit my needs
 */

public final class MaterialUtils {
  private MaterialUtils(){
  }

  protected static boolean isOre(Material data){
	switch(data){
	  case COAL_ORE:
	  case DIAMOND_ORE:
	  case NETHER_QUARTZ_ORE:
	  case GOLD_ORE:
	  case IRON_ORE:
	  case LAPIS_ORE:
	  case REDSTONE_ORE:
	  case EMERALD_ORE:
		return true;
	  default:
		return false;
	}
  }
}
