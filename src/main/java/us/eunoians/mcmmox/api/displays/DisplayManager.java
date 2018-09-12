package us.eunoians.mcmmox.api.displays;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class DisplayManager {

  @Getter
  private static DisplayManager instance = new DisplayManager();

  private ArrayList<GenericDisplay> displays = new ArrayList<>();


  public GenericDisplay getDisplay(Player p){
    return displays.stream().filter(display -> display.getPlayer().equals(p)).findFirst().orElse(null);
  }

  public void setGenericDisplay(GenericDisplay display){
    displays.add(display);
  }

  public void removePlayersDisplay(Player p){
    GenericDisplay display = displays.stream().filter(dis -> dis.getPlayer().equals(p)).findFirst().orElse(null);
    display.cancel();
    displays.remove(display);
  }

  public boolean doesPlayerHaveDisplay(Player p){
    for(GenericDisplay display : displays){
      if(display.getPlayer().equals(p)){
        return true;
      }
    }
    return false;
  }


}
