package us.eunoians.mcmmox.events.mcmmo;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.api.displays.DisplayManager;
import us.eunoians.mcmmox.api.displays.ExpDisplayType;
import us.eunoians.mcmmox.api.events.mcmmo.AbilityUnlockEvent;
import us.eunoians.mcmmox.api.events.mcmmo.McMMOPlayerLevelChangeEvent;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.skills.Skill;
import us.eunoians.mcmmox.types.GenericAbility;
import us.eunoians.mcmmox.types.Skills;
import us.eunoians.mcmmox.types.UnlockedAbilities;

import java.util.List;

@SuppressWarnings("ALL")
public class McMMOPlayerLevelChange implements Listener {

  @EventHandler (priority = EventPriority.NORMAL)
  public void levelChange(McMMOPlayerLevelChangeEvent e){
	Mcmmox mcmmox = Mcmmox.getInstance();
	e.getMcMMOPlayer().updatePowerLevel();
	//Send the player a message that they leveled up
	String message = Methods.color(mcmmox.getPluginPrefix() +
		mcmmox.getLangFile().getString("Messages.Players.LevelUp")
			.replaceAll("%Levels%", Integer.toString(e.getAmountOfLevelsIncreased())).replaceAll("%Skill%", e.getSkillLeveled().getName())
			.replaceAll("%Current_Level%", Integer.toString(e.getNextLevel())));
	Skill skillLeveled = e.getSkillLeveled();
	skillLeveled.updateExpToLevel();
	McMMOPlayer mp = e.getMcMMOPlayer();
	//iterate across all levels gained
	for(int i = e.getPreviousLevel() + 1; i <= e.getNextLevel(); i++){
	  //if the level is at a interval to gain the player an ability point, award it to them
	  if(i % mcmmox.getConfig().getInt("PlayerConfiguration.AbilityPointInterval") == 0){
		mp.setAbilityPoints(mp.getAbilityPoints() + 1);
		//Need to fiddle with this sound
		mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_YES, 5, 1);
		mp.getPlayer().sendMessage(Methods.color(mcmmox.getPluginPrefix() + mcmmox.getLangFile().getString("Messages.Players.AbilityPointGained")
			.replaceAll("%Ability_Points%", Integer.toString(e.getMcMMOPlayer().getAbilityPoints()))));
		mp.saveData();
	  }
	}
	//Do things for swords ability
	if(skillLeveled.getType().equals(Skills.SWORDS)){
	  //Get all enabled abilites
	  List<String> enabledAbilities = Skills.SWORDS.getEnabledAbilities();
	  //Iterate across these bois
	  addToPending(e, mcmmox, skillLeveled, mp, enabledAbilities);
	}
	//Do things for mining ability
	if(skillLeveled.getType().equals(Skills.MINING)){
	  //Get all enabled abilites
	  List<String> enabledAbilities = Skills.MINING.getEnabledAbilities();
	  //Iterate across these bois
	  addToPending(e, mcmmox, skillLeveled, mp, enabledAbilities);
	}
	//Do things for mining ability
	if(skillLeveled.getType().equals(Skills.UNARMED)){
	  //Get all enabled abilites
	  List<String> enabledAbilities = Skills.UNARMED.getEnabledAbilities();
	  //Iterate across these bois
	  addToPending(e, mcmmox, skillLeveled, mp, enabledAbilities);
	}
	//Do things for mining ability
	if(skillLeveled.getType().equals(Skills.HERBALISM)){
	  //Get all enabled abilites
	  List<String> enabledAbilities = Skills.HERBALISM.getEnabledAbilities();
	  //Iterate across these bois
	  addToPending(e, mcmmox, skillLeveled, mp, enabledAbilities);
	}
	//Update their general info and scoreboards
	if(e.getMcMMOPlayer().isOnline()){
	  Player p = e.getMcMMOPlayer().getPlayer();
	  p.sendMessage(message);
	  World w = p.getWorld();
	  w.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 5, 1);
	  if(!Mcmmox.getInstance().getDisplayManager().doesPlayerHaveDisplay(e.getMcMMOPlayer().getPlayer())){
		return;
	  }
	  DisplayManager displayManager = Mcmmox.getInstance().getDisplayManager();
	  if(displayManager.doesPlayerHaveDisplay(p)){
		if(displayManager.getDisplay(p) instanceof ExpDisplayType){
		  ExpDisplayType expDisplayType = (ExpDisplayType) displayManager.getDisplay(p);
		  Skill skill = mp.getSkill(expDisplayType.getSkill());
		  expDisplayType.sendUpdate(skill.getCurrentExp(), skill.getExpToLevel(), skill.getCurrentLevel(), 0);
		}
	  }
	}
  }

  private void addToPending(McMMOPlayerLevelChangeEvent e, Mcmmox mcmmox, Skill skillLeveled, McMMOPlayer mp, List<String> enabledAbilities){
	for(String s : enabledAbilities){
	  //Get the generic ability.
	  GenericAbility ability = skillLeveled.getGenericAbility(s);
	  //If its unlocked
	  if(ability instanceof UnlockedAbilities){
		//We get variables and verify that its not already unlocked
		UnlockedAbilities ab = (UnlockedAbilities) ability;
		BaseAbility base = skillLeveled.getAbility(ability);
		if(base.isUnlocked()){
		  continue;
		}
		else{
		  //Otherwise we check if they are allowed to unlock the ability
		  if(e.getNextLevel() >= ab.getUnlockLevel()){
			AbilityUnlockEvent abilityUnlockEvent = new AbilityUnlockEvent(mp, base);
			Bukkit.getPluginManager().callEvent(abilityUnlockEvent);
			if(abilityUnlockEvent.isCancelled()){
			  return;
			}
			if(mp.isOnline()){
			  Player p = mp.getPlayer();
			  if(mp.isAutoDeny()){
				p.sendMessage(Methods.color(mcmmox.getPluginPrefix() +
					mcmmox.getLangFile().getString("Messages.Players.AbilityUnlockedButDenied").replaceAll("%Ability%", ab.getName())));
			  }
			  else{
				p.sendMessage(Methods.color(mcmmox.getPluginPrefix() +
					mcmmox.getLangFile().getString("Messages.Players.AbilityUnlocked").replaceAll("%Ability%", ab.getName())));
				p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 5, 1);
				mp.addPendingAbilityUnlock(ab);
			  }
			}
			base.setUnlocked(true);
			base.setCurrentTier(1);
			mp.saveData();
		  }
		  else{
			continue;
		  }
		}
	  }
	  else{
		continue;
	  }
	}
  }
}
