package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import org.bukkit.Location;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class ChestLinkEvent extends AbilityActivateEvent {

  @Getter
  private Location chestLocation;

  public ChestLinkEvent(McRPGPlayer player, RemoteTransfer remoteTransfer, Location chestLocation){
    super(remoteTransfer, player);
    this.isCancelled = !remoteTransfer.isToggled();
    this.chestLocation = chestLocation;
  }
}
