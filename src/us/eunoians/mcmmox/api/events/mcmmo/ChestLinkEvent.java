package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import org.bukkit.Location;
import us.eunoians.mcmmox.abilities.mining.RemoteTransfer;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class ChestLinkEvent extends AbilityActivateEvent {

  @Getter
  private Location chestLocation;

  public ChestLinkEvent(McMMOPlayer player, RemoteTransfer remoteTransfer, Location chestLocation){
    super(remoteTransfer, player);
    this.isCancelled = !remoteTransfer.isToggled();
    this.chestLocation = chestLocation;
  }
}
