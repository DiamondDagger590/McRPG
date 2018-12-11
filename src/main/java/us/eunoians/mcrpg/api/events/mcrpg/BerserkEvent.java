package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.unarmed.Berserk;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class BerserkEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private double bonusChance;

  @Getter
  @Setter
  private int bonusDamage;

  public BerserkEvent(McRPGPlayer player, Berserk berserk, double bonusChance, int bonusDamage){
    super(berserk, player);
    this.bonusChance = bonusChance;
    this.bonusDamage = bonusDamage;
  }
}
