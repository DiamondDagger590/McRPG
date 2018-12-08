package us.eunoians.mcrpg.api.displays;

import lombok.Getter;
import us.eunoians.mcrpg.players.McMMOPlayer;
import us.eunoians.mcrpg.types.DisplayType;

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
   * A method all children must have. Cancels display
   */
  public void cancel(){}
}
