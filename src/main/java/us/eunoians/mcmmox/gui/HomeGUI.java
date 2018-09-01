package us.eunoians.mcmmox.gui;

import us.eunoians.mcmmox.players.McMMOPlayer;

public class HomeGUI extends GUI {

  public HomeGUI(McMMOPlayer p) {
    super(new GUIBuilder("guis/maingui.yml", "MainGUI", p));
    this.getGui().replacePlaceHolders(p);
  }

}
