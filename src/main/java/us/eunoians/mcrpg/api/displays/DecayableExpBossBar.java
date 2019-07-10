package us.eunoians.mcrpg.api.displays;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.skills.Skill;
import us.eunoians.mcrpg.types.DisplayType;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.util.Parser;

public class DecayableExpBossBar extends GenericDisplay implements BossbarBase, ExpDisplayType, DecayableDisplay {

  private BossBar expBar;

  @Getter private Skills skill;

  private int displayTime;

  private BukkitTask endTask;

  public DecayableExpBossBar(McRPGPlayer player, Skills skill, int displayTime){
    super(player, DisplayType.BOSS_BAR);
    this.skill = skill;
    this.displayTime = displayTime;
    createBossBar();
  }

  @Override
  public void createBossBar(){
    Skill s = this.player.getSkill(skill);
    String title = Methods.color(player.getPlayer(), McRPG.getInstance().getConfig().getString("DisplayConfig.BossBar.DisplayName").replace("%Skill%", skill.getDisplayName())
            .replace("%Exp_To_Level%", Integer.toString(s.getExpToLevel() - s.getCurrentExp())).replace("%Current_Level%", Integer.toString(s.getCurrentLevel())));
    BarStyle style = BarStyle.SEGMENTED_10;
    BarColor color = us.eunoians.mcrpg.types.BarColor.fromString(McRPG.getInstance().getConfig().getString("DisplayConfig.BossBar.Color." + skill.getName()));
    Parser equation = skill.getExpEquation();
    equation.setVariable("skill_level", s.getCurrentLevel());
    equation.setVariable("power_level", player.getPowerLevel());
    double progress = s.getCurrentExp()/equation.getValue();
    if(progress >= 1){
      progress = .9;
    }
    else if(progress < 0){
      progress = 0;
    }
    this.expBar = Bukkit.createBossBar(title, color, style);
    expBar.setProgress(progress);
    expBar.setVisible(true);
    expBar.addPlayer(player.getPlayer());
  }

  @Override
  public void sendUpdate(int currentExp, int expToLevel, int currentLevel, int expGained){
    Skill s = this.player.getSkill(skill);
    Parser equation = skill.getExpEquation();
    equation.setVariable("skill_level", s.getCurrentLevel());
    equation.setVariable("power_level", player.getPowerLevel());
    double progress = s.getCurrentExp()/equation.getValue();
    if(progress >= 1){
      progress = .9;
    }
    else if(progress < 0){
      progress = 0;
    }
    expBar.setProgress(progress);
    this.expBar.setTitle(Methods.color(player.getPlayer(), McRPG.getInstance().getConfig().getString("DisplayConfig.BossBar.DisplayName").replace("%Skill%", skill.getDisplayName())
            .replace("%Exp_Gained%", Integer.toString(expGained)).replace("%Exp_To_Level%", Integer.toString(s.getExpToLevel() - s.getCurrentExp())).replace("%Current_Level%", Integer.toString(currentLevel))));
    updateEndTime();
  }

  @Override
  public void cancel(){
    expBar.removeAll();
    if(endTask != null){
      endTask.cancel();
    }
  }

  @Override
  public BossBar getBossbar(){
    return expBar;
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
