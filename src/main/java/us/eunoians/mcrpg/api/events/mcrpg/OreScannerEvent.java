package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.mining.OreScanner;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class OreScannerEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int cooldown;
  public OreScannerEvent(McRPGPlayer player, OreScanner oreScanner, int cooldown){
    super(oreScanner, player);
    this.cooldown = cooldown;
  }
}
