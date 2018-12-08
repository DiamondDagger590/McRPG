package us.eunoians.mcrpg.gui;

import lombok.Getter;
import lombok.Setter;


/*
This class holds the players previous and current gui so that way back buttons can function properly
 */
public class GUITrackerBit {

  @Getter
  @Setter
  private GUI currentGUI;
  @Getter
  @Setter
  private GUI previousGUI;

  public GUITrackerBit(GUI currentGUI, GUI previousGUI) {
    this.currentGUI = currentGUI;
    this.previousGUI = previousGUI;
  }

  public GUITrackerBit(GUI currentGUI) {
    this.currentGUI = currentGUI;
    this.previousGUI = null;
  }

  public boolean hasPreviousGUI() {
    if (previousGUI == null) {
      return false;
    }
    return true;
  }


}
