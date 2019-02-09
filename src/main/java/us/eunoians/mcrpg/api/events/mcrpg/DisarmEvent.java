package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcrpg.abilities.unarmed.Disarm;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class DisarmEvent extends AbilityActivateEvent {

  @Getter
  McRPGPlayer target;

  @Getter
  ItemStack itemToDisarm;

  public DisarmEvent(McRPGPlayer mcRPGPlayer, McRPGPlayer target, Disarm disarm, ItemStack itemToDisarm){
    super(disarm, mcRPGPlayer, AbilityEventType.COMBAT);
    this.target = target;
    this.itemToDisarm = itemToDisarm;
  }
}
