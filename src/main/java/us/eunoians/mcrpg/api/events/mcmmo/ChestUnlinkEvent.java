package us.eunoians.mcrpg.api.events.mcmmo;

import lombok.Getter;
import org.bukkit.Location;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.players.McMMOPlayer;

public class ChestUnlinkEvent extends AbilityActivateEvent {

  @Getter
  private Location chestLocation;

  public ChestUnlinkEvent(McMMOPlayer player, RemoteTransfer remoteTransfer, Location chestLocation){
    super(remoteTransfer, player);
    this.chestLocation = chestLocation;
  }
}
