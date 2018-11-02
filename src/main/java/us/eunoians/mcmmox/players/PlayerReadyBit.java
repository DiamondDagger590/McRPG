package us.eunoians.mcmmox.players;

import lombok.Getter;
import org.bukkit.Bukkit;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.types.UnlockedAbilities;

public class PlayerReadyBit {

  @Getter
  private UnlockedAbilities abilityReady;

  @Getter
  private int endTaskID;

  @Getter
  private McMMOPlayer player;

  public PlayerReadyBit(UnlockedAbilities abilityReady, McMMOPlayer player){
    this.abilityReady = abilityReady;
    this.player = player;
    endTaskID = Bukkit.getScheduler().runTaskLater(Mcmmox.getInstance(), () ->{
      player.setReadying(false);
      player.setReadyingAbilityBit(null);
      String replaceName = "";
      if(abilityReady.getSkill().equalsIgnoreCase("swords")){
        replaceName = "Sword";
	  }
	  else if(abilityReady.getSkill().equalsIgnoreCase("mining")){
	    replaceName = "Pickaxe";
	  }
	  else if(abilityReady.getSkill().equalsIgnoreCase("unarmed")){
	    replaceName = "Fist";
	  }
      player.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
		  Mcmmox.getInstance().getLangFile().getString("Messages.Players.PlayerUnready").replace("%Skill_Item%", replaceName)));
	}, Mcmmox.getInstance().getConfig().getInt("PlayerConfiguration.PlayerReadyDuration") * 20).getTaskId();

  }
}
