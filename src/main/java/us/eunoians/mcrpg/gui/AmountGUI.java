package us.eunoians.mcrpg.gui;

import lombok.Getter;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.RedeemType;
import us.eunoians.mcrpg.types.Skills;

public class AmountGUI extends GUI {

  private static FileManager fm = McRPG.getInstance().getFileManager();

  private static FileManager.Files file = FileManager.Files.REDEEM_GUI;

  @Getter
  private RedeemType type;

  @Getter
  private Skills skill;

  public AmountGUI(McRPGPlayer player, RedeemType type, Skills skill) {
    super(new GUIBuilder(type.getName() + "AmountGUI", fm.getFile(file), player));
    this.type = type;
    this.skill = skill;
  }
}
