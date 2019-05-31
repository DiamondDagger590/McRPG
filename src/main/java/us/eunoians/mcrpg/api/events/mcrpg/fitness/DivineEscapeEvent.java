package us.eunoians.mcrpg.api.events.mcrpg.fitness;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.fitness.DivineEscape;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class DivineEscapeEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private double expDebuff;

  @Getter
  @Setter
  private double damageIncreaseDebuff;

  public DivineEscapeEvent(McRPGPlayer mcRPGPlayer, DivineEscape divineEscape, double expDebuff, double damageIncreaseDebuff) {
    super(divineEscape, mcRPGPlayer, AbilityEventType.COMBAT);
    this.expDebuff = expDebuff;
    this.damageIncreaseDebuff = damageIncreaseDebuff;
  }
}
