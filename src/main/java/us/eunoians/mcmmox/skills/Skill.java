package us.eunoians.mcmmox.skills;


import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.api.displays.ExpScoreboardDisplay;
import us.eunoians.mcmmox.api.displays.GenericDisplay;
import us.eunoians.mcmmox.api.events.mcmmo.McMMOPlayerExpGainEvent;
import us.eunoians.mcmmox.api.events.mcmmo.McMMOPlayerLevelChangeEvent;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.types.*;
import us.eunoians.mcmmox.util.Parser;

import java.util.Calendar;
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
   * The map of abilities a player has that is on cooldown. Keys are the enum values of abilities and the values are the end time of the cooldown in milis
   */
  @Setter
  private HashMap<GenericAbility, Long> abilitesOnCooldown = new HashMap<GenericAbility, Long>();
  /**
   * The map of all the abilities the skill has loaded. The key is the enum of abilities while the values are the corresponding instance of an ability
   */
  private HashMap<GenericAbility, BaseAbility> abilityMap;

  @Getter
  private McMMOPlayer player;

  public Skill(Skills type, HashMap<GenericAbility, BaseAbility> abilityMap, int currentLevel, int currentExp, McMMOPlayer player) {
    this.type = type;
    this.currentLevel = currentLevel;
    this.currentExp = currentExp;
    this.abilityMap = abilityMap;
    this.player = player;
    Parser equation = type.getExpEquation();
    equation.setVariable("skill_level", currentLevel);
    equation.setVariable("power_level", player.getPowerLevel());
    this.expToLevel = (int) type.getExpEquation().getValue();
  }

  /**
   * Check if the ability specified is on cooldown
   *
   * @param ability The ability you want to check the cooldown for
   * @return true if the ability is on cooldown and false if it isnt
   */
  public boolean isAbilityOnCooldown(GenericAbility ability) {
    return abilitesOnCooldown.keySet().stream().anyMatch(ab -> ab.equals(ability));
  }

  public BaseAbility getAbility(GenericAbility ability){
	return (abilityMap.containsKey(ability))? abilityMap.get(ability) : null;
  }

  public GenericAbility getGenericAbility(String ability){
    return abilityMap.keySet().stream().filter(ab -> ab.getName().equalsIgnoreCase(ability)).findFirst().orElse(null);
  }

  public BaseAbility getDefaultAbility(){
    return getAbility(DefaultAbilities.getSkillsDefaultAbility(this.getName()));
  }

  public Collection<BaseAbility> getAbilities(){
    return abilityMap.values();
  }

  public Set<GenericAbility> getAbilityKeys(){
    return abilityMap.keySet();
  }


  /**
   * Get the cooldown time for the specified ability
   *
   * @param ability
   * @return
   */
  public long getCooldownTimeLeft(GenericAbility ability) {
    for (GenericAbility ab : abilitesOnCooldown.keySet()) {
      if (ab.equals(ability)) {
		Calendar cal = Calendar.getInstance();
        return abilitesOnCooldown.get(ab) - cal.getTimeInMillis();
      }
    }
    return -1;
  }

  public String getName() {
    return type.getName();
  }

  /**
   *
   * @param exp The exp gained
   * @param gainReason The reason the player is gaining the exp
   */
  public void giveExp(int exp, GainReason gainReason){
	McMMOPlayerExpGainEvent expEvent = new McMMOPlayerExpGainEvent(exp, this, gainReason);
	Bukkit.getPluginManager().callEvent(expEvent);
	if(expEvent.isCancelled()){
	  return;
	}
	exp = expEvent.getExpGained();
    int oldLevel = currentLevel;
    if(exp + currentExp >= expToLevel){
	  int amountOfLevels = 1;
      int leftOverExp = currentExp + exp - expToLevel;
      currentLevel++;
      Parser parser = type.getExpEquation();
	  parser.setVariable("skill_level", currentLevel);
	  parser.setVariable("power_level", player.getPowerLevel());
	  expToLevel = (int) parser.getValue();
	  currentExp = leftOverExp;
	  while(currentExp >= expToLevel){
	    amountOfLevels++;
	    leftOverExp = currentExp - expToLevel;
	    currentLevel++;
		parser.setVariable("skill_level", currentLevel);
		parser.setVariable("power_level", player.getPowerLevel());
		expToLevel = (int) parser.getValue();
		currentExp = leftOverExp;
	  }
	  McMMOPlayerLevelChangeEvent event = new McMMOPlayerLevelChangeEvent(oldLevel, currentLevel, amountOfLevels, this);
	  Bukkit.getPluginManager().callEvent(event);
    }
	else{
	  currentExp += exp;
	  expToLevel -= exp;
	}
	if(!Mcmmox.getInstance().getDisplayManager().doesPlayerHaveDisplay(player.getPlayer())){
	  return ;
	}
	GenericDisplay display = Mcmmox.getInstance().getDisplayManager().getDisplay(player.getPlayer());
	if(display.getType().equals(DisplayType.EXP_SCOREBOARD)){
	  ExpScoreboardDisplay expBoard = (ExpScoreboardDisplay) display;
	  if(expBoard.getSkill().equals(this.getType())){
		expBoard.sendUpdate(currentExp, expToLevel, currentLevel);
	  }
	}
  }
}