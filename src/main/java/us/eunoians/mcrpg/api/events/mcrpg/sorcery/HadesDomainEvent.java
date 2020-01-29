package us.eunoians.mcrpg.api.events.mcrpg.sorcery;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.sorcery.HadesDomain;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class HadesDomainEvent extends AbilityActivateEvent {
  
  @Getter
  @Setter
  private double percentBonusMcRPGExp;
  
  @Getter
  @Setter
  private double percentBonusVanillaExp;
  
  @Getter
  private boolean forMcRPG;
  
  public HadesDomainEvent(McRPGPlayer player, HadesDomain hadesDomain, double percentBonus, boolean forMcRPG){
    super(hadesDomain, player, AbilityEventType.RECREATIONAL);
    if(forMcRPG){
      this.percentBonusMcRPGExp = percentBonus;
    }
    else{
      this.percentBonusVanillaExp = percentBonus;
    }
    this.forMcRPG = forMcRPG;
  }
}
