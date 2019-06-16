package us.eunoians.mcrpg.api.events.mcrpg.excavation;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.excavation.FrenzyDig;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class FrenzyDigEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int duration;

  @Getter
  @Setter
  private int cooldown;

  @Getter
  @Setter
  private double extractionBuff;

  public FrenzyDigEvent(McRPGPlayer player, FrenzyDig frenzyDig, int duration, int cooldown, double extractionBuff) {
    super(frenzyDig, player, AbilityEventType.RECREATIONAL);
    this.duration = duration;
    this.cooldown = cooldown;
    this.extractionBuff = extractionBuff;
  }
}
