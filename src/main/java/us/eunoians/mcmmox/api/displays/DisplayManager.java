package us.eunoians.mcmmox.api.displays;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class DisplayManager {

  @Getter
  private static DisplayManager instance = new DisplayManager();

  private ArrayList<GenericDisplay> displays = new ArrayList<>();

  /**
   *
   * @param p Player to get the display type for
   * @return The GenericDisplay of a player
   */
  public GenericDisplay getDisplay(Player p){
    return displays.stream().filter(display -> display.getPlayer().getUuid().equals(p.getUniqueId())).findFirst().orElse(null);
  }

  /**
   *
   * @param display GenericDisplay that the player will have
   */
  public void setGenericDisplay(GenericDisplay display){
    GenericDisplay display1 = getDisplay(display.player.getPlayer());
    if(display1 != null){
      displays.remove(display1);
	}
    displays.add(display);
  }

  /**
   *
   * @param p Players who GenericDisplay is to be removed
   */
  public void removePlayersDisplay(Player p){
    GenericDisplay display = getDisplay(p);
    display.cancel();
    displays.remove(display);
  }

  /**
   *
   * @param p Player to check
   * @return true if they have a display, else false
   */
  public boolean doesPlayerHaveDisplay(Player p){
	return displays.stream().anyMatch(display -> display.getPlayer().getUuid().equals(p.getUniqueId()));
  }
}