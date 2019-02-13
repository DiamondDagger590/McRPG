package us.eunoians.mcrpg.api.events.mcrpg.archery;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.archery.BlessingOfArtemis;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class BlessingOfArtemisEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int cooldown;

  @Getter
  @Setter
  private int invisDuration;

  @Getter
  @Setter
  private double dmgMultiplier;

  public BlessingOfArtemisEvent(McRPGPlayer mcRPGPlayer, BlessingOfArtemis blessingOfArtemis, int cooldown, int invisDuration, double dmgMultiplier){
    super(blessingOfArtemis, mcRPGPlayer, AbilityEventType.COMBAT);
    this.cooldown = cooldown;
    this.invisDuration = invisDuration;
    this.dmgMultiplier = dmgMultiplier;
  }
}
