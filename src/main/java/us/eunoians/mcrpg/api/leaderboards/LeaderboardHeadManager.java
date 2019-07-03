package us.eunoians.mcrpg.api.leaderboards;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.FileConfiguration;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.types.Skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class LeaderboardHeadManager {

    private Map<Location, Integer> powerLevelSigns = new HashMap<>();
    private Map<Location, Location> signToSkullMap = new HashMap<>();
    private Map<Location, SkillDataWrapper> skilLevelSigns = new HashMap<>();


    public LeaderboardHeadManager(){
        FileConfiguration fileConfiguration = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SIGN_CONFIG);
        List<String> toRemove = new ArrayList<>();
        for(String s : fileConfiguration.getConfigurationSection("").getKeys(false)){
            Location loc = stringToLoc(s);
            if(loc == null){
                toRemove.add(s);
            }
            int rank = fileConfiguration.getInt(s + ".Rank");

        }
        for(String s : toRemove){
            fileConfiguration.set(s, null);
        }
        McRPG.getInstance().getFileManager().saveFile(FileManager.Files.SIGN_CONFIG);
    }


    private void updateSigns(){
        ArrayList<Location> removePowerLocs = new ArrayList<>();
        ArrayList<Location> removeSkillLocs = new ArrayList<>();
        for(Location loc : powerLevelSigns.keySet()){
            if(loc.getChunk().isLoaded()){
                Location skullLoc = signToSkullMap.get(loc);
                if(!skullLoc.getChunk().isLoaded()){
                    continue;
                }
                if(!(loc.getBlock().getState() instanceof Sign)){
                    Bukkit.getLogger().log(Level.SEVERE, Methods.color("&cThe rank sign placed at " + loc.toString() + " is broken and will be unregistered"));
                    removePowerLocs.add(loc);
                    signToSkullMap.remove(loc);
                    continue;
                }
                else if(!(skullLoc.getBlock().getState() instanceof Skull)){
                    Bukkit.getLogger().log(Level.SEVERE, Methods.color("&cThe player skull placed at " + skullLoc.toString() + " is broken and will be unregistered"));
                    removePowerLocs.add(loc);
                    signToSkullMap.remove(loc);
                    continue;
                }
                Sign sign = (Sign) loc.getBlock().getState();
                PlayerLeaderboardData playerLeaderboardData = McRPG.getInstance().getLeaderboardManager().getPowerPlayer(powerLevelSigns.get(loc));
                sign.setLine(1, Bukkit.getOfflinePlayer(playerLeaderboardData.getUUID()).getName());
                sign.setLine(2, "Level: " + playerLeaderboardData.getLevel());
                sign.update();
                Skull skull = (Skull) skullLoc.getBlock().getState();
                skull.setOwningPlayer(Bukkit.getOfflinePlayer(playerLeaderboardData.getUUID()));
                skull.update();
            }
        }
        for(Location loc : skilLevelSigns.keySet()){
            if(loc.getChunk().isLoaded()){
                Location skullLoc = signToSkullMap.get(loc);
                if(!skullLoc.getChunk().isLoaded()){
                    continue;
                }
                if(!(loc.getBlock().getState() instanceof Sign)){
                    Bukkit.getLogger().log(Level.SEVERE, Methods.color("&cThe rank sign placed at " + loc.toString() + " is broken and will be unregistered"));
                    removeSkillLocs.add(loc);
                    signToSkullMap.remove(loc);
                    continue;
                }
                else if(!(skullLoc.getBlock().getState() instanceof Skull)){
                    Bukkit.getLogger().log(Level.SEVERE, Methods.color("&cThe player skull placed at " + skullLoc.toString() + " is broken and will be unregistered"));
                    removeSkillLocs.add(loc);
                    signToSkullMap.remove(loc);
                    continue;
                }
                Sign sign = (Sign) loc.getBlock().getState();
                PlayerLeaderboardData playerLeaderboardData = McRPG.getInstance().getLeaderboardManager().getSkillPlayer(skilLevelSigns.get(loc).rank, skilLevelSigns.get(loc).skill);
                sign.setLine(1, Bukkit.getOfflinePlayer(playerLeaderboardData.getUUID()).getName());
                sign.setLine(2, "Level: " + playerLeaderboardData.getLevel());
                sign.update();
                Skull skull = (Skull) skullLoc.getBlock().getState();
                skull.setOwningPlayer(Bukkit.getOfflinePlayer(playerLeaderboardData.getUUID()));
                skull.update();
            }
        }
    }

    private String locToString(Location loc) {
        return loc.getBlockX() + "|" + loc.getBlockY() + "|" + loc.getBlockZ() + "|" + loc.getWorld().getName();
    }

    private Location stringToLoc(String loc) {
        String[] args = loc.split("|");
        World w = Bukkit.getWorld(args[3]);
        if(w == null){
            return null;
        }
        return new Location(w, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }

    private class SkillDataWrapper{
        @Getter
        private Skills skill;
        @Getter
        private int rank;

        public SkillDataWrapper(Skills skill, int rank){
            this.skill = skill;
            this.rank = rank;
        }
    }
}
