package us.eunoians.mcrpg.api.displays;

import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.skills.Skill;
import us.eunoians.mcrpg.types.DisplayType;
import us.eunoians.mcrpg.types.Skills;

public class ExpActionBar extends GenericDisplay implements ExpDisplayType, ActionBarBase {

  @Getter
  private Skills skill;

  public ExpActionBar(McRPGPlayer player, Skills skill){
    super(player, DisplayType.ACTION_BAR);
    this.skill = skill;
  }

  @Override
  public void sendUpdate(int currentExp, int expToLevel, int currentLevel, int expGained){
	Skill s = player.getSkill(skill);
	player.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Methods.color(player.getPlayer(), McRPG.getInstance().getConfig()
		.getString("DisplayConfig.ActionBar." + skill.getName() + ".Message").replace("%Current_Level%", Integer.toString(s.getCurrentLevel())).replace("%Skill%", skill.getDisplayName())
            .replace("%Exp_Gained%", Integer.toString(expGained)).replace("%Exp_To_Level%", Integer.toString(s.getExpToLevel() - s.getCurrentExp())))));
  }
}
