package us.eunoians.mcmmox.api.displays;

import lombok.Getter;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.types.DisplayType;

public abstract class GenericDisplay {

  @Getter
  protected McMMOPlayer player;
  @Getter
  protected DisplayType type;

  public GenericDisplay(McMMOPlayer player, DisplayType type){
    this.player = player;
    this.type = type;
  }


  public void sendUpdate(int currentExp, int expToLevel, int currentLevel){}

  public void cancel(){}
}
