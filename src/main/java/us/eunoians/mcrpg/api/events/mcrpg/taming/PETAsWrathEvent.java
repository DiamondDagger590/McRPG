package us.eunoians.mcrpg.api.events.mcrpg.taming;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.potion.PotionEffect;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class PETAsWrathEvent extends AbilityActivateEvent{
  
  @Getter @Setter
  private PotionEffect potionEffect;
  
  public PETAsWrathEvent(McRPGPlayer player, PotionEffect potionEffect){
    super(player.getBaseAbility(UnlockedAbilities.PETAS_WRATH), player, AbilityEventType.COMBAT);
    this.potionEffect = potionEffect;
  }
}
