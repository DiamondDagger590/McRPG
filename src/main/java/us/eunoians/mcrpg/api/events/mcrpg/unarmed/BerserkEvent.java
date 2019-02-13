package us.eunoians.mcrpg.api.events.mcrpg.unarmed;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.unarmed.Berserk;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class BerserkEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private double bonusChance;

  @Getter
  @Setter
  private int bonusDamage;

  public BerserkEvent(McRPGPlayer player, Berserk berserk, double bonusChance, int bonusDamage){
    super(berserk, player, AbilityEventType.COMBAT);
    this.bonusChance = bonusChance;
    this.bonusDamage = bonusDamage;
  }
}
