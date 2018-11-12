package us.eunoians.mcmmox.api.displays;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.skills.Skill;
import us.eunoians.mcmmox.types.DisplayType;
import us.eunoians.mcmmox.types.Skills;
import us.eunoians.mcmmox.util.Parser;

public class ExpBossbarDisplay extends GenericDisplay implements BossbarBase, ExpDisplayType {

  @Getter
  private BossBar expBar;

  @Getter
  private Skills skill;

  public ExpBossbarDisplay(McMMOPlayer player, Skills skill){
    super(player, DisplayType.BOSS_BAR);
    this.skill = skill;
    createBossBar();
  }

  @Override
  public void createBossBar(){
    Skill s = this.player.getSkill(skill);
	String title = Methods.color(Mcmmox.getInstance().getConfig().getString("DisplayConfig.BossBar.DisplayName").replace("%Skill%", skill.getName())
	.replace("%Exp_To_Level%", Integer.toString(s.getExpToLevel())));
	BarStyle style = BarStyle.SEGMENTED_10;
	BarColor color = us.eunoians.mcmmox.types.BarColor.fromString(Mcmmox.getInstance().getConfig().getString("DisplayConfig.BossBar.Color." + skill.getName()));
	Parser equation = skill.getExpEquation();
	equation.setVariable("skill_level", s.getCurrentLevel());
	equation.setVariable("power_level", player.getPowerLevel());
	double progress = s.getCurrentExp()/equation.getValue();
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
    expBar.setProgress(progress);
    this.expBar.setTitle(Methods.color(Mcmmox.getInstance().getConfig().getString("DisplayConfig.BossBar.DisplayName").replace("%Skill%", skill.getName())
		.replace("%Exp_To_Level%", Integer.toString(s.getExpToLevel()))));
  }

  @Override
  public void cancel(){
    expBar.removeAll();
  }
}
