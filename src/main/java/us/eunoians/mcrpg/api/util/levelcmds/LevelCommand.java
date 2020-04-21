package us.eunoians.mcrpg.api.util.levelcmds;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.types.LevelCommandType;

public class LevelCommand{
  
  @Getter @Setter
  String command;
  
  @Getter
  private int level;
  
  @Getter
  private LevelCommandType levelCommandType;
  
  public LevelCommand(String command, int level, LevelCommandType levelCommandType){
    this.command = command;
    this.level = level;
    this.levelCommandType = levelCommandType;
  }
  
  public void runForPlayer(Player player){
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Methods.color(command.replace("%Player%", player.getName())));
  }
}
