package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.mining.RemoteTransfer;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class PreChestLinkEvent extends AbilityActivateEvent {

  @Getter
  private Location chestLocation;
  @Getter
  @Setter
  private String errorMessage;

  public PreChestLinkEvent(McMMOPlayer player, RemoteTransfer remoteTransfer, Location location){
    super(remoteTransfer, player);
    this.isCancelled = !remoteTransfer.isToggled();
	this.errorMessage = Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.RemoteTransfer.FailedLink");
	this.chestLocation = location;
  }
}
