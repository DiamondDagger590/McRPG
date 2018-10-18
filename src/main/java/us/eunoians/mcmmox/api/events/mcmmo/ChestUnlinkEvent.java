package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import org.bukkit.Location;
import us.eunoians.mcmmox.abilities.mining.RemoteTransfer;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class ChestUnlinkEvent extends AbilityActivateEvent {

  @Getter
  private Location chestLocation;

  public ChestUnlinkEvent(McMMOPlayer player, RemoteTransfer remoteTransfer, Location chestLocation){
    super(remoteTransfer, player);
    this.chestLocation = chestLocation;
  }
}
