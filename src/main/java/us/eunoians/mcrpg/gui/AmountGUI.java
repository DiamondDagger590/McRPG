package us.eunoians.mcrpg.gui;

import lombok.Getter;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class AmountGUI extends GUI {

  private static FileManager fm = McRPG.getInstance().getFileManager();

  private static FileManager.Files file = FileManager.Files.REDEEM_GUI;

  @Getter
  private AmountGUIType type;

  public AmountGUI(McRPGPlayer player, AmountGUIType type){
    super(new GUIBuilder(type.getName() + "AmountGUI", fm.getFile(file), player));
    this.type = type;
  }

  public enum AmountGUIType{
    EXP("Exp"),
    LEVEL("Level");

    @Getter
    private String name;

    AmountGUIType(String name){
      this.name = name;
    }
  }
}
