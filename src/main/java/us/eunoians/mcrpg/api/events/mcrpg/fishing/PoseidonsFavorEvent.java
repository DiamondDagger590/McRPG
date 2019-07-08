package us.eunoians.mcrpg.api.events.mcrpg.fishing;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.fishing.PoseidonsFavor;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class PoseidonsFavorEvent extends AbilityActivateEvent {

  @Getter @Setter private int bonusExp;

  public PoseidonsFavorEvent(McRPGPlayer mcRPGPlayer, PoseidonsFavor poseidonsFavor, int bonusExp){
    super(poseidonsFavor, mcRPGPlayer, AbilityEventType.RECREATIONAL);
    this.bonusExp = bonusExp;
  }
}
