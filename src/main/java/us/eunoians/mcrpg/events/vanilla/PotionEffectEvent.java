package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.sorcery.CircesProtection;
import us.eunoians.mcrpg.api.events.mcrpg.sorcery.CircesProtectionEvent;
import us.eunoians.mcrpg.api.events.mcrpg.sorcery.PreCircesProtectionEvent;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class PotionEffectEvent implements Listener{
  
  private static Set<PotionEffectType> debuffs= new HashSet<>(Arrays.asList(PotionEffectType.WITHER, PotionEffectType.BLINDNESS, PotionEffectType.CONFUSION, PotionEffectType.HARM, PotionEffectType.HUNGER,
    PotionEffectType.POISON, PotionEffectType.SLOW, PotionEffectType.SLOW_DIGGING, PotionEffectType.UNLUCK, PotionEffectType.WEAKNESS));
  
  @EventHandler
  public void potionEffectEvent(EntityPotionEffectEvent e){
    if(e.getEntity() instanceof Player && e.getNewEffect() != null && debuffs.contains(e.getNewEffect().getType())){
      try{
        McRPGPlayer mp = PlayerManager.getPlayer(e.getEntity().getUniqueId());
        FileConfiguration sorceryConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SORCERY_CONFIG);
        if(sorceryConfig.getBoolean("SorceryEnabled") && UnlockedAbilities.CIRCES_PROTECTION.isEnabled() && mp.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.CIRCES_PROTECTION)
        && mp.getBaseAbility(UnlockedAbilities.CIRCES_PROTECTION).isToggled()){
          CircesProtection circesProtection = (CircesProtection) mp.getBaseAbility(UnlockedAbilities.CIRCES_PROTECTION);
          double chanceOfResisting = sorceryConfig.getDouble("CircesProtection.Tier" + Methods.convertToNumeral(circesProtection.getCurrentTier()) + ".ChanceOfResisting");
          PreCircesProtectionEvent preCircesProtectionEvent = new PreCircesProtectionEvent(mp, circesProtection, chanceOfResisting, e.getNewEffect().getType());
          Bukkit.getPluginManager().callEvent(preCircesProtectionEvent);
          if(!preCircesProtectionEvent.isCancelled()){
            double chance = preCircesProtectionEvent.getResistanceChance();
            Random rand = new Random();
            if(chance >= rand.nextInt(100)){
              CircesProtectionEvent circesProtectionEvent = new CircesProtectionEvent(mp, circesProtection, e.getNewEffect().getType());
              Bukkit.getPluginManager().callEvent(circesProtectionEvent);
              if(!circesProtectionEvent.isCancelled()){
                e.setCancelled(true);
              }
            }
          }
        }
      }catch(McRPGPlayerNotFoundException ex){
        ex.printStackTrace();
      }
    }
  }
}
