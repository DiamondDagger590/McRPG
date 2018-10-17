package us.eunoians.mcmmox.util.mcmmo;

import org.bukkit.Material;

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
