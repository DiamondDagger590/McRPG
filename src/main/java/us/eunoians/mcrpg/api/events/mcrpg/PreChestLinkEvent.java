package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class PreChestLinkEvent extends AbilityActivateEvent {

  @Getter
  private Location chestLocation;
  @Getter
  @Setter
  private String errorMessage;

  public PreChestLinkEvent(McRPGPlayer player, RemoteTransfer remoteTransfer, Location location){
    super(remoteTransfer, player, AbilityEventType.RECREATIONAL);
    this.isCancelled = !remoteTransfer.isToggled();
	this.errorMessage = McRPG.getInstance().getLangFile().getString("Messages.Abilities.RemoteTransfer.FailedLink");
	this.chestLocation = location;
  }
}
