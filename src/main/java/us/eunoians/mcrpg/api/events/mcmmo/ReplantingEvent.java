package us.eunoians.mcrpg.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.herbalism.Replanting;
import us.eunoians.mcrpg.players.McMMOPlayer;

public class ReplantingEvent extends AbilityActivateEvent{

  @Getter
  @Setter
  private boolean doStageGrowth;

  @Getter
  @Setter
  private int maxAge;

  @Getter
  @Setter
  private int minAge;

  public ReplantingEvent(McMMOPlayer player, Replanting replanting, boolean doStageGrowth, int maxAge, int minAge){
    super(replanting, player);
    this.doStageGrowth = doStageGrowth;
    this.maxAge = maxAge;
    this.minAge = minAge;
  }
}
