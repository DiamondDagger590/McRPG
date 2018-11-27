package us.eunoians.mcmmox.gui;

import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class SkillGUI extends GUI {

  private static FileManager fm = Mcmmox.getInstance().getFileManager();

  private static FileManager.Files file = FileManager.Files.SKILLS_GUI;

  private static GUIPlaceHolderFunction function = (GUIBuilder guiBuilder) -> {
	McMMOPlayer player = guiBuilder.getPlayer();
	if(guiBuilder.getRawPath().equalsIgnoreCase("SkillsGUI")){
	  ReplaceSkillsGUI.skillsPlaceHolders(guiBuilder, player);
	}
  };

  public SkillGUI(McMMOPlayer p){
	super(new GUIBuilder("SkillsGUI", fm.getFile(file), p));
	this.getGui().setReplacePlaceHoldersFunction(function);
	this.getGui().replacePlaceHolders();
	if(!GUITracker.isPlayerTracked(p)){
	  GUITracker.trackPlayer(p, this);
	}
  }

}
