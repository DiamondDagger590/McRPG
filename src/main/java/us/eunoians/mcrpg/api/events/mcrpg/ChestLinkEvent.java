package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import org.bukkit.Location;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class ChestLinkEvent extends AbilityActivateEvent {

  @Getter
  private Location chestLocation;

  public ChestLinkEvent(McRPGPlayer player, RemoteTransfer remoteTransfer, Location chestLocation){
    super(remoteTransfer, player, AbilityEventType.RECREATIONAL);
    this.isCancelled = !remoteTransfer.isToggled();
    this.chestLocation = chestLocation;
  }
}
