package us.eunoians.mcrpg.api.events.mcrpg.sorcery;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcrpg.abilities.sorcery.PotionAffinity;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class PotionAffinityEvent extends AbilityActivateEvent{

  @Getter
  @Setter
  private double durationMultiplier = 0;
  
  @Getter
  private ItemStack potionItem;

  public PotionAffinityEvent(PotionAffinity potionAffinity, McRPGPlayer player, double durationMultiplier, ItemStack potionItem){
    super(potionAffinity, player, AbilityEventType.RECREATIONAL);
    this.durationMultiplier = durationMultiplier;
    this.potionItem = potionItem;
  }
  
}
