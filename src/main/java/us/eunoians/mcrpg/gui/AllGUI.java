package us.eunoians.mcrpg.gui;

import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class AllGUI extends GUI {

  private static FileManager fm = McRPG.getInstance().getFileManager();

  private static FileManager.Files file = FileManager.Files.REDEEM_GUI;

  public AllGUI(McRPGPlayer player){
    super(new GUIBuilder("AllGUI", fm.getFile(file), player));
  }
}
