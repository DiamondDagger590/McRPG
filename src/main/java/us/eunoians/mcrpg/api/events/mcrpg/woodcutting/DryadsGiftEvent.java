package us.eunoians.mcrpg.api.events.mcrpg.woodcutting;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.woodcutting.DryadsGift;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class DryadsGiftEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int expDropped;

  public DryadsGiftEvent(McRPGPlayer player, DryadsGift dryadsGift, int expDropped){
    super(dryadsGift, player, AbilityEventType.RECREATIONAL);
    this.expDropped = expDropped;
  }
}
