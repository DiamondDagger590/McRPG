package us.eunoians.mcmmox.util.mcmmo;

import lombok.Getter;
import org.bukkit.Material;
import us.eunoians.mcmmox.types.Skills;

import java.util.HashMap;

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
