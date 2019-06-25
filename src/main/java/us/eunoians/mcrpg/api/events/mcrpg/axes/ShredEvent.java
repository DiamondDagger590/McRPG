package us.eunoians.mcrpg.api.events.mcrpg.axes;

import lombok.Getter;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.abilities.axes.Shred;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class ShredEvent extends AbilityActivateEvent {

  @Getter private Player target;

  public ShredEvent(McRPGPlayer player, Shred shred, Player target){
    super(shred, player, AbilityEventType.COMBAT);
    this.target = target;
  }
}
