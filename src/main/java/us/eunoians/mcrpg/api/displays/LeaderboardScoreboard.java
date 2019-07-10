package us.eunoians.mcrpg.api.displays;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.leaderboards.LeaderboardManager;
import us.eunoians.mcrpg.api.leaderboards.PlayerLeaderboardData;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.DisplayType;
import us.eunoians.mcrpg.types.LeaderboardType;
import us.eunoians.mcrpg.types.Skills;

import java.util.List;

public class LeaderboardScoreboard extends GenericDisplay implements ScoreboardBase, LeaderboardDisplay, DecayableDisplay{

    private Scoreboard oldBoard;

    private static ScoreboardManager manager = Bukkit.getScoreboardManager();
    @Getter
    private Scoreboard board;

    @Getter
    private LeaderboardType leaderboardType;
    @Getter
    private List<PlayerLeaderboardData> storedData;
    @Getter
    private Skills storedSkill;
    @Getter
    private int page;

    private int displayTime;
    private BukkitTask endTask;

    /**
     *
     * @param player Player that the scoreboard belongs to
     * @param leaderboardType The type of leaderboard we are getting
     * @param displayTime Duration for which the board needs to be displayed
     */
    public LeaderboardScoreboard(McRPGPlayer player, LeaderboardType leaderboardType, int displayTime, int page){
        super(player, DisplayType.SCOREBOARD);
        this.leaderboardType = leaderboardType;
        this.displayTime = displayTime;
        this.page = page;
        createScoreboard();
        updateEndTime();
    }

    /**
     *
     * @param player Player that the scoreboard belongs to
     * @param leaderboardType The type of leaderboard we are getting
     * @param skill the skill for the leaderboard
     * @param displayTime Duration for which the board needs to be displayed
     */
    public LeaderboardScoreboard(McRPGPlayer player, LeaderboardType leaderboardType, Skills skill, int displayTime, int page){
        super(player, DisplayType.SCOREBOARD);
        this.leaderboardType = leaderboardType;
        this.displayTime = displayTime;
        this.storedSkill = skill;
        this.page = page;
        createScoreboard();
        updateEndTime();
    }

    /**
     *
     * @param player Player that the scoreboard belongs to
     * @param leaderboardType The type of leaderboard we are getting
     * @param displayTime Duration for which the board needs to be displayed
     */
    public LeaderboardScoreboard(McRPGPlayer player, LeaderboardType leaderboardType, Scoreboard old, int displayTime, int page){
        super(player, DisplayType.SCOREBOARD);
        this.leaderboardType = leaderboardType;
        this.displayTime = displayTime;
        this.oldBoard = old;
        this.page = page;
        createScoreboard();
        updateEndTime();
    }

    /**
     *
     * @param player Player that the scoreboard belongs to
     * @param leaderboardType The type of leaderboard we are getting
     * @param skill the skill for the leaderboard
     * @param displayTime Duration for which the board needs to be displayed
     */
    public LeaderboardScoreboard(McRPGPlayer player, LeaderboardType leaderboardType, Scoreboard old, Skills skill, int displayTime, int page){
        super(player, DisplayType.SCOREBOARD);
        this.leaderboardType = leaderboardType;
        this.displayTime = displayTime;
        this.storedSkill = skill;
        this.oldBoard = old;
        this.page = page;
        createScoreboard();
        updateEndTime();
    }

    public void nextPage(){
        page += 1;
        createScoreboard();
    }

    public void previousPage(){
        page = page - 1 >= 0 ? page - 1  : 0;
        createScoreboard();
    }

    private void createScoreboard(){
        FileConfiguration config = McRPG.getInstance().getConfig();
        board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("mcrpg", "dummy");
        //String displayName = config.getString("DisplayConfig.LeaderboardScoreboard.DisplayName").replace("%Player%", player.getPlayer().getDisplayName());
        LeaderboardManager leaderboardManager = McRPG.getInstance().getLeaderboardManager();
        int i = 1 + ((page-1) * 10);

        int min = i;
        if(leaderboardType == LeaderboardType.POWER){
            this.storedData = leaderboardManager.getPowerPage(page);
            for(PlayerLeaderboardData leaderboardData : storedData){
                String name = leaderboardData.getUUID().equals(player.getUuid()) ? "&6--You--" : Bukkit.getOfflinePlayer(leaderboardData.getUUID()).getName();
                objective.getScore(Methods.color(name)).setScore(leaderboardData.getLevel());
                i++;
            }
            //objective.getScore(Methods.color("&eYou:")).setScore(player.getPowerRank().getRank());
        }
        else{
            this.storedData = leaderboardManager.getSkillPage(page, storedSkill);
            for(PlayerLeaderboardData leaderboardData : storedData){
                String name = leaderboardData.getUUID().equals(player.getUuid()) ? "&6--You--" : Bukkit.getOfflinePlayer(leaderboardData.getUUID()).getName();
                objective.getScore(Methods.color(name)).setScore(leaderboardData.getLevel());
                i++;
            }
            //objective.getScore(Methods.color("&eYou:")).setScore(player.getSkillRanks().get(storedSkill).getRank());
        }
        String displayName = Methods.color("&a%Type% (" + min + "-" + (i - 1) + ")"); //config.getString("DisplayConfig.LeaderboardScoreboard.DisplayName").replace("%Player%", player.getPlayer().getDisplayName());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        if(leaderboardType == LeaderboardType.POWER){
            displayName = displayName.replace("%Type%", "Power");
        }
        else{
            displayName = displayName.replace("%Type%", storedSkill.getDisplayName());
        }
        objective.setDisplayName(Methods.color(player.getPlayer(), displayName));
    }

    /**
     * Cancel the display
     */
    @Override
    public void cancel(){
        if(hasOldScoreBoard()){
            player.getPlayer().setScoreboard(oldBoard);
            return;
        }
        board = manager.getNewScoreboard();
        player.getPlayer().setScoreboard(board);
    }

    /**
     *
     * @return true if the player has an old baord
     */
    public boolean hasOldScoreBoard(){
        return oldBoard != null;
    }

    /**
     *
     * @return The old scoreboard a player has
     */
    public Scoreboard getOldScoreBoard(){
        return this.oldBoard;
    }

    @Override
    public int getDisplayTime(){
        return displayTime;
    }

    private void updateEndTime(){
        if(endTask != null){
            endTask.cancel();
        }
        endTask = new BukkitRunnable(){
            @Override
            public void run(){

                McRPG.getInstance().getDisplayManager().removePlayersDisplay(player.getUuid());
            }
        }.runTaskLater(McRPG.getInstance(), displayTime * 20);
    }
}
