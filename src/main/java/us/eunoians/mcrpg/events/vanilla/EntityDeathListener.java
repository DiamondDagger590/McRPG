package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.fishing.PoseidonsFavor;
import us.eunoians.mcrpg.abilities.sorcery.HadesDomain;
import us.eunoians.mcrpg.abilities.taming.PETAsWrath;
import us.eunoians.mcrpg.api.events.mcrpg.fishing.PoseidonsFavorEvent;
import us.eunoians.mcrpg.api.events.mcrpg.sorcery.HadesDomainEvent;
import us.eunoians.mcrpg.api.events.mcrpg.taming.PETAsWrathEvent;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.List;
import java.util.Random;

public class EntityDeathListener implements Listener{
  
  @EventHandler
  public void deathEvent(EntityDeathEvent e){
    LivingEntity entity = e.getEntity();
    //Disabled Worlds
    if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
         McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(e.getEntity().getWorld().getName())) {
      return;
    }
    
    //Handle poseidon's guardian
    if(entity.getKiller() != null && entity.hasMetadata("GuardianExp")){
      int exp = entity.getMetadata("GuardianExp").get(0).asInt();
      Player p = entity.getKiller();
      McRPGPlayer mp;
      try{
        mp = PlayerManager.getPlayer(p.getUniqueId());
      }catch(McRPGPlayerNotFoundException exception){
        return;
      }
      if(UnlockedAbilities.POSEIDONS_FAVOR.isEnabled() && mp.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.POSEIDONS_FAVOR) && mp.getBaseAbility(UnlockedAbilities.POSEIDONS_FAVOR).isToggled()){
        PoseidonsFavor poseidonsFavor = (PoseidonsFavor) mp.getBaseAbility(UnlockedAbilities.POSEIDONS_FAVOR);
        int extraExp = McRPG.getInstance().getFileManager().getFile(FileManager.Files.FISHING_CONFIG).getInt("PoseidonsFavorConfig.Tier" + Methods.convertToNumeral(poseidonsFavor.getCurrentTier()) + ".ExpIncrease");
        PoseidonsFavorEvent poseidonsFavorEvent = new PoseidonsFavorEvent(mp, poseidonsFavor, extraExp);
        Bukkit.getPluginManager().callEvent(poseidonsFavorEvent);
        if(!poseidonsFavorEvent.isCancelled()){
          exp += poseidonsFavorEvent.getBonusExp();
        }
      }
      mp.giveRedeemableExp(exp);
      p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Utility.ObtainedRedeemableExp")
                                                                            .replace("%Amount%", Integer.toString(exp))));
    }
    
    //Handle PETAs Wrath
    if(entity.getKiller() != null && entity.getKiller().isValid() && entity instanceof Tameable){
      Tameable tameableEntity = (Tameable) entity;
      AnimalTamer owner = tameableEntity.getOwner();
      if(owner == null){
        return;
      }
      if(PlayerManager.isPlayerStored(owner.getUniqueId())){
        try{
          McRPGPlayer mcRPGPlayer = PlayerManager.getPlayer(owner.getUniqueId());
          FileConfiguration tamingFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.TAMING_CONFIG);
          if(tamingFile.getBoolean("TamingEnabled") && UnlockedAbilities.PETAS_WRATH.isEnabled() && mcRPGPlayer.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.PETAS_WRATH)
          && mcRPGPlayer.getBaseAbility(UnlockedAbilities.PETAS_WRATH).isToggled()){
            PETAsWrath petasWrath = (PETAsWrath) mcRPGPlayer.getBaseAbility(UnlockedAbilities.PETAS_WRATH);
            List<String> potionEffects = tamingFile.getStringList("PETAsWrathConfig.Tier" + Methods.convertToNumeral(petasWrath.getCurrentTier()) + ".PotionEffects");
            Random random = new Random();
            String[] data = potionEffects.get(random.nextInt(potionEffects.size())).split(":");
            PotionEffectType potionEffectType = PotionEffectType.getByName(data[0]);
            if(data.length != 3 || potionEffectType == null || !Methods.isInt(data[1]) || !Methods.isInt(data[2])){
              return;
            }
            int level = Math.min(0, Integer.parseInt(data[1]) - 1);
            int duration = Integer.parseInt(data[2]) * 20;
            PotionEffect potionEffect = new PotionEffect(potionEffectType, duration, level);
            PETAsWrathEvent petasWrathEvent = new PETAsWrathEvent(mcRPGPlayer, petasWrath, potionEffect);
            Bukkit.getPluginManager().callEvent(petasWrathEvent);
            if(!petasWrathEvent.isCancelled()){
              entity.getKiller().addPotionEffect(potionEffect);
            }
          }
        }catch(McRPGPlayerNotFoundException mcRPGPlayerNotFoundException){
          return;
        }
      }
    }
    
    //Handle hades domain
    if(entity.getKiller() != null && entity.getLocation().getBlock().getWorld().getEnvironment() == World.Environment.NETHER){
      Player killer = entity.getKiller();
      try{
        McRPGPlayer mp = PlayerManager.getPlayer(killer.getUniqueId());
        FileConfiguration sorceryFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SORCERY_CONFIG);
        if(sorceryFile.getBoolean("SorceryEnabled") && UnlockedAbilities.HADES_DOMAIN.isEnabled() && mp.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.HADES_DOMAIN)
        && mp.getBaseAbility(UnlockedAbilities.HADES_DOMAIN).isToggled()){
          HadesDomain hadesDomain = (HadesDomain) mp.getBaseAbility(UnlockedAbilities.HADES_DOMAIN);
          double multiplier = sorceryFile.getDouble("HadesDomainConfiguration.Tier" + Methods.convertToNumeral(hadesDomain.getCurrentTier()) + ".VanillaExpBoost");
          HadesDomainEvent hadesDomainEvent = new HadesDomainEvent(mp, hadesDomain, multiplier, false);
          Bukkit.getPluginManager().callEvent(hadesDomainEvent);
          if(!hadesDomainEvent.isCancelled()){
            multiplier = hadesDomainEvent.getPercentBonusVanillaExp();
            multiplier /= 100;
            multiplier += 1;
            e.setDroppedExp((int) (e.getDroppedExp() * multiplier));
          }
        }
      }
      catch(McRPGPlayerNotFoundException ex){
        ex.printStackTrace();
      }
    }
    
  }
}
