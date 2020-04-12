package us.eunoians.mcrpg.skills;


import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.api.displays.ExpDisplayFactory;
import us.eunoians.mcrpg.api.displays.ExpDisplayType;
import us.eunoians.mcrpg.api.displays.GenericDisplay;
import us.eunoians.mcrpg.api.events.mcrpg.McRPGPlayerExpGainEvent;
import us.eunoians.mcrpg.api.events.mcrpg.McRPGPlayerLevelChangeEvent;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.*;
import us.eunoians.mcrpg.util.Parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/*
A parent skill class that defines the basic behaviour of every skill
 */
public abstract class Skill {

  /**
   * The enum value of a skill
   */
  @Getter
  private Skills type;
  /**
   * The current level of the player in the skill
   */
  @Getter
  @Setter
  private int currentLevel;
  /**
   * The current exp a player has towards leveling up in this skill
   */
  @Getter
  @Setter
  private int currentExp;
  /**
   * The exp needed for a player to reach the next level in the skill
   */
  @Getter
  private int expToLevel;
  /**
   * The map of all the abilities the skill has loaded. The key is the enum of abilities while the values are the corresponding instance of an ability
   */
  private HashMap<GenericAbility, BaseAbility> abilityMap;

  /**
   * Player the skill belongs to
   */
  @Getter
  private McRPGPlayer player;

  public Skill(Skills type, HashMap<GenericAbility, BaseAbility> abilityMap, int currentLevel, int currentExp, McRPGPlayer player){
    this.type = type;
    this.currentLevel = currentLevel;
    this.currentExp = currentExp;
    this.abilityMap = abilityMap;
    this.player = player;
    Parser equation = type.getExpEquation();
    equation.setVariable("skill_level", currentLevel);
    equation.setVariable("power_level", player.getPowerLevel());
    this.expToLevel = (int) equation.getValue();
  }

  /**
   * @param ability Enum value you want to get the ability instance of
   * @return instance of provided ability or null if invalid
   */
  public BaseAbility getAbility(GenericAbility ability){
    return abilityMap.getOrDefault(ability, null);
  }

  /**
   * @param ability The ability you want to get a GenericAbility from
   * @return the enum value of the ability or null if invalid
   */
  public GenericAbility getGenericAbility(String ability){
    return abilityMap.keySet().stream().filter(ab -> ab.getName().equalsIgnoreCase(ability)).findFirst().orElse(null);
  }

  /**
   * @return The default ability for this skill
   */
  public BaseAbility getDefaultAbility(){
    return getAbility(DefaultAbilities.getSkillsDefaultAbility(this.getType()));
  }

  /**
   * @return The array of Base Abilities
   */
  public Collection<BaseAbility> getAbilities(){
    return abilityMap.values();
  }

  public Set<GenericAbility> getAbilityKeys(){
    return abilityMap.keySet();
  }

  public String getName(){
    return type.getName();
  }

  public void updateExpToLevel(){
    Parser parser = type.getExpEquation();
    parser.setVariable("skill_level", currentLevel);
    parser.setVariable("power_level", player.getPowerLevel());
    expToLevel = (int) parser.getValue();
  }

  /**
   * @param exp        The exp gained
   * @param gainReason The reason the player is gaining the exp
   */
  public void giveExp(McRPGPlayer player, int exp, GainReason gainReason){
    if(!player.isOnline()){
      return;
    }
    McRPGPlayerExpGainEvent expEvent = new McRPGPlayerExpGainEvent(player, exp, this, gainReason);
    Bukkit.getPluginManager().callEvent(expEvent);
    if(expEvent.isCancelled()){
      return;
    }
    exp = expEvent.getExpGained();
    int oldLevel = currentLevel;
    if(exp + currentExp >= expToLevel){
      int leftOverExp = currentExp + exp - expToLevel;
      currentLevel++;
      Parser parser = type.getExpEquation();
      parser.setVariable("skill_level", currentLevel);
      parser.setVariable("power_level", player.getPowerLevel());
      expToLevel = (int) parser.getValue();
      currentExp = leftOverExp;
      while(currentExp >= expToLevel){
        if(currentLevel >= type.getMaxLevel()){
          expToLevel = 0;
          currentExp = 0;
          leftOverExp = 0;
          break;
        }
        leftOverExp = currentExp - expToLevel;
        currentLevel++;
        parser.setVariable("skill_level", currentLevel);
        parser.setVariable("power_level", player.getPowerLevel());
        expToLevel = (int) parser.getValue();
        currentExp = leftOverExp;
      }
      McRPGPlayerLevelChangeEvent event = new McRPGPlayerLevelChangeEvent(player, oldLevel, currentLevel, this);
      Bukkit.getPluginManager().callEvent(event);
    }
    else{
      currentExp += exp;
      //expToLevel -= exp;
    }
    if(McRPG.getInstance().getFileManager().getFile(FileManager.Files.CONFIG).getBoolean("Configuration.ConstantExpUpdates.Enabled")){
      if(McRPG.getInstance().getDisplayManager().doesPlayerHaveDisplay(player.getPlayer())){
        if(McRPG.getInstance().getDisplayManager().getDisplay(player.getPlayer()) instanceof ExpDisplayType){
          ExpDisplayType expDisplay = (ExpDisplayType) McRPG.getInstance().getDisplayManager().getDisplay(player.getPlayer());
          if(expDisplay.getSkill() == this.type){
            expDisplay.sendUpdate(currentExp, expToLevel, currentLevel, exp);
            return;
          }
        }
      }
      else{
        DisplayType displayType = DisplayType.fromString(McRPG.getInstance().getFileManager().getFile(FileManager.Files.CONFIG).getString("Configuration.ConstantExpUpdates.DisplayType"));
        int duration = McRPG.getInstance().getFileManager().getFile(FileManager.Files.CONFIG).getInt("Configuration.ConstantExpUpdates.DisplayDuration");
        GenericDisplay display = ExpDisplayFactory.createDisplay(displayType, player, this.type, duration);
        McRPG.getInstance().getDisplayManager().setGenericDisplay(display);
        ((ExpDisplayType) display).sendUpdate(currentExp, expToLevel, currentLevel, exp);
      }
    }
    else if(!McRPG.getInstance().getDisplayManager().doesPlayerHaveDisplay(player.getPlayer())){
      return;
    }
    else{
      GenericDisplay display = McRPG.getInstance().getDisplayManager().getDisplay(player.getPlayer());
      if(display instanceof ExpDisplayType){
        ExpDisplayType expDisplayType = (ExpDisplayType) display;
        if(expDisplayType.getSkill().equals(this.getType())){
          expDisplayType.sendUpdate(currentExp, expToLevel, currentLevel, exp);
        }
      }
    }
  }

  /**
   * @param levels   The amount of levels to give
   * @param resetExp If the exp should be reset on level ip
   */
  public void giveLevels(McRPGPlayer player, int levels, boolean resetExp){
    int old = currentLevel;
    currentLevel += levels;
    if(currentLevel > type.getMaxLevel()){
      currentLevel = type.getMaxLevel();
    }
    McRPGPlayerLevelChangeEvent event = new McRPGPlayerLevelChangeEvent(player, old, currentLevel, this);
    Bukkit.getPluginManager().callEvent(event);
    Parser parser = type.getExpEquation();
    parser.setVariable("skill_level", currentLevel);
    parser.setVariable("power_level", player.getPowerLevel());
    expToLevel = (int) parser.getValue();
    if(resetExp){
      currentExp = 0;
    }
  }

  public void resetSkill(){
    expToLevel = 0;
    currentLevel = 0;
    currentExp = 0;
    for(BaseAbility baseAbility : this.getAbilities()){
      baseAbility.setToggled(true);
      baseAbility.setCurrentTier(0);
      baseAbility.setUnlocked(false);
    }
    updateExpToLevel();
  }
}