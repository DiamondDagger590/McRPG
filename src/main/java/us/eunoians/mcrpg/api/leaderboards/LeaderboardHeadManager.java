package us.eunoians.mcrpg.api.leaderboards;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
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
    private Map<Location, SkillDataWrapper> skillLevelSigns = new HashMap<>();


    public LeaderboardHeadManager(){
        FileConfiguration fileConfiguration = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SIGN_CONFIG);
        List<String> toRemovePower = new ArrayList<>();
        List<String> toRemoveSkills = new ArrayList<>();
        if(fileConfiguration.contains("Power")){
            for(String s : fileConfiguration.getConfigurationSection("Power").getKeys(false)){
                Location loc = stringToLoc(s);
                if(loc == null){
                    toRemovePower.add(s);
                }
                int rank = fileConfiguration.getInt("Power." + s + ".Rank");
                powerLevelSigns.put(loc, rank);
                Location skull = stringToLoc(fileConfiguration.getString("Power." + s + ".Skull"));
                signToSkullMap.put(loc, skull);
            }
        }
        if(fileConfiguration.contains("Skills")){
            for(String s : fileConfiguration.getConfigurationSection("Skills").getKeys(false)){
                Location loc = stringToLoc(s);
                if(loc == null){
                    toRemoveSkills.add(s);
                }
                Skills skill = Skills.fromString(fileConfiguration.getString("Skills." + s + ".Skill"));
                int rank = fileConfiguration.getInt("Skills." + s + ".Rank");
                SkillDataWrapper dataWrapper = new SkillDataWrapper(skill, rank);
                skillLevelSigns.put(loc, dataWrapper);
                Location skull = stringToLoc(fileConfiguration.getString("Skills." + s + ".Skull"));
                signToSkullMap.put(loc, skull);
            }
        }
        for(String s : toRemovePower){
            fileConfiguration.set("Power." + s, null);
        }
        for(String s : toRemoveSkills){
            fileConfiguration.set("Skills." + s, null);
        }
        McRPG.getInstance().getFileManager().saveFile(FileManager.Files.SIGN_CONFIG);

        new BukkitRunnable(){
            @Override
            public void run(){
                updateSigns();
            }
        }.runTaskTimer(McRPG.getInstance(), 1 * 60 * 20, 5 * 60 * 20);
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
                if(playerLeaderboardData == null){
                    continue;
                }
                int rank = powerLevelSigns.get(loc);
                int level = playerLeaderboardData.getLevel();
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerLeaderboardData.getUUID());
                sign.setLine(0, Methods.color(McRPG.getInstance().getLangFile().getString("Signs.PowerLeaderboard.Line1").replace("%Rank%", Integer.toString(rank))
                        .replace("%Level%", Integer.toString(level)).replace("%Player%", offlinePlayer.getName())));
                sign.setLine(1, Methods.color(McRPG.getInstance().getLangFile().getString("Signs.PowerLeaderboard.Line2").replace("%Rank%", Integer.toString(rank))
                        .replace("%Level%", Integer.toString(level)).replace("%Player%", offlinePlayer.getName())));
                sign.setLine(2, Methods.color(McRPG.getInstance().getLangFile().getString("Signs.PowerLeaderboard.Line3").replace("%Rank%", Integer.toString(rank))
                        .replace("%Level%", Integer.toString(level)).replace("%Player%", offlinePlayer.getName())));
                sign.setLine(3, Methods.color(McRPG.getInstance().getLangFile().getString("Signs.PowerLeaderboard.Line4").replace("%Rank%", Integer.toString(rank))
                        .replace("%Level%", Integer.toString(level)).replace("%Player%", offlinePlayer.getName())));
                sign.update();
                Skull skull = (Skull) skullLoc.getBlock().getState();
                if(!skull.getOwningPlayer().getUniqueId().equals(playerLeaderboardData.getUUID())){
                    skull.setOwningPlayer(Bukkit.getOfflinePlayer(playerLeaderboardData.getUUID()));
                    skull.update();
                }
            }
        }
        for(Location loc : skillLevelSigns.keySet()){
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
                PlayerLeaderboardData playerLeaderboardData = McRPG.getInstance().getLeaderboardManager().getSkillPlayer(skillLevelSigns.get(loc).rank, skillLevelSigns.get(loc).skill);
                if(playerLeaderboardData == null){
                    continue;
                }
                Skills skill = skillLevelSigns.get(loc).skill;
                int rank = skillLevelSigns.get(loc).rank;
                int level = playerLeaderboardData.getLevel();
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerLeaderboardData.getUUID());
                sign.setLine(0, Methods.color(McRPG.getInstance().getLangFile().getString("Signs.PowerLeaderboard.Line1").replace("%Skill%", skill.getDisplayName()).replace("%Rank%", Integer.toString(rank))
                        .replace("%Level%", Integer.toString(level)).replace("%Player%", offlinePlayer.getName())));
                sign.setLine(1, Methods.color(McRPG.getInstance().getLangFile().getString("Signs.PowerLeaderboard.Line2").replace("%Skill%", skill.getDisplayName()).replace("%Rank%", Integer.toString(rank))
                        .replace("%Level%", Integer.toString(level)).replace("%Player%", offlinePlayer.getName())));
                sign.setLine(2, Methods.color(McRPG.getInstance().getLangFile().getString("Signs.PowerLeaderboard.Line3").replace("%Skill%", skill.getDisplayName()).replace("%Rank%", Integer.toString(rank))
                        .replace("%Level%", Integer.toString(level)).replace("%Player%", offlinePlayer.getName())));
                sign.setLine(3, Methods.color(McRPG.getInstance().getLangFile().getString("Signs.PowerLeaderboard.Line4").replace("%Skill%", skill.getDisplayName()).replace("%Rank%", Integer.toString(rank))
                        .replace("%Level%", Integer.toString(level)).replace("%Player%", offlinePlayer.getName())));
                sign.update();
                Skull skull = (Skull) skullLoc.getBlock().getState();
                if(!skull.getOwningPlayer().getUniqueId().equals(playerLeaderboardData.getUUID())){
                    skull.setOwningPlayer(Bukkit.getOfflinePlayer(playerLeaderboardData.getUUID()));
                    skull.update();
                }
            }
        }
        for(Location loc : removePowerLocs){
            powerLevelSigns.remove(loc);
            removeSign("Power." + locToString(loc));
        }
        for(Location loc : removeSkillLocs){
            skillLevelSigns.remove(loc);
            removeSign("Skills." + locToString(loc));
        }
    }

    public void removeSign(String path){
        McRPG.getInstance().getFileManager().getFile(FileManager.Files.SIGN_CONFIG).set(path, null);
        McRPG.getInstance().getFileManager().saveFile(FileManager.Files.SIGN_CONFIG);
    }

    public void addPowerSign(Location signLoc, Location skullLoc, int rank){
        powerLevelSigns.put(signLoc, rank);
        signToSkullMap.put(signLoc, skullLoc);
        FileConfiguration fileConfiguration = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SIGN_CONFIG);
        fileConfiguration.set("Power." + locToString(signLoc) + ".Rank", rank);
        fileConfiguration.set("Power." + locToString(signLoc) + ".Skull", locToString(skullLoc));
        McRPG.getInstance().getFileManager().saveFile(FileManager.Files.SIGN_CONFIG);
    }

    public void addSkillSign(Location signLoc, Location skullLoc, int rank, Skills skill){
        skillLevelSigns.put(signLoc, new SkillDataWrapper(skill, rank));
        signToSkullMap.put(signLoc, skullLoc);
        FileConfiguration fileConfiguration = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SIGN_CONFIG);
        fileConfiguration.set("Skills." + locToString(signLoc) + ".Rank", rank);
        fileConfiguration.set("Skills." + locToString(signLoc) + ".Skill", skill.getName());
        fileConfiguration.set("Skills." + locToString(signLoc) + ".Skull", locToString(skullLoc));
        McRPG.getInstance().getFileManager().saveFile(FileManager.Files.SIGN_CONFIG);
    }

    private String locToString(Location loc) {
        return loc.getBlockX() + "&" + loc.getBlockY() + "&" + loc.getBlockZ() + "&" + loc.getWorld().getName();
    }

    private Location stringToLoc(String loc) {
        String[] args = loc.split("&");
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
