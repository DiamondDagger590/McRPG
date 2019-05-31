package us.eunoians.mcrpg.events.vanilla;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.fitness.*;
import us.eunoians.mcrpg.abilities.swords.Bleed;
import us.eunoians.mcrpg.abilities.swords.SerratedStrikes;
import us.eunoians.mcrpg.abilities.swords.TaintedBlade;
import us.eunoians.mcrpg.abilities.unarmed.*;
import us.eunoians.mcrpg.api.events.mcrpg.fitness.*;
import us.eunoians.mcrpg.api.events.mcrpg.swords.BleedEvent;
import us.eunoians.mcrpg.api.events.mcrpg.swords.SerratedStrikesEvent;
import us.eunoians.mcrpg.api.events.mcrpg.swords.TaintedBladeEvent;
import us.eunoians.mcrpg.api.events.mcrpg.unarmed.*;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.players.PlayerReadyBit;
import us.eunoians.mcrpg.skills.Skill;
import us.eunoians.mcrpg.types.DefaultAbilities;
import us.eunoians.mcrpg.types.GainReason;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.Parser;
import us.eunoians.mcrpg.util.mcmmo.MobHealthbarUtils;
import us.eunoians.mcrpg.util.worldguard.ActionLimiterParser;
import us.eunoians.mcrpg.util.worldguard.WGRegion;
import us.eunoians.mcrpg.util.worldguard.WGSupportManager;

import java.util.*;

public class VanillaDamageEvent implements Listener {

  public static void handleHealthbars(Entity attacker, LivingEntity target, double damage) {
    if(!(attacker instanceof Player)) {
      return;
    }

    Player player = (Player) attacker;

    if(isNPCEntity(player) || isNPCEntity(target)) {
      return;
    }


    MobHealthbarUtils.handleMobHealthbars(player, target, damage);
  }

  /**
   * This code is not mine. It is copyright from the original mcMMO allowed for use by their license.
   * Modified by  * This code has been modified from it source material
   * It was released under the GPLv3 license
   */
  /**
   * Checks to see if an entity is currently invincible.
   *
   * @param entity      The {@link LivingEntity} to check
   * @param eventDamage The damage from the event the entity is involved in
   * @return true if the entity is invincible, false otherwise
   */
  public static boolean isInvincible(LivingEntity entity, double eventDamage) {
    /*
     * So apparently if you do more damage to a LivingEntity than its last damage int you bypass the invincibility.
     * So yeah, this is for that.
     */
    return (entity.getNoDamageTicks() > entity.getMaximumNoDamageTicks() / 2.0F) && (eventDamage <= entity.getLastDamage());
  }
  //End mcMMO code

  private static boolean isNPCEntity(Entity entity) {
    return (entity == null || entity.hasMetadata("NPC") || entity instanceof NPC || entity.getClass().getName().equalsIgnoreCase("cofh.entity.PlayerFake"));
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void fallListener(EntityDamageEvent e) {
    FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.FITNESS_CONFIG);
    if(e.isCancelled() || !Skills.FITNESS.isEnabled()) {
      return;
    }
    else {
      if(e.getEntity() instanceof Player) {
        Player player = (Player) e.getEntity();
        McRPGPlayer mcRPGPlayer = PlayerManager.getPlayer(player.getUniqueId());
        if(McRPG.getInstance().isWorldGuardEnabled()) {
          WGSupportManager wgSupportManager = McRPG.getInstance().getWgSupportManager();

          if(wgSupportManager.isWorldTracker(player.getWorld())) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            Location loc = player.getLocation();
            RegionManager manager = container.get(BukkitAdapter.adapt(loc.getWorld()));
            HashMap<String, WGRegion> regions = wgSupportManager.getRegionManager().get(loc.getWorld());
            assert manager != null;
            ApplicableRegionSet set = manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc));
            for(ProtectedRegion region : set) {
              if(regions.containsKey(region.getId()) && regions.get(region.getId()).getAttackExpressions().containsKey(e.getEntity().getType())) {
                List<String> expressions = regions.get(region.getId()).getAttackExpressions().get(e.getEntity().getType());
                for(String s : expressions) {
                  if(s.contains("difference")) {
                    if(!(e.getEntity() instanceof Player)) {
                      continue;
                    }
                    else {
                      ActionLimiterParser actionLimiterParser = new ActionLimiterParser(s, mcRPGPlayer, PlayerManager.getPlayer(e.getEntity().getUniqueId()));
                      if(actionLimiterParser.evaluateExpression()) {
                        e.setCancelled(true);
                        return;
                      }
                    }
                  }
                  else {
                    ActionLimiterParser actionLimiterParser = new ActionLimiterParser(s, mcRPGPlayer);
                    if(actionLimiterParser.evaluateExpression()) {
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
        if(e.getCause() == EntityDamageEvent.DamageCause.FALL) {
          if(mcRPGPlayer.getLastFallLocation() == null) {
            mcRPGPlayer.setLastFallLocation(player.getLocation());
          }
          else {
            Location oldLoc = mcRPGPlayer.getLastFallLocation();
            Location currentLocation = player.getLocation();
            int diffInX = Math.abs(oldLoc.getBlockX() - currentLocation.getBlockX());
            int diffInY = Math.abs(oldLoc.getBlockY() - currentLocation.getBlockY());
            int diffInZ = Math.abs(oldLoc.getBlockZ() - currentLocation.getBlockZ());
            if(diffInX <= config.getInt("AntiAFK.XRange")) {
              afk = true;
            }
            else if(diffInY <= config.getInt("AntiAFK.YRange")) {
              afk = true;
            }
            else if(diffInZ <= config.getInt("AntiAFK.ZRange")) {
              afk = true;
            }
          }
          if(!afk) {
            expAwarded = config.getInt("ExpAwardedPerDamage.FALL_DAMAGE");
            Parser equation = new Parser(config.getString("FallEquation"));
            equation.setVariable("damage", e.getDamage());
            equation.setVariable("exp_awarded", expAwarded);
            equation.setVariable("feather_falling_level", featherFallingLevel);
            expAwarded = (int) equation.getValue();
          }
          else expAwarded = 0;

          Roll roll = (Roll) mcRPGPlayer.getBaseAbility(DefaultAbilities.ROLL);
          if(roll.getGenericAbility().isEnabled() && roll.isToggled()) {
            Parser rollEquation = new Parser(config.getString("RollConfig.RollChanceEquation"));
            rollEquation.setVariable("fitness_level", mcRPGPlayer.getSkill(Skills.FITNESS).getCurrentLevel());
            int chance = (int) rollEquation.getValue() * 1000;
            Random rand = new Random();
            int val = rand.nextInt(100000);
            if(chance >= val) {
              RollEvent rollEvent = new RollEvent(mcRPGPlayer, roll);
              Bukkit.getPluginManager().callEvent(rollEvent);
              if(!rollEvent.isCancelled()) {
                e.setDamage(e.getDamage() / 2);
                player.sendMessage(Methods.color(player, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.Roll.Activated")));
              }
            }
          }
          mcRPGPlayer.giveExp(Skills.FITNESS, expAwarded, GainReason.DAMAGE);
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void fitnessEvent(EntityDamageByEntityEvent e) {
    if(e.isCancelled()){
      return;
    }
    FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.FITNESS_CONFIG);
    if(e.getEntity() instanceof Player) {
      McRPGPlayer mcRPGPlayer = PlayerManager.getPlayer(e.getEntity().getUniqueId());
      //Deal with Divine Escape debuff
      if(mcRPGPlayer.getDivineEscapeDamageDebuff() > 0){
        double debuff = mcRPGPlayer.getDivineEscapeDamageDebuff()/100 + 1;
        e.setDamage(e.getDamage() * debuff);
      }
      if(!Skills.FITNESS.isEnabled()){
        return;
      }
      if(e.getDamager() instanceof LivingEntity) {
        LivingEntity attacker = (LivingEntity) e.getDamager();
        Material weaponType = attacker.getEquipment().getItemInMainHand().getType();
        if(weaponType.toString().contains("SWORD") || weaponType.toString().contains("AXE") || weaponType.toString().contains("TRIDENT")) {
          if(UnlockedAbilities.THICK_SKIN.isEnabled() && mcRPGPlayer.getAbilityLoadout().contains(UnlockedAbilities.THICK_SKIN)
                  && mcRPGPlayer.getBaseAbility(UnlockedAbilities.THICK_SKIN).isToggled()) {
            ThickSkin thickSkin = (ThickSkin) mcRPGPlayer.getBaseAbility(UnlockedAbilities.THICK_SKIN);
            double damageDecrease = config.getDouble("ThickSkinConfig.Tier" + Methods.convertToNumeral(thickSkin.getCurrentTier())
                    + ".DamageDecrease");
            ThickSkinEvent thickSkinEvent = new ThickSkinEvent(mcRPGPlayer, thickSkin, damageDecrease, (LivingEntity) e.getDamager());
            Bukkit.getPluginManager().callEvent(thickSkinEvent);
            if(!thickSkinEvent.isCancelled()) {
              e.setDamage(e.getDamage() * ((100 - damageDecrease) / 100));
            }
          }
          if(attacker instanceof Player && UnlockedAbilities.IRON_MUSCLES.isEnabled() && mcRPGPlayer.getAbilityLoadout().contains(UnlockedAbilities.IRON_MUSCLES)
                  && mcRPGPlayer.getBaseAbility(UnlockedAbilities.IRON_MUSCLES).isToggled()) {
            IronMuscles ironMuscles = (IronMuscles) mcRPGPlayer.getBaseAbility(UnlockedAbilities.IRON_MUSCLES);
            double activationChance = config.getDouble("IronMusclesConfig.Tier" + Methods.convertToNumeral(ironMuscles.getCurrentTier())
                    + ".ActivationChance");
            int chance = (int) activationChance * 1000;
            Random rand = new Random();
            int val = rand.nextInt(100000);
            if(chance >= val) {
              int weaponDamage = config.getInt("IronMusclesConfig.Tier" + Methods.convertToNumeral(ironMuscles.getCurrentTier()) +
                      ".WeaponDamage");
              IronMusclesEvent ironMusclesEvent = new IronMusclesEvent(mcRPGPlayer, ironMuscles, weaponDamage, (Player) attacker);
              Bukkit.getPluginManager().callEvent(ironMusclesEvent);
              if(!ironMusclesEvent.isCancelled()) {
                attacker.getEquipment().getItemInMainHand().setDurability((short) (attacker.getEquipment().getItemInMainHand().getDurability() + ironMusclesEvent.getDurabilityLoss()));
              }
            }
          }
        }
        if(UnlockedAbilities.DODGE.isEnabled() && mcRPGPlayer.getAbilityLoadout().contains(UnlockedAbilities.DODGE) &&
                mcRPGPlayer.getBaseAbility(UnlockedAbilities.DODGE).isToggled()) {
          Dodge dodge = (Dodge) mcRPGPlayer.getBaseAbility(UnlockedAbilities.DODGE);
          double activationChance = config.getDouble("DodgeConfig.Tier" + Methods.convertToNumeral(dodge.getCurrentTier())
                  + ".ActivationChance");
          int chance = (int) activationChance * 1000;
          Random rand = new Random();
          int val = rand.nextInt(100000);
          if(chance >= val) {
            double damageReduction = config.getDouble("DodgeConfig.Tier" + Methods.convertToNumeral(dodge.getCurrentTier()) +
                    ".DamageReduction");
            DodgeEvent dodgeEvent = new DodgeEvent(mcRPGPlayer, dodge, attacker, damageReduction);
            Bukkit.getPluginManager().callEvent(dodgeEvent);
            if(!dodgeEvent.isCancelled()) {
              e.setDamage(e.getDamage() * ((100 - damageReduction) / 100));
              mcRPGPlayer.getPlayer().sendMessage(Methods.color(mcRPGPlayer.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.Dodge.Activated")));
            }
          }
        }
      }
      else if(e.getDamager() instanceof Projectile) {
        if(UnlockedAbilities.BULLET_PROOF.isEnabled() && mcRPGPlayer.getAbilityLoadout().contains(UnlockedAbilities.BULLET_PROOF)
                && mcRPGPlayer.getBaseAbility(UnlockedAbilities.BULLET_PROOF).isToggled()) {
          BulletProof bulletProof = (BulletProof) mcRPGPlayer.getBaseAbility(UnlockedAbilities.BULLET_PROOF);
          double activationChance = config.getDouble("BulletProofConfig.Tier" + Methods.convertToNumeral(bulletProof.getCurrentTier())
                  + ".ActivationChance");
          int chance = (int) activationChance * 1000;
          Random rand = new Random();
          int val = rand.nextInt(100000);
          if(chance >= val) {
            BulletProofEvent bulletProofEvent = new BulletProofEvent(mcRPGPlayer, bulletProof, (Projectile) e.getDamager());
            Bukkit.getPluginManager().callEvent(bulletProofEvent);
            if(!bulletProofEvent.isCancelled()) {
              e.setCancelled(true);
              mcRPGPlayer.getPlayer().sendMessage(Methods.color(mcRPGPlayer.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.BulletProof.Activated")));
            }
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void awardFitnessExp(EntityDamageByEntityEvent e) {
    if(!e.isCancelled() && Skills.FITNESS.isEnabled() && e.getEntity() instanceof Player && e.getDamager() instanceof LivingEntity){
      McRPGPlayer mp = PlayerManager.getPlayer(e.getEntity().getUniqueId());
      double damage = e.getDamage();
      int expAwarded = (int) (damage * McRPG.getInstance().getFileManager().getFile(FileManager.Files.FITNESS_CONFIG).getInt("ExpAwardedPerDamage.ENTITY_DAMAGE"));
      mp.giveExp(Skills.FITNESS, expAwarded, GainReason.DAMAGE);
    }
  }

  /**
   * This code is not mine. It is copyright from the original mcMMO allowed for use by their license.
   * This code has been modified from it source material
   * It was released under the GPLv3 license
   */
  @EventHandler(priority = EventPriority.HIGH)
  public void damageEvent(EntityDamageByEntityEvent e) {
    if(e.isCancelled()) {
      return;
    }
    FileConfiguration config;
    if(e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity) {
      Player damager = (Player) e.getDamager();
      if(e.getEntity().getUniqueId() == e.getDamager().getUniqueId()) {
        return;
      }
      McRPGPlayer mp = PlayerManager.getPlayer(damager.getUniqueId());
      //Deal with world guard
      if(McRPG.getInstance().isWorldGuardEnabled()) {
        WGSupportManager wgSupportManager = McRPG.getInstance().getWgSupportManager();

        if(wgSupportManager.isWorldTracker(damager.getWorld())) {
          RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
          Location loc = damager.getLocation();
          RegionManager manager = container.get(BukkitAdapter.adapt(loc.getWorld()));
          HashMap<String, WGRegion> regions = wgSupportManager.getRegionManager().get(loc.getWorld());
          assert manager != null;
          ApplicableRegionSet set = manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc));
          for(ProtectedRegion region : set) {
            if(regions.containsKey(region.getId()) && regions.get(region.getId()).getAttackExpressions().containsKey(e.getEntity().getType())) {
              List<String> expressions = regions.get(region.getId()).getAttackExpressions().get(e.getEntity().getType());
              for(String s : expressions) {
                if(s.contains("difference")) {
                  if(!(e.getEntity() instanceof Player)) {
                    continue;
                  }
                  else {
                    ActionLimiterParser actionLimiterParser = new ActionLimiterParser(s, mp, PlayerManager.getPlayer(e.getEntity().getUniqueId()));
                    if(actionLimiterParser.evaluateExpression()) {
                      e.setCancelled(true);
                      return;
                    }
                  }
                }
                else {
                  ActionLimiterParser actionLimiterParser = new ActionLimiterParser(s, mp);
                  if(actionLimiterParser.evaluateExpression()) {
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
      if(damager.getItemInHand() == null || damager.getItemInHand().getType() == Material.AIR) {
        if(!Skills.UNARMED.isEnabled()) {
          return;
        }
        config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.UNARMED_CONFIG);
        e.setDamage(e.getDamage() + config.getInt("BonusDamage"));
        //Give exp
        int baseExp = 0;
        if(!config.contains("ExpAwardedPerMob." + e.getEntity().toString())) {
          baseExp = config.getInt("ExpAwardedPerMob.OTHER");
        }
        else {
          baseExp = config.getInt("ExpAwardedPerMob." + e.getEntity().toString());
        }
        double dmg = e.getDamage();
        double mobSpawnValue = 1.0;
        if(e.getEntity().hasMetadata("ExpModifier")) {
          mobSpawnValue = e.getEntity().getMetadata("ExpModifier").get(0).asDouble();
        }
        int expAwarded = (int) ((dmg * baseExp) * mobSpawnValue);
        mp.getSkill(Skills.UNARMED).giveExp(mp, expAwarded, GainReason.DAMAGE);
        if(mp.isCanSmite()) {
          if(!(e.getEntity().getFireTicks() > 0)) {
            LivingEntity entity = (LivingEntity) e.getEntity();
            int chance = (int) mp.getSmitingFistData().getSmiteChance() * 1000;
            Random rand = new Random();
            int val = rand.nextInt(100000);
            if(chance >= val) {
              if(entity.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                entity.removePotionEffect(PotionEffectType.INVISIBILITY);
              }
              entity.setFireTicks(mp.getSmitingFistData().getSmiteDuration() * 20);
              if(entity instanceof Player) {
                mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                        McRPG.getInstance().getLangFile().getString("Messages.Abilities.SmitingFist.Smited").replace("%Player%", entity.getName())));
              }
              entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 5, 1);
            }
          }
        }
        else if(mp.isCanDenseImpact()) {
          if(e.getEntity() instanceof Player) {
            Player damaged = (Player) e.getEntity();
            for(ItemStack armour : damaged.getInventory().getArmorContents()) {
              armour.setDurability((short) (armour.getDurability() + mp.getArmourDmg()));
            }
          }
        }
        if(mp.isReadying()) {
          if(mp.getReadyingAbilityBit() == null) {
            mp.setReadying(false);
            return;
          }
          PlayerReadyBit playerReadyBit = mp.getReadyingAbilityBit();
          if(playerReadyBit.getAbilityReady().equals(UnlockedAbilities.BERSERK)) {
            if(UnlockedAbilities.BERSERK.isEnabled() && mp.getBaseAbility(UnlockedAbilities.BERSERK).isToggled()) {

              Berserk berserk = (Berserk) mp.getBaseAbility(UnlockedAbilities.BERSERK);
              double bonusChance = config.getDouble("BerserkConfig.Tier" + Methods.convertToNumeral(berserk.getCurrentTier()) + ".ActivationBoost");
              int bonusDmg = config.getInt("BerserkConfig.Tier" + Methods.convertToNumeral(berserk.getCurrentTier()) + ".DamageBoost");
              BerserkEvent event = new BerserkEvent(mp, (Berserk) mp.getBaseAbility(UnlockedAbilities.BERSERK), bonusChance, bonusDmg);
              Bukkit.getPluginManager().callEvent(event);
              if(!event.isCancelled()) {
                mp.getActiveAbilities().add(UnlockedAbilities.BERSERK);
                //cancel the readying task and null the bit
                Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
                mp.setReadyingAbilityBit(null);
                mp.setReadying(false);
                //get the bleed ability and set the bonus chance
                Disarm disarm = (Disarm) mp.getSkill(Skills.UNARMED).getAbility(UnlockedAbilities.DISARM);
                disarm.setBonusChance(event.getBonusChance());
                e.setDamage(e.getDamage() + event.getBonusDamage());
                //Tell player ability started
                mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                        McRPG.getInstance().getLangFile().getString("Messages.Abilities.Berserk.Activated")));
                mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 5, 1);
                new BukkitRunnable() {
                  @Override
                  public void run() {
                    //Undo all the things that berserk did and set it on cooldown
                    disarm.setBonusChance(0);
                    if(mp.getPlayer().isOnline()) {
                      mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                              McRPG.getInstance().getLangFile().getString("Messages.Abilities.Berserk.Deactivated")));
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.SECOND,
                            McRPG.getInstance().getFileManager().getFile(FileManager.Files.UNARMED_CONFIG).getInt("BerserkConfig.Tier" + Methods.convertToNumeral(berserk.getCurrentTier()) + ".Cooldown"));
                    mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.ENTITY_IRON_GOLEM_DEATH, 5, 1);
                    mp.getActiveAbilities().add(UnlockedAbilities.BERSERK);
                    mp.addAbilityOnCooldown(UnlockedAbilities.BERSERK, cal.getTimeInMillis());
                  }
                }.runTaskLater(McRPG.getInstance(), config.getInt("BerserkConfig.Tier" + Methods.convertToNumeral(berserk.getCurrentTier()) + ".Duration") * 20);
              }
            }
          }
          else if(playerReadyBit.getAbilityReady().equals(UnlockedAbilities.SMITING_FIST)) {
            if(UnlockedAbilities.SMITING_FIST.isEnabled() && mp.getBaseAbility(UnlockedAbilities.SMITING_FIST).isToggled()) {
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
              if(!smitingFistEvent.isCancelled()) {
                mp.getActiveAbilities().add(UnlockedAbilities.SMITING_FIST);
                mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                        McRPG.getInstance().getLangFile().getString("Messages.Abilities.SmitingFist.Activated")));
                mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 5, 1);
                mp.setSmitingFistData(smitingFistEvent);
                mp.setCanSmite(true);
                if(smitingFistEvent.isRemoveDebuffs()) {
                  for(PotionEffect effectType : damager.getActivePotionEffects()) {
                    if(Debuffs.isDebuff(effectType.getType())) {
                      damager.removePotionEffect(effectType.getType());
                    }
                  }
                }
                damager.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, smitingFistEvent.getDuration() * 20, smitingFistEvent.getAbsorptionLevel()));
                new BukkitRunnable() {
                  @Override
                  public void run() {
                    //Undo all the things that smiting fist did and set it on cooldown
                    mp.setCanSmite(false);
                    mp.setSmitingFistData(null);
                    if(mp.getPlayer().isOnline()) {
                      mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                              McRPG.getInstance().getLangFile().getString("Messages.Abilities.SmitingFist.Deactivated")));
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.SECOND,
                            smitingFistEvent.getCooldown());
                    mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.ENTITY_ILLUSIONER_DEATH, 5, 1);
                    mp.getActiveAbilities().remove(UnlockedAbilities.SMITING_FIST);
                    mp.addAbilityOnCooldown(UnlockedAbilities.SMITING_FIST, cal.getTimeInMillis());
                  }
                }.runTaskLater(McRPG.getInstance(), smitingFistEvent.getDuration() * 20);
              }
            }
          }
          else if(playerReadyBit.getAbilityReady().equals(UnlockedAbilities.DENSE_IMPACT)) {
            if(UnlockedAbilities.DENSE_IMPACT.isEnabled() && mp.getBaseAbility(UnlockedAbilities.DENSE_IMPACT).isToggled()) {
              DenseImpact denseImpact = (DenseImpact) mp.getBaseAbility(UnlockedAbilities.DENSE_IMPACT);
              int cooldown = config.getInt("DenseImpactConfig.Tier" + Methods.convertToNumeral(denseImpact.getCurrentTier()) + ".Cooldown");
              int duration = config.getInt("DenseImpactConfig.Tier" + Methods.convertToNumeral(denseImpact.getCurrentTier()) + ".Duration");
              int armourDmg = config.getInt("DenseImpactConfig.Tier" + Methods.convertToNumeral(denseImpact.getCurrentTier()) + ".ArmorDamage");
              mp.setReadying(false);
              Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
              mp.setReadyingAbilityBit(null);
              DenseImpactEvent denseImpactEvent = new DenseImpactEvent(mp, denseImpact, armourDmg);
              Bukkit.getPluginManager().callEvent(denseImpactEvent);
              if(!denseImpactEvent.isCancelled()) {
                mp.getActiveAbilities().add(UnlockedAbilities.DENSE_IMPACT);
                mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                        McRPG.getInstance().getLangFile().getString("Messages.Abilities.DenseImpact.Activated")));
                mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 5, 1);
                mp.setCanDenseImpact(true);
                mp.setArmourDmg(denseImpactEvent.getArmourDmg());
                new BukkitRunnable() {
                  @Override
                  public void run() {
                    //Undo all the things that dense impact did and set it on cooldown
                    mp.setCanDenseImpact(false);
                    mp.setArmourDmg(0);
                    if(mp.getPlayer().isOnline()) {
                      mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                              McRPG.getInstance().getLangFile().getString("Messages.Abilities.DenseImpact.Deactivated")));
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.SECOND,
                            cooldown);
                    mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.ENTITY_IRON_GOLEM_DEATH, 5, 1);
                    mp.getActiveAbilities().remove(UnlockedAbilities.DENSE_IMPACT);
                    mp.addAbilityOnCooldown(UnlockedAbilities.DENSE_IMPACT, cal.getTimeInMillis());
                  }
                }.runTaskLater(McRPG.getInstance(), duration * 20);
              }
            }
          }
        }
        //Manage disarm
        if(e.getEntity() instanceof Player && UnlockedAbilities.DISARM.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.DISARM) && mp.getBaseAbility(UnlockedAbilities.DISARM).isToggled()) {
          Disarm disarm = (Disarm) mp.getBaseAbility(UnlockedAbilities.DISARM);
          Player damagedPlayer = (Player) e.getEntity();
          McRPGPlayer damagedMcRPGPlayer = PlayerManager.getPlayer(damagedPlayer.getUniqueId());
          if(damagedPlayer.getItemInHand() != null || damagedPlayer.getItemInHand().getType() != Material.AIR) {
            double disarmChance = config.getDouble("DisarmConfig.Tier" + Methods.convertToNumeral(disarm.getCurrentTier()) + ".ActivationChance") + disarm.getBonusChance();
            int chance = (int) disarmChance * 1000;
            Random rand = new Random();
            int val = rand.nextInt(100000);
            if(chance >= val) {
              DisarmEvent disarmEvent = new DisarmEvent(mp, damagedMcRPGPlayer, disarm, damagedPlayer.getItemInHand());
              Bukkit.getPluginManager().callEvent(disarmEvent);
              if(!disarmEvent.isCancelled()) {
                int slot = -1;
                for(int i = 9; i < 36; i++) {
                  ItemStack item = damagedPlayer.getInventory().getItem(i);
                  if(item == null || item.getType() == Material.AIR) {
                    slot = i;
                    break;
                  }
                }
                int heldSlot = damagedPlayer.getInventory().getHeldItemSlot();
                if(damagedPlayer.getInventory().getItem(heldSlot) == null) {
                  return;
                }
                if(slot == -1) {
                  damagedPlayer.getLocation().getWorld().dropItemNaturally(damagedPlayer.getLocation(), disarmEvent.getItemToDisarm());
                }
                else {
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
        if(UnlockedAbilities.IRON_ARM.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.IRON_ARM) && mp.getBaseAbility(UnlockedAbilities.IRON_ARM).isToggled()) {
          IronArm ironArm = (IronArm) mp.getBaseAbility(UnlockedAbilities.IRON_ARM);
          int chance = (int) config.getDouble("IronArmConfig.Tier" + Methods.convertToNumeral(ironArm.getCurrentTier()) + ".ActivationChance") * 1000;
          int bonusDmg = config.getInt("IronArmConfig.Tier" + Methods.convertToNumeral(ironArm.getCurrentTier()) + ".DamageBoost");
          Random rand = new Random();
          int val = rand.nextInt(100000);
          if(chance >= val) {
            IronArmEvent ironArmEvent = new IronArmEvent(mp, ironArm, bonusDmg);
            Bukkit.getPluginManager().callEvent(ironArmEvent);
            if(!ironArmEvent.isCancelled()) {
              e.setDamage(e.getDamage() + ironArmEvent.getBonusDamage());
            }
          }
        }
      }
      else {
        Material weapon = damager.getItemInHand().getType();
        if(weapon.name().contains("SWORD")) {
          Skill playersSkill = mp.getSkill(Skills.SWORDS);
          if(!Skills.SWORDS.isEnabled()) {
            return;
          }
          //If the player is readying for an ability
          if(mp.isReadying()) {
            //If we need to use serrated strikes (We can preemptively assume that its enabled as we have checked earlier on in the code)
            if(mp.getReadyingAbilityBit().getAbilityReady().getName().equalsIgnoreCase(UnlockedAbilities.SERRATED_STRIKES.getName())) {
              //call api event
              SerratedStrikesEvent event = new SerratedStrikesEvent(mp, (SerratedStrikes) mp.getBaseAbility(UnlockedAbilities.SERRATED_STRIKES));
              Bukkit.getPluginManager().callEvent(event);
              if(!event.isCancelled()) {
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
                new BukkitRunnable() {
                  @Override
                  public void run() {
                    //Undo all the things that serrated strikes did and set it on cooldown
                    bleed.setBonusChance(0);
                    if(mp.getPlayer().isOnline()) {
                      mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                              McRPG.getInstance().getLangFile().getString("Messages.Abilities.SerratedStrikes.Deactivated")));
                    }
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.SECOND,
                            event.getCooldown());
                    mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.ENTITY_SHULKER_HURT, 5, 1);
                    mp.getActiveAbilities().remove(UnlockedAbilities.SERRATED_STRIKES);
                    mp.addAbilityOnCooldown(UnlockedAbilities.SERRATED_STRIKES, cal.getTimeInMillis());
                  }
                }.runTaskLater(McRPG.getInstance(), event.getDuration() * 20);
              }
            }
            //If we need to use tainted blade
            else if(mp.getReadyingAbilityBit().getAbilityReady().getName().equalsIgnoreCase(UnlockedAbilities.TAINTED_BLADE.getName())) {
              TaintedBladeEvent event = new TaintedBladeEvent(mp, (TaintedBlade) mp.getBaseAbility(UnlockedAbilities.TAINTED_BLADE));
              Bukkit.getPluginManager().callEvent(event);
              if(!event.isCancelled()) {
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
                p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_SKELETON_HURT, 5, 1);
                mp.getActiveAbilities().remove(UnlockedAbilities.TAINTED_BLADE);
                mp.addAbilityOnCooldown(UnlockedAbilities.TAINTED_BLADE, cal.getTimeInMillis());
              }
            }
          }
          if(Skills.SWORDS.getEnabledAbilities().contains("Bleed")) {
            if(playersSkill.getAbility(DefaultAbilities.BLEED).isToggled()) {
              Bleed bleed = (Bleed) playersSkill.getAbility(DefaultAbilities.BLEED);
              Parser parser = DefaultAbilities.BLEED.getActivationEquation();
              if(e.getEntity() instanceof Player) {
                Player damagedPlayer = (Player) e.getEntity();
                McRPGPlayer dmged = PlayerManager.getPlayer(damagedPlayer.getUniqueId());
                if(!dmged.isHasBleedImmunity() && bleed.canTarget()) {
                  parser.setVariable("swords_level", playersSkill.getCurrentLevel());
                  parser.setVariable("power_level", mp.getPowerLevel());
                  int chance = (int) parser.getValue() * 1000;
                  Random rand = new Random();
                  int val = rand.nextInt(100000);
                  if(chance >= val) {
                    BleedEvent event = new BleedEvent(mp, (Player) e.getEntity(), bleed);
                    Bukkit.getPluginManager().callEvent(event);
                  }
                }
              }
              else {
                parser.setVariable("swords_level", playersSkill.getCurrentLevel());
                parser.setVariable("power_level", mp.getPowerLevel());
                Random rand = new Random();
                int chance = (int) (parser.getValue() + bleed.getBonusChance()) * 1000;
                int val = rand.nextInt(100000);
                if(chance >= val && e.getEntity() instanceof LivingEntity) {
                  BleedEvent event = new BleedEvent(mp, (LivingEntity) e.getEntity(), bleed);
                  Bukkit.getPluginManager().callEvent(event);
                }
              }
            }
          }
          config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG);
          double multiplier = config.getDouble("MaterialBonus." + weapon);
          int baseExp = 0;
          if(!config.contains("ExpAwardedPerMob." + e.getEntity().toString())) {
            baseExp = config.getInt("ExpAwardedPerMob.OTHER");
          }
          else {
            baseExp = config.getInt("ExpAwardedPerMob." + e.getEntity().toString());
          }
          double dmg = e.getDamage();
          double mobSpawnValue = 1.0;
          if(e.getEntity().hasMetadata("ExpModifier")) {
            mobSpawnValue = e.getEntity().getMetadata("ExpModifier").get(0).asDouble();
          }
          int expAwarded = (int) ((dmg * baseExp * multiplier) * mobSpawnValue);
          mp.getSkill(Skills.SWORDS).giveExp(mp, expAwarded, GainReason.DAMAGE);
        }
      }
      handleHealthbars(e.getDamager(), (LivingEntity) e.getEntity(), e.getFinalDamage());
    }
    else if(e.getEntity() instanceof LivingEntity && e.getDamager() instanceof Arrow && Skills.ARCHERY.isEnabled()) {
      Arrow arrow = (Arrow) e.getDamager();
      if(arrow.getShooter() instanceof Player) {
        Player shooter = (Player) arrow.getShooter();
        if(shooter.getUniqueId().equals(e.getEntity().getUniqueId())) {
          return;
        }
        McRPGPlayer mp = PlayerManager.getPlayer(shooter.getUniqueId());
        //give exp
        config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.ARCHERY_CONFIG);
        int baseExp = 0;
        if(!config.contains("ExpAwardedPerMob." + e.getEntity().toString())) {
          baseExp = config.getInt("ExpAwardedPerMob.OTHER");
        }
        else {
          baseExp = config.getInt("ExpAwardedPerMob." + e.getEntity().toString());
        }
        double dmg = e.getDamage();
        if(!arrow.hasMetadata("ShootLoc")){
          return;
        }
        Location loc = Methods.stringToLoc(arrow.getMetadata("ShootLoc").get(0).asString());
        Location hitLoc = e.getEntity().getLocation();
        double distance = loc.distance(hitLoc);
        if(distance > config.getInt("DistanceBonusCap")) {
          distance = config.getInt("DistanceBonusCap");
        }
        Parser parser = new Parser(config.getString("DistanceBonus"));
        parser.setVariable("block_distance", distance);
        double mobSpawnValue = 1.0;
        if(e.getEntity().hasMetadata("ExpModifier")) {
          mobSpawnValue = e.getEntity().getMetadata("ExpModifier").get(0).asDouble();
        }
        int expAwarded = (int) ((dmg * baseExp + (dmg * baseExp * parser.getValue())) * mobSpawnValue);
        mp.getSkill(Skills.ARCHERY).giveExp(mp, expAwarded, GainReason.DAMAGE);

        //Handle the hp bars when dealing with archery
        handleHealthbars(e.getDamager(), (LivingEntity) e.getEntity(), e.getFinalDamage());

        if(e.getEntity() instanceof LivingEntity) {
          LivingEntity target = (LivingEntity) e.getEntity();
          if(arrow.hasMetadata("Artemis")) {
            double dmgMultiplier = arrow.getMetadata("Artemis").get(0).asDouble();
            e.setDamage(e.getDamage() * dmgMultiplier);
            if(target instanceof Player) {
              target.sendMessage(Methods.color((Player) target, McRPG.getInstance().getPluginPrefix() +
                      McRPG.getInstance().getLangFile().getString("Messages.Abilities.BlessingOfArtemis.Hit")));
            }
          }

          else if(arrow.hasMetadata("Apollo")) {
            int fireDuration = arrow.getMetadata("Apollo").get(0).asInt();
            target.setFireTicks(fireDuration * 20);
            if(target instanceof Player) {
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

          else if(arrow.hasMetadata("Hades1")) {
            int witherDuration = arrow.getMetadata("Hades1").get(0).asInt();
            int witherLevel = arrow.getMetadata("Hades2").get(0).asInt();
            int slownessDuration = arrow.getMetadata("Hades3").get(0).asInt();
            int slownessLevel = arrow.getMetadata("Hades4").get(0).asInt();
            int blindnessDuration = arrow.getMetadata("Hades5").get(0).asInt();
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witherDuration * 20, witherLevel - 1));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slownessDuration * 20, slownessLevel - 1));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, blindnessDuration * 20, 0));

            if(target instanceof Player) {
              target.sendMessage(Methods.color((Player) target, McRPG.getInstance().getPluginPrefix() +
                      McRPG.getInstance().getLangFile().getString("Messages.Abilities.CurseOfHades.Hit")));
            }
          }

          if(arrow.hasMetadata("Puncture")) {
            McRPGPlayer s = PlayerManager.getPlayer(UUID.fromString(arrow.getMetadata("Puncture").get(0).asString()));
            BleedEvent bleedEvent = new BleedEvent(s, target, (Bleed) s.getBaseAbility(DefaultAbilities.BLEED));
            Bukkit.getPluginManager().callEvent(bleedEvent);
            if(!bleedEvent.isCancelled() && target instanceof Player) {
              target.sendMessage(Methods.color((Player) target, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.Puncture.Hit")));
            }
          }

          if(arrow.hasMetadata("TippedArrows")) {
            String effect = arrow.getMetadata("TippedArrows").get(0).asString();
            String[] data = effect.split(":");
            PotionEffect potionEffect = new PotionEffect(PotionEffectType.getByName(data[0]), Integer.parseInt(data[2]) * 20, Integer.parseInt(data[1]) - 1);
            target.addPotionEffect(potionEffect);
            if(target instanceof Player) {
              target.sendMessage(Methods.color((Player) target, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.TippedArrows.Hit")));
            }
          }

          if(arrow.hasMetadata("Combo1")) {
            //Kinda unneeded but better safe than sorry ig?
            UUID shooterUUID = UUID.fromString(arrow.getMetadata("Combo1").get(0).asString());
            long lastShotTime = 0;
            int lengthBetweenShots = arrow.getMetadata("Combo3").get(0).asInt();
            double dmgMultiplier = arrow.getMetadata("Combo2").get(0).asDouble();
            Calendar cal = Calendar.getInstance();
            if(shooter.getUniqueId().equals(shooterUUID)) {
              //Update the last shot time to the most recent one
              if(target.hasMetadata("LastShotTime")) {
                lastShotTime = target.getMetadata("LastShotTime").get(0).asLong();
              }
              if(target.hasMetadata("TaggedBy")) {
                UUID taggedByUUID = UUID.fromString(target.getMetadata("TaggedBy").get(0).asString());
                if(taggedByUUID.equals(shooterUUID)) {
                  //This shouldnt happen but lets check it just in case we need to set the time for the next time
                  if(lastShotTime == 0) {
                    Methods.setMetadata(target, "LastShotTime", cal.getTimeInMillis());
                  }
                  //If it is within the time frame
                  else if(cal.getTimeInMillis() - lastShotTime <= lengthBetweenShots * 1000) {
                    //do dmg buff
                    e.setDamage(e.getDamage() * dmgMultiplier);
                    if(target instanceof Player) {
                      target.sendMessage(Methods.color((Player) target, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.Combo.Hit")));
                    }
                    //if for some reason i didnt catch this remove it
                    if(shooter.hasMetadata("ComboCooldown")) {
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
                else {
                  //If there is no stored time we need to set it
                  if(lastShotTime == 0) {
                    Methods.setMetadata(target, "LastShotTime", cal.getTimeInMillis());
                  }
                  //otherwise we need to reset the time since they've been shot again
                  else {
                    target.removeMetadata("LastShotTime", McRPG.getInstance());
                    Methods.setMetadata(target, "LastShotTime", cal.getTimeInMillis());
                  }
                  //Remove the old shooter and set the new one
                  target.removeMetadata("TaggedBy", McRPG.getInstance());
                  Methods.setMetadata(target, "TaggedBy", shooterUUID.toString());
                }
              }
              //If they arent already tagged by smt
              else {
                Methods.setMetadata(target, "TaggedBy", shooterUUID.toString());
                Methods.setMetadata(target, "LastShotTime", cal.getTimeInMillis());
              }
            }
          }
        }
        //Deal with player specific archery abilities.
        if(e.getEntity() instanceof Player) {
          Player target = (Player) e.getEntity();
          if(arrow.hasMetadata("DazeN")) {
            int nauseaDuration = arrow.getMetadata("DazeN").get(0).asInt();
            int blindnessDuration = arrow.getMetadata("DazeB").get(0).asInt();
            boolean forcePlayerLookup = arrow.getMetadata("DazeF").get(0).asBoolean();
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindnessDuration * 20, 0));
            target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, nauseaDuration * 20, 0));
            if(forcePlayerLookup) {
              Location l = target.getLocation();
              Random rand = new Random();
              //pick a bound between 0-180 and subtract off 90 to give us a range of 90 to -90
              l.setPitch(90 - rand.nextInt(181));
              target.teleport(l);
              target.sendMessage(Methods.color(target, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.Daze.Hit")));
            }
          }
        }
        else {
          return;
        }
      }
    }
    else {
      return;
    }
  }

  private enum Debuffs {
    BLINDNESS(PotionEffectType.BLINDNESS),
    WEAKNESS(PotionEffectType.WEAKNESS),
    SLOWNESS(PotionEffectType.SLOW),
    MINING_FATIGUE(PotionEffectType.SLOW_DIGGING),
    HUNGER(PotionEffectType.HUNGER),
    WITHER(PotionEffectType.WITHER),
    POISON(PotionEffectType.POISON);

    @Getter
    private PotionEffectType effectType;

    Debuffs(PotionEffectType effectType) {
      this.effectType = effectType;
    }

    public static boolean isDebuff(PotionEffectType test) {
      for(Debuffs debuff : values()) {
        if(debuff.getEffectType().equals(test)) {
          return true;
        }
      }
      return false;
    }
  }
}
