package us.eunoians.mcrpg.gui;

import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class HomeGUI extends GUI {

  private static FileManager fm = McRPG.getInstance().getFileManager();
  private static FileManager.Files file = FileManager.Files.MAIN_GUI;

  public HomeGUI(McRPGPlayer p) {
    super(new GUIBuilder("MainGUI", fm.getFile(file), p));
    this.getGui().replacePlaceHolders();
    if(!GUITracker.isPlayerTracked(p)){
      GUITracker.trackPlayer(p , this);
    }
  }
}
