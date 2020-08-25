package us.eunoians.mcrpg.events.vanilla;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.axes.AresBlessing;
import us.eunoians.mcrpg.abilities.axes.CripplingBlow;
import us.eunoians.mcrpg.abilities.axes.HeavyStrike;
import us.eunoians.mcrpg.abilities.axes.SharperAxe;
import us.eunoians.mcrpg.abilities.axes.Shred;
import us.eunoians.mcrpg.abilities.axes.WhirlwindStrike;
import us.eunoians.mcrpg.abilities.fitness.BulletProof;
import us.eunoians.mcrpg.abilities.fitness.Dodge;
import us.eunoians.mcrpg.abilities.fitness.IronMuscles;
import us.eunoians.mcrpg.abilities.fitness.Roll;
import us.eunoians.mcrpg.abilities.fitness.ThickSkin;
import us.eunoians.mcrpg.abilities.swords.Bleed;
import us.eunoians.mcrpg.abilities.swords.SerratedStrikes;
import us.eunoians.mcrpg.abilities.swords.TaintedBlade;
import us.eunoians.mcrpg.abilities.taming.Comradery;
import us.eunoians.mcrpg.abilities.taming.DivineFur;
import us.eunoians.mcrpg.abilities.taming.FuryOfCerberus;
import us.eunoians.mcrpg.abilities.taming.Gore;
import us.eunoians.mcrpg.abilities.taming.LinkedFangs;
import us.eunoians.mcrpg.abilities.taming.SharpenedFangs;
import us.eunoians.mcrpg.abilities.unarmed.Berserk;
import us.eunoians.mcrpg.abilities.unarmed.DenseImpact;
import us.eunoians.mcrpg.abilities.unarmed.Disarm;
import us.eunoians.mcrpg.abilities.unarmed.IronArm;
import us.eunoians.mcrpg.abilities.unarmed.SmitingFist;
import us.eunoians.mcrpg.api.events.mcrpg.axes.AresBlessingEvent;
import us.eunoians.mcrpg.api.events.mcrpg.axes.CripplingBlowEvent;
import us.eunoians.mcrpg.api.events.mcrpg.axes.HeavyStrikeEvent;
import us.eunoians.mcrpg.api.events.mcrpg.axes.SharperAxeEvent;
import us.eunoians.mcrpg.api.events.mcrpg.axes.ShredEvent;
import us.eunoians.mcrpg.api.events.mcrpg.axes.WhirlwindStrikeEvent;
import us.eunoians.mcrpg.api.events.mcrpg.fitness.BulletProofEvent;
import us.eunoians.mcrpg.api.events.mcrpg.fitness.DodgeEvent;
import us.eunoians.mcrpg.api.events.mcrpg.fitness.IronMusclesEvent;
import us.eunoians.mcrpg.api.events.mcrpg.fitness.RollEvent;
import us.eunoians.mcrpg.api.events.mcrpg.fitness.ThickSkinEvent;
import us.eunoians.mcrpg.api.events.mcrpg.swords.BleedEvent;
import us.eunoians.mcrpg.api.events.mcrpg.swords.SerratedStrikesEvent;
import us.eunoians.mcrpg.api.events.mcrpg.swords.TaintedBladeEvent;
import us.eunoians.mcrpg.api.events.mcrpg.taming.ComraderyEvent;
import us.eunoians.mcrpg.api.events.mcrpg.taming.DivineFurEvent;
import us.eunoians.mcrpg.api.events.mcrpg.taming.FuryOfCerberusEvent;
import us.eunoians.mcrpg.api.events.mcrpg.taming.GoreEvent;
import us.eunoians.mcrpg.api.events.mcrpg.taming.LinkedFangsEvent;
import us.eunoians.mcrpg.api.events.mcrpg.taming.SharpenedFangsEvent;
import us.eunoians.mcrpg.api.events.mcrpg.unarmed.BerserkEvent;
import us.eunoians.mcrpg.api.events.mcrpg.unarmed.DenseImpactEvent;
import us.eunoians.mcrpg.api.events.mcrpg.unarmed.DisarmEvent;
import us.eunoians.mcrpg.api.events.mcrpg.unarmed.IronArmEvent;
import us.eunoians.mcrpg.api.events.mcrpg.unarmed.SmitingFistEvent;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.blood.BloodFactory;
import us.eunoians.mcrpg.api.util.blood.BloodManager;
import us.eunoians.mcrpg.events.mcrpg.BleedHandler;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.players.PlayerReadyBit;
import us.eunoians.mcrpg.skills.Axes;
import us.eunoians.mcrpg.skills.Skill;
import us.eunoians.mcrpg.skills.Taming;
import us.eunoians.mcrpg.types.DefaultAbilities;
import us.eunoians.mcrpg.types.GainReason;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.Parser;
import us.eunoians.mcrpg.util.mcmmo.MobHealthbarUtils;
import us.eunoians.mcrpg.util.worldguard.ActionLimiterParser;
import us.eunoians.mcrpg.util.worldguard.WGRegion;
import us.eunoians.mcrpg.util.worldguard.WGSupportManager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class VanillaDamageEvent implements Listener{
  
  private static final String FALL_DAMAGE_DIVINE_FUR_KEY = "Fall_Damage";
  private static final String FIRE_DAMAGE_DIVINE_FUR_KEY = "Fire_Damage";
  private static final String MAGIC_DAMAGE_DIVINE_FUR_KEY = "Magic_Damage";
  private static final String ALL_DAMAGE_DIVINE_FUR_KEY = "All_Damage";
  
  public static final NamespacedKey HELL_HOUND_KEY = new NamespacedKey(McRPG.getInstance(), "is-hell-hound");
  public static final NamespacedKey HELL_HOUND_IGNITE_KEY = new NamespacedKey(McRPG.getInstance(), "hell-hound-ignite");
  public static final NamespacedKey HELL_HOUND_DESTROY_KEY = new NamespacedKey(McRPG.getInstance(), "hell-hound-destroy");
  public static final NamespacedKey HELL_HOUND_SELF_DESTRUCT_KEY = new NamespacedKey(McRPG.getInstance(), "self-destruct-time");
  
  private static Map<UUID, BukkitTask> hellHoundSelfDestructMap = new HashMap<>();
  
  public static void handleHealthbars(Entity attacker, LivingEntity target, double damage){
    if(!(attacker instanceof Player) || target instanceof ArmorStand){
      return;
    }
    
    Player player = (Player) attacker;
    
    if(isNPCEntity(player) || isNPCEntity(target)){
      return;
    }
    
    MobHealthbarUtils.handleMobHealthbars(player, target, damage);
  }
  
  /**
   * This code is not mine. It is copyright from the original mcMMO allowed for use by their license.
   * This code has been modified from it source material
   * It was released under the GPLv3 license
   */
  /**
   * Checks to see if an entity is currently invincible.
   *
   * @param entity      The {@link LivingEntity} to check
   * @param eventDamage The damage from the event the entity is involved in
   * @return true if the entity is invincible, false otherwise
   */
  public static boolean isInvincible(LivingEntity entity, double eventDamage){
    /*
     * So apparently if you do more damage to a LivingEntity than its last damage int you bypass the invincibility.
     * So yeah, this is for that.
     */
    return (entity.getNoDamageTicks() > entity.getMaximumNoDamageTicks() / 2.0F) && (eventDamage <= entity.getLastDamage());
  }
  //End mcMMO code
  
  private static boolean isNPCEntity(Entity entity){
    return (entity == null || entity.hasMetadata("NPC") || entity instanceof NPC || entity.getClass().getName().equalsIgnoreCase("cofh.entity.PlayerFake"));
  }
  
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void genericDamageListener(EntityDamageEvent e){
    //Disabled Worlds
    if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
         McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(e.getEntity().getWorld().getName())){
      return;
    }
    
    if(e.getEntity() instanceof Wolf){
      Wolf wolf = (Wolf) e.getEntity();
      
      if(wolf.getOwner() != null && wolf.isValid()){
        
        if(wolf.getPersistentDataContainer().has(HELL_HOUND_KEY, PersistentDataType.STRING)){
          if(wolf.getHealth() > 0 && wolf.getHealth() - e.getDamage() <= 0){
            e.setCancelled(true);
            new BukkitRunnable(){
              @Override
              public void run(){
                wolf.getWorld().createExplosion(wolf.getLocation(), 2, false, wolf.getPersistentDataContainer().get(HELL_HOUND_DESTROY_KEY, PersistentDataType.STRING).equalsIgnoreCase("true"));
              }
            }.runTaskLater(McRPG.getInstance(), 1);
            wolf.setHealth(0);
            hellHoundSelfDestructMap.remove(wolf.getUniqueId()).cancel();
          }
        }
        
        McRPGPlayer mcRPGPlayer;
        try{
          mcRPGPlayer = PlayerManager.getPlayer(wolf.getOwner().getUniqueId());
        }catch(McRPGPlayerNotFoundException exception){
          return;
        }
        
        if(UnlockedAbilities.DIVINE_FUR.isEnabled() && mcRPGPlayer.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.DIVINE_FUR)
             && mcRPGPlayer.getBaseAbility(UnlockedAbilities.DIVINE_FUR).isToggled()){
          
          FileConfiguration tamingConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.TAMING_CONFIG);
          DivineFur divineFur = (DivineFur) mcRPGPlayer.getBaseAbility(UnlockedAbilities.DIVINE_FUR);
          String tier = Methods.convertToNumeral(divineFur.getCurrentTier());
          
          List<String> damagePreventions = tamingConfig.getStringList("DivineFurConfig.Tier" + tier + ".Protections");
          Map<String, Double> preventionAmounts = new HashMap<>();
          
          for(String prevention : damagePreventions){
            String[] data = prevention.split(":");
            preventionAmounts.put(data[0], Double.parseDouble(data[1]));
          }
          
          if(e.getCause() == EntityDamageEvent.DamageCause.FALL){
            if(preventionAmounts.containsKey(FALL_DAMAGE_DIVINE_FUR_KEY)){
              DivineFurEvent divineFurEvent = new DivineFurEvent(mcRPGPlayer, divineFur, e.getCause(), preventionAmounts.get(FALL_DAMAGE_DIVINE_FUR_KEY));
              Bukkit.getPluginManager().callEvent(divineFurEvent);
              if(!divineFurEvent.isCancelled()){
                if(divineFurEvent.getPercentProtected() >= 100.0d){
                  e.setCancelled(true);
                }
                else{
                  e.setDamage(e.getDamage() - (e.getDamage() * divineFurEvent.getPercentProtected()));
                }
              }
            }
            else if(preventionAmounts.containsKey(ALL_DAMAGE_DIVINE_FUR_KEY)){
              DivineFurEvent divineFurEvent = new DivineFurEvent(mcRPGPlayer, divineFur, e.getCause(), preventionAmounts.get(ALL_DAMAGE_DIVINE_FUR_KEY));
              Bukkit.getPluginManager().callEvent(divineFurEvent);
              if(!divineFurEvent.isCancelled()){
                if(divineFurEvent.getPercentProtected() >= 100.0d){
                  e.setCancelled(true);
                }
                else{
                  e.setDamage(e.getDamage() - (e.getDamage() * divineFurEvent.getPercentProtected()));
                }
              }
            }
          }
          else if(e.getCause() == EntityDamageEvent.DamageCause.FIRE || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK){
            if(preventionAmounts.containsKey(FIRE_DAMAGE_DIVINE_FUR_KEY)){
              DivineFurEvent divineFurEvent = new DivineFurEvent(mcRPGPlayer, divineFur, e.getCause(), preventionAmounts.get(FIRE_DAMAGE_DIVINE_FUR_KEY));
              Bukkit.getPluginManager().callEvent(divineFurEvent);
              if(!divineFurEvent.isCancelled()){
                if(divineFurEvent.getPercentProtected() >= 100.0d){
                  e.setCancelled(true);
                }
                else{
                  e.setDamage(e.getDamage() - (e.getDamage() * divineFurEvent.getPercentProtected()));
                }
              }
            }
            else if(preventionAmounts.containsKey(ALL_DAMAGE_DIVINE_FUR_KEY)){
              DivineFurEvent divineFurEvent = new DivineFurEvent(mcRPGPlayer, divineFur, e.getCause(), preventionAmounts.get(ALL_DAMAGE_DIVINE_FUR_KEY));
              Bukkit.getPluginManager().callEvent(divineFurEvent);
              if(!divineFurEvent.isCancelled()){
                if(divineFurEvent.getPercentProtected() >= 100.0d){
                  e.setCancelled(true);
                }
                else{
                  e.setDamage(e.getDamage() - (e.getDamage() * divineFurEvent.getPercentProtected()));
                }
              }
            }
          }
          else if(e.getCause() == EntityDamageEvent.DamageCause.MAGIC || e.getCause() == EntityDamageEvent.DamageCause.DRAGON_BREATH){
            if(preventionAmounts.containsKey(MAGIC_DAMAGE_DIVINE_FUR_KEY)){
              DivineFurEvent divineFurEvent = new DivineFurEvent(mcRPGPlayer, divineFur, e.getCause(), preventionAmounts.get(MAGIC_DAMAGE_DIVINE_FUR_KEY));
              Bukkit.getPluginManager().callEvent(divineFurEvent);
              if(!divineFurEvent.isCancelled()){
                if(divineFurEvent.getPercentProtected() >= 100.0d){
                  e.setCancelled(true);
                }
                else{
                  e.setDamage(e.getDamage() - (e.getDamage() * divineFurEvent.getPercentProtected()));
                }
              }
            }
            else if(preventionAmounts.containsKey(ALL_DAMAGE_DIVINE_FUR_KEY)){
              DivineFurEvent divineFurEvent = new DivineFurEvent(mcRPGPlayer, divineFur, e.getCause(), preventionAmounts.get(ALL_DAMAGE_DIVINE_FUR_KEY));
              Bukkit.getPluginManager().callEvent(divineFurEvent);
              if(!divineFurEvent.isCancelled()){
                if(divineFurEvent.getPercentProtected() >= 100.0d){
                  e.setCancelled(true);
                }
                else{
                  e.setDamage(e.getDamage() - (e.getDamage() * divineFurEvent.getPercentProtected()));
                }
              }
            }
          }
          else{
            if(preventionAmounts.containsKey(ALL_DAMAGE_DIVINE_FUR_KEY)){
              DivineFurEvent divineFurEvent = new DivineFurEvent(mcRPGPlayer, divineFur, e.getCause(), preventionAmounts.get(ALL_DAMAGE_DIVINE_FUR_KEY));
              Bukkit.getPluginManager().callEvent(divineFurEvent);
              if(!divineFurEvent.isCancelled()){
                if(divineFurEvent.getPercentProtected() >= 100.0d){
                  e.setCancelled(true);
                }
                else{
                  e.setDamage(e.getDamage() - (e.getDamage() * divineFurEvent.getPercentProtected()));
                }
              }
            }
          }
        }
      }
    }
    
    FileConfiguration fitnessConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.FITNESS_CONFIG);
    if(!(e instanceof EntityDamageByEntityEvent)){
      if(e.isCancelled() || !Skills.FITNESS.isEnabled() || e.getEntity().isInsideVehicle()){
        return;
      }
      else{
        if(e.getEntity() instanceof Player){
          Player player = (Player) e.getEntity();
          McRPGPlayer mcRPGPlayer;
          try{
            mcRPGPlayer = PlayerManager.getPlayer(player.getUniqueId());
          }catch(McRPGPlayerNotFoundException exception){
            return;
          }
          if(McRPG.getInstance().isWorldGuardEnabled()){
            WGSupportManager wgSupportManager = McRPG.getInstance().getWgSupportManager();
            
            if(wgSupportManager.isWorldTracker(player.getWorld())){
              RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
              Location loc = player.getLocation();
              RegionManager manager = container.get(BukkitAdapter.adapt(loc.getWorld()));
              HashMap<String, WGRegion> regions = wgSupportManager.getRegionManager().get(loc.getWorld());
              assert manager != null;
              ApplicableRegionSet set = manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc));
              for(ProtectedRegion region : set){
                if(regions.containsKey(region.getId()) && regions.get(region.getId()).getAttackExpressions().containsKey(e.getEntity().getType())){
                  List<String> expressions = regions.get(region.getId()).getAttackExpressions().get(e.getEntity().getType());
                  for(String s : expressions){
                    if(s.contains("difference")){
                      if(!(e.getEntity() instanceof Player)){
                        continue;
                      }
                      else{
                        try{
                          McRPGPlayer target = PlayerManager.getPlayer(e.getEntity().getUniqueId());
                          ActionLimiterParser actionLimiterParser = new ActionLimiterParser(s, mcRPGPlayer, target);
                          if(actionLimiterParser.evaluateExpression()){
                            e.setCancelled(true);
                            return;
                          }
                        }catch(McRPGPlayerNotFoundException exception){
                        }
                      }
                    }
                    else{
                      ActionLimiterParser actionLimiterParser = new ActionLimiterParser(s, mcRPGPlayer);
                      if(actionLimiterParser.evaluateExpression()){
                        e.setCancelled(true);
                        return;
                      }
                    }
                  }
                }
              }
            }
          }
          
          int featherFallingLevel = player.getEquipment().getBoots() != null
                                      && player.getEquipment().getBoots().containsEnchantment(Enchantment.PROTECTION_FALL) ? player.getEquipment().getBoots().getEnchantmentLevel(Enchantment.PROTECTION_FALL) : 1;
          int expAwarded;
          boolean afk = false;
          if(e.getCause() == EntityDamageEvent.DamageCause.FALL){
            if(mcRPGPlayer.getLastFallLocation().size() < 4){
              mcRPGPlayer.getLastFallLocation().add(player.getLocation());
            }
            else{
              Location currentLocation = player.getLocation();
              for(Location oldLoc : mcRPGPlayer.getLastFallLocation()){
                if(afk){
                  break;
                }
                int diffInX = Math.abs(oldLoc.getBlockX() - currentLocation.getBlockX());
                int diffInY = Math.abs(oldLoc.getBlockY() - currentLocation.getBlockY());
                int diffInZ = Math.abs(oldLoc.getBlockZ() - currentLocation.getBlockZ());
                int numOfDiffAxis = diffInY <= fitnessConfig.getInt("AntiAFK.YRange") ? 1 : 0;
                numOfDiffAxis += diffInX <= fitnessConfig.getInt("AntiAfk.XRange") ? 1 : 0;
                numOfDiffAxis += diffInZ <= fitnessConfig.getInt("AntiAfk.ZRange") ? 1 : 0;
                afk = numOfDiffAxis >= fitnessConfig.getInt("AntiAFK.AmountOfDifferences", 1);
              }
              if(mcRPGPlayer.getLastFallLocation().size() >= 4){
                while(mcRPGPlayer.getLastFallLocation().size() >= 4){
                  mcRPGPlayer.getLastFallLocation().remove(0);
                }
                mcRPGPlayer.getLastFallLocation().add(currentLocation);
              }
            }
            if(!afk && player.getHealth() - e.getDamage() > 0){
              expAwarded = fitnessConfig.getInt("ExpAwardedPerDamage.FALL_DAMAGE");
              Parser equation = new Parser(fitnessConfig.getString("FallEquation"));
              equation.setVariable("damage", e.getDamage());
              equation.setVariable("exp_awarded", expAwarded);
              equation.setVariable("feather_falling_level", featherFallingLevel);
              expAwarded = (int) equation.getValue();
              mcRPGPlayer.giveExp(Skills.FITNESS, expAwarded, GainReason.DAMAGE);
            }
            else{
              expAwarded = 0;
            }
            Roll roll = (Roll) mcRPGPlayer.getBaseAbility(DefaultAbilities.ROLL);
            if(roll.getGenericAbility().isEnabled() && roll.isToggled()){
              Parser rollEquation = new Parser(fitnessConfig.getString("RollConfig.RollChanceEquation"));
              rollEquation.setVariable("fitness_level", mcRPGPlayer.getSkill(Skills.FITNESS).getCurrentLevel());
              int chance = (int) (rollEquation.getValue() * 1000);
              Random rand = new Random();
              int val = rand.nextInt(100000);
              if(chance >= val){
                RollEvent rollEvent = new RollEvent(mcRPGPlayer, roll);
                Bukkit.getPluginManager().callEvent(rollEvent);
                if(!rollEvent.isCancelled()){
                  e.setDamage(e.getDamage() / 2);
                  player.sendMessage(Methods.color(player, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.Roll.Activated")));
                }
              }
            }
          }
        }
      }
    }
  }
  
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void handlePlayerDamageAbilities(EntityDamageByEntityEvent e){
    //Disabled Worlds
    if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
         McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(e.getEntity().getWorld().getName())){
      return;
    }
    
    if(e.getEntity() instanceof Player){
      McRPGPlayer mcRPGPlayer;
      try{
        mcRPGPlayer = PlayerManager.getPlayer(e.getEntity().getUniqueId());
      }catch(McRPGPlayerNotFoundException exception){
        return;
      }
      if(e.getDamager() instanceof Player && e.getEntity() instanceof Player){
        if(!Methods.canPlayersPVP((Player) e.getEntity(), (Player) e.getDamager())){
          return;
        }
      }
      
      //Deal with Divine Escape debuff
      if(mcRPGPlayer.getDivineEscapeDamageDebuff() > 0){
        double debuff = mcRPGPlayer.getDivineEscapeDamageDebuff() / 100 + 1;
        e.setDamage(e.getDamage() * debuff);
      }
      
      //Handle comradery
      if(UnlockedAbilities.COMRADERY.isEnabled() && mcRPGPlayer.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.COMRADERY) && mcRPGPlayer.getBaseAbility(UnlockedAbilities.COMRADERY).isToggled()){
        
        FileConfiguration tamingConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.TAMING_CONFIG);
        Comradery comradery = (Comradery) mcRPGPlayer.getBaseAbility(UnlockedAbilities.COMRADERY);
        
        String tier = Methods.convertToNumeral(comradery.getCurrentTier());
        double activationChance = tamingConfig.getDouble("ComraderyConfig.Tier" + tier + ".ActivationChance") * 1000;
        int wolfRange = tamingConfig.getInt("ComraderyConfig.Tier" + tier + ".WolfRange");
        
        Random random = new Random();
        int val = random.nextInt(100000);
        
        if(activationChance >= val){
          for(Entity entity : mcRPGPlayer.getPlayer().getNearbyEntities(wolfRange, 1, wolfRange)){
            if(entity instanceof Wolf){
              Wolf wolf = (Wolf) entity;
              if(wolf.getOwner() != null && wolf.getOwner().getUniqueId() == mcRPGPlayer.getUuid()){
                ComraderyEvent comraderyEvent = new ComraderyEvent(mcRPGPlayer, comradery, wolf);
                if(!comraderyEvent.isCancelled()){
                  e.setCancelled(true);
                  wolf.damage(e.getDamage());
                  return;
                }
              }
            }
          }
        }
      }
      
      //Handle fitness abilities
      if(Skills.FITNESS.isEnabled()){
        FileConfiguration fitnessConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.FITNESS_CONFIG);
        if(e.getDamager() instanceof LivingEntity){
          LivingEntity attacker = (LivingEntity) e.getDamager();
          Material weaponType = attacker.getEquipment().getItemInMainHand().getType();
          if(weaponType.toString().contains("SWORD") || weaponType.toString().contains("AXE") || weaponType.toString().contains("TRIDENT")){
            if(UnlockedAbilities.THICK_SKIN.isEnabled() && mcRPGPlayer.getAbilityLoadout().contains(UnlockedAbilities.THICK_SKIN)
                 && mcRPGPlayer.getBaseAbility(UnlockedAbilities.THICK_SKIN).isToggled()){
              ThickSkin thickSkin = (ThickSkin) mcRPGPlayer.getBaseAbility(UnlockedAbilities.THICK_SKIN);
              double damageDecrease = fitnessConfig.getDouble("ThickSkinConfig.Tier" + Methods.convertToNumeral(thickSkin.getCurrentTier())
                                                                + ".DamageDecrease");
              ThickSkinEvent thickSkinEvent = new ThickSkinEvent(mcRPGPlayer, thickSkin, damageDecrease, (LivingEntity) e.getDamager());
              Bukkit.getPluginManager().callEvent(thickSkinEvent);
              if(!thickSkinEvent.isCancelled()){
                e.setDamage(e.getDamage() * ((100 - damageDecrease) / 100));
              }
            }
            if(attacker instanceof Player && UnlockedAbilities.IRON_MUSCLES.isEnabled() && mcRPGPlayer.getAbilityLoadout().contains(UnlockedAbilities.IRON_MUSCLES)
                 && mcRPGPlayer.getBaseAbility(UnlockedAbilities.IRON_MUSCLES).isToggled()){
              IronMuscles ironMuscles = (IronMuscles) mcRPGPlayer.getBaseAbility(UnlockedAbilities.IRON_MUSCLES);
              double activationChance = fitnessConfig.getDouble("IronMusclesConfig.Tier" + Methods.convertToNumeral(ironMuscles.getCurrentTier())
                                                                  + ".ActivationChance");
              int chance = (int) (activationChance * 1000);
              Random rand = new Random();
              int val = rand.nextInt(100000);
              if(chance >= val){
                int weaponDamage = fitnessConfig.getInt("IronMusclesConfig.Tier" + Methods.convertToNumeral(ironMuscles.getCurrentTier()) +
                                                          ".WeaponDamage");
                IronMusclesEvent ironMusclesEvent = new IronMusclesEvent(mcRPGPlayer, ironMuscles, weaponDamage, (Player) attacker);
                Bukkit.getPluginManager().callEvent(ironMusclesEvent);
                if(!ironMusclesEvent.isCancelled()){
                  attacker.getEquipment().getItemInMainHand().setDurability((short) (attacker.getEquipment().getItemInMainHand().getDurability() + ironMusclesEvent.getDurabilityLoss()));
                }
              }
            }
          }
          if(UnlockedAbilities.DODGE.isEnabled() && mcRPGPlayer.getAbilityLoadout().contains(UnlockedAbilities.DODGE) &&
               mcRPGPlayer.getBaseAbility(UnlockedAbilities.DODGE).isToggled()){
            Dodge dodge = (Dodge) mcRPGPlayer.getBaseAbility(UnlockedAbilities.DODGE);
            double activationChance = fitnessConfig.getDouble("DodgeConfig.Tier" + Methods.convertToNumeral(dodge.getCurrentTier())
                                                                + ".ActivationChance");
            int chance = (int) (activationChance * 1000);
            Random rand = new Random();
            int val = rand.nextInt(100000);
            if(chance >= val){
              double damageReduction = fitnessConfig.getDouble("DodgeConfig.Tier" + Methods.convertToNumeral(dodge.getCurrentTier()) +
                                                                 ".DamageReduction");
              DodgeEvent dodgeEvent = new DodgeEvent(mcRPGPlayer, dodge, attacker, damageReduction);
              Bukkit.getPluginManager().callEvent(dodgeEvent);
              if(!dodgeEvent.isCancelled()){
                e.setDamage(e.getDamage() * ((100 - damageReduction) / 100));
                mcRPGPlayer.getPlayer().sendMessage(Methods.color(mcRPGPlayer.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.Dodge.Activated")));
              }
            }
          }
        }
        else if(e.getDamager() instanceof Projectile){
          if(UnlockedAbilities.BULLET_PROOF.isEnabled() && mcRPGPlayer.getAbilityLoadout().contains(UnlockedAbilities.BULLET_PROOF)
               && mcRPGPlayer.getBaseAbility(UnlockedAbilities.BULLET_PROOF).isToggled()){
            BulletProof bulletProof = (BulletProof) mcRPGPlayer.getBaseAbility(UnlockedAbilities.BULLET_PROOF);
            double activationChance = fitnessConfig.getDouble("BulletProofConfig.Tier" + Methods.convertToNumeral(bulletProof.getCurrentTier())
                                                                + ".ActivationChance");
            int chance = (int) (activationChance * 1000);
            Random rand = new Random();
            int val = rand.nextInt(100000);
            if(chance >= val){
              BulletProofEvent bulletProofEvent = new BulletProofEvent(mcRPGPlayer, bulletProof, (Projectile) e.getDamager());
              Bukkit.getPluginManager().callEvent(bulletProofEvent);
              if(!bulletProofEvent.isCancelled()){
                e.setCancelled(true);
                mcRPGPlayer.getPlayer().sendMessage(Methods.color(mcRPGPlayer.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.BulletProof.Activated")));
              }
            }
          }
        }
      }
    }
  }
  
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void awardFitnessExp(EntityDamageByEntityEvent e){
    if(e.getDamager().getType() == EntityType.ENDER_PEARL && McRPG.getInstance().getFileManager().getFile(FileManager.Files.CONFIG).getBoolean("Configuration.DisableEPearlExp")){
      return;
    }
    //Disabled Worlds
    if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
         McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(e.getEntity().getWorld().getName())){
      return;
    }
    if(e.getDamager() instanceof Player && e.getEntity() instanceof Player){
      if(!Methods.canPlayersPVP((Player) e.getEntity(), (Player) e.getDamager())){
        return;
      }
    }
    if(!e.isCancelled() && Skills.FITNESS.isEnabled() && e.getEntity() instanceof Player && e.getDamage() >= 1.0 && ((Player) e.getEntity()).getHealth() - e.getDamage() > 0){
      McRPGPlayer mp;
      try{
        mp = PlayerManager.getPlayer(e.getEntity().getUniqueId());
      }catch(McRPGPlayerNotFoundException exception){
        return;
      }
      if(e.getEntity() instanceof Player && !((Player) e.getEntity()).isBlocking()){
        double damage = e.getDamage();
        int expAwarded = (int) (damage * McRPG.getInstance().getFileManager().getFile(FileManager.Files.FITNESS_CONFIG).getInt("ExpAwardedPerDamage.ENTITY_DAMAGE"));
        mp.giveExp(Skills.FITNESS, expAwarded, GainReason.DAMAGE);
      }
    }
  }
  
  /**
   * This code is not all mine. It is copyright from the original mcMMO allowed for use by their license.
   * This code has been modified from it source material
   * It was released under the GPLv3 license
   */
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void damageEvent(EntityDamageByEntityEvent e){
    //TODO do entity/plugin checks
    if(e.getDamage() >= McRPG.getInstance().getConfig().getInt("Configuration.MaxDamageCap")){
      return;
    }
    //Disabled Worlds
    if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
         McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(e.getEntity().getWorld().getName())){
      return;
    }
    
    //We need to handle taming before we deal with player stuff
    if(Skills.TAMING.isEnabled() && e.getDamager() instanceof Tameable && Skills.TAMING.isEnabled() && e.getDamager() instanceof LivingEntity &&
         e.getEntity() instanceof LivingEntity && e.getDamage() > 0){
      Tameable tameable = (Tameable) e.getDamager();
      if(tameable.getOwner() != null){
        
        McRPGPlayer mp;
        try{
          mp = PlayerManager.getPlayer(tameable.getOwner().getUniqueId());
        }catch(McRPGPlayerNotFoundException exception){
          return;
        }
        
        if(((LivingEntity) e.getEntity()).getHealth() - e.getDamage() <= 0){
          if(BloodManager.getInstance().isEnabled()){
            if(!BloodManager.getInstance().isIgnoreNaturalMobs() || !e.getEntity().hasMetadata("ExpModifier")){
              double spawnChance = BloodManager.getInstance().getNormalSpawnChance();
              if(BleedHandler.isTargeted(e.getEntity().getUniqueId())){
                spawnChance = BloodManager.getInstance().getBleedingSpawnChance();
              }
              int val = (int) (spawnChance * 1000);
              if(val >= new Random().nextInt(100000)){
                e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), BloodFactory.generateBlood());
              }
            }
          }
        }
        
        FileConfiguration tamingConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.TAMING_CONFIG);
        
        //We want to handle increased damage before awarding exp to give exp in compensation of the extra damage
        if(UnlockedAbilities.SHARPENED_FANGS.isEnabled() && mp.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.SHARPENED_FANGS)
             && mp.getBaseAbility(UnlockedAbilities.SHARPENED_FANGS).isToggled()){
          
          SharpenedFangs sharpenedFangs = (SharpenedFangs) mp.getBaseAbility(UnlockedAbilities.SHARPENED_FANGS);
          String tier = Methods.convertToNumeral(sharpenedFangs.getCurrentTier());
          double activationChance = tamingConfig.getDouble("SharpenedFangsConfig.Tier" + tier + ".ActivationChance") * 1000;
          int extraDamage = tamingConfig.getInt("SharpenedFangsConfig.Tier" + tier + ".ExtraDamage");
          Random rand = new Random();
          int val = rand.nextInt(100000);
          if(activationChance >= val){
            SharpenedFangsEvent sharpenedFangsEvent = new SharpenedFangsEvent(mp, sharpenedFangs, extraDamage);
            Bukkit.getPluginManager().callEvent(sharpenedFangsEvent);
            if(!sharpenedFangsEvent.isCancelled()){
              e.setDamage(e.getDamage() + sharpenedFangsEvent.getExtraDamage());
            }
          }
        }
        
        int expToAward;
        if(tamingConfig.contains("ExpAwardedPerMob." + e.getEntityType().name())){
          expToAward = tamingConfig.getInt("ExpAwardedPerMob." + e.getEntityType().name());
        }
        else{
          expToAward = tamingConfig.getInt("ExpAwardedPerMob.OTHER", 0);
        }
        mp.giveExp(Skills.TAMING, (int) (expToAward * e.getDamage()), GainReason.DAMAGE);
        
        if(DefaultAbilities.GORE.isEnabled() && mp.getBaseAbility(DefaultAbilities.GORE).isToggled()){
          Gore gore = (Gore) mp.getBaseAbility(DefaultAbilities.GORE);
          Parser parser = DefaultAbilities.GORE.getActivationEquation();
          parser.setVariable("taming_level", mp.getSkill(Skills.TAMING).getCurrentLevel());
          parser.setVariable("power_level", mp.getPowerLevel());
          int chance = (int) (parser.getValue() * 1000);
          Random rand = new Random();
          int val = rand.nextInt(100000);
          if(chance >= val){
            GoreEvent goreEvent = new GoreEvent(mp, gore);
            Bukkit.getPluginManager().callEvent(goreEvent);
            if(!goreEvent.isCancelled()){
              BleedEvent bleedEvent = new BleedEvent(mp, (LivingEntity) e.getEntity(), (Bleed) mp.getBaseAbility(DefaultAbilities.BLEED));
              Bukkit.getPluginManager().callEvent(bleedEvent);
            }
          }
        }
        
        if(UnlockedAbilities.LINKED_FANGS.isEnabled() && mp.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.LINKED_FANGS) && mp.getBaseAbility(UnlockedAbilities.LINKED_FANGS).isToggled()){
          LinkedFangs linkedFangs = (LinkedFangs) mp.getBaseAbility(UnlockedAbilities.LINKED_FANGS);
          
          String tier = Methods.convertToNumeral(linkedFangs.getCurrentTier());
          double activationChance = tamingConfig.getDouble("LinkedFangsConfig.Tier" + tier + ".ActivationChance") * 1000;
          int healthToRestore = tamingConfig.getInt("LinkedFangsConfig.Tier" + tier + ".HealthToRestore");
          int hungerToRestore = tamingConfig.getInt("LinkedFangsConfig.Tier" + tier + ".HungerToRestore");
          int saturationToRestore = tamingConfig.getInt("LinkedFangsConfig.Tier" + tier + ".SaturationToRestore");
          
          Random rand = new Random();
          int val = rand.nextInt(100000);
          if(activationChance >= val){
            LinkedFangsEvent linkedFangsEvent = new LinkedFangsEvent(mp, linkedFangs, healthToRestore, hungerToRestore, saturationToRestore);
            Bukkit.getPluginManager().callEvent(linkedFangsEvent);
            if(!linkedFangsEvent.isCancelled()){
              LivingEntity len = (LivingEntity) e.getDamager();
              len.setHealth(Math.min(len.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), len.getHealth() + linkedFangsEvent.getHealthToRestore()));
              mp.getPlayer().setFoodLevel(Math.min(20, mp.getPlayer().getFoodLevel() + linkedFangsEvent.getHungerToRestore()));
              if(mp.getPlayer().getSaturation() < 10){
                mp.getPlayer().setSaturation(Math.min(10, mp.getPlayer().getSaturation() + linkedFangsEvent.getSaturationToRestore()));
              }
            }
          }
        }
        
        if(tameable instanceof Wolf){
          Wolf wolf = (Wolf) tameable;
          if(wolf.getPersistentDataContainer().has(HELL_HOUND_KEY, PersistentDataType.STRING)){
            Calendar calendar = Calendar.getInstance();
            if(calendar.getTimeInMillis() >= wolf.getPersistentDataContainer().get(HELL_HOUND_SELF_DESTRUCT_KEY, PersistentDataType.LONG)){
              hellHoundSelfDestructMap.remove(wolf.getUniqueId()).cancel();
              wolf.setHealth(0);
              new BukkitRunnable(){
                @Override
                public void run(){
                  wolf.getWorld().createExplosion(wolf.getLocation(), 2, false, wolf.getPersistentDataContainer().get(HELL_HOUND_DESTROY_KEY, PersistentDataType.STRING).equalsIgnoreCase("true"));
                }
              }.runTaskLater(McRPG.getInstance(), 1);
            }
            else if(wolf.getPersistentDataContainer().get(HELL_HOUND_IGNITE_KEY, PersistentDataType.STRING).equalsIgnoreCase("true")){
              e.getEntity().setFireTicks(100);
            }
          }
        }
      }
      
      //We don't care about other abilities because a wolf can't do anything past here
      return;
    }
    
    if(e.getEntity() instanceof Player && e.getDamager() instanceof Player){
      if(!Methods.canPlayersPVP((Player) e.getEntity(), (Player) e.getDamager())){
        e.setCancelled(true);
        return;
      }
    }
    
    FileConfiguration config;
    if(e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity && !(e.getEntity() instanceof ArmorStand) && !
      
                                                                                                                                isNPCEntity(e.getEntity())){
      Player damager = (Player) e.getDamager();
      
      //Fixes exploit with /sit
      if(damager.isInsideVehicle()){
        return;
      }
      
      //Don't award exp for self harm
      if(e.getEntity().getUniqueId() == e.getDamager().getUniqueId()){
        return;
      }
      McRPGPlayer mp;
      try{
        mp = PlayerManager.getPlayer(damager.getUniqueId());
      }catch(McRPGPlayerNotFoundException exception){
        return;
      }
      
      //Deal with world guard
      if(McRPG.getInstance().isWorldGuardEnabled()){
        WGSupportManager wgSupportManager = McRPG.getInstance().getWgSupportManager();
        
        if(wgSupportManager.isWorldTracker(damager.getWorld())){
          RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
          Location loc = damager.getLocation();
          RegionManager manager = container.get(BukkitAdapter.adapt(loc.getWorld()));
          HashMap<String, WGRegion> regions = wgSupportManager.getRegionManager().get(loc.getWorld());
          assert manager != null;
          ApplicableRegionSet set = manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc));
          for(ProtectedRegion region : set){
            if(regions.containsKey(region.getId()) && regions.get(region.getId()).getAttackExpressions().containsKey(e.getEntity().getType())){
              List<String> expressions = regions.get(region.getId()).getAttackExpressions().get(e.getEntity().getType());
              for(String s : expressions){
                if(s.contains("difference")){
                  if(!(e.getEntity() instanceof Player)){
                    continue;
                  }
                  else{
                    try{
                      McRPGPlayer mcRPGPlayer = PlayerManager.getPlayer(e.getEntity().getUniqueId());
                      ActionLimiterParser actionLimiterParser = new ActionLimiterParser(s, mp, mcRPGPlayer);
                      if(actionLimiterParser.evaluateExpression()){
                        e.setCancelled(true);
                        return;
                      }
                    }catch(McRPGPlayerNotFoundException exception){
                    }
                  }
                }
                else{
                  ActionLimiterParser actionLimiterParser = new ActionLimiterParser(s, mp);
                  if(actionLimiterParser.evaluateExpression()){
                    e.setCancelled(true);
                    return;
                  }
                }
              }
            }
          }
        }
      }
      
      //Deal with unarmed
      if(damager.getInventory().getItemInMainHand() == null || damager.getInventory().getItemInMainHand().getType() == Material.AIR){
        if(!Skills.UNARMED.isEnabled()){
          return;
        }
        config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.UNARMED_CONFIG);
        e.setDamage(e.getDamage() + config.getInt("BonusDamage"));
        //Give exp
        int baseExp = 0;
        if(!config.contains("ExpAwardedPerMob." + e.getEntityType().name())){
          baseExp = config.getInt("ExpAwardedPerMob.OTHER");
        }
        else{
          baseExp = config.getInt("ExpAwardedPerMob." + e.getEntityType().name());
        }
        double dmg = e.getDamage();
        double mobSpawnValue = 1.0;
        if(e.getEntity().hasMetadata("ExpModifier")){
          mobSpawnValue = e.getEntity().getMetadata("ExpModifier").get(0).asDouble();
        }
        int expAwarded = (int) ((dmg * baseExp) * mobSpawnValue);
        
        //Handle shield modifications
        if(e.getEntity() instanceof Player && ((Player) e.getEntity()).isBlocking()){
          expAwarded = (int) (expAwarded * McRPG.getInstance().getFileManager().getFile(FileManager.Files.CONFIG).getDouble("Configuration.ShieldBlockingModifier"));
        }
        
        if(expAwarded > 0){
          mp.getSkill(Skills.UNARMED).giveExp(mp, expAwarded, GainReason.DAMAGE);
        }
        if(mp.isCanSmite()){
          if(!(e.getEntity().getFireTicks() > 0)){
            LivingEntity entity = (LivingEntity) e.getEntity();
            int chance = (int) (mp.getSmitingFistData().getSmiteChance() * 1000);
            Random rand = new Random();
            int val = rand.nextInt(100000);
            if(chance >= val){
              if(entity.hasPotionEffect(PotionEffectType.INVISIBILITY)){
                entity.removePotionEffect(PotionEffectType.INVISIBILITY);
              }
              entity.setFireTicks(mp.getSmitingFistData().getSmiteDuration() * 20);
              if(entity instanceof Player){
                mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                                                           McRPG.getInstance().getLangFile().getString("Messages.Abilities.SmitingFist.Smited").replace("%Player%", entity.getName())));
              }
              FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
              entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.valueOf(soundFile.getString("Sounds.Unarmed.SmitingFist.Sound")),
                Float.parseFloat(soundFile.getString("Sounds.Unarmed.SmitingFist.Volume")), Float.parseFloat(soundFile.getString("Sounds.Unarmed.SmitingFist.Pitch")));
            }
          }
        }
        else if(mp.isCanDenseImpact()){
          if(e.getEntity() instanceof Player){
            Player damaged = (Player) e.getEntity();
            for(ItemStack armour : damaged.getInventory().getArmorContents()){
              armour.setDurability((short) (armour.getDurability() + mp.getArmourDmg()));
            }
          }
        }
        if(mp.isReadying()){
          if(mp.getReadyingAbilityBit() == null){
            mp.setReadying(false);
            return;
          }
          PlayerReadyBit playerReadyBit = mp.getReadyingAbilityBit();
          if(playerReadyBit.getAbilityReady().equals(UnlockedAbilities.BERSERK)){
            if(UnlockedAbilities.BERSERK.isEnabled() && mp.getBaseAbility(UnlockedAbilities.BERSERK).isToggled()){
              
              Berserk berserk = (Berserk) mp.getBaseAbility(UnlockedAbilities.BERSERK);
              double bonusChance = config.getDouble("BerserkConfig.Tier" + Methods.convertToNumeral(berserk.getCurrentTier()) + ".ActivationBoost");
              int bonusDmg = config.getInt("BerserkConfig.Tier" + Methods.convertToNumeral(berserk.getCurrentTier()) + ".DamageBoost");
              BerserkEvent event = new BerserkEvent(mp, (Berserk) mp.getBaseAbility(UnlockedAbilities.BERSERK), bonusChance, bonusDmg);
              Bukkit.getPluginManager().callEvent(event);
              if(!event.isCancelled()){
                mp.getActiveAbilities().add(UnlockedAbilities.BERSERK);
                //cancel the readying task and null the bit
                Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
                mp.setReadyingAbilityBit(null);
                mp.setReadying(false);
                Disarm disarm = (Disarm) mp.getSkill(Skills.UNARMED).getAbility(UnlockedAbilities.DISARM);
                disarm.setBonusChance(event.getBonusChance());
                e.setDamage(e.getDamage() + event.getBonusDamage());
                //Tell player ability started
                mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                                                           McRPG.getInstance().getLangFile().getString("Messages.Abilities.Berserk.Activated")));
                FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
                mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.valueOf(soundFile.getString("Sounds.Unarmed.Berserk.Sound")),
                  Float.parseFloat(soundFile.getString("Sounds.Unarmed.Berserk.Volume")), Float.parseFloat(soundFile.getString("Sounds.Unarmed.Berserk.Pitch")));
                new BukkitRunnable(){
                  @Override
                  public void run(){
                    //Undo all the things that berserk did and set it on cooldown
                    disarm.setBonusChance(0);
                    if(mp.getPlayer().isOnline()){
                      mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                                                                 McRPG.getInstance().getLangFile().getString("Messages.Abilities.Berserk.Deactivated")));
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.SECOND,
                      McRPG.getInstance().getFileManager().getFile(FileManager.Files.UNARMED_CONFIG).getInt("BerserkConfig.Tier" + Methods.convertToNumeral(berserk.getCurrentTier()) + ".Cooldown"));
                    FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
                    mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.valueOf(soundFile.getString("Sounds.Unarmed.BerserkEnded.Sound")),
                      Float.parseFloat(soundFile.getString("Sounds.Unarmed.BerserkEnded.Volume")), Float.parseFloat(soundFile.getString("Sounds.Unarmed.BerserkEnded.Pitch")));
                    mp.getActiveAbilities().remove(UnlockedAbilities.BERSERK);
                    mp.addAbilityOnCooldown(UnlockedAbilities.BERSERK, cal.getTimeInMillis());
                  }
                }.runTaskLater(McRPG.getInstance(), config.getInt("BerserkConfig.Tier" + Methods.convertToNumeral(berserk.getCurrentTier()) + ".Duration") * 20);
              }
            }
          }
          else if(playerReadyBit.getAbilityReady().equals(UnlockedAbilities.SMITING_FIST)){
            if(UnlockedAbilities.SMITING_FIST.isEnabled() && mp.getBaseAbility(UnlockedAbilities.SMITING_FIST).isToggled()){
              SmitingFist smitingFist = (SmitingFist) mp.getBaseAbility(UnlockedAbilities.SMITING_FIST);
              //cancel the readying task and null the bit
              Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
              mp.setReadyingAbilityBit(null);
              mp.setReadying(false);
              int absorptionLevel = config.getInt("SmitingFistConfig.Tier" + Methods.convertToNumeral(smitingFist.getCurrentTier()) + ".AbsorptionLevel");
              double smiteChance = config.getDouble("SmitingFistConfig.Tier" + Methods.convertToNumeral(smitingFist.getCurrentTier()) + ".SmiteChance");
              int smiteDuration = config.getInt("SmitingFistConfig.Tier" + Methods.convertToNumeral(smitingFist.getCurrentTier()) + ".SmiteDuration");
              boolean removeInvis = config.getBoolean("SmitingFistConfig.Tier" + Methods.convertToNumeral(smitingFist.getCurrentTier()) + ".RemoveInvis");
              boolean removeDebuff = config.getBoolean("SmitingFistConfig.Tier" + Methods.convertToNumeral(smitingFist.getCurrentTier()) + ".RemovePotionEffects");
              int duration = config.getInt("SmitingFistConfig.Tier" + Methods.convertToNumeral(smitingFist.getCurrentTier()) + ".Duration");
              int cooldown = config.getInt("SmitingFistConfig.Tier" + Methods.convertToNumeral(smitingFist.getCurrentTier()) + ".Cooldown");
              SmitingFistEvent smitingFistEvent = new SmitingFistEvent(mp, smitingFist, absorptionLevel, smiteChance, smiteDuration, removeInvis, removeDebuff, duration, cooldown);
              Bukkit.getPluginManager().callEvent(smitingFistEvent);
              if(!smitingFistEvent.isCancelled()){
                mp.getActiveAbilities().add(UnlockedAbilities.SMITING_FIST);
                mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                                                           McRPG.getInstance().getLangFile().getString("Messages.Abilities.SmitingFist.Activated")));
                FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
                mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.valueOf(soundFile.getString("Sounds.Unarmed.SmitingFistActivated.Sound")),
                  Float.parseFloat(soundFile.getString("Sounds.Unarmed.SmitingFistActivated.Volume")), Float.parseFloat(soundFile.getString("Sounds.Unarmed.SmitingFistActivated.Pitch")));
                mp.setSmitingFistData(smitingFistEvent);
                mp.setCanSmite(true);
                if(smitingFistEvent.isRemoveDebuffs()){
                  for(PotionEffect effectType : damager.getActivePotionEffects()){
                    if(Debuffs.isDebuff(effectType.getType())){
                      damager.removePotionEffect(effectType.getType());
                    }
                  }
                }
                damager.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, smitingFistEvent.getDuration() * 20, smitingFistEvent.getAbsorptionLevel()));
                new BukkitRunnable(){
                  @Override
                  public void run(){
                    //Undo all the things that smiting fist did and set it on cooldown
                    mp.setCanSmite(false);
                    mp.setSmitingFistData(null);
                    if(mp.getPlayer().isOnline()){
                      mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                                                                 McRPG.getInstance().getLangFile().getString("Messages.Abilities.SmitingFist.Deactivated")));
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.SECOND,
                      smitingFistEvent.getCooldown());
                    FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
                    mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.valueOf(soundFile.getString("Sounds.Unarmed.SmitingFistEnded.Sound")),
                      Float.parseFloat(soundFile.getString("Sounds.Unarmed.SmitingFistEnded.Volume")), Float.parseFloat(soundFile.getString("Sounds.Unarmed.SmitingFistEnded.Pitch")));
                    mp.getActiveAbilities().remove(UnlockedAbilities.SMITING_FIST);
                    mp.addAbilityOnCooldown(UnlockedAbilities.SMITING_FIST, cal.getTimeInMillis());
                  }
                }.runTaskLater(McRPG.getInstance(), smitingFistEvent.getDuration() * 20);
              }
            }
          }
          else if(playerReadyBit.getAbilityReady().equals(UnlockedAbilities.DENSE_IMPACT)){
            if(UnlockedAbilities.DENSE_IMPACT.isEnabled() && mp.getBaseAbility(UnlockedAbilities.DENSE_IMPACT).isToggled()){
              DenseImpact denseImpact = (DenseImpact) mp.getBaseAbility(UnlockedAbilities.DENSE_IMPACT);
              int cooldown = config.getInt("DenseImpactConfig.Tier" + Methods.convertToNumeral(denseImpact.getCurrentTier()) + ".Cooldown");
              int duration = config.getInt("DenseImpactConfig.Tier" + Methods.convertToNumeral(denseImpact.getCurrentTier()) + ".Duration");
              int armourDmg = config.getInt("DenseImpactConfig.Tier" + Methods.convertToNumeral(denseImpact.getCurrentTier()) + ".ArmorDamage");
              mp.setReadying(false);
              Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
              mp.setReadyingAbilityBit(null);
              DenseImpactEvent denseImpactEvent = new DenseImpactEvent(mp, denseImpact, armourDmg);
              Bukkit.getPluginManager().callEvent(denseImpactEvent);
              if(!denseImpactEvent.isCancelled()){
                mp.getActiveAbilities().add(UnlockedAbilities.DENSE_IMPACT);
                mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                                                           McRPG.getInstance().getLangFile().getString("Messages.Abilities.DenseImpact.Activated")));
                FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
                mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.valueOf(soundFile.getString("Sounds.Unarmed.DenseImpactActivated.Sound")),
                  Float.parseFloat(soundFile.getString("Sounds.Unarmed.DenseImpactActivated.Volume")), Float.parseFloat(soundFile.getString("Sounds.Unarmed.DenseImpactActivated.Pitch")));
                mp.setCanDenseImpact(true);
                mp.setArmourDmg(denseImpactEvent.getArmourDmg());
                new BukkitRunnable(){
                  @Override
                  public void run(){
                    //Undo all the things that dense impact did and set it on cooldown
                    mp.setCanDenseImpact(false);
                    mp.setArmourDmg(0);
                    if(mp.getPlayer().isOnline()){
                      mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                                                                 McRPG.getInstance().getLangFile().getString("Messages.Abilities.DenseImpact.Deactivated")));
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.SECOND,
                      cooldown);
                    mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.valueOf(soundFile.getString("Sounds.Unarmed.DenseImpactEnded.Sound")),
                      Float.parseFloat(soundFile.getString("Sounds.Unarmed.DenseImpactEnded.Volume")), Float.parseFloat(soundFile.getString("Sounds.Unarmed.DenseImpactEnded.Pitch")));
                    mp.getActiveAbilities().remove(UnlockedAbilities.DENSE_IMPACT);
                    mp.addAbilityOnCooldown(UnlockedAbilities.DENSE_IMPACT, cal.getTimeInMillis());
                  }
                }.runTaskLater(McRPG.getInstance(), duration * 20);
              }
            }
          }
        }
        //Manage disarm
        if(e.getEntity() instanceof Player && UnlockedAbilities.DISARM.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.DISARM) && mp.getBaseAbility(UnlockedAbilities.DISARM).isToggled()){
          Disarm disarm = (Disarm) mp.getBaseAbility(UnlockedAbilities.DISARM);
          Player damagedPlayer = (Player) e.getEntity();
          McRPGPlayer damagedMcRPGPlayer;
          try{
            damagedMcRPGPlayer = PlayerManager.getPlayer(damagedPlayer.getUniqueId());
          }catch(McRPGPlayerNotFoundException exception){
            return;
          }
          if(damagedPlayer.getItemInHand() != null || damagedPlayer.getItemInHand().getType() != Material.AIR){
            double disarmChance = config.getDouble("DisarmConfig.Tier" + Methods.convertToNumeral(disarm.getCurrentTier()) + ".ActivationChance") + disarm.getBonusChance();
            int chance = (int) (disarmChance * 1000);
            Random rand = new Random();
            int val = rand.nextInt(100000);
            if(chance >= val){
              DisarmEvent disarmEvent = new DisarmEvent(mp, damagedMcRPGPlayer, disarm, damagedPlayer.getItemInHand());
              Bukkit.getPluginManager().callEvent(disarmEvent);
              if(!disarmEvent.isCancelled()){
                int slot = -1;
                for(int i = 9; i < 36; i++){
                  ItemStack item = damagedPlayer.getInventory().getItem(i);
                  if(item == null || item.getType() == Material.AIR){
                    slot = i;
                    break;
                  }
                }
                int heldSlot = damagedPlayer.getInventory().getHeldItemSlot();
                if(damagedPlayer.getInventory().getItem(heldSlot) == null){
                  return;
                }
                if(slot == -1){
                  damagedPlayer.getLocation().getWorld().dropItemNaturally(damagedPlayer.getLocation(), disarmEvent.getItemToDisarm());
                }
                else{
                  damagedPlayer.getInventory().setItem(slot, disarmEvent.getItemToDisarm());
                }
                damagedPlayer.getInventory().setItem(heldSlot, new ItemStack(Material.AIR));
                damager.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix()
                                                    + McRPG.getInstance().getLangFile().getString("Messages.Abilities.Disarm.PlayerDisarmed").replaceAll("%Player%", damagedPlayer.getDisplayName())));
                damagedPlayer.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.Disarm.BeenDisarmed")));
              }
            }
          }
        }
        if(UnlockedAbilities.IRON_ARM.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.IRON_ARM) && mp.getBaseAbility(UnlockedAbilities.IRON_ARM).isToggled()){
          IronArm ironArm = (IronArm) mp.getBaseAbility(UnlockedAbilities.IRON_ARM);
          int chance = (int) (config.getDouble("IronArmConfig.Tier" + Methods.convertToNumeral(ironArm.getCurrentTier()) + ".ActivationChance") * 1000);
          int bonusDmg = config.getInt("IronArmConfig.Tier" + Methods.convertToNumeral(ironArm.getCurrentTier()) + ".DamageBoost");
          Random rand = new Random();
          int val = rand.nextInt(100000);
          if(chance >= val){
            IronArmEvent ironArmEvent = new IronArmEvent(mp, ironArm, bonusDmg);
            Bukkit.getPluginManager().callEvent(ironArmEvent);
            if(!ironArmEvent.isCancelled()){
              e.setDamage(e.getDamage() + ironArmEvent.getBonusDamage());
            }
          }
        }
      }
      else{
        Material weapon = damager.getInventory().getItemInMainHand().getType();
        if(weapon.name().contains("SWORD")){
          Skill playersSkill = mp.getSkill(Skills.SWORDS);
          if(!Skills.SWORDS.isEnabled()){
            return;
          }
          //If the player is readying for an ability
          if(mp.isReadying()){
            //If we need to use serrated strikes (We can preemptively assume that its enabled as we have checked earlier on in the code)
            if(mp.getReadyingAbilityBit().getAbilityReady() == UnlockedAbilities.SERRATED_STRIKES){
              //call api event
              SerratedStrikesEvent event = new SerratedStrikesEvent(mp, (SerratedStrikes) mp.getBaseAbility(UnlockedAbilities.SERRATED_STRIKES));
              Bukkit.getPluginManager().callEvent(event);
              if(!event.isCancelled()){
                mp.getActiveAbilities().add(UnlockedAbilities.SERRATED_STRIKES);
                //cancel the readying task and null the bit
                Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
                mp.setReadyingAbilityBit(null);
                //get the bleed ability and set the bonus chance
                Bleed bleed = (Bleed) mp.getSkill(Skills.SWORDS).getAbility(DefaultAbilities.BLEED);
                bleed.setBonusChance(event.getActivationRateBoost());
                //Tell player ability started
                mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                                                           McRPG.getInstance().getLangFile().getString("Messages.Abilities.SerratedStrikes.Activated")));
                new BukkitRunnable(){
                  @Override
                  public void run(){
                    //Undo all the things that serrated strikes did and set it on cooldown
                    bleed.setBonusChance(0);
                    if(mp.getPlayer().isOnline()){
                      mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                                                                 McRPG.getInstance().getLangFile().getString("Messages.Abilities.SerratedStrikes.Deactivated")));
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.SECOND,
                      event.getCooldown());
                    FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
                    mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.valueOf(soundFile.getString("Sounds.Swords.SerratedStrikesEnded.Sound")),
                      Float.parseFloat(soundFile.getString("Sounds.Swords.SerratedStrikesEnded.Volume")), Float.parseFloat(soundFile.getString("Sounds.Swords.SerratedStrikesEnded.Pitch")));
                    mp.getActiveAbilities().remove(UnlockedAbilities.SERRATED_STRIKES);
                    mp.addAbilityOnCooldown(UnlockedAbilities.SERRATED_STRIKES, cal.getTimeInMillis());
                  }
                }.runTaskLater(McRPG.getInstance(), event.getDuration() * 20);
              }
            }
            //If we need to use tainted blade
            else if(mp.getReadyingAbilityBit().getAbilityReady() == UnlockedAbilities.TAINTED_BLADE){
              TaintedBladeEvent event = new TaintedBladeEvent(mp, (TaintedBlade) mp.getBaseAbility(UnlockedAbilities.TAINTED_BLADE));
              Bukkit.getPluginManager().callEvent(event);
              if(!event.isCancelled()){
                mp.getActiveAbilities().add(UnlockedAbilities.TAINTED_BLADE);
                Player p = mp.getPlayer();
                p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                                              McRPG.getInstance().getLangFile().getString("Messages.Abilities.TaintedBlade.Activated")));
                PotionEffect strength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, event.getStrengthDuration() * 20, 1);
                PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, event.getResistanceDuration() * 20, 1);
                PotionEffect hunger = new PotionEffect(PotionEffectType.HUNGER, event.getHungerDuration() * 20, 3);
                p.addPotionEffect(strength);
                p.addPotionEffect(resistance);
                p.addPotionEffect(hunger);
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.SECOND,
                  event.getCooldown());
                FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
                p.getLocation().getWorld().playSound(p.getLocation(), Sound.valueOf(soundFile.getString("Sounds.Swords.TaintedBladeEnd.Sound")),
                  Float.parseFloat(soundFile.getString("Sounds.Swords.TaintedBladeEnd.Volume")), Float.parseFloat(soundFile.getString("Sounds.Swords.TaintedBladeEnd.Pitch")));
                mp.getActiveAbilities().remove(UnlockedAbilities.TAINTED_BLADE);
                mp.addAbilityOnCooldown(UnlockedAbilities.TAINTED_BLADE, cal.getTimeInMillis());
              }
            }
          }
          if(DefaultAbilities.BLEED.isEnabled()){
            if(playersSkill.getAbility(DefaultAbilities.BLEED).isToggled()){
              Bleed bleed = (Bleed) playersSkill.getAbility(DefaultAbilities.BLEED);
              Parser parser = DefaultAbilities.BLEED.getActivationEquation();
              if(e.getEntity() instanceof Player){
                Player damagedPlayer = (Player) e.getEntity();
                McRPGPlayer dmged;
                try{
                  dmged = PlayerManager.getPlayer(damagedPlayer.getUniqueId());
                }catch(McRPGPlayerNotFoundException exception){
                  return;
                }
                if(!dmged.isHasBleedImmunity() && bleed.canTarget()){
                  parser.setVariable("swords_level", playersSkill.getCurrentLevel());
                  parser.setVariable("power_level", mp.getPowerLevel());
                  int chance = (int) (parser.getValue() * 1000);
                  Random rand = new Random();
                  int val = rand.nextInt(100000);
                  if(chance >= val){
                    BleedEvent event = new BleedEvent(mp, (Player) e.getEntity(), bleed);
                    Bukkit.getPluginManager().callEvent(event);
                  }
                }
              }
              else{
                parser.setVariable("swords_level", playersSkill.getCurrentLevel());
                parser.setVariable("power_level", mp.getPowerLevel());
                Random rand = new Random();
                int chance = (int) (parser.getValue() + bleed.getBonusChance()) * 1000;
                int val = rand.nextInt(100000);
                if(chance >= val && e.getEntity() instanceof LivingEntity){
                  BleedEvent event = new BleedEvent(mp, (LivingEntity) e.getEntity(), bleed);
                  Bukkit.getPluginManager().callEvent(event);
                }
              }
            }
          }
          config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG);
          double multiplier = config.getDouble("MaterialBonus." + weapon.name());
          int baseExp = 0;
          if(!config.contains("ExpAwardedPerMob." + e.getEntityType().name())){
            baseExp = config.getInt("ExpAwardedPerMob.OTHER");
          }
          else{
            baseExp = config.getInt("ExpAwardedPerMob." + e.getEntityType().name());
          }
          double dmg = e.getDamage();
          double mobSpawnValue = 1.0;
          if(e.getEntity().hasMetadata("ExpModifier")){
            mobSpawnValue = e.getEntity().getMetadata("ExpModifier").get(0).asDouble();
          }
          int expAwarded = (int) ((dmg * baseExp * multiplier) * mobSpawnValue);
          //Deal with possible shield exploits
          if(e.getEntity() instanceof Player && ((Player) e.getEntity()).isBlocking()){
            expAwarded = (int) (expAwarded * config.getDouble("Configuration.ShieldBlockingModifier"));
          }
          if(expAwarded > 0){
            mp.getSkill(Skills.SWORDS).giveExp(mp, expAwarded, GainReason.DAMAGE);
          }
        }
        
        else if(weapon.name().contains("AXE")){
          Axes axes = (Axes) mp.getSkill(Skills.AXES);
          config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.AXES_CONFIG);
          if(mp.isReadying()){
            if(mp.getReadyingAbilityBit().getAbilityReady() == UnlockedAbilities.CRIPPLING_BLOW){
              CripplingBlow cripplingBlow = (CripplingBlow) mp.getBaseAbility(UnlockedAbilities.CRIPPLING_BLOW);
              String key = "CripplingBlowConfig.Tier" + Methods.convertToNumeral(cripplingBlow.getCurrentTier()) + ".";
              int duration = config.getInt(key + "Duration");
              int slownessDuration = config.getInt(key + "SlownessDuration");
              int slownessLevel = config.getInt(key + "SlownessLevel");
              int nauseaDuration = config.getInt(key + "NauseaDuration");
              int cooldown = config.getInt(key + "Cooldown");
              CripplingBlowEvent cripplingBlowEvent = new CripplingBlowEvent(mp, cripplingBlow, duration, slownessDuration, slownessLevel, nauseaDuration, cooldown);
              Bukkit.getPluginManager().callEvent(cripplingBlowEvent);
              if(!cripplingBlowEvent.isCancelled()){
                Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
                mp.setReadyingAbilityBit(null);
                mp.setReadying(false);
                mp.getActiveAbilities().add(UnlockedAbilities.CRIPPLING_BLOW);
                mp.setCripplingBlowData(cripplingBlowEvent);
                damager.sendMessage(Methods.color(damager, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.CripplingBlow.Activated")));
                new BukkitRunnable(){
                  @Override
                  public void run(){
                    mp.getActiveAbilities().remove(UnlockedAbilities.CRIPPLING_BLOW);
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.SECOND, cripplingBlowEvent.getCooldown());
                    mp.addAbilityOnCooldown(UnlockedAbilities.CRIPPLING_BLOW, cal.getTimeInMillis());
                    mp.setCripplingBlowData(null);
                    //TODO event text
                  }
                }.runTaskLater(McRPG.getInstance(), cripplingBlowEvent.getDuration() * 20);
              }
            }
            else if(mp.getReadyingAbilityBit().getAbilityReady() == UnlockedAbilities.WHIRLWIND_STRIKE){
              WhirlwindStrike whirlwindStrike = (WhirlwindStrike) mp.getBaseAbility(UnlockedAbilities.WHIRLWIND_STRIKE);
              String key = "WhirlwindStrikeConfig.Tier" + Methods.convertToNumeral(whirlwindStrike.getCurrentTier()) + ".";
              double radius = config.getDouble(key + "Radius");
              int damage = config.getInt(key + "Damage");
              int cooldown = config.getInt(key + "Cooldown");
              WhirlwindStrikeEvent whirlwindStrikeEvent = new WhirlwindStrikeEvent(mp, whirlwindStrike, damage, radius, cooldown);
              Bukkit.getPluginManager().callEvent(whirlwindStrikeEvent);
              if(!whirlwindStrikeEvent.isCancelled()){
                mp.getActiveAbilities().add(UnlockedAbilities.WHIRLWIND_STRIKE);
                damager.sendMessage(Methods.color(damager, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.WhirlwindStrike.Activated")));
                for(Entity en : mp.getPlayer().getNearbyEntities(whirlwindStrikeEvent.getRange(), 2, whirlwindStrikeEvent.getRange())){
                  if(en instanceof LivingEntity && !(en instanceof ArmorStand)){
                    //make target go voom
                    org.bukkit.util.Vector targVector = new Vector(en.getLocation().getDirection().getX(), en.getLocation().getDirection().getY(), mp.getPlayer().getLocation().getDirection().getZ());
                    en.setVelocity(targVector.multiply(-5.1));
                    //damage target and add them to list
                    ((LivingEntity) en).damage(whirlwindStrikeEvent.getDamage());
                    en.sendMessage(Methods.color(damager, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.WhirlwindStrike.Hit")));
                  }
                }
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.SECOND, whirlwindStrikeEvent.getCooldown());
                mp.addAbilityOnCooldown(UnlockedAbilities.WHIRLWIND_STRIKE, cal.getTimeInMillis());
                mp.getActiveAbilities().remove(UnlockedAbilities.WHIRLWIND_STRIKE);
                mp.setReadying(false);
                Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
                mp.setReadyingAbilityBit(null);
              }
            }
            else if(mp.getReadyingAbilityBit().getAbilityReady() == UnlockedAbilities.ARES_BLESSING){
              AresBlessing aresBlessing = (AresBlessing) mp.getBaseAbility(UnlockedAbilities.ARES_BLESSING);
              String key = "AresBlessingConfig.Tier" + Methods.convertToNumeral(aresBlessing.getCurrentTier()) + ".";
              int strengthDuration = config.getInt(key + "StrengthDuration");
              int strengthLevel = config.getInt(key + "StrengthLevel") - 1;
              int resistanceDuration = config.getInt(key + "ResistanceDuration");
              int resistanceLevel = config.getInt(key + "ResistanceLevel") - 1;
              int weaknessDuration = config.getInt(key + "WeaknessDuration");
              int weaknessLevel = config.getInt(key + "WeaknessLevel") - 1;
              int miningFatigueDuration = config.getInt(key + "MiningFatigueDuration");
              int miningFatigueLevel = config.getInt(key + "MiningFatigueLevel") - 1;
              int cooldown = config.getInt(key + "Cooldown");
              AresBlessingEvent aresBlessingEvent = new AresBlessingEvent(mp, aresBlessing, strengthDuration, strengthLevel, resistanceDuration, resistanceLevel, weaknessDuration, weaknessLevel, miningFatigueDuration, miningFatigueLevel, cooldown);
              Bukkit.getPluginManager().callEvent(aresBlessingEvent);
              if(!aresBlessingEvent.isCancelled()){
                Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
                mp.setReadyingAbilityBit(null);
                mp.setReadying(false);
                mp.getActiveAbilities().add(UnlockedAbilities.ARES_BLESSING);
                damager.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, aresBlessingEvent.getStrengthDuration() * 20, aresBlessingEvent.getStrengthLevel()));
                damager.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, aresBlessingEvent.getResistanceDuration() * 20, aresBlessingEvent.getResistanceLevel()));
                damager.sendMessage(Methods.color(damager, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.AresBlessing.Activated")));
                new BukkitRunnable(){
                  @Override
                  public void run(){
                    damager.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, aresBlessingEvent.getMiningFatigueDuration() * 20, aresBlessingEvent.getMiningFatigueLevel()));
                    damager.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, aresBlessingEvent.getWeaknessDuration() * 20, aresBlessingEvent.getWeaknessDuration()));
                    damager.sendMessage(Methods.color(damager, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.AresBlessing.Deactivated")));
                    mp.getActiveAbilities().remove(UnlockedAbilities.ARES_BLESSING);
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.SECOND, aresBlessingEvent.getCooldown());
                    mp.addAbilityOnCooldown(UnlockedAbilities.ARES_BLESSING, cal.getTimeInMillis());
                  }
                }.runTaskLater(McRPG.getInstance(), Math.max(aresBlessingEvent.getStrengthDuration(), aresBlessingEvent.getResistanceDuration()) * 20);
              }
            }
          }
          if(mp.getActiveAbilities().contains(UnlockedAbilities.CRIPPLING_BLOW)){
            if(e.getEntity() instanceof Player){
              if(mp.getCripplingBlowData() != null){
                Player target = (Player) e.getEntity();
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, mp.getCripplingBlowData().getSlownessDuration() * 20, mp.getCripplingBlowData().getSlownessLevel()));
                target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, mp.getCripplingBlowData().getNauseaDuration() * 20, 0));
                target.sendMessage(Methods.color(mp.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.CripplingBlow.Hit")));
              }
              else{
                mp.getActiveAbilities().remove(UnlockedAbilities.CRIPPLING_BLOW);
              }
            }
          }
          if(e.getEntity() instanceof Player && DefaultAbilities.SHRED.isEnabled() && mp.getBaseAbility(DefaultAbilities.SHRED).isToggled()){
            Player target = (Player) e.getEntity();
            Shred shred = (Shred) mp.getBaseAbility(DefaultAbilities.SHRED);
            int armourDamage = 1;
            double bonusChance = 0.0;
            if(UnlockedAbilities.SHARPER_AXE.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.SHARPER_AXE) && mp.getBaseAbility(UnlockedAbilities.SHARPER_AXE).isToggled()){
              SharperAxe sharperAxe = (SharperAxe) mp.getBaseAbility(UnlockedAbilities.SHARPER_AXE);
              int highEnd = config.getInt("SharperAxeConfig.Tier" + Methods.convertToNumeral(sharperAxe.getCurrentTier()) + ".HighEnd");
              int lowEnd = config.getInt("SharperAxeConfig.Tier" + Methods.convertToNumeral(sharperAxe.getCurrentTier()) + ".LowEnd");
              SharperAxeEvent sharperAxeEvent = new SharperAxeEvent(mp, sharperAxe, lowEnd, highEnd);
              Bukkit.getPluginManager().callEvent(sharperAxeEvent);
              if(!sharperAxeEvent.isCancelled()){
                Random rand = new Random();
                int diff = rand.nextInt(highEnd - lowEnd);
                armourDamage = lowEnd + diff;
              }
            }
            if(UnlockedAbilities.HEAVY_STRIKE.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.HEAVY_STRIKE) && mp.getBaseAbility(UnlockedAbilities.HEAVY_STRIKE).isToggled()){
              HeavyStrike heavyStrike = (HeavyStrike) mp.getBaseAbility(UnlockedAbilities.HEAVY_STRIKE);
              double bonus = config.getDouble("HeavyStrikeConfig.Tier" + Methods.convertToNumeral(heavyStrike.getCurrentTier()) + ".ActivationChanceBoost");
              HeavyStrikeEvent heavyStrikeEvent = new HeavyStrikeEvent(mp, heavyStrike, bonus);
              Bukkit.getPluginManager().callEvent(heavyStrikeEvent);
              if(!heavyStrikeEvent.isCancelled()){
                bonusChance = heavyStrikeEvent.getBonusChance();
              }
            }
            Parser parser = DefaultAbilities.SHRED.getActivationEquation();
            parser.setVariable("axes_level", axes.getCurrentLevel());
            parser.setVariable("power_level", mp.getPowerLevel());
            int chance = (int) (parser.getValue() + bonusChance) * 1000;
            Random rand = new Random();
            int val = rand.nextInt(100000);
            if(chance >= val){
              ShredEvent event = new ShredEvent(mp, shred, target, armourDamage);
              Bukkit.getPluginManager().callEvent(event);
              if(!event.isCancelled() && target.getEquipment() != null){
                for(ItemStack i : target.getEquipment().getArmorContents()){
                  if(i == null){
                    continue;
                  }
                  i.setDurability((short) (i.getDurability() + event.getArmourDamage()));
                }
              }
            }
          }
          double multiplier = config.getDouble("MaterialBonus." + weapon.name());
          int baseExp = 0;
          if(!config.contains("ExpAwardedPerMob." + e.getEntityType().name())){
            baseExp = config.getInt("ExpAwardedPerMob.OTHER");
          }
          else{
            baseExp = config.getInt("ExpAwardedPerMob." + e.getEntityType().name());
          }
          double dmg = e.getDamage();
          double mobSpawnValue = 1.0;
          if(e.getEntity().hasMetadata("ExpModifier")){
            mobSpawnValue = e.getEntity().getMetadata("ExpModifier").get(0).asDouble();
          }
          int expAwarded = (int) ((dmg * baseExp * multiplier) * mobSpawnValue);
          if(e.getEntity() instanceof Player && ((Player) e.getEntity()).isBlocking()){
            expAwarded = (int) (expAwarded * config.getDouble("Configuration.ShieldBlockingModifier"));
          }
          if(expAwarded > 0){
            mp.getSkill(Skills.AXES).giveExp(mp, expAwarded, GainReason.DAMAGE);
          }
        }
        
        else if(weapon == Material.BLAZE_ROD){
          Taming taming = (Taming) mp.getSkill(Skills.TAMING);
          config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.TAMING_CONFIG);
          
          if(mp.isReadying()){
            if(mp.getReadyingAbilityBit().getAbilityReady() == UnlockedAbilities.FURY_OF_CERBERUS){
              
              FuryOfCerberus furyOfCerberus = (FuryOfCerberus) mp.getBaseAbility(UnlockedAbilities.FURY_OF_CERBERUS);
              String tier = Methods.convertToNumeral(furyOfCerberus.getCurrentTier());
              
              int hellHoundHealth = config.getInt("FuryOfCerberusConfig.Tier" + tier + ".HellHoundHealth");
              boolean igniteTarget = config.getBoolean("FuryOfCerberusConfig.Tier" + tier + ".IgniteTarget");
              boolean explosionsDestroyBlocks = config.getBoolean("FuryOfCerberusConfig.Tier" + tier + ".ExplosionDestroyBlocks");
              int selfDestructTimer = config.getInt("FuryOfCerberusConfig.Tier" + tier + ".SelfDestructTimer");
              int cooldown = config.getInt("FuryOfCerberusConfig.Tier" + tier + ".Cooldown");
              
              FuryOfCerberusEvent furyOfCerberusEvent = new FuryOfCerberusEvent(mp, furyOfCerberus, hellHoundHealth, igniteTarget, explosionsDestroyBlocks, selfDestructTimer, cooldown);
              Bukkit.getPluginManager().callEvent(furyOfCerberusEvent);
              if(!furyOfCerberusEvent.isCancelled()){
                Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
                mp.setReadyingAbilityBit(null);
                mp.setReadying(false);
                
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND, furyOfCerberusEvent.getSelfDestructTimer());
                for(int i = 0; i < 3; i++){
                  Wolf hellHound = (Wolf) mp.getPlayer().getWorld().spawnEntity(mp.getPlayer().getLocation(), EntityType.WOLF);
                  hellHound.setCollarColor(DyeColor.ORANGE);
                  hellHound.setAdult();
                  hellHound.setOwner(mp.getPlayer());
                  hellHound.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(furyOfCerberusEvent.getHellHoundHealth());
                  hellHound.setHealth(furyOfCerberusEvent.getHellHoundHealth());
                  hellHound.getPersistentDataContainer().set(HELL_HOUND_KEY, PersistentDataType.STRING, "true");
                  hellHound.getPersistentDataContainer().set(HELL_HOUND_IGNITE_KEY, PersistentDataType.STRING, Boolean.toString(furyOfCerberusEvent.isIgniteTarget()));
                  hellHound.getPersistentDataContainer().set(HELL_HOUND_DESTROY_KEY, PersistentDataType.STRING, Boolean.toString(furyOfCerberusEvent.isExplosionDestroyBlocks()));
                  hellHound.getPersistentDataContainer().set(HELL_HOUND_SELF_DESTRUCT_KEY, PersistentDataType.LONG, calendar.getTimeInMillis());
                  hellHound.setTarget((LivingEntity) e.getEntity());
                  hellHound.setCustomName(ChatColor.RED + "Hell Hound");
                  hellHound.setCustomNameVisible(true);
                  
                  FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
                  mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.valueOf(soundFile.getString("Sounds.Taming.HellHoundSummon.Sound")),
                    Float.parseFloat(soundFile.getString("Sounds.Taming.HellHoundSummon.Volume")), Float.parseFloat(soundFile.getString("Sounds.Taming.HellHoundSummon.Pitch")));
                  
                  Calendar cal = Calendar.getInstance();
                  cal.add(Calendar.SECOND, furyOfCerberusEvent.getCooldown());
                  mp.addAbilityOnCooldown(UnlockedAbilities.FURY_OF_CERBERUS, cal.getTimeInMillis());
                  
                  BukkitTask selfDestructTask = new BukkitRunnable(){
                    @Override
                    public void run(){
                      if(hellHound.isValid()){
                        hellHound.setHealth(0);
                        new BukkitRunnable(){
                          @Override
                          public void run(){
                            hellHound.getWorld().createExplosion(hellHound.getLocation(), 2, false, hellHound.getPersistentDataContainer().get(HELL_HOUND_DESTROY_KEY, PersistentDataType.STRING).equalsIgnoreCase("true"));
                          }
                        }.runTaskLater(McRPG.getInstance(), 1);
                        hellHoundSelfDestructMap.remove(hellHound.getUniqueId());
                      }
                      cancel();
                    }
                  }.runTaskLater(McRPG.getInstance(), furyOfCerberusEvent.getSelfDestructTimer() * 20);
                  hellHoundSelfDestructMap.put(hellHound.getUniqueId(), selfDestructTask);
                }
              }
            }
          }
        }
        
        else if(weapon == Material.REDSTONE && e.getEntity() instanceof Player){
          NBTItem nbtItem = new NBTItem(damager.getInventory().getItemInMainHand());
          if(nbtItem.hasKey("McRPGBlood")){
            BloodManager.BloodType bloodType = BloodManager.BloodType.getFromID(nbtItem.getString("BloodType"));
            if(bloodType == BloodManager.BloodType.CURSE){
              int duration = nbtItem.getInteger("Duration");
              BloodManager.getInstance().setPlayerUnderCurse(e.getEntity().getUniqueId(), duration);
              damager.getInventory().getItemInMainHand().setAmount(damager.getInventory().getItemInMainHand().getAmount() - 1);
              damager.updateInventory();
              damager.sendMessage(Methods.color(damager, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Blood.AppliedCurse").replace("%Target%", e.getEntity().getName()).replace("%Duration%", Integer.toString(duration))));
              e.getEntity().sendMessage(Methods.color((Player) e.getEntity(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Blood.BeenCursed").replace("%Duration%", Integer.toString(duration))));
            }
          }
        }
      }
      handleHealthbars(e.getDamager(), (LivingEntity) e.getEntity(), e.getFinalDamage());
    }
    else if(e.getEntity() instanceof LivingEntity && e.getDamager() instanceof Arrow && Skills.ARCHERY.isEnabled()){
      Arrow arrow = (Arrow) e.getDamager();
      if(arrow.getShooter() instanceof Player){
        Player shooter = (Player) arrow.getShooter();
        if(shooter.getUniqueId().equals(e.getEntity().getUniqueId()) || shooter.isInsideVehicle()){
          return;
        }
        McRPGPlayer mp;
        try{
          mp = PlayerManager.getPlayer(shooter.getUniqueId());
        }catch(McRPGPlayerNotFoundException exception){
          return;
        }
        //give exp
        config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.ARCHERY_CONFIG);
        int baseExp = 0;
        if(!config.contains("ExpAwardedPerMob." + e.getEntity().getType().name())){
          baseExp = config.getInt("ExpAwardedPerMob.OTHER");
        }
        else{
          baseExp = config.getInt("ExpAwardedPerMob." + e.getEntity().getType().name());
        }
        double dmg = e.getDamage();
        if(!arrow.hasMetadata("ShootLoc")){
          return;
        }
        Location loc = Methods.stringToLoc(arrow.getMetadata("ShootLoc").get(0).asString());
        Location hitLoc = e.getEntity().getLocation();
        double distance = loc.distance(hitLoc);
        if(distance > config.getInt("DistanceBonusCap")){
          distance = config.getInt("DistanceBonusCap");
        }
        Parser parser = new Parser(config.getString("DistanceBonus"));
        parser.setVariable("block_distance", distance);
        double mobSpawnValue = 1.0;
        if(e.getEntity().hasMetadata("ExpModifier")){
          mobSpawnValue = e.getEntity().getMetadata("ExpModifier").get(0).asDouble();
        }
        int expAwarded = (int) ((dmg * baseExp + (dmg * baseExp * parser.getValue())) * mobSpawnValue);
        if(expAwarded > 0){
          mp.getSkill(Skills.ARCHERY).giveExp(mp, expAwarded, GainReason.DAMAGE);
        }
        //Handle the hp bars when dealing with archery
        handleHealthbars(e.getDamager(), (LivingEntity) e.getEntity(), e.getFinalDamage());
        
        if(e.getEntity() instanceof LivingEntity){
          LivingEntity target = (LivingEntity) e.getEntity();
          if(arrow.hasMetadata("Artemis")){
            double dmgMultiplier = arrow.getMetadata("Artemis").get(0).asDouble();
            e.setDamage(e.getDamage() * dmgMultiplier);
            if(target instanceof Player){
              target.sendMessage(Methods.color((Player) target, McRPG.getInstance().getPluginPrefix() +
                                                                  McRPG.getInstance().getLangFile().getString("Messages.Abilities.BlessingOfArtemis.Hit")));
            }
          }
          
          else if(arrow.hasMetadata("Apollo")){
            int fireDuration = arrow.getMetadata("Apollo").get(0).asInt();
            target.setFireTicks(fireDuration * 20);
            if(target instanceof Player){
              target.sendMessage(Methods.color((Player) target, McRPG.getInstance().getPluginPrefix() +
                                                                  McRPG.getInstance().getLangFile().getString("Messages.Abilities.BlessingOfApollo.Hit")));
            }
            target.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
            target.removePotionEffect(PotionEffectType.REGENERATION);
            target.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            target.removePotionEffect(PotionEffectType.FAST_DIGGING);
            target.removePotionEffect(PotionEffectType.SPEED);
            target.removePotionEffect(PotionEffectType.REGENERATION);
            target.removePotionEffect(PotionEffectType.ABSORPTION);
            target.removePotionEffect(PotionEffectType.HEALTH_BOOST);
            target.removePotionEffect(PotionEffectType.WATER_BREATHING);
          }
          
          else if(arrow.hasMetadata("Hades1")){
            int witherDuration = arrow.getMetadata("Hades1").get(0).asInt();
            int witherLevel = arrow.getMetadata("Hades2").get(0).asInt();
            int slownessDuration = arrow.getMetadata("Hades3").get(0).asInt();
            int slownessLevel = arrow.getMetadata("Hades4").get(0).asInt();
            int blindnessDuration = arrow.getMetadata("Hades5").get(0).asInt();
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witherDuration * 20, witherLevel - 1));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slownessDuration * 20, slownessLevel - 1));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, blindnessDuration * 20, 0));
            
            if(target instanceof Player){
              target.sendMessage(Methods.color((Player) target, McRPG.getInstance().getPluginPrefix() +
                                                                  McRPG.getInstance().getLangFile().getString("Messages.Abilities.CurseOfHades.Hit")));
            }
          }
          
          if(arrow.hasMetadata("Puncture")){
            McRPGPlayer s;
            try{
              s = PlayerManager.getPlayer(UUID.fromString(arrow.getMetadata("Puncture").get(0).asString()));
            }catch(McRPGPlayerNotFoundException exception){
              return;
            }
            BleedEvent bleedEvent = new BleedEvent(s, target, (Bleed) s.getBaseAbility(DefaultAbilities.BLEED));
            Bukkit.getPluginManager().callEvent(bleedEvent);
            if(!bleedEvent.isCancelled() && target instanceof Player){
              target.sendMessage(Methods.color((Player) target, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.Puncture.Hit")));
            }
          }
          
          if(arrow.hasMetadata("TippedArrows")){
            String effect = arrow.getMetadata("TippedArrows").get(0).asString();
            String[] data = effect.split(":");
            PotionEffect potionEffect = new PotionEffect(PotionEffectType.getByName(data[0]), Integer.parseInt(data[2]) * 20, Integer.parseInt(data[1]) - 1);
            target.addPotionEffect(potionEffect);
            if(target instanceof Player){
              target.sendMessage(Methods.color((Player) target, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.TippedArrows.Hit")));
            }
          }
          
          if(arrow.hasMetadata("Combo1")){
            //Kinda unneeded but better safe than sorry ig?
            UUID shooterUUID = UUID.fromString(arrow.getMetadata("Combo1").get(0).asString());
            long lastShotTime = 0;
            int lengthBetweenShots = arrow.getMetadata("Combo3").get(0).asInt();
            double dmgMultiplier = arrow.getMetadata("Combo2").get(0).asDouble();
            Calendar cal = Calendar.getInstance();
            if(shooter.getUniqueId().equals(shooterUUID)){
              //Update the last shot time to the most recent one
              if(target.hasMetadata("LastShotTime")){
                lastShotTime = target.getMetadata("LastShotTime").get(0).asLong();
              }
              if(target.hasMetadata("TaggedBy")){
                UUID taggedByUUID = UUID.fromString(target.getMetadata("TaggedBy").get(0).asString());
                if(taggedByUUID.equals(shooterUUID)){
                  //This shouldnt happen but lets check it just in case we need to set the time for the next time
                  if(lastShotTime == 0){
                    Methods.setMetadata(target, "LastShotTime", cal.getTimeInMillis());
                  }
                  //If it is within the time frame
                  else if(cal.getTimeInMillis() - lastShotTime <= lengthBetweenShots * 1000){
                    //do dmg buff
                    e.setDamage(e.getDamage() * dmgMultiplier);
                    if(target instanceof Player){
                      target.sendMessage(Methods.color((Player) target, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.Combo.Hit")));
                    }
                    //if for some reason i didnt catch this remove it
                    if(shooter.hasMetadata("ComboCooldown")){
                      shooter.removeMetadata("ComboCooldown", McRPG.getInstance());
                    }
                    //add the new value to ComboCooldown
                    cal.add(Calendar.SECOND, arrow.getMetadata("Combo4").get(0).asInt());
                    Methods.setMetadata(shooter, "ComboCooldown", cal.getTimeInMillis());
                  }
                  target.removeMetadata("LastShotTime", McRPG.getInstance());
                  Methods.setMetadata(target, "LastShotTime", cal.getTimeInMillis());
                }
                //if they have been shot by someone other than the previous shooter
                else{
                  //If there is no stored time we need to set it
                  if(lastShotTime == 0){
                    Methods.setMetadata(target, "LastShotTime", cal.getTimeInMillis());
                  }
                  //otherwise we need to reset the time since they've been shot again
                  else{
                    target.removeMetadata("LastShotTime", McRPG.getInstance());
                    Methods.setMetadata(target, "LastShotTime", cal.getTimeInMillis());
                  }
                  //Remove the old shooter and set the new one
                  target.removeMetadata("TaggedBy", McRPG.getInstance());
                  Methods.setMetadata(target, "TaggedBy", shooterUUID.toString());
                }
              }
              //If they arent already tagged by smt
              else{
                Methods.setMetadata(target, "TaggedBy", shooterUUID.toString());
                Methods.setMetadata(target, "LastShotTime", cal.getTimeInMillis());
              }
            }
          }
        }
        //Deal with player specific archery abilities.
        if(e.getEntity() instanceof Player){
          Player target = (Player) e.getEntity();
          if(arrow.hasMetadata("DazeN")){
            int nauseaDuration = arrow.getMetadata("DazeN").get(0).asInt();
            int blindnessDuration = arrow.getMetadata("DazeB").get(0).asInt();
            boolean forcePlayerLookup = arrow.getMetadata("DazeF").get(0).asBoolean();
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindnessDuration * 20, 0));
            target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, nauseaDuration * 20, 0));
            if(forcePlayerLookup){
              Location l = target.getLocation();
              Random rand = new Random();
              //pick a bound between 0-180 and subtract off 90 to give us a range of 90 to -90
              l.setPitch(90 - rand.nextInt(181));
              target.teleport(l);
              target.sendMessage(Methods.color(target, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.Daze.Hit")));
            }
          }
        }
        else{
          return;
        }
      }
    }
    else{
      return;
    }
  }
  
  private enum Debuffs{
    BLINDNESS(PotionEffectType.BLINDNESS),
    WEAKNESS(PotionEffectType.WEAKNESS),
    SLOWNESS(PotionEffectType.SLOW),
    MINING_FATIGUE(PotionEffectType.SLOW_DIGGING),
    HUNGER(PotionEffectType.HUNGER),
    WITHER(PotionEffectType.WITHER),
    POISON(PotionEffectType.POISON);
    
    @Getter
    private PotionEffectType effectType;
    
    Debuffs(PotionEffectType effectType){
      this.effectType = effectType;
    }
    
    public static boolean isDebuff(PotionEffectType test){
      for(Debuffs debuff : values()){
        if(debuff.getEffectType().equals(test)){
          return true;
        }
      }
      return false;
    }
  }
}
