package us.eunoians.mcrpg.commands.prompts;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import us.eunoians.mcrpg.types.Skills;

import java.util.ArrayList;
import java.util.List;

public class McExpPrompt implements TabCompleter{
  
  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String commandLable, String[] args){
  
    List<String> completions = new ArrayList<>();
    Player p = (Player) sender;
    if(args.length == 1){
      for(Skills skill : Skills.values()){
        if(skill.isEnabled()){
          completions.add(skill.getDisplayName());
        }
      }
    }
    return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
  }
}
