package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.excavation.PansShrine;
import us.eunoians.mcrpg.abilities.sorcery.CircesShrine;
import us.eunoians.mcrpg.abilities.woodcutting.DemetersShrine;
import us.eunoians.mcrpg.api.events.mcrpg.PansShrineTestEvent;
import us.eunoians.mcrpg.api.events.mcrpg.excavation.PansShrineEvent;
import us.eunoians.mcrpg.api.events.mcrpg.sorcery.CircesShrineEvent;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.events.mcrpg.McRPGExpGain;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerTossItemEvent implements Listener {

  @EventHandler
  public void tossItem(PlayerDropItemEvent e){
    if(PlayerManager.isPlayerFrozen(e.getPlayer().getUniqueId())){
      return;
    }
    Player p = e.getPlayer();
    //Disabled Worlds
    if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
         McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(e.getPlayer().getWorld().getName())) {
      return;
    }
    McRPGPlayer mp;
    try{
      mp = PlayerManager.getPlayer(p.getUniqueId());
    }
    catch(McRPGPlayerNotFoundException exception){
      return;
    }
    FileConfiguration woodcuttingConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.WOODCUTTING_CONFIG);
    FileConfiguration excavationConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.EXCAVATION_CONFIG);
    FileConfiguration sorceryConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SORCERY_CONFIG);
    if(woodcuttingConfig.getBoolean("WoodcuttingEnabled") && UnlockedAbilities.DEMETERS_SHRINE.isEnabled() && mp.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.DEMETERS_SHRINE)
    && mp.getBaseAbility(UnlockedAbilities.DEMETERS_SHRINE).isToggled()){
      DemetersShrine demetersShrine = (DemetersShrine) mp.getBaseAbility(UnlockedAbilities.DEMETERS_SHRINE);
      String key = "DemetersShrineConfig.Tier" + Methods.convertToNumeral(demetersShrine.getCurrentTier()) + ".";
      if(woodcuttingConfig.getStringList(key + "SacrificialItems").contains(e.getItemDrop().getItemStack().getType().toString())) {
        Item item = e.getItemDrop();
        new BukkitRunnable() {
          @Override
          public void run() {
            if(mp.getCooldown(UnlockedAbilities.DEMETERS_SHRINE) != -1) {
              if(item.getLocation().getBlock().getType() == Material.WATER){
                p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.DemetersShrine.StillOnCooldown")));
              }
              return;
            }
            if(item.isValid()) {
              Location loc = item.getLocation();
              if(loc.getBlock().getType() == Material.WATER) {

                if(item.getLocation().add(1, 0, 0).getBlock().getType() == Material.GOLD_BLOCK && item.getLocation().add(-1, 0, 0).getBlock().getType() == Material.GOLD_BLOCK
                        && item.getLocation().add(0, 0, 1).getBlock().getType() == Material.GOLD_BLOCK && item.getLocation().add(0, 0, -1).getBlock().getType() == Material.GOLD_BLOCK) {
                  item.getItemStack().setAmount(item.getItemStack().getAmount() - 1);
                  if(item.getItemStack().getAmount() == 0) {
                    item.remove();
                  }
                  loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 5);
                  FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
                  loc.getWorld().playSound(loc, Sound.valueOf(soundFile.getString("Sounds.Woodcutting.DemetersShrine.Sound")),
                    Float.parseFloat(soundFile.getString("Sounds.Woodcutting.DemetersShrine.Volume")), Float.parseFloat(soundFile.getString("Sounds.Woodcutting.DemetersShrine.Pitch")));
                  int duration = woodcuttingConfig.getInt(key + "Duration");
                  double multiplier = woodcuttingConfig.getDouble(key + "ExpBoost");
                  int cooldown = woodcuttingConfig.getInt(key + "Cooldown");
                  McRPGExpGain.addDemetersShrineEffect(e.getPlayer().getUniqueId(), multiplier, duration);
                  Calendar cal = Calendar.getInstance();
                  cal.add(Calendar.SECOND, cooldown);
                  p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.DemetersShrine.Activated")
                          .replace("%Multiplier%", Double.toString(multiplier)).replace("%Duration%", Integer.toString(duration / 60))));
                  mp.getActiveAbilities().add(UnlockedAbilities.DEMETERS_SHRINE);
                  mp.addAbilityOnCooldown(UnlockedAbilities.DEMETERS_SHRINE, cal.getTimeInMillis());
                }
              }
            }
          }
        }.runTaskLater(McRPG.getInstance(), 5 * 20);
      }
    }
    if(excavationConfig.getBoolean("ExcavationEnabled") && UnlockedAbilities.PANS_SHRINE.isEnabled() && mp.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.PANS_SHRINE)
            && mp.getBaseAbility(UnlockedAbilities.PANS_SHRINE).isToggled()){
      PansShrine pansShrine = (PansShrine) mp.getBaseAbility(UnlockedAbilities.PANS_SHRINE);
      String key = "PansShrineConfig.Tier" + Methods.convertToNumeral(pansShrine.getCurrentTier()) + ".";
      if(excavationConfig.getStringList(key + "SacrificialItems").contains(e.getItemDrop().getItemStack().getType().toString())) {
        Item item = e.getItemDrop();
        new BukkitRunnable(){
          @Override
          public void run() {
            if(mp.getCooldown(UnlockedAbilities.PANS_SHRINE) != -1){
              if(item.getLocation().getBlock().getType() == Material.WATER){
                p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.PansShrine.StillOnCooldown")));
              }
              return;
            }
            if(item.isValid()){
              Location loc = item.getLocation();
              if(loc.getBlock().getType() == Material.WATER) {
                if(item.getLocation().add(1, 0, 0).getBlock().getType() == Material.EMERALD_BLOCK && item.getLocation().add(-1, 0, 0).getBlock().getType() == Material.EMERALD_BLOCK
                        && item.getLocation().add(0, 0, 1).getBlock().getType() == Material.EMERALD_BLOCK && item.getLocation().add(0, 0, -1).getBlock().getType() == Material.EMERALD_BLOCK) {
                  int radius = excavationConfig.getInt(key + "Radius");
                  int Yradius = excavationConfig.getInt(key + "Yadius");
                  int cooldown = excavationConfig.getInt(key + "Cooldown");
                  Set<Material> replaceMaterials = excavationConfig.getStringList("PansShrineConfig.Tier" + Methods.convertToNumeral(pansShrine.getCurrentTier()) + ".AffectableBlocks")
                          .stream().map(Material::getMaterial).collect(Collectors.toSet());
                  PansShrineEvent pansShrineEvent = new PansShrineEvent(mp, pansShrine, item.getItemStack().getType(), replaceMaterials, excavationConfig.getStringList("PansShrineConfig.Tier" + Methods.convertToNumeral(pansShrine.getCurrentTier()) + ".ReplaceBlocks"), cooldown);
                  Bukkit.getPluginManager().callEvent(pansShrineEvent);
                  if(!pansShrineEvent.isCancelled()) {
                    Queue<Material> processedChanges = populateBlockArray((int) Math.pow(radius + radius + 1, 2) * ((Yradius * 2) + 1), pansShrineEvent.getReplaceableBlocks());
                    item.getItemStack().setAmount(item.getItemStack().getAmount() - 1);
                    if(item.getItemStack().getAmount() == 0) {
                      item.remove();
                    }
                    loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 5);
                    FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
                    loc.getWorld().playSound(loc, Sound.valueOf(soundFile.getString("Sounds.Excavation.PansShrine.Sound")),
                      Float.parseFloat(soundFile.getString("Sounds.Excavation.PansShrine.Volume")), Float.parseFloat(soundFile.getString("Sounds.Excavation.PansShrine.Pitch")));
                    Block b = item.getLocation().getBlock();
                    for(int y = Yradius * -1; y <= Yradius; y++) {
                      for(int x = radius * -1; x <= radius; x++) {
                        for(int z = radius * -1; z <= radius; z++) {
                          Block newBlock = b.getLocation().add(x, y - 1, z).getBlock();
                          if(replaceMaterials.contains(newBlock.getType())) {
                            if(!processedChanges.isEmpty()) {
                              PansShrineTestEvent pansShrineTestEvent = new PansShrineTestEvent(mp.getPlayer(), newBlock);
                              Bukkit.getPluginManager().callEvent(pansShrineEvent);
                              if(!pansShrineTestEvent.isCancelled()) {
                                Material mat = processedChanges.poll();
                                if(mat == Material.AIR || mat == null) {
                                  continue;
                                }
                                else {
                                  newBlock.setType(mat);
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.SECOND, cooldown);
                    mp.addAbilityOnCooldown(UnlockedAbilities.PANS_SHRINE, cal.getTimeInMillis());
                  }
                }
              }
            }
          }
        }.runTaskLater(McRPG.getInstance(), 5 * 20);
      }
    }
    if(sorceryConfig.getBoolean("SorceryEnabled") && UnlockedAbilities.CIRCES_SHRINE.isEnabled() && mp.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.CIRCES_SHRINE)
         && mp.getBaseAbility(UnlockedAbilities.CIRCES_SHRINE).isToggled()){
      CircesShrine circesShrine = (CircesShrine) mp.getBaseAbility(UnlockedAbilities.CIRCES_SHRINE);
      String key = "CircesShrineConfig.Tier" + Methods.convertToNumeral(circesShrine.getCurrentTier()) + ".";
      if(e.getItemDrop().getItemStack().getType() == Material.getMaterial(sorceryConfig.getString(key + "SacrificialItem"))
           && e.getItemDrop().getItemStack().getAmount() >= sorceryConfig.getInt(key + "MinimumAmountToSacrifice")) {
        Item item = e.getItemDrop();
        new BukkitRunnable() {
          @Override
          public void run() {
            if(mp.getCooldown(UnlockedAbilities.CIRCES_SHRINE) != -1) {
              if(item.getLocation().getBlock().getType() == Material.WATER){
                p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.CircesShrine.StillOnCooldown")));
              }
              return;
            }
            if(item.isValid()) {
              Location loc = item.getLocation();
              if(loc.getBlock().getType() == Material.WATER) {
                if(item.getLocation().add(1, 0, 0).getBlock().getType() == Material.NETHER_QUARTZ_ORE && item.getLocation().add(-1, 0, 0).getBlock().getType() == Material.NETHER_QUARTZ_ORE
                     && item.getLocation().add(0, 0, 1).getBlock().getType() == Material.NETHER_QUARTZ_ORE && item.getLocation().add(0, 0, -1).getBlock().getType() == Material.NETHER_QUARTZ_ORE) {
                  item.getItemStack().setAmount(item.getItemStack().getAmount() - 1);
                  if(item.getItemStack().getAmount() == 0) {
                    item.remove();
                  }
                  loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 5);
                  FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
                  loc.getWorld().playSound(loc, Sound.valueOf(soundFile.getString("Sounds.Brewing.CircesShrine.Sound")),
                    Float.parseFloat(soundFile.getString("Sounds.Brewing.CircesShrine.Volume")), Float.parseFloat(soundFile.getString("Sounds.Brewing.CircesShrine.Pitch")));
                  int minAmount = sorceryConfig.getInt(key + "MinimumAmountToSacrifice");
                  double percentAdded = sorceryConfig.getDouble(key + "AdditionalPercentagePerItem");
                  List<Integer> successRange = Arrays.stream(sorceryConfig.getString(key + "SuccessRateRange").split("-")).map(Integer::parseInt).collect(Collectors.toList());
                  int lowEndSuccessChance = successRange.get(0);
                  int highEndSuccessChance = successRange.size() > 1 ? successRange.get(1) : lowEndSuccessChance;
                  List<Integer> conversionRange = Arrays.stream(sorceryConfig.getString(key + "ConversionRateRange").split("-")).map(Integer::parseInt).collect(Collectors.toList());
                  int lowEndConversionChance = conversionRange.get(0);
                  int highEndConversionChance = conversionRange.size() > 1 ? conversionRange.get(1) : lowEndConversionChance;
                  boolean consumeItemOnFail = sorceryConfig.getBoolean(key + "ConsumeItemOnFail");
                  boolean consumeLevelsOnFail = sorceryConfig.getBoolean(key + "ConsumeLevelsOnFail");
                  double percentLevelsToConsume = sorceryConfig.getDouble(key + "LevelConsumePercent");
                  int cooldown = sorceryConfig.getInt(key + "Cooldown");
                  int exp = Methods.getPlayerExp(p);
                  CircesShrineEvent circesShrineEvent = new CircesShrineEvent(mp, circesShrine, minAmount, percentAdded, lowEndSuccessChance,
                    highEndSuccessChance, lowEndConversionChance, highEndConversionChance, consumeItemOnFail, consumeLevelsOnFail, percentLevelsToConsume, cooldown);
                  Bukkit.getPluginManager().callEvent(circesShrineEvent);
                  if(!circesShrineEvent.isCancelled()){
                    Random rand = new Random();
                    int chance = circesShrineEvent.getLowEndSuccessRate();
                    int extraAmount = e.getItemDrop().getItemStack().getAmount() - circesShrineEvent.getMinAmount();
                    int newItemAmount = 0;
                    if(extraAmount > 0){
                      int maxAmountExtra = (int) Math.ceil((circesShrineEvent.getHighEndSuccessRate() - circesShrineEvent.getLowEndSuccessRate())/circesShrineEvent.getPercentAddedPerExtra());
                      newItemAmount = e.getItemDrop().getItemStack().getAmount() - circesShrineEvent.getMinAmount();
                      if(newItemAmount > maxAmountExtra){
                        newItemAmount = newItemAmount - maxAmountExtra;
                        extraAmount = maxAmountExtra;
                      }
                      else{
                        extraAmount = newItemAmount;
                        newItemAmount = 0;
                      }
                    }
                    else{
                      extraAmount = 0;
                    }
                    chance += circesShrineEvent.getPercentAddedPerExtra() * extraAmount;
                    if(chance >= rand.nextInt(100)){
                      double conversionRate = circesShrineEvent.getLowEndConversionRate() + rand.nextInt(circesShrineEvent.getHighEndConversionRate() - circesShrineEvent.getLowEndConversionRate());
                      conversionRate /= 100;
                      exp = (int) (exp * conversionRate);
                      p.setLevel(0);
                      p.setExp(0);
                      mp.giveRedeemableExp(exp);
                      p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.CircesShrine.Activated")));
                    }
                    else{
                      p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.CircesShrine.Failed")));
                      if(!circesShrineEvent.isConsumeItemsOnFail()){
                        newItemAmount = e.getItemDrop().getItemStack().getAmount();
                      }
                      if(circesShrineEvent.isConsumeLevelsOnFail()){
                        exp *= (circesShrineEvent.getPercentLevelsToConsume() / 100);
                        p.setLevel(0);
                        p.setExp(0);
                        p.giveExp(exp);
                      }
                    }
                    e.getItemDrop().getItemStack().setAmount(newItemAmount);
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.SECOND, circesShrineEvent.getCooldown());
                    mp.addAbilityOnCooldown(UnlockedAbilities.CIRCES_SHRINE, cal.getTimeInMillis());
                  }
                }
              }
            }
          }
        }.runTaskLater(McRPG.getInstance(), 5 * 20);
      }
    }
  }

  private Queue<Material> populateBlockArray(int size, List<String> blocks){
    HashMap<Material, Integer> blocksToReplace = new HashMap<>();
    Queue<Material> returnBlocks = new LinkedList<>();
    for(String s : blocks){
      String[] data = s.split(":");
      blocksToReplace.put(Material.getMaterial(data[0]), Integer.parseInt(data[1]));
    }
    Random rand = new Random();
    List<Material> itemsForIter = new ArrayList<>(blocksToReplace.size());
    b: for(int i = 0; i < size; i++){
      int currentChecks = 0;
      for(;;){
        //Limits checks for 5 iterations per block in order to account for very small percentages that can be configured and stopping a possible endless loop
        if(currentChecks >= 5){
          returnBlocks.add(Material.AIR);
          continue b;
        }
        int check = rand.nextInt(100);
        for(Material mat : blocksToReplace.keySet()){
          if(blocksToReplace.get(mat) >= check){
            itemsForIter.add(mat);
          }
        }
        currentChecks++;
        if(!itemsForIter.isEmpty()){
          returnBlocks.add(itemsForIter.get(rand.nextInt(itemsForIter.size())));
          itemsForIter.clear();
          continue b;
        }
      }
    }
    return returnBlocks;
  }
}
