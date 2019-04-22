package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.herbalism.FarmersDiet;
import us.eunoians.mcrpg.abilities.woodcutting.HesperidesApples;
import us.eunoians.mcrpg.api.events.mcrpg.herbalism.FarmersDietEvent;
import us.eunoians.mcrpg.api.events.mcrpg.woodcutting.HesperidesApplesEvent;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class PlayerNomNomEvent implements Listener {

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onFoodLevelChange(FoodLevelChangeEvent event) {
    Entity entity = event.getEntity();

    if(!(entity instanceof Player)) {
      return;
    }

    Player player = (Player) entity;
    int currentFoodLevel = player.getFoodLevel();
    int newFoodLevel = event.getFoodLevel();
    int foodChange = newFoodLevel - currentFoodLevel;

    if(foodChange <= 0) {
      return;
    }
    McRPGPlayer mp = PlayerManager.getPlayer(player.getUniqueId());
    if(UnlockedAbilities.FARMERS_DIET.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.FARMERS_DIET) && mp.getBaseAbility(UnlockedAbilities.FARMERS_DIET).isToggled()) {
      FarmersDiet farmersDiet = (FarmersDiet) mp.getBaseAbility(UnlockedAbilities.FARMERS_DIET);
      FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.HERBALISM_CONFIG);
      String key = "FarmersDietConfig.Tier" + Methods.convertToNumeral(farmersDiet.getCurrentTier()) + ".";
      int foodRestorationBonus = config.getInt(key + "FoodRestorationBonus");
      double saturationBonus = config.getDouble(key + "SaturationBonus");
      ItemStack mainHand = player.getInventory().getItemInMainHand();
      ItemStack offHand = player.getInventory().getItemInOffHand();
      boolean useMainHand = config.getStringList("FarmersDietConfig.FoodForFarmersDiet").contains(mainHand.getType().toString());
      boolean useOffHand = config.getStringList("FarmersDietConfig.FoodForFarmersDiet").contains(offHand.getType().toString());
      if((useMainHand || useOffHand)) {
        Material foodType;
        if(useMainHand) {
          foodType = mainHand.getType();
        }
        else {
          foodType = offHand.getType();
        }
        FarmersDietEvent farmersDietEvent = new FarmersDietEvent(mp, farmersDiet, foodRestorationBonus, saturationBonus, foodType);
        Bukkit.getPluginManager().callEvent(farmersDietEvent);
        if(!farmersDietEvent.isCancelled()) {
          player.setSaturation((float) (player.getSaturation() + farmersDietEvent.getSaturationBonus()));
          event.setFoodLevel(newFoodLevel + farmersDietEvent.getFoodRestorationBonus());
        }
      }
    }
    if(UnlockedAbilities.HESPERIDES_APPLES.isEnabled() && mp.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.HESPERIDES_APPLES)
            && mp.getBaseAbility(UnlockedAbilities.HESPERIDES_APPLES).isToggled() && mp.getCooldown(UnlockedAbilities.HESPERIDES_APPLES) == -1) {
      HesperidesApples hesperidesApples = (HesperidesApples) mp.getBaseAbility(UnlockedAbilities.HESPERIDES_APPLES);
      FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.WOODCUTTING_CONFIG);
      String key = "HesperidesApplesConfig.Tier" + Methods.convertToNumeral(hesperidesApples.getCurrentTier()) + ".";
      ItemStack mainHand = player.getInventory().getItemInMainHand();
      ItemStack offHand = player.getInventory().getItemInOffHand();
      boolean useMainHand = mainHand.getType() == Material.GOLDEN_APPLE || mainHand.getType() == Material.APPLE;
      boolean useOffHand = offHand.getType() == Material.GOLDEN_APPLE || offHand.getType() == Material.APPLE;
      int cooldown = config.getInt(key + "Cooldown");
      if((useMainHand || useOffHand)) {

        Material foodType;
        if(useMainHand) {
          foodType = mainHand.getType();
        }
        else {
          foodType = offHand.getType();
        }
        String apple = foodType == Material.GOLDEN_APPLE ? "GoldenApple" : "Apple";
        List<String> potionEffects = config.getStringList(key + apple + "Buffs");
        Random rand = new Random();
        if(potionEffects.size() != 0) {
          int i = rand.nextInt(potionEffects.size());
          String[] effect = potionEffects.get(i).split(":");
          PotionEffectType effectType = PotionEffectType.getByName(effect[0]);
          int multiplier = Integer.parseInt(effect[1]);
          multiplier = multiplier > 0 ? multiplier - 1 : 0;
          int duration = Integer.parseInt(effect[2]);
          HesperidesApplesEvent hesperidesApplesEvent = new HesperidesApplesEvent(mp, hesperidesApples, effectType, multiplier, duration, cooldown, foodType);
          Bukkit.getPluginManager().callEvent(hesperidesApplesEvent);
          if(!hesperidesApplesEvent.isCancelled()) {
            PotionEffect potionEffect = new PotionEffect(hesperidesApplesEvent.getPotionEffectType(), hesperidesApplesEvent.getDuration(), hesperidesApplesEvent.getMultiplier());
            player.addPotionEffect(potionEffect);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, hesperidesApplesEvent.getCooldown());
            mp.addAbilityOnCooldown(UnlockedAbilities.HESPERIDES_APPLES, cal.getTimeInMillis());
            player.sendMessage(Methods.color(player, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.HesperidesApples.Activated")));
          }
        }
      }
    }
  }
}
