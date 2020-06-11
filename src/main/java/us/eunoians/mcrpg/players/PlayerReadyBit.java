package us.eunoians.mcrpg.players;

import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import static us.eunoians.mcrpg.types.Skills.*;

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
        if(abilityReady.getSkill().equals(SWORDS)){
          replaceName = "Sword";
        }
        else if(abilityReady.getSkill().equals(MINING)){
          replaceName = "Pickaxe";
        }
        else if(abilityReady.getSkill().equals(UNARMED)){
          replaceName = "Fist";
        }
        else if(abilityReady.getSkill().equals(HERBALISM)){
          replaceName = "Hoe";
        }
        else if(abilityReady.getSkill().equals(ARCHERY)){
          replaceName = "Bow";
        }
        else if(abilityReady.getSkill().equals(WOODCUTTING)){
          replaceName = "Axe";
        }
        else if(abilityReady.getSkill().equals(EXCAVATION)){
          replaceName = "Shovel";
          if(abilityReady == UnlockedAbilities.HAND_DIGGING){
            replaceName = "Fist";
          }
        }
        else if(abilityReady.getSkill().equals(AXES)){
          replaceName = "Battle Axe";
        }
        else if(abilityReady.getSkill().equals(TAMING)){
          replaceName = "Summoning Stick";
        }
        player.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                McRPG.getInstance().getLangFile().getString("Messages.Players.PlayerUnready").replace("%Skill_Item%", replaceName)));
      }
    }.runTaskLater(McRPG.getInstance(), McRPG.getInstance().getConfig().getInt("PlayerConfiguration.PlayerReadyDuration") * 20).getTaskId();
  }
}
