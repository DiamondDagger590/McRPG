package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcmmox.abilities.unarmed.Disarm;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class DisarmEvent extends AbilityActivateEvent {

  @Getter
  McMMOPlayer target;

  @Getter
  ItemStack itemToDisarm;

  public DisarmEvent(McMMOPlayer mcMMOPlayer, McMMOPlayer target, Disarm disarm, ItemStack itemToDisarm){
    super(disarm, mcMMOPlayer);
    this.target = target;
    this.itemToDisarm = itemToDisarm;
  }
}
