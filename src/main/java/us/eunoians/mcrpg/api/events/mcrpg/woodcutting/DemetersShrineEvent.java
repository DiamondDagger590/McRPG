package us.eunoians.mcrpg.api.events.mcrpg.woodcutting;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import us.eunoians.mcrpg.abilities.woodcutting.DemetersShrine;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class DemetersShrineEvent extends AbilityActivateEvent {

  @Getter
  private Material itemSacrificed;

  @Getter
  @Setter
  private double multiplier;

  @Getter
  @Setter
  private int durationOfBoost;

  @Getter
  @Setter
  private int cooldown;

  public DemetersShrineEvent(McRPGPlayer player, DemetersShrine demetersShrine, Material itemSacrificed, double multiplier, int durationOfBoost, int cooldown){
    super(demetersShrine, player, AbilityEventType.RECREATIONAL);
    this.itemSacrificed = itemSacrificed;
    this.multiplier = multiplier;
    this.durationOfBoost = durationOfBoost;
    this.cooldown = cooldown;
  }
}
