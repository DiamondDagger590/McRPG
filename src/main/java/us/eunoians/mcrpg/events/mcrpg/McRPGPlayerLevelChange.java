package us.eunoians.mcrpg.events.mcrpg;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.api.displays.DisplayManager;
import us.eunoians.mcrpg.api.displays.ExpDisplayType;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityUnlockEvent;
import us.eunoians.mcrpg.api.events.mcrpg.McRPGPlayerLevelChangeEvent;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.skills.Skill;
import us.eunoians.mcrpg.types.GenericAbility;
import us.eunoians.mcrpg.types.TipType;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.List;
import java.util.Random;

@SuppressWarnings("ALL")
public class McRPGPlayerLevelChange implements Listener {

  //private FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_GUI);

  @EventHandler(priority = EventPriority.NORMAL)
  public void levelChange(McRPGPlayerLevelChangeEvent e) {
    McRPG mcRPG = McRPG.getInstance();
    if(e.getNextLevel() > e.getSkillLeveled().getType().getMaxLevel()){
      e.setNextLevel( e.getSkillLeveled().getType().getMaxLevel());
      if(e.getNextLevel() == e.getPreviousLevel()){
        e.setCancelled(true);
        return;
      }
    }
    
    McRPG.getInstance().getLevelCommandManager().handleLevelUp(e.getMcRPGPlayer(), e.getSkillLeveled().getType(), e.getPreviousLevel(), e.getNextLevel());
    
    e.getMcRPGPlayer().updatePowerLevel();
    //Send the player a message that they leveled up
    String message = Methods.color(e.getMcRPGPlayer().getPlayer(), mcRPG.getPluginPrefix() +
            mcRPG.getLangFile().getString("Messages.Players.LevelUp")
                    .replaceAll("%Levels%", Integer.toString(e.getAmountOfLevelsIncreased())).replaceAll("%Skill%", e.getSkillLeveled().getType().getDisplayName())
                    .replaceAll("%Current_Level%", Integer.toString(e.getNextLevel())));
    Skill skillLeveled = e.getSkillLeveled();
    skillLeveled.updateExpToLevel();
    McRPGPlayer mp = e.getMcRPGPlayer();
    Random rand = new Random();
    
    boolean quickSave = false;
    //iterate across all levels gained
    for(int i = e.getPreviousLevel() + 1; i <= e.getNextLevel(); i++) {
      //if the level is at a interval to gain the player an ability point, award it to them
      if (i % mcRPG.getConfig().getInt("PlayerConfiguration.AbilityPointInterval") == 0) {
        mp.setAbilityPoints(mp.getAbilityPoints() + 1);
        //Need to fiddle with this sound
        FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
        mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.valueOf(soundFile.getString("Sounds.Misc.AbilityPointGain.Sound")),
          Float.parseFloat(soundFile.getString("Sounds.Misc.AbilityPointGain.Volume")), Float.parseFloat(soundFile.getString("Sounds.Misc.AbilityPointGain.Pitch")));
        mp.getPlayer().sendMessage(Methods.color(mp.getPlayer(), mcRPG.getPluginPrefix() + mcRPG.getLangFile().getString("Messages.Players.AbilityPointGained")
                .replaceAll("%Ability_Points%", Integer.toString(e.getMcRPGPlayer().getAbilityPoints()))));
      }
      TipType tipType = TipType.getSkillTipType(e.getSkillLeveled().getType());
      if (!McRPG.getInstance().getFileManager().getFile(FileManager.Files.CONFIG).getBoolean("Configuration.DisableTips") && !mp.isIgnoreTips() && !mp.getUsedTips().contains(tipType)) {
        List<String> possibleMessages = mcRPG.getLangFile().getStringList("Messages.Tips.LevelUp" + e.getSkillLeveled().getName());
        if (possibleMessages.size() > 0) {
          int val = rand.nextInt(possibleMessages.size());
          mp.getPlayer().sendMessage(Methods.color(mp.getPlayer(), possibleMessages.get(val)));
          mp.getUsedTips().add(tipType);
        }
      }
      quickSave = true;
    }

    addToPending(e, mcRPG, skillLeveled, mp, skillLeveled.getType().getEnabledAbilities());
    
    if(quickSave){
      mp.saveData();
    }
    
    //Update their general info and scoreboards
    if(e.getMcRPGPlayer().isOnline()) {
      Player p = e.getMcRPGPlayer().getPlayer();
      p.sendMessage(message);
      World w = p.getWorld();
      FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
      w.playSound(p.getLocation(), Sound.valueOf(soundFile.getString("Sounds.Misc.LevelUp.Sound")),
        Float.parseFloat(soundFile.getString("Sounds.Misc.LevelUp.Volume")), Float.parseFloat(soundFile.getString("Sounds.Misc.LevelUp.Pitch")));
      if(!McRPG.getInstance().getDisplayManager().doesPlayerHaveDisplay(e.getMcRPGPlayer().getPlayer())) {
        return;
      }
      DisplayManager displayManager = McRPG.getInstance().getDisplayManager();
      if(displayManager.doesPlayerHaveDisplay(p)) {
        if(displayManager.getDisplay(p) instanceof ExpDisplayType) {
          ExpDisplayType expDisplayType = (ExpDisplayType) displayManager.getDisplay(p);
          Skill skill = mp.getSkill(expDisplayType.getSkill());
          expDisplayType.sendUpdate(skill.getCurrentExp(), skill.getExpToLevel(), skill.getCurrentLevel(), 0);
        }
      }
    }
  }

  private void addToPending(McRPGPlayerLevelChangeEvent e, McRPG mcRPG, Skill skillLeveled, McRPGPlayer mp, List<String> enabledAbilities) {
    for(String s : enabledAbilities) {
      //Get the generic ability.
      GenericAbility ability = skillLeveled.getGenericAbility(s);
      //If its unlocked
      if(ability instanceof UnlockedAbilities) {
        //We get variables and verify that its not already unlocked
        UnlockedAbilities ab = (UnlockedAbilities) ability;
        BaseAbility base = skillLeveled.getAbility(ability);
        if(base.isUnlocked()) {
          continue;
        }
        else {
          //Otherwise we check if they are allowed to unlock the ability
          if(e.getNextLevel() >= ab.getUnlockLevel()) {
            AbilityUnlockEvent abilityUnlockEvent = new AbilityUnlockEvent(mp, base);
            Bukkit.getPluginManager().callEvent(abilityUnlockEvent);
            if(abilityUnlockEvent.isCancelled()) {
              return;
            }
            if(mp.isOnline()) {
              Player p = mp.getPlayer();
              if(mp.isAutoDeny()) {
                p.sendMessage(Methods.color(mp.getPlayer(),mcRPG.getPluginPrefix() +
                        mcRPG.getLangFile().getString("Messages.Players.AbilityUnlockedButDenied").replaceAll("%Ability%", ab.getName())));
              }
              else {
                p.sendMessage(Methods.color(mp.getPlayer(),mcRPG.getPluginPrefix() +
                        mcRPG.getLangFile().getString("Messages.Players.AbilityUnlocked").replaceAll("%Ability%", ab.getName())));
                FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
                p.getLocation().getWorld().playSound(p.getLocation(), Sound.valueOf(soundFile.getString("Sounds.Misc.AbilityUnlocked.Sound")),
                  Float.parseFloat(soundFile.getString("Sounds.Misc.AbilityUnlocked.Volume")), Float.parseFloat(soundFile.getString("Sounds.Misc.AbilityUnlocked.Pitch")));
                mp.addPendingAbilityUnlock(ab);
              }
            }
            base.setUnlocked(true);
            base.setCurrentTier(1);
            mp.saveData();
          }
          else {
            continue;
          }
        }
      }
      else {
        continue;
      }
    }
  }
}
