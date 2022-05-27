package us.eunoians.mcrpg.worldguard;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.types.ActionParserType;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class WGRegion {

  @Getter
  boolean failedLoad = false;
  @Getter
  private HashMap<Material, List<String>> breakExpressions = new HashMap<>();
  @Getter
  private HashMap<EntityType, List<String>> attackExpressions = new HashMap<>();
  @Getter
  private HashMap<String, List<String>> expGainExpressions = new HashMap<>();
  @Getter
  private HashMap<String, List<String>> abilityExpressions = new HashMap<>();
  @Getter
  private List<String> enterExpressions;
  @Getter
  private double expMultiplier;

  public WGRegion(String key) {
    FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.WORLDGUARD_CONFIG);
    expMultiplier = config.getDouble(key + "RegionExpMultiplier");
    enterExpressions = config.getStringList(key + "McRPGLimiters.BanEntry");
    List<String> actionExpressions = config.getStringList(key + "McRPGLimiters.BanAction");
    for(String s : actionExpressions) {
      String[] info = s.split("-");
      ActionParserType type = ActionParserType.fromString(info[0]);
      if(type == null) {
        Bukkit.getConsoleSender().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                McRPG.getInstance().getLangFile().getString("Messages.WorldGuard.InvalidActionType").replace("%path%", key)));
        failedLoad = true;
        return;
      }
      if(type == ActionParserType.BREAK) {
        Material mat = Material.getMaterial(info[1]);
        if(mat == null) {
          Bukkit.getConsoleSender().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                  McRPG.getInstance().getLangFile().getString("Messages.WorldGuard.InvalidMaterial").replace("%path%", key)));
          failedLoad = true;
          return;
        }
        if(breakExpressions.containsKey(mat)) {
          breakExpressions.get(mat).add(info[2]);
        }
        else {
          breakExpressions.put(mat, Collections.singletonList(info[2]));
        }
      }
      else if(type == ActionParserType.ATTACK) {
        EntityType entityType = EntityType.fromName(info[1]);
        if(entityType == null) {
          Bukkit.getConsoleSender().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                  McRPG.getInstance().getLangFile().getString("Messages.WorldGuard.InvalidEntity").replace("%path%", key)));
          failedLoad = true;
          return;
        }
        if(attackExpressions.containsKey(entityType)) {
          attackExpressions.get(entityType).add(info[2]);
        }
        else {
          attackExpressions.put(entityType, Collections.singletonList(info[2]));
        }
      }
      else if(type == ActionParserType.EXP_GAIN) {
        if(!(info[1].equalsIgnoreCase("all") || Skills.isSkill(info[1]))) {
          Bukkit.getConsoleSender().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                  McRPG.getInstance().getLangFile().getString("Messages.WorldGuard.InvalidSkillParameter").replace("%path%", key)));
          failedLoad = true;
          return;
        }
        if(expGainExpressions.containsKey(info[1])) {
          expGainExpressions.get(info[1]).add(info[2]);
        }
        else {
          expGainExpressions.put(info[1], Collections.singletonList(info[2]));
        }
      }
      else if(type == ActionParserType.ABILITY_ACTIVATE) {
        String abilityString = info[1];
        if(!(abilityString.equalsIgnoreCase("all") || Skills.isSkill(abilityString) || UnlockedAbilities.isAbility(abilityString))) {
          Bukkit.getConsoleSender().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                  McRPG.getInstance().getLangFile().getString("Messages.WorldGuard.InvalidAbilityParameter").replace("%path%", key)));
          failedLoad = true;
        }
        if(abilityExpressions.containsKey(abilityString)){
          abilityExpressions.get(abilityString).add(info[2]);
        }
        else{
          abilityExpressions.put(abilityString, Collections.singletonList(info[2]));
        }
      }
    }
  }
}
