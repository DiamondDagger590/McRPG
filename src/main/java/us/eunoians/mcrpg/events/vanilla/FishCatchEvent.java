package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;

import java.util.Random;

public class FishCatchEvent implements Listener {

  @EventHandler
  public void catchEvent(PlayerFishEvent e){
    if(e.getCaught() == null){
      return;
    }
    McRPGPlayer mp = PlayerManager.getPlayer(e.getPlayer().getUniqueId());
    if(mp.getLastFishCaughtLoc() != null){
      FileConfiguration config = McRPG.getInstance().getConfig();
      String key = "PoseidonsGuardian.";
      Location lastLoc = mp.getLastFishCaughtLoc();
      Location currentLoc = e.getHook().getLocation();
      if(!lastLoc.getWorld().equals(currentLoc.getWorld())){
        mp.setLastFishCaughtLoc(currentLoc);
        return;
      }
      if(lastLoc.distance(currentLoc) <= config.getInt(key + "Range")){
        double incAmount = config.getDouble(key + "WithinRangeIncrease");
        double maxAmount = config.getDouble(key + "MaxChance");
        if(mp.getGuardianSummonChance() + incAmount > maxAmount){
          mp.setGuardianSummonChance(maxAmount);
        }
        else{
          mp.setGuardianSummonChance(mp.getGuardianSummonChance() + incAmount);
        }
        int chance = (int) mp.getGuardianSummonChance() * 1000;
        Random rand = new Random();
        int val = rand.nextInt(100000);
        if(chance >= val){
          mp.setGuardianSummonChance(config.getDouble(key + "DefaultSummonChance"));
          LivingEntity entity = (LivingEntity) currentLoc.getWorld().spawnEntity(e.getPlayer().getLocation(), EntityType.fromName(config.getString(key + "GuardianType")));
          //entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(config.getDouble(key + "Health"));
          entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(config.getDouble(key + "Health"));
          entity.setHealth(config.getDouble(key + "Health"));
          entity.setCustomName(Methods.color("&bPoseidon's Guardian"));
          ItemStack weapon = new ItemStack(Material.valueOf(config.getString(key + "Weapon")));
          if(config.getBoolean(key + "Enchanted")){
            for(String ench : config.getStringList(key + "Enchants")){
              String[] data = ench.split(":");
              weapon.addEnchantment(Enchantment.getByName(data[0]), Integer.parseInt(data[1]));
            }
          }
          entity.getEquipment().setItemInMainHand(weapon);
          ((Creature) entity).setTarget(e.getPlayer());
          Methods.setMetadata(entity, "GuardianExp", config.getInt(key + "RedeemableExpReward"));
          e.getPlayer().sendMessage(Methods.color(e.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Misc.PoseidonsGuardianSummoned")));
        }
      }
      else{
        double decAmount = config.getDouble(key + "OutsideRangeDecrease");
        double minAmount = config.getDouble(key + "MinChance");
        if(mp.getGuardianSummonChance() - decAmount < minAmount){
          mp.setGuardianSummonChance(minAmount);
        }
        else{
          mp.setGuardianSummonChance(mp.getGuardianSummonChance() - decAmount);
        }
      }
    }
    mp.setLastFishCaughtLoc(e.getCaught().getLocation());
  }
}
