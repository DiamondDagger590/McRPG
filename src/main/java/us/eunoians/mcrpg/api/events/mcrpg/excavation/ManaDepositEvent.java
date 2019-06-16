package us.eunoians.mcrpg.api.events.mcrpg.excavation;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.excavation.ManaDeposit;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;
import us.eunoians.mcrpg.types.Skills;

public class ManaDepositEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private Skills skill;

  @Getter
  @Setter
  private int exp;
  public ManaDepositEvent(McRPGPlayer player, ManaDeposit manaDeposit, int exp, Skills skill) {
    super(manaDeposit, player, AbilityEventType.RECREATIONAL);
    this.exp = exp;
    this.skill = skill;
  }
}
