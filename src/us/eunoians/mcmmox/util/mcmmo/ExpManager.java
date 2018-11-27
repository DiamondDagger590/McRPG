package us.eunoians.mcmmox.util.mcmmo;

import lombok.Getter;
import org.bukkit.Material;
import us.eunoians.mcmmox.types.Skills;

import java.util.HashMap;

/**
 * This code is not mine. It is from the original McMMO allowed for use by their license.
 * All credit goes to the original authors as I have only changed a little to suit my needs
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
