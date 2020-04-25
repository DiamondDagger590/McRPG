package us.eunoians.mcrpg.commands.prompts;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.AbilityType;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.ArrayList;
import java.util.List;

public class McAdminPrompt implements TabCompleter {

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String commandLable, String[] args){

    List<String> completions = new ArrayList<>();
    Player p = (Player) sender;
    if(args.length == 1){
      completions.add("give");
      completions.add("replace");
      completions.add("remove");
      completions.add("view");
      completions.add("cooldown");
      completions.add("reset");
      completions.add("party");
      return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
    }
    else if(args.length == 2){
      switch(args[0].toLowerCase()){
        case "give":
          completions.add("abilitypoints");
          completions.add("exp");
          completions.add("level");
          completions.add("ability");
          completions.add("book");
          break;
        case "replace":
          for(Player player : Bukkit.getOnlinePlayers()){
            completions.add(player.getName());
          }
          break;
        case "remove":
          for(Player player : Bukkit.getOnlinePlayers()){
            completions.add(player.getName());
          }
          break;
        case "view":
          completions.add("loadout");
          for(Skills skill : Skills.values()){
            completions.add(skill.getName());
          }
          break;
        case "cooldown":
          completions.add("set");
          completions.add("remove");
          completions.add("add");
          break;
        case "reset":
          completions.add("skill");
          completions.add("ability");
          completions.add("player");
          break;
        case "party":
          completions.add("fdisband");
          completions.add("fkick");
          completions.add("fsetowner");
          completions.add("name");
          completions.add("give");
          break;
      }
      return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
    }
    else if(args.length == 3){
      //second argument
      switch(args[0].toLowerCase()){
        case "give":
          switch(args[1].toLowerCase()){
            case "abilitypoints":
            case "level":
            case "exp":
            case "ability":
              for(Player player : Bukkit.getOnlinePlayers()){
                completions.add(player.getName());
              }
              break;
            case "book":
              completions.add("upgrade");
              completions.add("unlock");
              break;
          }
          break;
        case "replace":
          if(Bukkit.getOfflinePlayer(args[1]).isOnline()){
            Player player = (Player) Bukkit.getOfflinePlayer(args[1]);
            try{
              McRPGPlayer mp = PlayerManager.getPlayer(player.getUniqueId());
              for(UnlockedAbilities ab : mp.getAbilityLoadout()){
                completions.add(ab.getName());
              }
            } catch(McRPGPlayerNotFoundException e){
            }
          }
          break;
        case "remove":
          if(Bukkit.getOfflinePlayer(args[1]).isOnline()){
            Player player = (Player) Bukkit.getOfflinePlayer(args[1]);
            try{
              McRPGPlayer mp = PlayerManager.getPlayer(player.getUniqueId());
              for(UnlockedAbilities ab : mp.getAbilityLoadout()){
                completions.add(ab.getName());
              }
            } catch(McRPGPlayerNotFoundException e){
            }
          }
          break;
        case "view":
          if(args[1].equalsIgnoreCase("loadout") || Skills.isSkill(args[1])){
            for(Player player : Bukkit.getOnlinePlayers()){
              completions.add(player.getName());
            }
            break;
          }
          break;
        case "cooldown":
          switch(args[1].toLowerCase()){
            case "set":
            case "add":
            case "remove":
              for(Player player : Bukkit.getOnlinePlayers()){
                completions.add(player.getName());
              }
              break;
          }
          break;
        case "reset":
          switch(args[1].toLowerCase()){
            case "skill":
            case "ability":
            case "player":
              for(Player player : Bukkit.getOnlinePlayers()){
                completions.add(player.getName());
              }
          }
          break;
        case "party":
          switch(args[1].toLowerCase()){
            case "fdisband":
              for(Player player : Bukkit.getOnlinePlayers()){
                completions.add(player.getName());
              }
              break;
            case "fkick":
              for(Player player : Bukkit.getOnlinePlayers()){
                completions.add(player.getName());
              }
              break;
            case "fsetowner":
              for(Player player : Bukkit.getOnlinePlayers()){
                completions.add(player.getName());
              }
              break;
            case "name":
              for(Player player : Bukkit.getOnlinePlayers()){
                completions.add(player.getName());
              }
              break;
            case "give":
              completions.add("exp");
              completions.add("level");
              break;
          }
          break;
      }
      return StringUtil.copyPartialMatches(args[2], completions, new ArrayList<>());
    }
    else if(args.length == 4){
      //second argument
      switch(args[0].toLowerCase()){
        case "give":
          switch(args[1].toLowerCase()){
            case "abilitypoints":
            case "level":
            case "exp":
              completions.add("1");
              break;
            case "ability":
              for(UnlockedAbilities ab : UnlockedAbilities.values()){
                completions.add(ab.getName().toLowerCase());
              }
              break;
            case "book":
              switch(args[2].toLowerCase()){
                case "unlock":
                case "upgrade":
                  for(Player player : Bukkit.getOnlinePlayers()){
                    completions.add(player.getName());
                  }
              }
              break;
          }
          break;
        case "replace":
          if(Bukkit.getOfflinePlayer(args[1]).isOnline()){
            Player player = (Player) Bukkit.getOfflinePlayer(args[1]);
            try{
              McRPGPlayer mp = PlayerManager.getPlayer(player.getUniqueId());
              for(UnlockedAbilities ab : UnlockedAbilities.values()){
                if(!mp.getAbilityLoadout().contains(ab)){
                  completions.add(ab.getName());
                }
              }
            } catch(McRPGPlayerNotFoundException e){
            }
          }
          break;
        case "cooldown":
          switch(args[1].toLowerCase()){
            case "set":
            case "add":
            case "remove":
              if(Bukkit.getOfflinePlayer(args[2]).isOnline()){
                Player player = (Player) Bukkit.getOfflinePlayer(args[2]);
                try{
                  McRPGPlayer mp = PlayerManager.getPlayer(player.getUniqueId());
                  for(UnlockedAbilities ab : mp.getAbilityLoadout()){
                    if(ab.getAbilityType() == AbilityType.ACTIVE){
                      completions.add(ab.getName());
                    }
                  }
                  if(mp.getEndTimeForReplaceCooldown() > 0){
                    completions.add("replace");
                  }
                } catch(McRPGPlayerNotFoundException e){
                }
              }

          }
          break;
        case "reset":
          switch(args[1].toLowerCase()){
            case "skill":
              for(Skills skill : Skills.values()){
                completions.add(skill.getName().toLowerCase());
              }
              break;
            case "ability":
              for(UnlockedAbilities ab : UnlockedAbilities.values()){
                completions.add(ab.getName());
              }
              break;
          }
          break;
        case "party":
          switch(args[1].toLowerCase()){
            case "name":
              completions.add("name");
              break;
            case "give":
              switch(args[2].toLowerCase()){
                case "exp":
                case "level":
                  completions.add("1");
                  break;
              }
              break;
          }
          break;
      }
      return StringUtil.copyPartialMatches(args[3], completions, new ArrayList<>());
    }
    else if(args.length == 5){
      //second argument
      switch(args[0].toLowerCase()){
        case "give":
          switch(args[1].toLowerCase()){
            case "level":
            case "exp":
              for(Skills skill : Skills.values()){
                completions.add(skill.getName().toLowerCase());
              }
              break;
          }
          break;
        case "cooldown":
          switch(args[1].toLowerCase()){
            case "set":
            case "add":
              completions.add("1");
          }
          break;
        case "party":
          switch(args[1].toLowerCase()){
            case "give":
              switch(args[2].toLowerCase()){
                case "exp":
                case "level":
                  for(Player player : Bukkit.getOnlinePlayers()){
                    completions.add(player.getName());
                  }
                  break;
              }
              break;
          }
          break;
      }
      return StringUtil.copyPartialMatches(args[4], completions, new ArrayList<>());
    }
    return null;
  }
}
