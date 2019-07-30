package us.eunoians.mcrpg.api.util.exp;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.skills.Skill;
import us.eunoians.mcrpg.types.Skills;

import java.util.HashMap;

public class ExpPermissionManager {

  @Getter private static ExpPermissionManager instance = new ExpPermissionManager();

  private McRPG p;

  @Getter private HashMap<String, ExpPerm> permissions;

  public ExpPermissionManager(){
    permissions = new HashMap<>();
  }

  public void reload(){
    permissions.clear();
    setup(p);
  }

  public ExpPermissionManager setup(McRPG p){
    this.p = p;
    FileConfiguration expFile = p.getFileManager().getFile(FileManager.Files.EXP_PERM_FILE);
    for(String s : expFile.getConfigurationSection("").getKeys(false)){
      String perm = expFile.getString(s + ".Perm");
      HashMap<Skills, Double> expVals = new HashMap<>();
      for(String skill : expFile.getConfigurationSection(s + ".AffectedSkills").getKeys(false)){
        Skills skillType = Skills.fromString(skill);
        double val = expFile.getDouble(s + ".AffectedSkills." + skill);
        expVals.put(skillType, val);
      }
      int prio = expFile.getInt(s + ".Priority");
      ExpPerm expPerm = new ExpPerm(perm, expVals, prio);
      permissions.put(perm, expPerm);
    }
    return this;
  }

  public double getPermBoost(Player p, Skill skill){
    ExpPerm perm = null;
    for(String s : permissions.keySet()){
      if(p.hasPermission(s)){
        if(perm != null){
          if(perm.getPriority() < permissions.get(s).getPriority() && permissions.get(s).getExpValues().containsKey(skill.getType())){
            perm = permissions.get(s);
          }
        }
        else{
          perm = permissions.get(s);
        }
      }
    }
    if(perm == null){
      return 1;
    }
    else{
      if(perm.getExpValues().containsKey(skill.getType())){
        return perm.getExpValues().get(skill.getType());
      }
      else{
        return 1;
      }
    }
  }
}
