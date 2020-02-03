package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.GainReason;
import us.eunoians.mcrpg.types.Skills;

public class EnchantingEvent implements Listener{
  
  @EventHandler(priority = EventPriority.MONITOR)
  public void enchantEvent(EnchantItemEvent e){
    try{
      McRPGPlayer mp = PlayerManager.getPlayer(e.getEnchanter().getUniqueId());
      FileConfiguration sorceryFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SORCERY_CONFIG);
      if(sorceryFile.getBoolean("SorceryEnabled")){
        if(sorceryFile.getBoolean("AwardExpPerMultipleEnchants")){
          int expToAward = 0;
          for(Enchantment enchantment : e.getEnchantsToAdd().keySet()){
            double expAmount = sorceryFile.getInt("ExpAwardedPerEnchantment." + enchantment.getName());
            expAmount *= sorceryFile.getDouble("EnchantmentLevelModifier." + e.getEnchantsToAdd().get(enchantment));
            expToAward += (int) expAmount;
          }
          if(expToAward != 0){
            mp.giveExp(Skills.SORCERY, expToAward, GainReason.ENCHANTING);
          }
        }
        else{
          double greatestWorth = 0;
          for(Enchantment ench : e.getEnchantsToAdd().keySet()){
            double expAmount = sorceryFile.getInt("ExpAwardedPerEnchantment." + ench.getName());
            expAmount *= sorceryFile.getDouble("EnchantmentLevelModifier." + e.getEnchantsToAdd().get(ench));
            if(expAmount > greatestWorth){
              greatestWorth = expAmount;
            }
          }
          if(greatestWorth != 0){
            mp.giveExp(Skills.SORCERY, (int) greatestWorth, GainReason.ENCHANTING);
          }
        }
      }
    }catch(McRPGPlayerNotFoundException ex){
      return;
    }
  }
}
