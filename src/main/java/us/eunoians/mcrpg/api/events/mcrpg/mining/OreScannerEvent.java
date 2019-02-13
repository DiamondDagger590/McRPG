package us.eunoians.mcrpg.api.events.mcrpg.mining;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.mining.OreScanner;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class OreScannerEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int cooldown;
  public OreScannerEvent(McRPGPlayer player, OreScanner oreScanner, int cooldown){
    super(oreScanner, player, AbilityEventType.RECREATIONAL);
    this.cooldown = cooldown;
  }
}
