package us.eunoians.mcrpg.api.events.mcrpg.woodcutting;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcrpg.abilities.woodcutting.TemporalHarvest;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

import java.util.ArrayList;

public class TemporalHarvestEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int woodAmount;

  @Getter
  @Setter
  private int appleAmount;

  @Getter
  @Setter
  private int saplingAmount;

  @Getter
  @Setter
  private int cooldown;

  public TemporalHarvestEvent(McRPGPlayer player, TemporalHarvest temporalHarvest, int woodAmount, int appleAmount, int saplingAmount, int cooldown){
    super(temporalHarvest, player, AbilityEventType.RECREATIONAL);
    this.woodAmount = woodAmount;
    this.appleAmount = appleAmount;
    this.saplingAmount = saplingAmount;
    this.cooldown = cooldown;
  }
}
