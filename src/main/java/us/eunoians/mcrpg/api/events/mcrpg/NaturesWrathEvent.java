package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.potion.PotionEffectType;
import us.eunoians.mcrpg.abilities.herbalism.NaturesWrath;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class NaturesWrathEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int hungerLost;

  @Getter
  @Setter
  private int modifier;

  @Getter
  @Setter
  private PotionEffectType effectType;

  @Getter
  @Setter
  private int duration;

  public NaturesWrathEvent(McRPGPlayer player, NaturesWrath naturesWrath, int hungerLost, int modifier, PotionEffectType effectType, int duration){
    super(naturesWrath, player);
    this.hungerLost = hungerLost;
    this.modifier = modifier;
    this.effectType = effectType;
    this.duration = duration;
  }
}
