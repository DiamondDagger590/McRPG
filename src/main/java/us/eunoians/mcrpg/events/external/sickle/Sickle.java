package us.eunoians.mcrpg.events.external.sickle;

import com.cmdengineer.s.SickleEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.GainReason;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.util.mcmmo.HerbalismMethods;

public class Sickle implements Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  public void sickle(SickleEvent e){
    Block block = e.getBlock();
    try{
      FileConfiguration herbalism = McRPG.getInstance().getFileManager().getFile(FileManager.Files.HERBALISM_CONFIG);
      McRPGPlayer mp = PlayerManager.getPlayer(e.getPlayer().getUniqueId());
      //Deal with herbalism
      if(herbalism.getBoolean("HerbalismEnabled")){
        if(!McRPG.getPlaceStore().isTrue(block)){
          if(herbalism.contains("ExpAwardedPerBlock." + block.getType().toString())){
            int expWorth = herbalism.getInt("ExpAwardedPerBlock." + block.getType().toString());
            boolean oneBlockPlant = !(block.getType() == Material.CACTUS || block.getType() == Material.CHORUS_PLANT || block.getType() == Material.SUGAR_CANE);
            if(!oneBlockPlant){
              int amount = HerbalismMethods.calculateMultiBlockPlantDrops(block.getState());
              expWorth *= amount;
            }
            mp.giveExp(Skills.HERBALISM, expWorth, GainReason.BREAK);
          }
        }
      }
    } catch(McRPGPlayerNotFoundException ex){
      ex.printStackTrace();
    }
  }
}
