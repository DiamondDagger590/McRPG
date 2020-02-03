package us.eunoians.mcrpg.api.events.mcrpg.sorcery;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.sorcery.CircesShrine;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class CircesShrineEvent extends AbilityActivateEvent{
  
  @Getter
  @Setter
  private int minAmount;
  
  @Getter
  @Setter
  private double percentAddedPerExtra;
  
  @Getter
  @Setter
  private int lowEndSuccessRate;
  
  @Getter
  @Setter
  private int highEndSuccessRate;
  
  @Getter
  @Setter
  private int lowEndConversionRate;
  
  @Getter
  @Setter
  private int highEndConversionRate;
  
  @Getter
  @Setter
  private boolean consumeItemsOnFail;
  
  @Getter
  @Setter
  private boolean consumeLevelsOnFail;
  
  @Getter
  @Setter
  private double percentLevelsToConsume;
  
  @Getter
  @Setter
  private int cooldown;
  
  public CircesShrineEvent(McRPGPlayer player, CircesShrine circesShrine, int minAmount, double percentAddedPerExtra,
                           int lowEndSuccessRate, int highEndSuccessRate, int lowEndConversionRate, int highEndConversionRate,
                           boolean consumeItemsOnFail, boolean consumeLevelsOnFail, double percentLevelsToConsume, int cooldown){
    super(circesShrine, player, AbilityEventType.RECREATIONAL);
    this.minAmount = minAmount;
    this.percentAddedPerExtra = percentAddedPerExtra;
    this.lowEndSuccessRate = lowEndSuccessRate;
    this.highEndSuccessRate = highEndSuccessRate;
    this.lowEndConversionRate = lowEndConversionRate;
    this.highEndConversionRate = highEndConversionRate;
    this.consumeItemsOnFail = consumeItemsOnFail;
    this.consumeLevelsOnFail = consumeLevelsOnFail;
    this.percentLevelsToConsume = percentLevelsToConsume;
    this.cooldown = cooldown;
  }
}
