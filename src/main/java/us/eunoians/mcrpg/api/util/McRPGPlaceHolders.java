package us.eunoians.mcrpg.api.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.Skills;

public class McRPGPlaceHolders extends PlaceholderExpansion {
  @Override
  public String getIdentifier() {
    return "mcrpg";
  }

  @Override
  public String getPlugin() {
    return McRPG.getInstance().getName();
  }

  @Override
  public String getAuthor() {
    return "DiamondDagger590";
  }

  @Override
  public String getVersion() {
    return "0.1";
  }

  @Override
  public String onPlaceholderRequest(Player player, String identifier) {

    if(player == null) {
      return null;
    }
    McRPGPlayer mp;
    if(player.isOnline()){
      mp = PlayerManager.getPlayer(player.getUniqueId());
    }
    else{
      mp = new McRPGPlayer(player.getUniqueId());
    }
    String [] args = identifier.split("_");
    if(identifier.equalsIgnoreCase("power_level")){
      return Integer.toString(mp.getPowerLevel());
    }
    else if(identifier.contains("_level")){
      Skills skill = Skills.fromString(args[0]);
      return Integer.toString(mp.getSkill(skill).getCurrentLevel());
    }
    else if(identifier.contains("_exp_needed")){
      Skills skill = Skills.fromString(args[0]);
      return Integer.toString(mp.getSkill(skill).getExpToLevel());
    }
    else if(identifier.contains("_exp")){
      Skills skill = Skills.fromString(args[0]);
      return Integer.toString(mp.getSkill(skill).getCurrentExp());
    }
    return "";
  }
}
