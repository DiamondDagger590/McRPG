package us.eunoians.mcrpg.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.players.McMMOPlayer;

public class PreChestLinkEvent extends AbilityActivateEvent {

  @Getter
  private Location chestLocation;
  @Getter
  @Setter
  private String errorMessage;

  public PreChestLinkEvent(McMMOPlayer player, RemoteTransfer remoteTransfer, Location location){
    super(remoteTransfer, player);
    this.isCancelled = !remoteTransfer.isToggled();
	this.errorMessage = McRPG.getInstance().getLangFile().getString("Messages.Abilities.RemoteTransfer.FailedLink");
	this.chestLocation = location;
  }
}
