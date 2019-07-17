package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.leaderboards.PlayerLeaderboardData;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.types.Skills;

public class SignEvent implements Listener {

  @EventHandler
  public void signChangeEvent(SignChangeEvent e){
    Player p = e.getPlayer();
    if(e.getBlock().getType().name().contains("WALL_SIGN")){
      if(p.hasPermission("mcrpg.*") || p.hasPermission("mcadmin.*") || p.hasPermission("mcadmin.sign")){
        String[] lines = e.getLines();
        if(lines.length > 3){
          if(lines[0].toLowerCase().equalsIgnoreCase("[mcrpg]")){
            if(lines[1].equalsIgnoreCase("power") || Skills.isSkill(lines[1])){
              if(Methods.isInt(lines[2])){
                WallSign sign = (WallSign) e.getBlock().getState().getBlockData();
                Block upBlock = e.getBlock().getRelative(sign.getFacing().getOppositeFace()).getRelative(BlockFace.UP);
                Block upOverBlock = e.getBlock().getLocation().add(0, 1,0).getBlock();
                boolean up = false;
                Location skullLoc;
                int rank = Integer.parseInt(lines[2]);
                if(upBlock.getType() == Material.AIR){
                  skullLoc = upBlock.getLocation();
                  up = true;
                }
                else if(upOverBlock.getType() == Material.AIR){
                  skullLoc = upOverBlock.getLocation();
                }
                else{
                  e.setCancelled(true);
                  return;
                }
                if(lines[1].equalsIgnoreCase("power")){
                  PlayerLeaderboardData playerLeaderboardData = McRPG.getInstance().getLeaderboardManager().getPowerPlayer(rank);
                  if(up){
                    skullLoc.getBlock().setType(Material.PLAYER_HEAD);
                  }
                  else{
                    skullLoc.getBlock().setType(Material.PLAYER_WALL_HEAD);
                  }
                  Skull skull = (Skull) skullLoc.getBlock().getState();
                  OfflinePlayer offlinePlayer = playerLeaderboardData != null ? Bukkit.getOfflinePlayer(playerLeaderboardData.getUUID()) : Bukkit.getOfflinePlayer("Steve");
                  int level = playerLeaderboardData != null ? playerLeaderboardData.getLevel() : 0;
                  skull.setOwningPlayer(offlinePlayer);
                  if(up){
                    skull.setRotation(sign.getFacing().getOppositeFace());
                  }
                  else{
                    skull.setRotation(sign.getFacing());
                  }
                  skull.update();
                  e.setLine(0, Methods.color(McRPG.getInstance().getLangFile().getString("Signs.PowerLeaderboard.Line1").replace("%Rank%", Integer.toString(rank))
                          .replace("%Level%", Integer.toString(level)).replace("%Player%", offlinePlayer.getName())));
                  e.setLine(1, Methods.color(McRPG.getInstance().getLangFile().getString("Signs.PowerLeaderboard.Line2").replace("%Rank%", Integer.toString(rank))
                          .replace("%Level%", Integer.toString(level)).replace("%Player%", offlinePlayer.getName())));
                  e.setLine(2, Methods.color(McRPG.getInstance().getLangFile().getString("Signs.PowerLeaderboard.Line3").replace("%Rank%", Integer.toString(rank))
                          .replace("%Level%", Integer.toString(level)).replace("%Player%", offlinePlayer.getName())));
                  e.setLine(3, Methods.color(McRPG.getInstance().getLangFile().getString("Signs.PowerLeaderboard.Line4").replace("%Rank%", Integer.toString(rank))
                          .replace("%Level%", Integer.toString(level)).replace("%Player%", offlinePlayer.getName())));
                  McRPG.getInstance().getLeaderboardHeadManager().addPowerSign(e.getBlock().getLocation(), skullLoc, rank);
                }
                else{
                  Skills skill = Skills.fromString(lines[1]);
                  PlayerLeaderboardData playerLeaderboardData = McRPG.getInstance().getLeaderboardManager().getSkillPlayer(rank, skill);
                  OfflinePlayer offlinePlayer = playerLeaderboardData != null ? Bukkit.getOfflinePlayer(playerLeaderboardData.getUUID()) : Bukkit.getOfflinePlayer("Steve");
                  int level = playerLeaderboardData != null ? playerLeaderboardData.getLevel() : 0;
                  if(up){
                    skullLoc.getBlock().setType(Material.PLAYER_HEAD);
                  }
                  else{
                    skullLoc.getBlock().setType(Material.PLAYER_WALL_HEAD);
                  }
                  Skull skull = (Skull) skullLoc.getBlock().getState();
                  skull.setOwningPlayer(offlinePlayer);
                  if(up){
                    skull.setRotation(sign.getFacing().getOppositeFace());
                  }
                  else{
                    skull.setRotation(sign.getFacing());
                  }
                  skull.update();
                  e.setLine(0, Methods.color(McRPG.getInstance().getLangFile().getString("Signs.PowerLeaderboard.Line1").replace("%Skill%", skill.getDisplayName()).replace("%Rank%", Integer.toString(rank))
                          .replace("%Level%", Integer.toString(level)).replace("%Player%", offlinePlayer.getName())));
                  e.setLine(1, Methods.color(McRPG.getInstance().getLangFile().getString("Signs.PowerLeaderboard.Line2").replace("%Skill%", skill.getDisplayName()).replace("%Rank%", Integer.toString(rank))
                          .replace("%Level%", Integer.toString(level)).replace("%Player%", offlinePlayer.getName())));
                  e.setLine(2, Methods.color(McRPG.getInstance().getLangFile().getString("Signs.PowerLeaderboard.Line3").replace("%Skill%", skill.getDisplayName()).replace("%Rank%", Integer.toString(rank))
                          .replace("%Level%", Integer.toString(level)).replace("%Player%", offlinePlayer.getName())));
                  e.setLine(3, Methods.color(McRPG.getInstance().getLangFile().getString("Signs.PowerLeaderboard.Line4").replace("%Skill%", skill.getDisplayName()).replace("%Rank%", Integer.toString(rank))
                          .replace("%Level%", Integer.toString(level)).replace("%Player%", offlinePlayer.getName())));
                  McRPG.getInstance().getLeaderboardHeadManager().addSkillSign(e.getBlock().getLocation(), skullLoc, rank, skill);
                }
              }
            }
          }
        }
      }
    }
  }
}
