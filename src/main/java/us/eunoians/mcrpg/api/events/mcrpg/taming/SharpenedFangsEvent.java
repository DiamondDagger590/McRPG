package us.eunoians.mcrpg.api.events.mcrpg.taming;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.taming.SharpenedFangs;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class SharpenedFangsEvent extends AbilityActivateEvent{
  
  @Getter @Setter
  private int extraDamage;
  
  public SharpenedFangsEvent(McRPGPlayer mcRPGPlayer, SharpenedFangs sharpenedFangs, int extraDamage){
    super(sharpenedFangs, mcRPGPlayer, AbilityEventType.COMBAT);
    this.extraDamage = extraDamage;
  }
}
