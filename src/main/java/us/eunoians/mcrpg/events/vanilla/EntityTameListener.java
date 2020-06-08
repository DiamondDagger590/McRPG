package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.GainReason;
import us.eunoians.mcrpg.types.Skills;

public class EntityTameListener implements Listener{
  
  @EventHandler(priority = EventPriority.MONITOR)
  public void handleTaming(EntityTameEvent event){
    McRPGPlayer mp;
    try{
      mp = PlayerManager.getPlayer(event.getOwner().getUniqueId());
    } catch(McRPGPlayerNotFoundException exception){
      return;
    }
    FileConfiguration tamingFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.TAMING_CONFIG);
    
    int expToAward = 0;
    if(tamingFile.contains("ExpPerMobTamed." + event.getEntityType().name())){
      expToAward = tamingFile.getInt("ExpPerMobTamed." + event.getEntityType().name());
    }
    else{
      expToAward = tamingFile.getInt("ExpPerMobTamed.OTHER", 0);
    }
    
    if(event.getEntity().hasMetadata("ExpModifier")){
      expToAward *= event.getEntity().getMetadata("ExpModifier").get(0).asDouble();
    }
    if(expToAward > 0){
      mp.giveExp(Skills.TAMING, expToAward, GainReason.TAME);
    }
  }
}
