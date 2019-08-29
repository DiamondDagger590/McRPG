package us.eunoians.mcrpg.api.util;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Parrot;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.types.Skills;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldModifierManager {


  @Getter
  private Map<String, ExpModifierWrapper> worldModifiers = new HashMap<>();

  public WorldModifierManager(){
    FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.WORLD_MODIFIER);
    for(String world : config.getConfigurationSection("WorldModifier").getKeys(false)){
      if(world.equalsIgnoreCase("test")){
        continue;
      }
      worldModifiers.put(world, new ExpModifierWrapper(config.getStringList("WorldModifier." + world)));
    }
  }

  public class ExpModifierWrapper{

    Map<Skills, Double> expModifiers = new HashMap<>();

    public ExpModifierWrapper(List<String> expModifierList){
      for(String s : expModifierList){
        String[] data = s.split(":");
        Skills skill = Skills.fromString(data[0]);
        double multiplier = Double.parseDouble(data[1]);
        expModifiers.put(skill, multiplier);
      }
    }

    public boolean isModified(Skills skill){
      return expModifiers.containsKey(skill);
    }

    public double getModifier(Skills skill){
      return expModifiers.getOrDefault(skill, 1.0d);
    }
  }
}
