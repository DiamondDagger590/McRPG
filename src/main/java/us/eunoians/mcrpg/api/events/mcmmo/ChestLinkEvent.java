package us.eunoians.mcrpg.api.events.mcmmo;

import lombok.Getter;
import org.bukkit.Location;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.players.McMMOPlayer;

public class ChestLinkEvent extends AbilityActivateEvent {

  @Getter
  private Location chestLocation;

  public ChestLinkEvent(McMMOPlayer player, RemoteTransfer remoteTransfer, Location chestLocation){
    super(remoteTransfer, player);
    this.isCancelled = !remoteTransfer.isToggled();
    this.chestLocation = chestLocation;
  }
}
