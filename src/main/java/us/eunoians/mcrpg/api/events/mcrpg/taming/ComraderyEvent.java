package us.eunoians.mcrpg.api.events.mcrpg.taming;

import lombok.Getter;
import org.bukkit.entity.Wolf;
import us.eunoians.mcrpg.abilities.taming.Comradery;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class ComraderyEvent extends AbilityActivateEvent{
  
  @Getter
  private Wolf wolf;
  
  public ComraderyEvent(McRPGPlayer mcRPGPlayer, Comradery comradery, Wolf wolf){
    super(comradery, mcRPGPlayer, AbilityEventType.COMBAT);
    this.wolf = wolf;
  }
}
