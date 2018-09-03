package us.eunoians.mcmmox.gui;

import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class HomeGUI extends GUI {

  private static FileManager fm = Mcmmox.getInstance().getFileManager();
  private static FileManager.Files file = FileManager.Files.MAIN_GUI;
  public HomeGUI(McMMOPlayer p) {
    super(new GUIBuilder(file.getFileName(),"MainGUI", fm.getFile(file), p));
    this.getGui().replacePlaceHolders(p);
  }

}
