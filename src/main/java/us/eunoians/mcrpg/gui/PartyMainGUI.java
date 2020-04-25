package us.eunoians.mcrpg.gui;

import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class PartyMainGUI extends GUI{
  
  public PartyMainGUI(McRPGPlayer mp){
    super(new GUIBuilder("MainGUI", McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_MAIN_GUI), mp));
    this.getGui().replacePlaceHolders();
    if(!GUITracker.isPlayerTracked(mp)){
      GUITracker.trackPlayer(mp , this);
    }
  }
}
