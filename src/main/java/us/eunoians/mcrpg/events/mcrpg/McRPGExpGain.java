package us.eunoians.mcrpg.events.mcrpg;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.sorcery.HadesDomain;
import us.eunoians.mcrpg.api.events.mcrpg.McRPGPlayerExpGainEvent;
import us.eunoians.mcrpg.api.events.mcrpg.sorcery.HadesDomainEvent;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.WorldModifierManager;
import us.eunoians.mcrpg.api.util.blood.BloodManager;
import us.eunoians.mcrpg.api.util.books.BookManager;
import us.eunoians.mcrpg.api.util.books.SkillBookFactory;
import us.eunoians.mcrpg.party.Party;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.GainReason;
import us.eunoians.mcrpg.types.PartyUpgrades;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.Parser;
import us.eunoians.mcrpg.util.worldguard.ActionLimiterParser;
import us.eunoians.mcrpg.util.worldguard.WGRegion;
import us.eunoians.mcrpg.util.worldguard.WGSupportManager;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class McRPGExpGain implements Listener{
  
  @Getter
  private static HashMap<UUID, Double> demetersShrineMultipliers = new HashMap<>();
  
  @EventHandler(priority = EventPriority.LOWEST)
  public void expGain(McRPGPlayerExpGainEvent e){
    Skills skill = e.getSkillGained().getType();
    Player p = e.getMcRPGPlayer().getPlayer();
    McRPGPlayer mp = e.getMcRPGPlayer();
    
    final int unmodifiedExp = e.getExpGained();
    int originalExp = e.getExpGained();
    double expMultiplier = 1.0;
    
    if(!skill.isEnabled()){
      e.setCancelled(true);
      return;
    }
    //Disabled Worlds
    if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
         McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(e.getMcRPGPlayer().getPlayer().getWorld().getName())) {
      return;
    }
    if(McRPG.getInstance().getConfig().getBoolean("Configuration.UseLevelPerms") && !(p.hasPermission("mcrpg.*") || p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".*")
                                                                                        || p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".level"))){
      e.setCancelled(true);
      return;
    }
    else if(e.getSkillGained().getCurrentLevel() >= McRPG.getInstance().getFileManager().getFile(FileManager.Files.getSkillFile(skill)).getInt("MaxLevel")){
      e.setCancelled(true);
      return;
    }
    else if(McRPG.getInstance().isWorldGuardEnabled() && e.getGainType() != GainReason.REDEEM && e.getGainType() != GainReason.ARTIFACT && e.getGainType() != GainReason.COMMAND && e.getGainType() != GainReason.PARTY){
      WGSupportManager wgSupportManager = McRPG.getInstance().getWgSupportManager();
      if(wgSupportManager.isWorldTracker(e.getMcRPGPlayer().getPlayer().getWorld())){
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        Location loc = e.getMcRPGPlayer().getPlayer().getLocation();
        RegionManager manager = container.get(BukkitAdapter.adapt(loc.getWorld()));
        
        HashMap<String, WGRegion> regions = wgSupportManager.getRegionManager().get(loc.getWorld());
        assert manager != null;
        ApplicableRegionSet set = manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc));
        boolean useMultiplier = false;
        double lowestMultiplier = 0;
        boolean continueSeaching = true;
        for(ProtectedRegion region : set){
          if(regions.containsKey(region.getId())){
            double multiplier = regions.get(region.getId()).getExpMultiplier();
            if(useMultiplier && multiplier < lowestMultiplier){
              lowestMultiplier = multiplier;
            }
            else{
              lowestMultiplier = multiplier;
              useMultiplier = true;
            }
            if(!continueSeaching){
              continue;
            }
            HashMap<String, List<String>> expExpression = regions.get(region.getId()).getExpGainExpressions();
            if(expExpression.containsKey("All")){
              List<String> expressions = expExpression.get("All");
              for(String s : expressions){
                ActionLimiterParser actionLimiterParser = new ActionLimiterParser(s, e.getMcRPGPlayer());
                if(actionLimiterParser.evaluateExpression(e.getSkillGained().getType())){
                  e.setCancelled(true);
                  continueSeaching = false;
                }
              }
            }
            if(expExpression.containsKey(e.getSkillGained().getType().getName())){
              List<String> expressions = expExpression.get(e.getSkillGained().getType().getName());
              for(String s : expressions){
                ActionLimiterParser actionLimiterParser = new ActionLimiterParser(s, e.getMcRPGPlayer());
                if(actionLimiterParser.evaluateExpression()){
                  e.setCancelled(true);
                  continueSeaching = false;
                }
              }
            }
          }
        }
        expMultiplier += (lowestMultiplier - 1);
      }
    }
    
    FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.CONFIG);
    if(McRPG.getInstance().getWorldModifierManager().getWorldModifiers().containsKey(p.getWorld().getName()) && e.getGainType() != GainReason.REDEEM && e.getGainType() != GainReason.ARTIFACT && e.getGainType() != GainReason.COMMAND && e.getGainType() != GainReason.PARTY){
      WorldModifierManager.ExpModifierWrapper modifierWrapper = McRPG.getInstance().getWorldModifierManager().getWorldModifiers().get(p.getWorld().getName());
      if(modifierWrapper.isModified(e.getSkillGained().getType())){
        expMultiplier += (modifierWrapper.getModifier(e.getSkillGained().getType()) - 1);
      }
    }
    
    if(e.getGainType() != GainReason.REDEEM && e.getGainType() != GainReason.ARTIFACT && e.getGainType() != GainReason.COMMAND && e.getGainType() != GainReason.PARTY && mp.getBoostedExp() > 0){
      Parser boostedExpParser = new Parser(config.getString("Configuration.BoostedExpUsageRate"));
      boostedExpParser.setVariable("gained_exp", e.getExpGained());
      int extraExp = (int) boostedExpParser.getValue();
      if(extraExp > mp.getBoostedExp()){
        extraExp = mp.getBoostedExp();
      }
      originalExp += extraExp;
      mp.setBoostedExp(mp.getBoostedExp() - extraExp);
    }
    BookManager bookManager = McRPG.getInstance().getBookManager();
    Random rand = new Random();
    int bookChance = config.getBoolean("Configuration.DisableBooksInEnd", false) && p.getLocation().getBlock().getBiome().name().contains("END") ? 100001 : rand.nextInt(100000);
    bookChance = (e.getGainType() != GainReason.REDEEM && e.getGainType() != GainReason.ARTIFACT && e.getGainType() != GainReason.COMMAND) ? 0 : bookChance;
    Location loc = e.getMcRPGPlayer().getPlayer().getLocation();
    
    if(bookManager.getEnabledUnlockEvents().contains("ExpGain")){
      if(!bookManager.getUnlockExcluded().contains(skill.getName())){
        double chance = bookManager.getDefaultUnlockChance();
        if(bookManager.getExpChances().containsKey("Unlock") && bookManager.getExpChances().get("Unlock").containsKey(skill)){
          chance = bookManager.getExpChances().get("Unlock").get(skill);
        }
        chance *= 1000;
        if(chance >= bookChance){
          loc.getWorld().dropItemNaturally(loc, SkillBookFactory.generateUnlockBook());
        }
      }
    }
    if(bookManager.getEnabledUpgradeEvents().contains("ExpGain")){
      if(!bookManager.getUpgradeExcluded().contains(skill.getName())){
        double chance = bookManager.getDefaultUpgradeChance();
        if(bookManager.getExpChances().containsKey("Upgrade") && bookManager.getExpChances().get("Upgrade").containsKey(skill)){
          chance = bookManager.getExpChances().get("Upgrade").get(skill);
        }
        chance *= 1000;
        if(chance >= bookChance){
          loc.getWorld().dropItemNaturally(loc, SkillBookFactory.generateUpgradeBook());
        }
      }
    }
    
    //Blood
    ItemStack heldItem = p.getInventory().getItemInMainHand();
    if(heldItem.getType() != Material.AIR){
      NBTItem nbtItem = new NBTItem(heldItem);
      if((e.getGainType() == GainReason.BREAK && BloodManager.BloodType.TOOL.isMaterialApplicable(heldItem.getType()))
           || (e.getGainType() == GainReason.KILL && BloodManager.BloodType.WEAPON.isMaterialApplicable(heldItem.getType()))){
        if(nbtItem.hasKey("McRPGBloodItem")){
          int multiplier = nbtItem.getInteger("ExpBoost");
          expMultiplier += (double) (multiplier/100);
    
          double chance = nbtItem.getDouble("ShatterChance");
          int val = (int) (chance * 1000);
          if(val >= rand.nextInt(100000)){
            p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            p.updateInventory();
            p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5f, 1f);
            p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Blood.ItemShatter")));
          }
        }
      }
    }
    ItemStack[] armorContents = p.getInventory().getArmorContents();
    for(int i = 0; i < armorContents.length; i++){
      ItemStack itemStack = p.getInventory().getArmorContents()[i];
      if(itemStack != null && itemStack.getType() != Material.AIR){
        NBTItem nbtItem = new NBTItem(itemStack);
        if(nbtItem.hasKey("McRPGBloodItem")){
          int multiplier = nbtItem.getInteger("ExpBoost");
          
          expMultiplier += (double) (multiplier/100);
          
          double chance = nbtItem.getDouble("ShatterChance");
          int val = (int) (chance * 1000);
          if(val >= rand.nextInt(100000)){
            armorContents[i] = new ItemStack(Material.AIR);
            p.updateInventory();
            p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5f, 1f);
            p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Blood.ItemShatter")));
          }
        }
      }
    }
    p.getInventory().setArmorContents(armorContents);
    
    //Divine Escape exp debuff
    if(e.getMcRPGPlayer().getDivineEscapeExpDebuff() > 0 && e.getGainType() != GainReason.REDEEM && e.getGainType() != GainReason.ARTIFACT && e.getGainType() != GainReason.COMMAND){
      expMultiplier -= (e.getMcRPGPlayer().getDivineEscapeExpDebuff() / 100);
    }
    if((e.getGainType() == GainReason.BREAK || e.getGainType() == GainReason.ENCHANTING || e.getGainType() == GainReason.FISHING || e.getGainType() == GainReason.BREW) && demetersShrineMultipliers.containsKey(e.getMcRPGPlayer().getUuid())){
      Skills type = e.getSkillGained().getType();
      if(type == Skills.HERBALISM || type == Skills.WOODCUTTING || type == Skills.MINING || type == Skills.EXCAVATION || type == Skills.FISHING || type == Skills.SORCERY){
        expMultiplier += (demetersShrineMultipliers.get(e.getMcRPGPlayer().getUuid()) - 1);
      }
    }
    FileConfiguration sorceryFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SORCERY_CONFIG);
    if(sorceryFile.getBoolean("SorceryEnabled") && p.getLocation().getWorld().getEnvironment() == World.Environment.NETHER
         && UnlockedAbilities.HADES_DOMAIN.isEnabled() && mp.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.HADES_DOMAIN) && mp.getBaseAbility(UnlockedAbilities.HADES_DOMAIN).isToggled() &&
         e.getGainType() != GainReason.REDEEM && e.getGainType() != GainReason.ARTIFACT && e.getGainType() != GainReason.COMMAND && e.getGainType() != GainReason.PARTY){
      HadesDomain hadesDomain = (HadesDomain) mp.getBaseAbility(UnlockedAbilities.HADES_DOMAIN);
      double multiplier = sorceryFile.getDouble("HadesDomainConfiguration.Tier" + Methods.convertToNumeral(hadesDomain.getCurrentTier()) + ".McRPGExpBoost");
      HadesDomainEvent hadesDomainEvent = new HadesDomainEvent(mp, hadesDomain, multiplier, true);
      Bukkit.getPluginManager().callEvent(hadesDomainEvent);
      if(!hadesDomainEvent.isCancelled()){
        multiplier = hadesDomainEvent.getPercentBonusMcRPGExp();
        multiplier /= 100;
        multiplier += 1;
        expMultiplier += (multiplier - 1);
        e.setExpGained((int) (e.getExpGained() * multiplier));
      }
    }
    expMultiplier += (McRPG.getInstance().getExpPermissionManager().getPermBoost(p, e.getSkillGained()) - 1);
    
    e.setExpGained((int) (originalExp * Math.min(expMultiplier, McRPG.getInstance().getConfig().getInt("Configuration.ExpMultiplierCap"))));
    
    if(e.getGainType() != GainReason.ARTIFACT && e.getGainType() != GainReason.REDEEM && e.getGainType() != GainReason.COMMAND && e.getGainType() != GainReason.PARTY){
      if(!McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getBoolean("PartiesEnabled", false)){
        return;
      }
      if(mp.getPartyID() != null){
        Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
        if(party != null){
          party.giveExp(originalExp);
          int expToGive = (int) (originalExp * (PartyUpgrades.getExpShareAmountAtTier(party.getUpgradeTier(PartyUpgrades.EXP_SHARE_AMOUNT)) / 100d));
          int expForPlayer = (int) (e.getExpGained() * (PartyUpgrades.getExpHolderPercent() / 100));
          boolean expGiven = false;
          for(Player player : party.getOnlinePlayers()){
            if(player.getUniqueId().equals(p.getUniqueId())){
              continue;
            }
            else{
              Location loc1 = p.getLocation();
              Location loc2 = player.getLocation();
              if(loc1.getWorld().equals(loc2.getWorld())){
                if(loc1.distance(loc2) <= PartyUpgrades.getExpShareRangeAtTier(party.getUpgradeTier(PartyUpgrades.EXP_SHARE_RANGE))){
                  expGiven = true;
                  try{
                    McRPGPlayer mcRPGPlayer = PlayerManager.getPlayer(player.getUniqueId());
                    mcRPGPlayer.giveExp(e.getSkillGained().getType(), expToGive, GainReason.PARTY);
                  }catch(McRPGPlayerNotFoundException ex){
                    continue;
                  }
                }
              }
            }
          }
          if(expGiven){
            e.setExpGained(expForPlayer);
          }
        }
      }
    }
  }
  
  public static void addDemetersShrineEffect(UUID uuid, double multiplier, int duration){
    demetersShrineMultipliers.put(uuid, multiplier);
    new BukkitRunnable(){
      @Override
      public void run(){
        demetersShrineMultipliers.remove(uuid);
        if(Bukkit.getOfflinePlayer(uuid).isOnline()){
          if(PlayerManager.isPlayerStored(uuid)){
            try{
              PlayerManager.getPlayer(uuid).getActiveAbilities().remove(UnlockedAbilities.DEMETERS_SHRINE);
            }catch(McRPGPlayerNotFoundException exception){
              return;
            }
          }
        }
      }
    }.runTaskLater(McRPG.getInstance(), duration * 20);
  }
}
