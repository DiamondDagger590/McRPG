package us.eunoians.mcrpg.api.events.mcrpg.taming;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.taming.LinkedFangs;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class LinkedFangsEvent extends AbilityActivateEvent{
  
  @Getter @Setter
  private int healthToRestore;
  
  @Getter @Setter
  private int hungerToRestore;
  
  @Getter @Setter
  private int saturationToRestore;
  
  public LinkedFangsEvent(McRPGPlayer mcRPGPlayer, LinkedFangs linkedFangs, int healthToRestore, int hungerToRestore, int saturationToRestore){
    super(linkedFangs, mcRPGPlayer, AbilityEventType.COMBAT);
    this.healthToRestore = healthToRestore;
    this.hungerToRestore = hungerToRestore;
    this.saturationToRestore = saturationToRestore;
  }
}
