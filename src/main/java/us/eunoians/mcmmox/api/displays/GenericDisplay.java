package us.eunoians.mcmmox.api.displays;

import lombok.Getter;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.types.DisplayType;

public abstract class GenericDisplay {

  @Getter
  protected McMMOPlayer player;
  @Getter
  protected DisplayType type;

  /**
   *
   * @param player The player the generic type belongs to
   * @param type The type of display this is
   */
  public GenericDisplay(McMMOPlayer player, DisplayType type){
    this.player = player;
    this.type = type;
  }

  /**
   * A method all children must have. Updates display w/ new info
   * @param currentExp
   * @param expToLevel
   * @param currentLevel
   */
  public void sendUpdate(int currentExp, int expToLevel, int currentLevel){}

  /**
   * A method all children must have. Cancels display
   */
  public void cancel(){}
}
