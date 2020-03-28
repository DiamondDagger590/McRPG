package us.eunoians.mcrpg.gui;

import org.bukkit.inventory.Inventory;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.exceptions.PartyNotFoundException;
import us.eunoians.mcrpg.party.Party;
import us.eunoians.mcrpg.party.PartyManager;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class PartyBankGUI extends GUI{
  
  private GUIInventoryFunction buildGUIFunction;
  
  public PartyBankGUI(McRPGPlayer mcRPGPlayer) throws PartyNotFoundException{
    super(new GUIBuilder(mcRPGPlayer));
    PartyManager partyManager = McRPG.getInstance().getPartyManager();
    if(mcRPGPlayer.getPartyID() == null || partyManager.getParty(mcRPGPlayer.getPartyID()) == null){
      throw new PartyNotFoundException("The player is either no in a party or the party is invalid and as such could not be handled");
    }
    Party party = partyManager.getParty(mcRPGPlayer.getPartyID());
    buildGUIFunction = (GUIBuilder guiBuilder) -> {
      Inventory inventory = party.getPartyBank();
      return inventory;
    };
    getGui().setBuildGUIFunction(buildGUIFunction);
    getGui().rebuildGUI();
  }
}
