package us.eunoians.mcrpg.api.util.blood;

import lombok.Getter;
import lombok.Setter;

public class BloodWrapper{
  
  @Getter @Setter
  private int expMultiplierLowEnd;
  
  @Getter @Setter
  private int expMultiplierHighEnd;
  
  @Getter @Setter
  private double itemShatterChance;
  
  @Getter @Setter
  private int curseDuration;
  
  public BloodWrapper(int expMultiplierLowEnd, int expMultiplierHighEnd, double itemShatterChance){
    this.expMultiplierLowEnd = expMultiplierLowEnd;
    this.expMultiplierHighEnd = expMultiplierHighEnd;
    this.itemShatterChance = itemShatterChance;
  }
  
  public BloodWrapper(int curseDuration){
    this.curseDuration = curseDuration;
  }
}
