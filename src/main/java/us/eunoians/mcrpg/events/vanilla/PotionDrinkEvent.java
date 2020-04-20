package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.sorcery.PotionAffinity;
import us.eunoians.mcrpg.api.events.mcrpg.sorcery.PotionAffinityEvent;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.brewing.BasePotion;
import us.eunoians.mcrpg.api.util.brewing.PotionFactory;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.ArrayList;
import java.util.List;

public class PotionDrinkEvent implements Listener{
  
  @EventHandler
  public void drinkEvent(PlayerItemConsumeEvent e){
    if(e.getItem().getType() == Material.POTION){
      //Disabled Worlds
      if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
           McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(e.getPlayer().getWorld().getName())) {
        return;
      }
      PotionMeta potionMeta = (PotionMeta) e.getItem().getItemMeta();
      try{
        McRPGPlayer mp = PlayerManager.getPlayer(e.getPlayer().getUniqueId());
        FileConfiguration sorceryFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SORCERY_CONFIG);
        if(sorceryFile.getBoolean("SorceryEnabled") && UnlockedAbilities.POTION_AFFINITY.isEnabled() && mp.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.POTION_AFFINITY)
        && mp.getBaseAbility(UnlockedAbilities.POTION_AFFINITY).isToggled()){
          PotionAffinity potionAffinity = (PotionAffinity) mp.getBaseAbility(UnlockedAbilities.POTION_AFFINITY);
          double multiplier = sorceryFile.getDouble("PotionAffinityConfig.Tier" + Methods.convertToNumeral(potionAffinity.getCurrentTier()) + ".PotionDurationBonus");
          PotionAffinityEvent potionAffinityEvent = new PotionAffinityEvent(potionAffinity, mp, multiplier, e.getItem());
          Bukkit.getPluginManager().callEvent(potionAffinityEvent);
          if(!potionAffinityEvent.isCancelled()){
            multiplier = potionAffinityEvent.getDurationMultiplier();
            multiplier /= 100;
            multiplier += 1;
            ItemStack itemStack = e.getItem();
            if(potionMeta.getBasePotionData().getType() != PotionType.UNCRAFTABLE){
              BasePotion basePotion = PotionFactory.convertItemStackToBasePotion(e.getItem());
              itemStack = basePotion.getAsItem();
            }
            if(potionMeta.hasCustomEffects()){
              List<PotionEffect> potionEffectList = potionMeta.getCustomEffects();
              List<PotionEffect> newList = new ArrayList<>();
              for(PotionEffect potionEffect : potionEffectList){
                PotionEffect newEffect = new PotionEffect(potionEffect.getType(), (int) (potionEffect.getDuration() * multiplier), potionEffect.getAmplifier());
                newList.add(newEffect);
              }
              potionMeta.clearCustomEffects();
              for(PotionEffect potionEffect : newList){
                potionMeta.addCustomEffect(potionEffect, true);
              }
            }
            itemStack.setItemMeta(potionMeta);
            e.setItem(itemStack);
          }
        }
      }catch(McRPGPlayerNotFoundException ex){
        ex.printStackTrace();
      }
    }
  }
}
