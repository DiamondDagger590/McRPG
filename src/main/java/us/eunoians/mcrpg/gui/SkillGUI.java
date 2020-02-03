package us.eunoians.mcrpg.gui;

import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class SkillGUI extends GUI{
  
  private static FileManager fm = McRPG.getInstance().getFileManager();
  
  private static FileManager.Files file = FileManager.Files.SKILLS_GUI;
  
  private static GUIPlaceHolderFunction function = (GUIBuilder guiBuilder) -> {
    McRPGPlayer player = guiBuilder.getPlayer();
    if(guiBuilder.getRawPath().equalsIgnoreCase("SkillsGUI")){
      ReplaceSkillsGUI.skillsPlaceHolders(guiBuilder, player);
    }
  };
  
  public SkillGUI(McRPGPlayer p){
    super(new GUIBuilder("SkillsGUI", fm.getFile(file), p));
    this.getGui().setReplacePlaceHoldersFunction(function);
    this.getGui().replacePlaceHolders();
    if(!GUITracker.isPlayerTracked(p)){
      GUITracker.trackPlayer(p, this);
    }
  }
  
}
