package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.abilities.excavation.Extraction;
import us.eunoians.mcrpg.abilities.excavation.FrenzyDig;
import us.eunoians.mcrpg.abilities.excavation.HandDigging;
import us.eunoians.mcrpg.abilities.herbalism.MassHarvest;
import us.eunoians.mcrpg.abilities.herbalism.PansBlessing;
import us.eunoians.mcrpg.abilities.mining.BlastMining;
import us.eunoians.mcrpg.abilities.mining.DoubleDrop;
import us.eunoians.mcrpg.abilities.mining.OreScanner;
import us.eunoians.mcrpg.abilities.mining.SuperBreaker;
import us.eunoians.mcrpg.api.events.mcrpg.excavation.FrenzyDigEvent;
import us.eunoians.mcrpg.api.events.mcrpg.excavation.HandDiggingEvent;
import us.eunoians.mcrpg.api.events.mcrpg.herbalism.MassHarvestEvent;
import us.eunoians.mcrpg.api.events.mcrpg.herbalism.PansBlessingEvent;
import us.eunoians.mcrpg.api.events.mcrpg.mining.BlastMiningEvent;
import us.eunoians.mcrpg.api.events.mcrpg.mining.BlastTestEvent;
import us.eunoians.mcrpg.api.events.mcrpg.mining.OreScannerEvent;
import us.eunoians.mcrpg.api.events.mcrpg.mining.SuperBreakerEvent;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.brewing.BrewingStandManager;
import us.eunoians.mcrpg.api.util.brewing.standmeta.BrewingGUI;
import us.eunoians.mcrpg.gui.GUITracker;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.players.PlayerReadyBit;
import us.eunoians.mcrpg.types.DefaultAbilities;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.mcmmo.ItemUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class InteractHandler implements Listener {

  private static ItemStack shovel;

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void interactHandler(PlayerInteractEvent e) {
    if(PlayerManager.isPlayerFrozen(e.getPlayer().getUniqueId())){
      return;
    }
    //Used for Hand Digging ability so we have a persistant silk shovel
    if(shovel == null){
      shovel = new ItemStack(Material.DIAMOND_SHOVEL);
      shovel.addEnchantment(Enchantment.SILK_TOUCH, 1);
      ItemMeta meta = shovel.getItemMeta();
      meta.setUnbreakable(true);
      shovel.setItemMeta(meta);
    }
    
    Player p = e.getPlayer();
    McRPGPlayer mp;
    try{
      mp = PlayerManager.getPlayer(p.getUniqueId());
    }
    catch(McRPGPlayerNotFoundException exception){
      return;
    }
    
    if(e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.BREWING_STAND){
      if(p.isSneaking() && !(p.getInventory().getItemInMainHand() == null || p.getInventory().getItemInMainHand().getType() == Material.AIR)){
        return;
      }
      BrewingStand brewingStand = (BrewingStand) e.getClickedBlock().getState();
      if(!Skills.SORCERY.isEnabled()){
        return;
      }
      mp.getPlayer().incrementStatistic(Statistic.BREWINGSTAND_INTERACTION);
      BrewingStandManager brewingStandManager = McRPG.getInstance().getBrewingStandManager();
      BrewingGUI brewingGUI;
      if(brewingStandManager.isBrewingStandLoaded(brewingStand)){
        brewingGUI = brewingStandManager.getBrewingStandWrapper(brewingStand).getBrewingGUI();
      }
      else{
        brewingGUI = McRPG.getInstance().getBrewingStandManager().initNewBrewingStand(brewingStand).getBrewingGUI();
      }
      brewingGUI.setLastInteractedPlayer(p);
      GUITracker.trackPlayer(p, brewingGUI);
      e.setCancelled(true);
      p.openInventory(brewingGUI.getInv());
      return;
    }
    ItemStack heldItem = e.getItem();
    //if they arent holding anything and they are hand digging/its a valid block, insta break it
    if(heldItem == null) {
      if(mp.isHandDigging() && e.getClickedBlock() != null){
        if(mp.getHandDiggingBlocks().contains(e.getClickedBlock().getType())){
          e.getClickedBlock().breakNaturally(shovel);
        }
      }
      return;
    }
    
    if(e.isCancelled() && e.getAction() == Action.RIGHT_CLICK_AIR) {
      return;
    }
    
    Block target = e.getClickedBlock();
    Material type;
    
    //prevent possible NPE's
    if(target == null) {
      type = Material.AIR;
    }
    else {
      type = target.getType();
    }
    
    //If the player is readying
    if(mp.isReadying()) {
      
      PlayerReadyBit bit = mp.getReadyingAbilityBit();
      
      //If bit for some reason is null but they are readying we want to set it so they arent
      if(bit == null) {
        mp.setReadying(false);
        return;
      }
      //Get the readying ability and the BaseAbility instance
      UnlockedAbilities abilityType = bit.getAbilityReady();
      BaseAbility ability = mp.getSkill(abilityType.getSkill()).getAbility(abilityType);
      //if they are readying blast mining
      if(abilityType == UnlockedAbilities.BLAST_MINING) {
        //verify they are trying to place tnt
        if(heldItem.getType() == Material.TNT) {
          BlastMining blastMining = (BlastMining) ability;
          FileConfiguration mining = McRPG.getInstance().getFileManager().getFile(FileManager.Files.MINING_CONFIG);
          e.setCancelled(true);
          String key = "BlastMiningConfig.Tier" + Methods.convertToNumeral(blastMining.getCurrentTier());
          int radius = mining.getInt(key + ".Radius");
          int cooldown = mining.getInt(key + ".Cooldown");
          boolean useBlacklist = mining.getBoolean("BlastMiningConfig.UseBlackList");
          boolean useWhiteList = mining.getBoolean("BlastMiningConfig.UseWhiteList");
          List<String> blackList = mining.getStringList("BlastMiningConfig.BlackList");
          List<String> whiteList = mining.getStringList("BlastMiningConfig.WhiteList");
          //Populate our blocks array
          ArrayList<Block> blocks = new ArrayList<>();
          for(int x = -1 * radius; x < radius; x++) {
            for(int z = -1 * radius; z < radius; z++) {
              for(int y = -1 * radius; y < radius; y++) {
                blocks.add(e.getClickedBlock().getLocation().add(x, y, z).getBlock());
              }
            }
          }
          //Call the blast mining event
          BlastMiningEvent blastMiningEvent = new BlastMiningEvent(mp, blastMining, blocks, cooldown);
          Bukkit.getPluginManager().callEvent(blastMiningEvent);
          if(!blastMiningEvent.isCancelled()) {
            //Set the active abilities (more internal use than anything)
            mp.getActiveAbilities().add(UnlockedAbilities.BLAST_MINING);
            heldItem.setAmount(heldItem.getAmount() - 1);
            if(heldItem.getAmount() <= 0) {
              heldItem.setType(Material.AIR);
            }
            p.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, p.getLocation(), 30);
            FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
            p.getLocation().getWorld().playSound(p.getLocation(), Sound.valueOf(soundFile.getString("Sounds.Mining.BlastMining.Sound")),
              Float.parseFloat(soundFile.getString("Sounds.Mining.BlastMining.Volume")), Float.parseFloat(soundFile.getString("Sounds.Mining.BlastMining.Pitch")));
            ItemStack pick = new ItemStack(Material.DIAMOND_PICKAXE, 1);
            for(Block b : blastMiningEvent.getBlocks()) {
              Material material = b.getType();
              if(material == Material.WATER || material == Material.LAVA || material.toString().contains("AIR")) {
                continue;
              }
              if(useBlacklist && blackList.contains(material.toString())) {
                continue;
              }
              if(useWhiteList && !whiteList.contains(material.toString())) {
                continue;
              }
              //Test for land protections
              BlastTestEvent breakEvent = new BlastTestEvent(b, p);
              Bukkit.getPluginManager().callEvent(breakEvent);
              if(breakEvent.isCancelled()) {
                continue;
              }
              b.breakNaturally(pick);
            }
            //set cooldown
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, blastMiningEvent.getCooldown());
            //Cancel and reset readying
            Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
            mp.setReadyingAbilityBit(null);
            mp.setReadying(false);
            mp.getActiveAbilities().remove(UnlockedAbilities.BLAST_MINING);
            mp.addAbilityOnCooldown(UnlockedAbilities.BLAST_MINING, cal.getTimeInMillis());
          }
        }
        return;
      }
      
      //Handle Super Breaker
      else if(abilityType.equals(UnlockedAbilities.SUPER_BREAKER) && (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR)) {
        SuperBreaker superBreaker = (SuperBreaker) ability;
        FileConfiguration mining = McRPG.getInstance().getFileManager().getFile(FileManager.Files.MINING_CONFIG);
        //e.setCancelled(true);
        int hasteDuration = mining.getInt("SuperBreakerConfig.Tier" + Methods.convertToNumeral(superBreaker.getCurrentTier()) + ".Duration");
        int cooldown = mining.getInt("SuperBreakerConfig.Tier" + Methods.convertToNumeral(superBreaker.getCurrentTier()) + ".Cooldown");
        double boost = mining.getDouble("SuperBreakerConfig.Tier" + Methods.convertToNumeral(superBreaker.getCurrentTier()) + ".ActivationBoost");
        SuperBreakerEvent superBreakerEvent = new SuperBreakerEvent(mp, superBreaker, cooldown, boost, hasteDuration);
        Bukkit.getPluginManager().callEvent(superBreakerEvent);
        if(superBreakerEvent.isCancelled()) {
          return;
        }
        mp.getActiveAbilities().add(UnlockedAbilities.SUPER_BREAKER);
        Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
        mp.setReadyingAbilityBit(null);
        mp.setReadying(false);
        DoubleDrop doubleDrop = (DoubleDrop) mp.getBaseAbility(DefaultAbilities.DOUBLE_DROP);
        doubleDrop.setBonusChance(doubleDrop.getBonusChance() + superBreakerEvent.getBoost());
        PotionEffect effect = new PotionEffect(PotionEffectType.FAST_DIGGING, superBreakerEvent.getHasteDuration() * 20, 20);
        p.addPotionEffect(effect);
        mp.getPlayer().sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() +
                McRPG.getInstance().getLangFile().getString("Messages.Abilities.SuperBreaker.Activated")));
        new BukkitRunnable() {
          @Override
          public void run() {
            doubleDrop.setBonusChance(0);
            if(mp.isOnline()) {
              mp.getPlayer().sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() +
                      McRPG.getInstance().getLangFile().getString("Messages.Abilities.SuperBreaker.Deactivated")));
              FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
              mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.valueOf(soundFile.getString("Sounds.Mining.SuperBreaker.Sound")),
                Float.parseFloat(soundFile.getString("Sounds.Mining.SuperBreaker.Volume")), Float.parseFloat(soundFile.getString("Sounds.Mining.SuperBreaker.Pitch")));
            }
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND,
                    superBreakerEvent.getCooldown());
            mp.getActiveAbilities().remove(UnlockedAbilities.SUPER_BREAKER);
            mp.addAbilityOnCooldown(UnlockedAbilities.SUPER_BREAKER, cal.getTimeInMillis());
          }
        }.runTaskLater(McRPG.getInstance(), superBreakerEvent.getHasteDuration() * 20);
        return;
      }
     
      //Handle Frenzy Dig
      else if(abilityType.equals(UnlockedAbilities.FRENZY_DIG) && (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR)) {
        FrenzyDig frenzyDig = (FrenzyDig) ability;
        FileConfiguration excavationConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.EXCAVATION_CONFIG);
        //e.setCancelled(true);
        int hasteDuration = excavationConfig.getInt("FrenzyDigConfig.Tier" + Methods.convertToNumeral(frenzyDig.getCurrentTier()) + ".Duration");
        int cooldown = excavationConfig.getInt("FrenzyDigConfig.Tier" + Methods.convertToNumeral(frenzyDig.getCurrentTier()) + ".Cooldown");
        double boost = excavationConfig.getDouble("FrenzyDigConfig.Tier" + Methods.convertToNumeral(frenzyDig.getCurrentTier()) + ".ActivationBoost");
        FrenzyDigEvent frenzyDigEvent = new FrenzyDigEvent(mp, frenzyDig, hasteDuration, cooldown, boost);
        Bukkit.getPluginManager().callEvent(frenzyDigEvent);
        if(frenzyDigEvent.isCancelled()) {
          return;
        }
        Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
        mp.setReadyingAbilityBit(null);
        mp.setReadying(false);
        Extraction extraction = (Extraction) mp.getBaseAbility(DefaultAbilities.EXTRACTION);
        extraction.setBonusChance(extraction.getBonusChance() + frenzyDigEvent.getExtractionBuff());
        PotionEffect effect = new PotionEffect(PotionEffectType.FAST_DIGGING, frenzyDigEvent.getHasteDuration() * 20, 20);
        p.addPotionEffect(effect);
        mp.getPlayer().sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() +
                McRPG.getInstance().getLangFile().getString("Messages.Abilities.FrenzyDig.Activated")));
        mp.getActiveAbilities().add(UnlockedAbilities.FRENZY_DIG);
        new BukkitRunnable() {
          @Override
          public void run() {
            extraction.setBonusChance(0);
            if(mp.isOnline()) {
              mp.getPlayer().sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() +
                      McRPG.getInstance().getLangFile().getString("Messages.Abilities.FrenzyDig.Deactivated")));
              FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
              mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.valueOf(soundFile.getString("Sounds.Excavation.FrenzyDig.Sound")),
                Float.parseFloat(soundFile.getString("Sounds.Excavation.FrenzyDig.Volume")), Float.parseFloat(soundFile.getString("Sounds.Excavation.FrenzyDig.Pitch")));
            }
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND,
                    frenzyDigEvent.getCooldown());
            mp.getActiveAbilities().remove(UnlockedAbilities.FRENZY_DIG);
            mp.addAbilityOnCooldown(UnlockedAbilities.FRENZY_DIG, cal.getTimeInMillis());
          }
        }.runTaskLater(McRPG.getInstance(), frenzyDigEvent.getHasteDuration() * 20);
        return;
      }
    
      //Handle Hand Digging
      else if(abilityType.equals(UnlockedAbilities.HAND_DIGGING) && (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR)){
        HandDigging handDigging = (HandDigging) ability;
        FileConfiguration excavationConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.EXCAVATION_CONFIG);
        if(e.getClickedBlock() == null || e.getClickedBlock().getType() == null){
          return;
        }
        if(BreakEvent.canLargerSpade(e.getClickedBlock().getType())){
          int duration = excavationConfig.getInt("HandDiggingConfig.Tier" + Methods.convertToNumeral(handDigging.getCurrentTier()) + ".Duration");
          int cooldown = excavationConfig.getInt("HandDiggingConfig.Tier" + Methods.convertToNumeral(handDigging.getCurrentTier()) + ".Cooldown");
          HandDiggingEvent handDiggingEvent = new HandDiggingEvent(mp, handDigging, duration, cooldown, BreakEvent.getExcavationBlocks());
          Bukkit.getPluginManager().callEvent(handDiggingEvent);
          if(!handDiggingEvent.isCancelled()){
            mp.getPlayer().sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() +
                    McRPG.getInstance().getLangFile().getString("Messages.Abilities.HandDigging.Activated")));
            Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
            mp.setReadyingAbilityBit(null);
            mp.setReadying(false);
            mp.setHandDigging(true);
            mp.setHandDiggingBlocks(handDiggingEvent.getBreakableBlocks());
            mp.getActiveAbilities().add(UnlockedAbilities.HAND_DIGGING);
            new BukkitRunnable(){
              @Override
              public void run() {
                mp.setHandDigging(false);
                mp.getPlayer().sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() +
                        McRPG.getInstance().getLangFile().getString("Messages.Abilities.HandDigging.Deactivated")));
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.SECOND,
                        handDiggingEvent.getCooldown());
                mp.getActiveAbilities().remove(UnlockedAbilities.HAND_DIGGING);
                mp.addAbilityOnCooldown(UnlockedAbilities.HAND_DIGGING, cal.getTimeInMillis());
              }
            }.runTaskLater(McRPG.getInstance(), handDiggingEvent.getDuration() * 20);
          }
        }
      }
   
      //Handle Ore Scanner
      else if(abilityType == UnlockedAbilities.ORE_SCANNER && (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR)) {
        OreScanner oreScanner = (OreScanner) ability;
        FileConfiguration mining = McRPG.getInstance().getFileManager().getFile(FileManager.Files.MINING_CONFIG);
        e.setCancelled(true);
        int radius = mining.getInt("OreScannerConfig.Tier" + Methods.convertToNumeral(oreScanner.getCurrentTier()) + ".Radius");
        int cooldown = mining.getInt("OreScannerConfig.Tier" + Methods.convertToNumeral(oreScanner.getCurrentTier()) + ".Cooldown");
        OreScannerEvent oreScannerEvent = new OreScannerEvent(mp, oreScanner, cooldown);
        Bukkit.getPluginManager().callEvent(oreScannerEvent);
        if(oreScannerEvent.isCancelled()) {
          return;
        }
        Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
        mp.setReadyingAbilityBit(null);
        mp.setReadying(false);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, oreScannerEvent.getCooldown());
        mp.addAbilityOnCooldown(UnlockedAbilities.ORE_SCANNER, cal.getTimeInMillis());
        Location goldOre = null;
        Location emeraldOre = null;
        Location diamondOre = null;
        int goldOreAmount = 0;
        int emeraldOreAmount = 0;
        int diamondOreAmount = 0;
        for(int x = -1 * radius; x < radius; x++) {
          for(int z = -1 * radius; z < radius; z++) {
            for(int y = -1 * radius; y < radius; y++) {
              Block block = p.getLocation().add(x, y, z).getBlock();
              Material blockType = block.getType();
              if(blockType == Material.GOLD_ORE) {
                if(goldOre == null) {
                  goldOre = block.getLocation();
                }
                else {
                  if(p.getLocation().distance(goldOre) > p.getLocation().distance(block.getLocation())) {
                    goldOre = block.getLocation();
                  }
                }
                goldOreAmount++;
              }
              else if(blockType == Material.EMERALD_ORE) {
                if(emeraldOre == null) {
                  emeraldOre = block.getLocation();
                }
                else {
                  if(p.getLocation().distance(emeraldOre) > p.getLocation().distance(block.getLocation())) {
                    emeraldOre = block.getLocation();
                  }
                }
                emeraldOreAmount++;
              }
              else if(blockType == Material.DIAMOND_ORE) {
                if(diamondOre == null) {
                  diamondOre = block.getLocation();
                }
                else {
                  if(p.getLocation().distance(diamondOre) > p.getLocation().distance(block.getLocation())) {
                    diamondOre = block.getLocation();
                  }
                }
                diamondOreAmount++;
              }
            }
          }
        }
        if(diamondOre == null && goldOre == null && emeraldOre == null) {
          p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() +
                  McRPG.getInstance().getLangFile().getString("Messages.Abilities.OreScanner.NothingFound")));
          return;
        }
        else {
          p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() +
                  McRPG.getInstance().getLangFile().getString("Messages.Abilities.OreScanner.PointingToValuable")));
        }
        Location lookAt = null;
        if(goldOre != null) {
          lookAt = goldOre;
          p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() +
                  McRPG.getInstance().getLangFile().getString("Messages.Abilities.OreScanner.GoldFound").replace("%Amount%", Integer.toString(goldOreAmount))));
        }
        if(emeraldOre != null) {
          lookAt = emeraldOre;
          p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() +
                  McRPG.getInstance().getLangFile().getString("Messages.Abilities.OreScanner.EmeraldsFound").replace("%Amount%", Integer.toString(emeraldOreAmount))));
        }
        if(diamondOre != null) {
          lookAt = diamondOre;
          p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() +
                  McRPG.getInstance().getLangFile().getString("Messages.Abilities.OreScanner.DiamondsFound").replace("%Amount%", Integer.toString(diamondOreAmount))));
        }
        p.teleport(Methods.lookAt(p.getLocation(), lookAt));
      }
      
      //Handle Mass Harvest
      else if(abilityType == UnlockedAbilities.MASS_HARVEST && (e.getAction() == Action.LEFT_CLICK_BLOCK)) {
        if(ItemUtils.isCrop(type)) {
          FileConfiguration herbalism = McRPG.getInstance().getFileManager().getFile(FileManager.Files.HERBALISM_CONFIG);
          MassHarvest massHarvest = (MassHarvest) mp.getBaseAbility(UnlockedAbilities.MASS_HARVEST);
          int radius = herbalism.getInt("MassHarvestConfig.Tier" + Methods.convertToNumeral(massHarvest.getCurrentTier()) + ".Range");
          ItemStack breakItem = p.getItemInHand().clone();
          MassHarvestEvent massHarvestEvent = new MassHarvestEvent(mp, massHarvest, radius);
          Bukkit.getPluginManager().callEvent(massHarvestEvent);
          if(!massHarvestEvent.isCancelled()) {
            mp.getActiveAbilities().add(UnlockedAbilities.MASS_HARVEST);
            Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
            mp.setReadyingAbilityBit(null);
            mp.setReadying(false);
            e.setCancelled(true);
            int cooldown = herbalism.getInt("MassHarvestConfig.Tier" + Methods.convertToNumeral(massHarvest.getCurrentTier()) + ".Cooldown");
            for(int x = -1 * radius; x < radius; x++) {
              for(int z = -1 * radius; z < radius; z++) {
                for(int y = -1 * 2; y < 2; y++) {
                  Block test = p.getLocation().add(x, y, z).getBlock();
                  Material cropType = test.getType();
                  if(ItemUtils.isCrop(cropType)) {
                    BlockBreakEvent breakEvent = new BlockBreakEvent(test, p);
                    Bukkit.getPluginManager().callEvent(breakEvent);
                    if(!breakEvent.isCancelled()) {
                      test.breakNaturally(breakItem);
                      if(type == Material.PUMPKIN || type == Material.MELON) {
                        break;
                      }
                      test.setType(cropType);
                      Ageable ageable = (Ageable) test.getBlockData();
                      ageable.setAge(0);
                      test.setBlockData(ageable);
                    }
                  }
                }
              }
            }
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, cooldown);
            p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.MassHarvest.Activated")));
            mp.getActiveAbilities().remove(UnlockedAbilities.MASS_HARVEST);
            mp.addAbilityOnCooldown(UnlockedAbilities.MASS_HARVEST, cal.getTimeInMillis());
          }
        }
      }
      
      //Handle Pans Blessing
      else if(abilityType == UnlockedAbilities.PANS_BLESSING && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
        if(heldItem.getType() == Material.BONE_MEAL) {
          if(!ItemUtils.isCrop(type)) {
            return;
          }

          FileConfiguration herbalism = McRPG.getInstance().getFileManager().getFile(FileManager.Files.HERBALISM_CONFIG);
          PansBlessing pansBlessing = (PansBlessing) mp.getBaseAbility(UnlockedAbilities.PANS_BLESSING);
          int radius = herbalism.getInt("PansBlessingConfig.Tier" + Methods.convertToNumeral(pansBlessing.getCurrentTier()) + ".Radius");
          PansBlessingEvent pansBlessingEvent = new PansBlessingEvent(mp, pansBlessing, radius);
          Bukkit.getPluginManager().callEvent(pansBlessingEvent);
          if(!pansBlessingEvent.isCancelled()) {
            mp.setReadying(false);
            Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
            mp.setReadyingAbilityBit(null);
            mp.getActiveAbilities().add(UnlockedAbilities.PANS_BLESSING);
            int cooldown = herbalism.getInt("PansBlessingConfig.Tier" + Methods.convertToNumeral(pansBlessing.getCurrentTier()) + ".Cooldown");
            for(int x = -1 * radius; x <= radius; x++) {
              for(int z = -1 * radius; z <= radius; z++) {
                for(int y = -1; y <= 1; y++) {
                  Block test = e.getClickedBlock().getLocation().add(x, y, z).getBlock();
                  Material cropType = test.getType();
                  if(ItemUtils.isCrop(cropType)) {
                    Ageable ageable = (Ageable) test.getBlockData();
                    int originalAge = ageable.getMaximumAge();
                    ageable.setAge(ageable.getMaximumAge());
                    BlockGrowEvent growEvent = new BlockGrowEvent(test, test.getState());
                    if(!growEvent.isCancelled()) {
                      test.setBlockData(ageable);
                      test.getLocation().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, test.getLocation(), 5);
                    }
                  }
                }
              }
            }
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, cooldown);
            p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.PansBlessing.Activated")));
            mp.getActiveAbilities().remove(UnlockedAbilities.PANS_BLESSING);
            mp.addAbilityOnCooldown(UnlockedAbilities.PANS_BLESSING, cal.getTimeInMillis());
          }
        }
      }
    }
  }
}
