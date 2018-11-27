package us.eunoians.mcmmox.api.displays;

import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.skills.Skill;
import us.eunoians.mcmmox.types.DisplayType;
import us.eunoians.mcmmox.types.Skills;

public class ExpActionBar extends GenericDisplay implements ExpDisplayType, ActionBarBase {

  @Getter
  private Skills skill;

  public ExpActionBar(McMMOPlayer player, Skills skill){
    super(player, DisplayType.ACTION_BAR);
    this.skill = skill;
  }

  @Override
  public void sendUpdate(int currentExp, int expToLevel, int currentLevel, int expGained){
	Skill s = player.getSkill(skill);
	player.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Methods.color(Mcmmox.getInstance().getConfig()
		.getString("DisplayConfig.ActionBar." + skill.getName() + ".Message").replace("%Exp_Gained%", Integer.toString(expGained))
	.replace("%Exp_To_Level%", Integer.toString(s.getExpToLevel() - s.getCurrentExp())))));
  }
}
