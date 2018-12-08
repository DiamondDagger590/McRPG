package us.eunoians.mcrpg.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.unarmed.Berserk;
import us.eunoians.mcrpg.players.McMMOPlayer;

public class BerserkEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private double bonusChance;

  @Getter
  @Setter
  private int bonusDamage;

  public BerserkEvent(McMMOPlayer player, Berserk berserk, double bonusChance, int bonusDamage){
    super(berserk, player);
    this.bonusChance = bonusChance;
    this.bonusDamage = bonusDamage;
  }
}
