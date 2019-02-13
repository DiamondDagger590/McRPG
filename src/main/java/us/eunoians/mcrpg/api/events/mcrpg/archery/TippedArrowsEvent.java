package us.eunoians.mcrpg.api.events.mcrpg.archery;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.potion.PotionEffectType;
import us.eunoians.mcrpg.abilities.archery.TippedArrows;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class TippedArrowsEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private PotionEffectType effectType;

  @Getter
  @Setter
  private int duration;

  @Getter
  @Setter
  private int potency;

  public TippedArrowsEvent(McRPGPlayer mcRPGPlayer, TippedArrows tippedArrows, String effect){
    super(tippedArrows, mcRPGPlayer, AbilityEventType.COMBAT);
    String[] data = effect.split(":");
    effectType = PotionEffectType.getByName(data[0]);
    duration = Integer.parseInt(data[2]);
    potency = Integer.parseInt(data[1]);
  }

  public String getEffectString(){
    return effectType.getName() + ":" + potency + ":" + duration;
  }


}
