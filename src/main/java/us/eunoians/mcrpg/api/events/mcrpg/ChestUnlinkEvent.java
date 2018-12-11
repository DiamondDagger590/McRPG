package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import org.bukkit.Location;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class ChestUnlinkEvent extends AbilityActivateEvent {

  @Getter
  private Location chestLocation;

  public ChestUnlinkEvent(McRPGPlayer player, RemoteTransfer remoteTransfer, Location chestLocation){
    super(remoteTransfer, player);
    this.chestLocation = chestLocation;
  }
}
