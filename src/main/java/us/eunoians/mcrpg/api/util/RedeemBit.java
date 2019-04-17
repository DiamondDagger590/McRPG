package us.eunoians.mcrpg.api.util;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.types.RedeemType;
import us.eunoians.mcrpg.types.Skills;

public class RedeemBit {

  @Getter
  @Setter
  private RedeemType redeemType;

  @Getter
  @Setter
  private Skills skill;

  public RedeemBit(RedeemType type, Skills skill){
    this.redeemType = type;
    this.skill = skill;
  }
}
