package us.eunoians.mcrpg.gui;

import lombok.Getter;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.RedeemBit;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class AllGUI extends GUI {

  private static FileManager fm = McRPG.getInstance().getFileManager();

  private static FileManager.Files file = FileManager.Files.REDEEM_GUI;

  @Getter
  private RedeemBit redeemBit;

  public AllGUI(McRPGPlayer player, RedeemBit redeemBit){
    super(new GUIBuilder("AllGUI", fm.getFile(file), player));
    this.redeemBit = redeemBit;
  }
}
