package us.eunoians.mcrpg.api.events.mcrpg.woodcutting;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import us.eunoians.mcrpg.abilities.woodcutting.ExtraLumber;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class ExtraLumberEvent extends AbilityActivateEvent {

  @Getter
  private Material type;

  public ExtraLumberEvent(McRPGPlayer player, ExtraLumber extraLumber, Material type){
    super(extraLumber, player, AbilityEventType.RECREATIONAL);
    this.type = type;
  }
}
