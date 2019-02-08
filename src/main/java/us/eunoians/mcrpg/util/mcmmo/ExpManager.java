package us.eunoians.mcrpg.util.mcmmo;

import lombok.Getter;
import org.bukkit.Material;
import us.eunoians.mcrpg.types.Skills;

import java.util.HashMap;

/**
 * This code is not mine. It is copyright from the original mcMMO allowed for use by their license. Modified 12/7/18
 * This code has been modified from its source
 * It was released under the GPLv3 license
 */

public class ExpManager {
  @Getter
  private static ExpManager instance = new ExpManager();

  HashMap<Material, Integer> enabledBlocks = new HashMap<>();

  public ExpManager(){
    //Read in from a config file and fill appropriate arrays
  }

  public boolean canGiveExp(Skills skill, Material mat){
    if(skill == Skills.MINING){
      return enabledBlocks.keySet().contains(mat);
	}
	else{
	  return false;
	}
  }

}
