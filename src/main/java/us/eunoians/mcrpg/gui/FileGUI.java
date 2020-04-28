package us.eunoians.mcrpg.gui;

import org.bukkit.configuration.file.YamlConfiguration;
import us.eunoians.mcrpg.players.McRPGPlayer;

import java.io.File;

public class FileGUI extends GUI{
  
  
  public FileGUI(McRPGPlayer mcRPGPlayer, File file, String guiName){
    super(new GUIBuilder(guiName, YamlConfiguration.loadConfiguration(file), mcRPGPlayer));
    this.getGui().replacePlaceHolders();
  }
}
