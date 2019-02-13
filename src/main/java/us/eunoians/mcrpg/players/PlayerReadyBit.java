package us.eunoians.mcrpg.players;

import lombok.Getter;
import org.bukkit.Bukkit;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class PlayerReadyBit {

  @Getter
  private UnlockedAbilities abilityReady;

  @Getter
  private int endTaskID;

  @Getter
  private McRPGPlayer player;

  public PlayerReadyBit(UnlockedAbilities abilityReady, McRPGPlayer player){
    this.abilityReady = abilityReady;
    this.player = player;
    endTaskID = Bukkit.getScheduler().runTaskLater(McRPG.getInstance(), () ->{
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
	  else if(abilityReady.getSkill().equalsIgnoreCase("herbalism")){
	    replaceName = "Hoe";
	  }
	  else if(abilityReady.getSkill().equalsIgnoreCase("archery")){
	    replaceName = "Bow";
      }
      player.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
		  McRPG.getInstance().getLangFile().getString("Messages.Players.PlayerUnready").replace("%Skill_Item%", replaceName)));
	}, McRPG.getInstance().getConfig().getInt("PlayerConfiguration.PlayerReadyDuration") * 20).getTaskId();

  }
}
