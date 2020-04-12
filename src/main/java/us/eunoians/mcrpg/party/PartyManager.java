package us.eunoians.mcrpg.party;

import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.util.Parser;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PartyManager{
  
  private Map<UUID, Party> partyMap = new HashMap<>();
  private File partyFolder = new File(McRPG.getInstance().getDataFolder(), "parties");
  
  public PartyManager(){
    if(!partyFolder.exists()){
      partyFolder.mkdirs();
    }
  }
  
  public void init(){
    if(partyFolder.listFiles() != null && partyFolder.listFiles().length > 0){
      for(File file : partyFolder.listFiles()){
        Party party = new Party(file);
        partyMap.put(party.getPartyID(), party);
      }
    }
  }
  
  public Party getParty(UUID partyID){
    return partyMap.getOrDefault(partyID, null);
  }
  
  public boolean isPartyNameUsed(String name){
    for(Party party : partyMap.values()){
      if(party.getName().equalsIgnoreCase(name)){
        return true;
      }
    }
    return false;
  }
  
  public Party addParty(String name, UUID partyCreator){
    Party party = new Party(partyCreator, name);
    partyMap.put(party.getPartyID(), party);
    return party;
  }
  
  public Parser getExpEquation(){
    return new Parser(McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getString("PartyExp.ExpEquation"));
  }
  
  public int getMaxLevel(){
    return McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getInt("PartyExp.MaxLevel", 10);
  }
  
  public void removeParty(UUID partyID){
    partyMap.remove(partyID).disband(true);
  }
  
  public Collection<Party> getParties(){
    return partyMap.values();
  }
  
  public void saveAllParties(){
    for(Party party : partyMap.values()){
      party.saveParty();
    }
  }
}
