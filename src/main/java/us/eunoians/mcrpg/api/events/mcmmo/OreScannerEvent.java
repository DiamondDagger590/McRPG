package us.eunoians.mcrpg.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.mining.OreScanner;
import us.eunoians.mcrpg.players.McMMOPlayer;

public class OreScannerEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int cooldown;
  public OreScannerEvent(McMMOPlayer player, OreScanner oreScanner, int cooldown){
    super(oreScanner, player);
    this.cooldown = cooldown;
  }
}
