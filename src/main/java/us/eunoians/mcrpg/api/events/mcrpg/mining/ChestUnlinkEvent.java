package us.eunoians.mcrpg.api.events.mcrpg.mining;

import lombok.Getter;
import org.bukkit.Location;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class ChestUnlinkEvent extends AbilityActivateEvent {

  @Getter
  private Location chestLocation;

  public ChestUnlinkEvent(McRPGPlayer player, RemoteTransfer remoteTransfer, Location chestLocation){
    super(remoteTransfer, player, AbilityEventType.RECREATIONAL);
    this.chestLocation = chestLocation;
  }
}
