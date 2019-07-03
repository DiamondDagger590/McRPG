package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.fishing.*;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.fishing.FishingResult;
import us.eunoians.mcrpg.api.util.fishing.ShakeResult;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.skills.Fishing;
import us.eunoians.mcrpg.types.*;
import us.eunoians.mcrpg.util.Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@SuppressWarnings("ALL")
public class FishCatchEvent implements Listener {

  @EventHandler
  public void catchEvent(PlayerFishEvent e) {
    if (PlayerManager.isPlayerFrozen(e.getPlayer().getUniqueId())) {
      return;
    }
    if (e.getCaught() == null) {
      return;
    }
    McRPGPlayer mp = PlayerManager.getPlayer(e.getPlayer().getUniqueId());
    if(e.getState() == PlayerFishEvent.State.CAUGHT_ENTITY){
      if(Skills.FISHING.isEnabled()){
        if(UnlockedAbilities.SHAKE.isEnabled() && mp.getBaseAbility(UnlockedAbilities.SHAKE).isUnlocked() && mp.getBaseAbility(UnlockedAbilities.SHAKE).isToggled()
                && McRPG.getInstance().getFishingItemManager().canShake(e.getCaught().getType())){
          Shake shake = (Shake) mp.getBaseAbility(UnlockedAbilities.SHAKE);
          Random rand = new Random();
          int chance = (int) McRPG.getInstance().getFileManager().getFile(FileManager.Files.FISHING_CONFIG).getDouble("ShakeConfig.Tier" + Methods.convertToNumeral(shake.getCurrentTier()) + ".ActivationChance") * 1000;
          int val = rand.nextInt(100000);
          if (chance >= val) {
            ShakeResult result = McRPG.getInstance().getFishingItemManager().getShakeItem(e.getCaught().getType());
            e.getCaught().getLocation().getWorld().dropItemNaturally(e.getCaught().getLocation(), result.getItemStack());
            mp.giveExp(Skills.FISHING, result.getExp(), GainReason.ABILITY);
          }
        }
      }
    }
    if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
      if (Skills.FISHING.isEnabled()) {
        Fishing fishing = (Fishing) mp.getSkill(Skills.FISHING);
        Item caughtItem = (Item) e.getCaught();
        FileConfiguration fishingLoot = McRPG.getInstance().getFileManager().getFile(FileManager.Files.FISHING_LOOT);
        FileConfiguration fishingConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.FISHING_CONFIG);
        ItemStack fishingRod = e.getPlayer().getItemInHand();
        List<String> validCategories = new ArrayList<>();
        HashMap<String, GenericAbility> categoryToAbilityMap = new HashMap<>();
        Random rand = new Random();

        for (String category : fishingConfig.getConfigurationSection("CategoriesDefault").getKeys(false)) {
          double c = fishingConfig.getDouble("CategoriesDefault." + category);
          if (category.equalsIgnoreCase("Treasure") && DefaultAbilities.GREAT_ROD.isEnabled() && mp.getBaseAbility(DefaultAbilities.GREAT_ROD).isToggled()) {
            Parser equation = DefaultAbilities.GREAT_ROD.getActivationEquation();
            equation.setVariable("fishing_level", fishing.getCurrentLevel());
            c += equation.getValue();
            if (fishingRod.getEnchantments().containsKey(Enchantment.LUCK)) {
              if (category.equalsIgnoreCase("Treasure")) {
                Parser parser = new Parser(fishingConfig.getString("LuckOfSeaModifiers.TreasureModifier"));
                parser.setVariable("level", fishingRod.getEnchantmentLevel(Enchantment.LUCK));
                c += parser.getValue();
              }
            }
          }
          else if (category.equalsIgnoreCase("Junk") && fishingRod.getEnchantments().containsKey(Enchantment.LUCK)) {
            Parser parser = new Parser(fishingConfig.getString("LuckOfSeaModifiers.JunkModifier"));
            parser.setVariable("level", fishingRod.getEnchantmentLevel(Enchantment.LUCK));
            c -= parser.getValue();
          }
          int chance = (int) c * 1000;
          int val = rand.nextInt(100000);
          if (chance >= val) {
            validCategories.add(category);
            categoryToAbilityMap.put(category, DefaultAbilities.GREAT_ROD);
          }
        }
        if (UnlockedAbilities.SUNKEN_ARMORY.isEnabled() && mp.getBaseAbility(UnlockedAbilities.SUNKEN_ARMORY).isUnlocked() && mp.getBaseAbility(UnlockedAbilities.SUNKEN_ARMORY).isToggled()) {
          SunkenArmory sunkenArmory = (SunkenArmory) mp.getBaseAbility(UnlockedAbilities.SUNKEN_ARMORY);
          String key = "SunkenArmoryConfig.Tier" + Methods.convertToNumeral(sunkenArmory.getCurrentTier()) + ".ExtraCategories";
          for (String category : fishingConfig.getConfigurationSection(key).getKeys(false)) {
            double c = fishingConfig.getDouble(key + "." + category);
            int chance = (int) c * 1000;
            int val = rand.nextInt(100000);
            if (chance >= val) {
              validCategories.add(category);
              categoryToAbilityMap.put(category, UnlockedAbilities.SUNKEN_ARMORY);
            }
          }
        }
        if (UnlockedAbilities.SEA_GODS_BLESSING.isEnabled() && mp.getBaseAbility(UnlockedAbilities.SEA_GODS_BLESSING).isUnlocked() && mp.getBaseAbility(UnlockedAbilities.SEA_GODS_BLESSING).isToggled()) {
          SeaGodsBlessing seaGodsBlessing = (SeaGodsBlessing) mp.getBaseAbility(UnlockedAbilities.SEA_GODS_BLESSING);
          String key = "SeaGodsBlessingConfig.Tier" + Methods.convertToNumeral(seaGodsBlessing.getCurrentTier()) + ".ExtraCategories";
          for (String category : fishingConfig.getConfigurationSection(key).getKeys(false)) {
            double c = fishingConfig.getDouble(key + "." + category);
            int chance = (int) c * 1000;
            int val = rand.nextInt(100000);
            if (chance >= val) {
              validCategories.add(category);
              categoryToAbilityMap.put(category, UnlockedAbilities.SEA_GODS_BLESSING);
            }
          }
        }
        if (UnlockedAbilities.MAGIC_TOUCH.isEnabled() && mp.getBaseAbility(UnlockedAbilities.MAGIC_TOUCH).isUnlocked() && mp.getBaseAbility(UnlockedAbilities.MAGIC_TOUCH).isToggled()) {
          MagicTouch magicTouch = (MagicTouch) mp.getBaseAbility(UnlockedAbilities.MAGIC_TOUCH);
          String key = "MagicTouchConfig.Tier" + Methods.convertToNumeral(magicTouch.getCurrentTier()) + ".ExtraCategories";
          for (String category : fishingConfig.getConfigurationSection(key).getKeys(false)) {
            double c = fishingConfig.getDouble(key + "." + category);
            int chance = (int) c * 1000;
            int val = rand.nextInt(100000);
            if (chance >= val) {
              validCategories.add(category);
              categoryToAbilityMap.put(category, UnlockedAbilities.MAGIC_TOUCH);
            }
          }
        }
        String category = !validCategories.isEmpty() ? validCategories.get(rand.nextInt(validCategories.size())) : "Junk";
        if (category.equalsIgnoreCase("Treasure") && UnlockedAbilities.SUPER_ROD.isEnabled() && mp.getBaseAbility(UnlockedAbilities.SUPER_ROD).isUnlocked() && mp.getBaseAbility(UnlockedAbilities.SUPER_ROD).isToggled()) {
          SuperRod superRod = (SuperRod) mp.getBaseAbility(UnlockedAbilities.SUPER_ROD);
          categoryToAbilityMap.put("SuperRod", UnlockedAbilities.SUPER_ROD);
          double c = fishingConfig.getDouble("SuperRodConfig.Tier" + Methods.convertToNumeral(superRod.getCurrentTier()) + ".ActivationChance");
          int chance = (int) c * 1000;
          int val = rand.nextInt(100000);
          if (chance >= val) {
            category = "SuperRod";
          }
        }
        FishingResult fishingResult = McRPG.getInstance().getFishingItemManager().generateItem(category, mp.getBaseAbility(categoryToAbilityMap.get(category)), mp);
        caughtItem.setItemStack(fishingResult.getItemStack());
        mp.giveExp(Skills.FISHING, fishingResult.getMcrpgExp(), GainReason.FISHING);
        e.setExpToDrop(fishingResult.getVanillaExp());
      }
      //Poseidons Guardian info
      if (mp.getLastFishCaughtLoc() != null) {
        FileConfiguration config = McRPG.getInstance().getConfig();
        String key = "PoseidonsGuardian.";
        Location lastLoc = mp.getLastFishCaughtLoc();
        Location currentLoc = e.getHook().getLocation();
        if (!lastLoc.getWorld().equals(currentLoc.getWorld())) {
          mp.setLastFishCaughtLoc(currentLoc);
          return;
        }
        if (lastLoc.distance(currentLoc) <= config.getInt(key + "Range")) {
          double incAmount = config.getDouble(key + "WithinRangeIncrease");
          double maxAmount = config.getDouble(key + "MaxChance");
          if (mp.getGuardianSummonChance() + incAmount > maxAmount) {
            mp.setGuardianSummonChance(maxAmount);
          } else {
            mp.setGuardianSummonChance(mp.getGuardianSummonChance() + incAmount);
          }
          int chance = (int) mp.getGuardianSummonChance() * 1000;
          Random rand = new Random();
          int val = rand.nextInt(100000);
          if (chance >= val) {
            mp.setGuardianSummonChance(config.getDouble(key + "DefaultSummonChance"));
            LivingEntity entity = (LivingEntity) currentLoc.getWorld().spawnEntity(e.getPlayer().getLocation(), EntityType.fromName(config.getString(key + "GuardianType")));
            //entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(config.getDouble(key + "Health"));
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(config.getDouble(key + "Health"));
            entity.setHealth(config.getDouble(key + "Health"));
            entity.setCustomName(Methods.color("&bPoseidon's Guardian"));
            ItemStack weapon = new ItemStack(Material.valueOf(config.getString(key + "Weapon")));
            if (config.getBoolean(key + "Enchanted")) {
              for (String ench : config.getStringList(key + "Enchants")) {
                String[] data = ench.split(":");
                weapon.addEnchantment(Enchantment.getByName(data[0]), Integer.parseInt(data[1]));
              }
            }
            entity.getEquipment().setItemInMainHand(weapon);
            ((Creature) entity).setTarget(e.getPlayer());
            Methods.setMetadata(entity, "GuardianExp", config.getInt(key + "RedeemableExpReward"));
            e.getPlayer().sendMessage(Methods.color(e.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Misc.PoseidonsGuardianSummoned")));
          }
        } else {
          double decAmount = config.getDouble(key + "OutsideRangeDecrease");
          double minAmount = config.getDouble(key + "MinChance");
          if (mp.getGuardianSummonChance() - decAmount < minAmount) {
            mp.setGuardianSummonChance(minAmount);
          } else {
            mp.setGuardianSummonChance(mp.getGuardianSummonChance() - decAmount);
          }
        }
      }
      mp.setLastFishCaughtLoc(e.getCaught().getLocation());
    }
  }
}
