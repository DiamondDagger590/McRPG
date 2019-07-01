package us.eunoians.mcrpg.players;

import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;
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
    endTaskID = new BukkitRunnable(){
      @Override
      public void run() {
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
        else if(abilityReady.getSkill().equalsIgnoreCase("woodcutting")){
          replaceName = "Axe";
        }
        else if(abilityReady.getSkill().equalsIgnoreCase("excavation")){
          replaceName = "Shovel";
          if(abilityReady == UnlockedAbilities.HAND_DIGGING){
            replaceName = "Fist";
          }
        }
        else if(abilityReady.getSkill().equalsIgnoreCase("axes")){
          replaceName = "Battle Axe";
        }
        player.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                McRPG.getInstance().getLangFile().getString("Messages.Players.PlayerUnready").replace("%Skill_Item%", replaceName)));
      }
    }.runTaskLater(McRPG.getInstance(), McRPG.getInstance().getConfig().getInt("PlayerConfiguration.PlayerReadyDuration") * 20).getTaskId();
  }
}
