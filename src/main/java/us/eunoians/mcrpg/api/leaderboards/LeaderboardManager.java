package us.eunoians.mcrpg.api.leaderboards;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.Skills;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class LeaderboardManager{
  
  private List<PlayerLeaderboardData> powerLevel;
  private Map<Skills, List<PlayerLeaderboardData>> skillSets;
  
  private Map<Skills, Map<UUID, Integer>> playerSkillRanks;
  private Map<UUID, Integer> playerPowerMap;
  
  private static PreparedStatement powerLevelStatement;
  private static Map<Skills, PreparedStatement> skillsPreparedStatementMap = new HashMap<>();
  private List<UUID> loadingPlayers = new ArrayList<>();
  private long lastTimeRan = 0;
  private long lastTimeCompleted = 0;
  private McRPG plugin;
  private static final Calendar cal = Calendar.getInstance();
  
  public LeaderboardManager(McRPG plugin){
    this.plugin = plugin;
    try{
      powerLevelStatement = plugin.getMcRPGDb().getDatabase().getConnection().prepareStatement("SELECT uuid, power_level FROM mcrpg_player_data ORDER BY power_level DESC ");
      for(Skills skill : Skills.values()){
        PreparedStatement statement = plugin.getMcRPGDb().getDatabase().getConnection()
                                        .prepareStatement("SELECT uuid, current_level FROM mcrpg_" + skill.getName().toLowerCase() + "_data ORDER BY current_level DESC ");
        skillsPreparedStatementMap.put(skill, statement);
      }
      startLeaderBoardTask();
    }catch(SQLException e){
      e.printStackTrace();
    }
  }
  
  public boolean hasInit(){
    return lastTimeCompleted != 0;
  }
  
  public List<PlayerLeaderboardData> getPowerPage(int page){
    if((10 * page) - 10 > powerLevel.size()){
      page = 1;
    }
    int max = 10 * page <= powerLevel.size() ? 10 * page : powerLevel.size();
    return powerLevel.subList((10 * page) - 10, max);
  }
  
  public List<PlayerLeaderboardData> getSkillPage(int page, Skills skill){
    if((10 * page) - 10 > skillSets.get(skill).size()){
      page = 1;
    }
    int max = 10 * page <= skillSets.get(skill).size() ? 10 * page : skillSets.get(skill).size();
    return skillSets.get(skill).subList((10 * page) - 10, max);
  }
  
  public int getPlayersPowerRank(UUID uuid){
    if(playerPowerMap != null && playerPowerMap.containsKey(uuid)){
      return playerPowerMap.get(uuid);
    }
    else{
      return -1;
    }
  }
  
  public int getPlayersSkillRank(UUID uuid, Skills skill){
    if(playerSkillRanks != null && playerSkillRanks.containsKey(skill) && playerSkillRanks.get(skill).containsKey(uuid)){
      return playerSkillRanks.get(skill).get(uuid);
    }
    else{
      return -1;
    }
  }
  
  public PlayerLeaderboardData getPowerPlayer(int rank){
    if(powerLevel.size() <= rank - 1){
      Bukkit.getLogger().log(Level.WARNING, Methods.color("&cYou are trying to get the #" + rank + "s player power level and it does not exist. This may be because you are using a placeholder, if so ignore this"));
      return null;
    }
    return powerLevel.get(rank - 1);
  }
  
  public PlayerLeaderboardData getSkillPlayer(int rank, Skills skill){
    if(skillSets.get(skill).size() <= rank - 1){
      Bukkit.getLogger().log(Level.WARNING, Methods.color("&cYou are trying to get the #" + rank + "s player " + skill.getName() + " level and it does not exist. This may be because you are using a placeholder, if so ignore this"));
      return null;
    }
    return skillSets.get(skill).get(rank - 1);
  }
  
  public boolean updateRank(McRPGPlayer player, String type){
    if(type.equalsIgnoreCase("power") || type.equalsIgnoreCase("powerlevel")){
      if(player.getPowerRank() == null || player.getPowerRank().getLastTimeUpdated() > lastTimeCompleted){
        generatePlayerPowerRank(player);
        return true;
      }
      else{
        return false;
      }
    }
    else if(Skills.isSkill(type)){
      Skills skill = Skills.fromString(type);
      if(!player.getSkillRanks().containsKey(skill) || player.getSkillRanks().get(skill).getLastTimeUpdated() > lastTimeCompleted){
        generatePlayerSkillRank(player, skill);
        return true;
      }
      else{
        return false;
      }
    }
    else{
      return false;
    }
  }
  
  public boolean isLoading(McRPGPlayer player){
    return loadingPlayers.contains(player.getUuid());
  }
  
  private void generatePlayerPowerRank(McRPGPlayer player){
    loadingPlayers.add(player.getUuid());
    new BukkitRunnable(){
      @Override
      public void run(){
        int i = 0;
        for(PlayerLeaderboardData data : powerLevel){
          i++;
          if(data.getUUID().equals(player.getUuid())){
            break;
          }
        }
        if(PlayerManager.isPlayerStored(player.getUuid())){
          player.setPowerRank(new PlayerRank(i, cal.getTimeInMillis()));
        }
        loadingPlayers.remove(player.getUuid());
      }
    }.runTaskAsynchronously(plugin);
  }
  
  private void generatePlayerSkillRank(McRPGPlayer player, Skills skill){
    loadingPlayers.add(player.getUuid());
    new BukkitRunnable(){
      @Override
      public void run(){
        int i = 0;
        List<PlayerLeaderboardData> playerLeaderboardData = skillSets.get(skill);
        for(PlayerLeaderboardData data : playerLeaderboardData){
          i++;
          if(data.getUUID().equals(player.getUuid())){
            break;
          }
        }
        if(PlayerManager.isPlayerStored(player.getUuid())){
          player.getSkillRanks().put(skill, new PlayerRank(i, cal.getTimeInMillis()));
        }
        loadingPlayers.remove(player.getUuid());
      }
    }.runTaskAsynchronously(plugin);
  }
  
  private void startLeaderBoardTask(){
    new BukkitRunnable(){
      @Override
      public void run(){
        if(lastTimeRan == 0){
          updateLeaderboards();
        }
        else if(lastTimeRan < lastTimeCompleted){
          updateLeaderboards();
        }
      }
    }.runTaskTimer(plugin, 5 * 20, 5 * 60 * 20);
  }
  
  private void updateLeaderboards(){
    lastTimeRan = cal.getTimeInMillis();
    new BukkitRunnable(){
      @Override
      public void run(){
        List<PlayerLeaderboardData> newPowerLevel = new ArrayList<>();
        Map<Skills, List<PlayerLeaderboardData>> newSkillSets = new HashMap<>();
        Map<Skills, Map<UUID, Integer>> newPlayerSkillsMap = new HashMap<>();
        Map<UUID, Integer> newPlayerPowerMap = new HashMap<>();
        try{
          ResultSet resultSet = powerLevelStatement.executeQuery();
          while(resultSet.next()){
            UUID uuid = UUID.fromString(resultSet.getString("uuid"));
            int level = resultSet.getInt("power_level");
            newPowerLevel.add(new PlayerLeaderboardData(uuid, level));
            newPlayerPowerMap.put(uuid, newPowerLevel.size() + 1);
          }
          //newPowerLevel.sort(Comparator.comparingInt(PlayerLeaderboardData::getLevel));
          for(Skills skill : Skills.values()){
            ResultSet results = skillsPreparedStatementMap.get(skill).executeQuery();
            List<PlayerLeaderboardData> playerLeaderboardData = new ArrayList<>();
            Map<UUID, Integer> skillMap = new HashMap<>();
            while(results.next()){
              UUID uuid = UUID.fromString(results.getString("uuid"));
              int level = results.getInt("current_level");
              playerLeaderboardData.add(new PlayerLeaderboardData(uuid, level));
              skillMap.put(uuid, playerLeaderboardData.size() + 1);
            }
            //playerLeaderboardData.sort(Comparator.comparingInt(PlayerLeaderboardData::getLevel));
            newPlayerSkillsMap.put(skill, skillMap);
            newSkillSets.put(skill, playerLeaderboardData);
          }
          powerLevel = newPowerLevel;
          skillSets = newSkillSets;
          playerPowerMap = newPlayerPowerMap;
          playerSkillRanks = newPlayerSkillsMap;
          lastTimeCompleted = cal.getTimeInMillis();
        }catch(SQLException e){
          e.printStackTrace();
        }
      }
    }.runTaskTimerAsynchronously(McRPG.getInstance(), 0L, 5 * 60 * 20);
  }
}
