package us.eunoians.mcmmox.events.mcmmo;

import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.displays.ExpScoreboardDisplay;
import us.eunoians.mcmmox.api.displays.GenericDisplay;
import us.eunoians.mcmmox.api.events.mcmmo.McMMOPlayerLevelChangeEvent;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.types.DisplayType;

public class McMMOPlayerLevelChange implements Listener {

  @EventHandler
  public void levelChange(McMMOPlayerLevelChangeEvent e){
    String message = Methods.color(Mcmmox.getInstance().getPluginPrefix() +
		Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.CONFIG).getString("Messages.Players.LevelUp")
			.replaceAll("%Levels%", Integer.toString(e.getAmountOfLevelsIncreased())).replaceAll("%Skill%", e.getSkillLeveled().getName()));
	if(e.getMcMMOPlayer().isOnline()){
	  Player p = e.getMcMMOPlayer().getPlayer();
	  p.sendMessage(message);
	  World w = p.getWorld();
	  w.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 1);
	  if(!Mcmmox.getInstance().getDisplayManager().doesPlayerHaveDisplay(e.getMcMMOPlayer().getPlayer())){
		return ;
	  }
	  GenericDisplay display = Mcmmox.getInstance().getDisplayManager().getDisplay(e.getMcMMOPlayer().getPlayer());
	  if(display.getType().equals(DisplayType.EXP_SCOREBOARD)){
		ExpScoreboardDisplay exp = (ExpScoreboardDisplay) display;
		if(exp.getSkill().equals(e.getSkillLeveled().getType())){
		  ((ExpScoreboardDisplay) display).sendUpdate(e.getSkillLeveled().getCurrentExp(), e.getSkillLeveled().getExpToLevel(), e.getSkillLeveled().getCurrentLevel());
		}
	  }
  	}
  }
}
