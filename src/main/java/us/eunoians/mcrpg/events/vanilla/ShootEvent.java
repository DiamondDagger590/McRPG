package us.eunoians.mcrpg.events.vanilla;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.archery.*;
import us.eunoians.mcrpg.api.events.mcrpg.archery.*;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.DefaultAbilities;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.Parser;

import java.util.*;

public class ShootEvent implements Listener {

  @Getter
  private static HashMap<UUID, BukkitTask> arrowTasks = new HashMap<>();

  @EventHandler
  public static void shootEvent(EntityShootBowEvent e) {
    if(PlayerManager.isPlayerFrozen(e.getEntity().getUniqueId())){
      return;
    }
    //Disabled Worlds
    if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
         McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(e.getEntity().getWorld().getName())) {
      return;
    }
    if(e.getEntity() instanceof Player) {
      Player p = (Player) e.getEntity();
      if(e.getProjectile() instanceof Arrow) {
        McRPGPlayer mp;
        try{
          mp = PlayerManager.getPlayer(p.getUniqueId());
        }
        catch(McRPGPlayerNotFoundException exception){
          return;
        }
        Arrow arrow = (Arrow) e.getProjectile();
        if(!Skills.ARCHERY.isEnabled()){
          return;
        }
        //Use this to give the player exp when the arrow hits
        Methods.setMetadata(arrow, "ShootLoc", Methods.locToString(e.getEntity().getLocation()));
        Methods.setMetadata(arrow, "Shooter", e.getEntity().getUniqueId().toString());

        FileConfiguration archeryConfiguration = McRPG.getInstance().getFileManager().getFile(FileManager.Files.ARCHERY_CONFIG);
        //Deal with active abilities for archery
        if(mp.isReadying()) {
          //Deal with Blessing Of Artemis Ability
          //Set the metadata of the arrow to contain the multiplier of damage so it will be easier for us to deal with different contingencies
          if(mp.getReadyingAbilityBit().getAbilityReady() == UnlockedAbilities.BLESSING_OF_ARTEMIS) {
            String key = "BlessingOfArtemisConfig.Tier"
                    + Methods.convertToNumeral(mp.getBaseAbility(UnlockedAbilities.BLESSING_OF_ARTEMIS).getCurrentTier()) + ".";
            double damageMultiplier = archeryConfiguration.getDouble(key + "DamageMultiplier");
            int cooldown = archeryConfiguration.getInt(key + "Cooldown");
            int invisDuration = archeryConfiguration.getInt(key + "InvisDuration");
            BlessingOfArtemisEvent blessingOfArtemisEvent = new BlessingOfArtemisEvent(mp, (BlessingOfArtemis) mp.getBaseAbility(UnlockedAbilities.BLESSING_OF_ARTEMIS), cooldown, invisDuration, damageMultiplier);
            Bukkit.getPluginManager().callEvent(blessingOfArtemisEvent);
            if(!blessingOfArtemisEvent.isCancelled()) {
              Methods.setMetadata(arrow, "Artemis", blessingOfArtemisEvent.getDmgMultiplier());
              p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, blessingOfArtemisEvent.getInvisDuration() * 20, 0));
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, blessingOfArtemisEvent.getCooldown());
              mp.addAbilityOnCooldown(UnlockedAbilities.BLESSING_OF_ARTEMIS, cal.getTimeInMillis());
              mp.setReadying(false);
              Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
              mp.setReadyingAbilityBit(null);
              FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
              p.getLocation().getWorld().playSound(p.getLocation(), Sound.valueOf(soundFile.getString("Sounds.Archery.BlessingOfArtemis.Sound")),
                Float.parseFloat(soundFile.getString("Sounds.Archery.BlessingOfArtemis.Volume")), Float.parseFloat(soundFile.getString("Sounds.Archery.BlessingOfArtemis.Pitch")));
              p.getLocation().getWorld().spawnParticle(Particle.SMOKE_LARGE, p.getEyeLocation(), 1);
              trackArrowParticles(arrow, Particle.SMOKE_NORMAL);
              p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.BlessingOfArtemis.Activated")));
            }
          }
          //Deal with Blessing Of Apollo Ability
          //Set the metadata of the arrow to contain the fire ticks so it will be easier for us to deal with different contingencies
          else if(mp.getReadyingAbilityBit().getAbilityReady() == UnlockedAbilities.BLESSING_OF_APOLLO) {
            String key = "BlessingOfApolloConfig.Tier"
                    + Methods.convertToNumeral(mp.getBaseAbility(UnlockedAbilities.BLESSING_OF_APOLLO).getCurrentTier()) + ".";
            int cooldown = archeryConfiguration.getInt(key + "Cooldown");
            int fireResDuration = archeryConfiguration.getInt(key + "FireResDuration");
            int igniteDuration = archeryConfiguration.getInt(key + "IgniteDuration");
            BlessingOfApolloEvent blessingOfApolloEvent = new BlessingOfApolloEvent(mp, (BlessingOfApollo) mp.getBaseAbility(UnlockedAbilities.BLESSING_OF_APOLLO), cooldown, fireResDuration, igniteDuration);
            Bukkit.getPluginManager().callEvent(blessingOfApolloEvent);
            if(!blessingOfApolloEvent.isCancelled()) {
              Methods.setMetadata(arrow, "Apollo", blessingOfApolloEvent.getIgniteDuration());
              p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, blessingOfApolloEvent.getFireResDuration() * 20, 0));
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, blessingOfApolloEvent.getCooldown());
              mp.addAbilityOnCooldown(UnlockedAbilities.BLESSING_OF_APOLLO, cal.getTimeInMillis());
              mp.setReadying(false);
              Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
              mp.setReadyingAbilityBit(null);
              FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
              p.getLocation().getWorld().playSound(p.getLocation(), Sound.valueOf(soundFile.getString("Sounds.Archery.BlessingOfApollo.Sound")),
                Float.parseFloat(soundFile.getString("Sounds.Archery.BlessingOfApollo.Volume")), Float.parseFloat(soundFile.getString("Sounds.Archery.BlessingOfApollo.Pitch")));
              trackArrowParticles(arrow, Particle.FLAME);
              p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.BlessingOfApollo.Activated")));
            }
          }
          //Deal with Curse of Hades Ability
          //Set the metadata of the arrow to contain the various debuffs so it will be easier for us to deal with different contingencies
          else if(mp.getReadyingAbilityBit().getAbilityReady() == UnlockedAbilities.CURSE_OF_HADES) {
            String key = "CurseOfHadesConfig.Tier"
                    + Methods.convertToNumeral(mp.getBaseAbility(UnlockedAbilities.CURSE_OF_HADES).getCurrentTier()) + ".";
            int cooldown = archeryConfiguration.getInt(key + "Cooldown");
            int witherDuration = archeryConfiguration.getInt(key + "WitherDuration");
            int witherLevel = archeryConfiguration.getInt(key + "WitherLevel");
            int slownessDuration = archeryConfiguration.getInt(key + "SlownessDuration");
            int slownessLevel = archeryConfiguration.getInt(key + "SlownessLevel");
            int blindnessDuration = archeryConfiguration.getInt(key + "BlindnessDuration");

            CurseOfHadesEvent curseOfHadesEvent = new CurseOfHadesEvent(mp, (CurseOfHades) mp.getBaseAbility(UnlockedAbilities.CURSE_OF_HADES), cooldown, witherDuration, witherLevel, slownessDuration, slownessLevel, blindnessDuration);
            Bukkit.getPluginManager().callEvent(curseOfHadesEvent);
            if(!curseOfHadesEvent.isCancelled()) {
              Methods.setMetadata(arrow, "Hades1", curseOfHadesEvent.getWitherDuration());
              Methods.setMetadata(arrow, "Hades2", curseOfHadesEvent.getWitherLevel());
              Methods.setMetadata(arrow, "Hades3", curseOfHadesEvent.getSlownessDuration());
              Methods.setMetadata(arrow, "Hades4", curseOfHadesEvent.getSlownessLevel());
              Methods.setMetadata(arrow, "Hades5", curseOfHadesEvent.getBlindnessDuration());

              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, curseOfHadesEvent.getCooldown());
              mp.addAbilityOnCooldown(UnlockedAbilities.CURSE_OF_HADES, cal.getTimeInMillis());
              mp.setReadying(false);
              Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
              mp.setReadyingAbilityBit(null);
              FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
              p.getLocation().getWorld().playSound(p.getLocation(), Sound.valueOf(soundFile.getString("Sounds.Archery.CurseOfHades.Sound")),
                Float.parseFloat(soundFile.getString("Sounds.Archery.CurseOfHades.Volume")), Float.parseFloat(soundFile.getString("Sounds.Archery.CurseOfHades.Pitch")));
              trackArrowParticles(arrow, Particle.SQUID_INK);
              p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.CurseOfHades.Activated")));
            }
          }
        }
        else{
          if(e.getProjectile().getType() == EntityType.ARROW && DefaultAbilities.DAZE.isEnabled() && mp.getBaseAbility(DefaultAbilities.DAZE).isToggled()){
            Parser parser = DefaultAbilities.DAZE.getActivationEquation();
            parser.setVariable("archery_level", mp.getSkill(Skills.ARCHERY).getCurrentLevel());
            parser.setVariable("power_level", mp.getPowerLevel());
            int chance = (int) (parser.getValue() * 1000);
            Random rand = new Random();
            int val = rand.nextInt(100000);
            if(chance >= val) {
              Daze daze = (Daze) mp.getBaseAbility(DefaultAbilities.DAZE);
              String key = "DazeConfig.";
              int nauseaDuration = archeryConfiguration.getInt(key + "NauseaDuration");
              int blindnessDuration = archeryConfiguration.getInt(key + "BlindnessDuration");
              boolean forcePlayerLookup = archeryConfiguration.getBoolean(key + "MakePlayerLookUp");

              DazeEvent dazeEvent = new DazeEvent(mp, daze, forcePlayerLookup, blindnessDuration, nauseaDuration);
              Bukkit.getPluginManager().callEvent(dazeEvent);
              if(!dazeEvent.isCancelled()) {
                Methods.setMetadata(arrow, "DazeN", dazeEvent.getNauseaDuration());
                Methods.setMetadata(arrow, "DazeB", dazeEvent.getBlindnessDuration());
                Methods.setMetadata(arrow, "DazeF", dazeEvent.isForcePlayerLookup());
              }
            }
          }
          if(UnlockedAbilities.PUNCTURE.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.PUNCTURE) && mp.getBaseAbility(UnlockedAbilities.PUNCTURE).isToggled()){
            int chance = archeryConfiguration.getInt("PunctureConfig.Tier" +
                    Methods.convertToNumeral(mp.getBaseAbility(UnlockedAbilities.PUNCTURE).getCurrentTier()) + ".ActivationChance") * 1000;
            Random rand = new Random();
            int val = rand.nextInt(100000);
            if(chance >= val){
              PunctureEvent punctureEvent = new PunctureEvent(mp, (Puncture) mp.getBaseAbility(UnlockedAbilities.PUNCTURE));
              Bukkit.getPluginManager().callEvent(punctureEvent);
              if(!punctureEvent.isCancelled()){
                Methods.setMetadata(arrow, "Puncture", p.getUniqueId().toString());
              }
            }
          }
          if(UnlockedAbilities.TIPPED_ARROWS.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.TIPPED_ARROWS) && mp.getBaseAbility(UnlockedAbilities.TIPPED_ARROWS).isToggled()){
            int chance = archeryConfiguration.getInt("TippedArrowsConfig.Tier" +
                    Methods.convertToNumeral(mp.getBaseAbility(UnlockedAbilities.TIPPED_ARROWS).getCurrentTier()) + ".ActivationChance") * 1000;
            Random rand = new Random();
            int val = rand.nextInt(100000);
            if(chance >= val){
              List<String> effects = archeryConfiguration.getStringList("TippedArrowsConfig.Tier" +
                      Methods.convertToNumeral(mp.getBaseAbility(UnlockedAbilities.TIPPED_ARROWS).getCurrentTier()) + ".PossibleEffects");
              int i = rand.nextInt(effects.size());
              TippedArrowsEvent tippedArrowsEvent = new TippedArrowsEvent(mp, (TippedArrows) mp.getBaseAbility(UnlockedAbilities.TIPPED_ARROWS), effects.get(i));
              if(!tippedArrowsEvent.isCancelled()) {
                Methods.setMetadata(arrow, "TippedArrows", tippedArrowsEvent.getEffectString());
              }
            }
          }
          if(UnlockedAbilities.COMBO.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.COMBO) && mp.getBaseAbility(UnlockedAbilities.COMBO).isToggled()){
            //Check if the player has a cooldown for combo and if so then we need to check to see if the combo has expired and remove it if so
            if(p.hasMetadata("ComboCooldown")){
              long lastComboShot = p.getMetadata("ComboCooldown").get(0).asLong();
              Calendar cal = Calendar.getInstance();
              //If combo is ready to go again
              if(cal.getTimeInMillis() - lastComboShot >= archeryConfiguration.getInt("ComboConfig.Tier" +
                      Methods.convertToNumeral(mp.getBaseAbility(UnlockedAbilities.COMBO).getCurrentTier()) + ".CooldownBetweenActivation")){
                p.removeMetadata("ComboCooldown", McRPG.getInstance());
              }
              else{
                return;
              }
            }
            String key = "ComboConfig.Tier" + Methods.convertToNumeral(mp.getBaseAbility(UnlockedAbilities.COMBO).getCurrentTier()) + ".";
            double dmgMultiplier = archeryConfiguration.getDouble(key + "DamageMultiplier");
            int lengthBetweenShots = archeryConfiguration.getInt(key + "MaxLengthBetweenShots");
            int cooldownBetweenActivation = archeryConfiguration.getInt(key + "CooldownBetweenActivation");
            ComboEvent comboEvent = new ComboEvent(mp, (Combo) mp.getBaseAbility(UnlockedAbilities.COMBO), dmgMultiplier, lengthBetweenShots, cooldownBetweenActivation);
            Bukkit.getPluginManager().callEvent(comboEvent);
            if(!comboEvent.isCancelled()) {
              Methods.setMetadata(arrow, "Combo1", p.getUniqueId().toString());
              Methods.setMetadata(arrow, "Combo2", comboEvent.getDmgMultiplier());
              Methods.setMetadata(arrow, "Combo3", lengthBetweenShots);
              Methods.setMetadata(arrow, "Combo4", cooldownBetweenActivation);
            }
          }
        }
      }
      else {
        return;
      }
    }
  }

  public static void trackArrowParticles(Arrow arrow, Particle p){
    BukkitTask task = new BukkitRunnable(){
      @Override
      public void run() {
        if(arrow.isValid()) {
          arrow.getLocation().getWorld().spawnParticle(p, arrow.getLocation(), 4);
        }
        else{
          arrowTasks.remove(arrow.getUniqueId()).cancel();
          return;
        }
      }
    }.runTaskTimer(McRPG.getInstance(), 10, 15);
    arrowTasks.put(arrow.getUniqueId(), task);
  }
}
