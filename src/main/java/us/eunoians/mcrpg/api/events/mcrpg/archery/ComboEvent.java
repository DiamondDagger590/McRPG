package us.eunoians.mcrpg.api.events.mcrpg.archery;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.archery.Combo;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class ComboEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private double dmgMultiplier;

  @Getter
  @Setter
  private int lengthBetweenShots;

  @Getter
  @Setter
  private int cooldownBetweenActivation;


  public ComboEvent(McRPGPlayer mcRPGPlayer, Combo combo, double dmgMultiplier, int lengthBetweenShots, int cooldownBetweenActivation){
    super(combo, mcRPGPlayer, AbilityEventType.COMBAT);
    this.dmgMultiplier = dmgMultiplier;
    this.lengthBetweenShots = lengthBetweenShots;
    this.cooldownBetweenActivation = cooldownBetweenActivation;
  }
}
