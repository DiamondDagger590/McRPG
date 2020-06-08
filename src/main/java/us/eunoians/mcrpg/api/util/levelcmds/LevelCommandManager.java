package us.eunoians.mcrpg.api.util.levelcmds;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.LevelCommandType;
import us.eunoians.mcrpg.types.Skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class LevelCommandManager{
  
  private Map<LevelCommandType, List<LevelCommand>> levelCommandMap = new HashMap<>();
  
  public LevelCommandManager(){
    init();
  }
  
  private void init(){
    FileConfiguration configuration = McRPG.getInstance().getFileManager().getFile(FileManager.Files.LEVEL_COMMAND);
    if(!configuration.contains("Commands")){
      return;
    }
    for(String string : configuration.getStringList("Commands")){
      String[] data = string.split(":");
      if(data.length < 3 || !LevelCommandType.isCommandType(data[0]) || !Methods.isInt(data[1])){
        Bukkit.getLogger().log(Level.WARNING, "There is an invalid level command that is being skipped: " + string);
        continue;
      }
      LevelCommandType levelCommandType = LevelCommandType.fromString(data[0]);
      int level = Integer.parseInt(data[1]);
      StringBuilder commandBuilder = new StringBuilder(data[2]);
      for(int i = 3; i < data.length; i++){
        commandBuilder.append(":" + data[i]);
      }
      LevelCommand levelCommand = new LevelCommand(commandBuilder.toString(), level, levelCommandType);
      if(levelCommandMap.containsKey(levelCommandType)){
        levelCommandMap.get(levelCommandType).add(levelCommand);
      }
      else{
        List<LevelCommand> levelCommands = new ArrayList<>();
        levelCommands.add(levelCommand);
        levelCommandMap.put(levelCommandType, levelCommands);
      }
    }
  }
  
  public void reload(){
    levelCommandMap.clear();
    init();
  }
  
  public void handleLevelUp(McRPGPlayer mcRPGPlayer, Skills skill, int oldLevel, int newLevel){
    LevelCommandType levelCommandType = LevelCommandType.fromString(skill.getName());
    int powerLevel = mcRPGPlayer.getPowerLevel() + 1;
    
    //Offset it so we dont count the level when we didnt "level up" into that level
    oldLevel += 1;
    if(levelCommandMap.containsKey(levelCommandType) || levelCommandMap.containsKey(LevelCommandType.POWER)){
      //Cache to reduce iterations
      Map<Integer, LevelCommand> commandMap = new HashMap<>();
      Map<Integer, LevelCommand> commandPowerMap = new HashMap<>();
      //Iterate through all levels gained (we need to offset the offset)
      for(int i = 0; i <= (newLevel - oldLevel) + 1; i++){
        //Load cache for first level
        if(i == 0){
          //Load for the skill leveled
          if(levelCommandMap.containsKey(levelCommandType)){
            for(LevelCommand levelCommand : levelCommandMap.get(levelCommandType)){
              commandMap.put(levelCommand.getLevel(), levelCommand);
              if(levelCommand.getLevel() == oldLevel + i){
                levelCommand.runForPlayer(mcRPGPlayer.getPlayer());
              }
            }
          }
          //Load for power levels
          if(levelCommandMap.containsKey(LevelCommandType.POWER)){
            for(LevelCommand levelCommand : levelCommandMap.get(LevelCommandType.POWER)){
              commandPowerMap.put(levelCommand.getLevel(), levelCommand);
              if(levelCommand.getLevel() == powerLevel + i){
                levelCommand.runForPlayer(mcRPGPlayer.getPlayer());
              }
            }
          }
          continue;
        }
        else{
          if(commandMap.containsKey(i + oldLevel)){
            commandMap.get(i + oldLevel).runForPlayer(mcRPGPlayer.getPlayer());
          }
          if(commandPowerMap.containsKey(i + powerLevel)){
            commandPowerMap.get(i + powerLevel).runForPlayer(mcRPGPlayer.getPlayer());
          }
        }
      }
    }
  }
}