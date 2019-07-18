package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.axes.BloodFrenzy;
import us.eunoians.mcrpg.abilities.fitness.DivineEscape;
import us.eunoians.mcrpg.api.events.mcrpg.axes.BloodFrenzyEvent;
import us.eunoians.mcrpg.api.events.mcrpg.fitness.DivineEscapeEvent;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.mcmmo.MobHealthbarUtils;

import java.util.Calendar;

public class DeathEvent implements Listener {
  /**
   * This code is not mine. It is copyright from the original mcMMO allowed for use by their license.
   * This code has been modified from it source material
   * It was released under the GPLv3 license
   */


  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerDeathHighest(EntityDamageEvent e) {
    if(PlayerManager.isPlayerFrozen(e.getEntity().getUniqueId())){
      return;
    }
    if(e.getEntity() instanceof Player ) {
      Player p = (Player) e.getEntity();
      if(p.getHealth() - e.getDamage() <= 0 && p.getBedSpawnLocation() != null) {
        McRPGPlayer mp;
        try{
          mp = PlayerManager.getPlayer(p.getUniqueId());
        }
        catch(McRPGPlayerNotFoundException exception){
          return;
        }
        if(UnlockedAbilities.DIVINE_ESCAPE.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.DIVINE_ESCAPE)
        && mp.getBaseAbility(UnlockedAbilities.DIVINE_ESCAPE).isToggled()){
          if(mp.getCooldown(UnlockedAbilities.DIVINE_ESCAPE) > -1){
            return;
          }
          FileConfiguration fitnessConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.FITNESS_CONFIG);
          DivineEscape divineEscape = (DivineEscape) mp.getBaseAbility(UnlockedAbilities.DIVINE_ESCAPE);
          String key = "DivineEscapeConfig.Tier" + Methods.convertToNumeral(divineEscape.getCurrentTier()) + ".";
          double mcrpgExpPen = fitnessConfig.getDouble(key + "McRPGExpPenalty");
          int mcrpgExpPenDur = fitnessConfig.getInt(key + "McRPGExpPenaltyDuration");
          double damagePenalty = fitnessConfig.getDouble(key + "DamagePenalty");
          int damagePenDur = fitnessConfig.getInt(key + "DamagePenaltyDuration");
          int cooldown = fitnessConfig.getInt(key + "Cooldown");
          DivineEscapeEvent divineEscapeEvent = new DivineEscapeEvent(mp, divineEscape, mcrpgExpPen, damagePenalty);
          Bukkit.getPluginManager().callEvent(divineEscapeEvent);
          if(!divineEscapeEvent.isCancelled()){
            e.setCancelled(true);
            p.setHealth(p.getMaxHealth());
            p.teleport(p.getBedSpawnLocation());
            p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.DivineEscape.Activated"))
            .replace("%Exp_Debuff%", Double.toString(mcrpgExpPen)).replace("%Damage_Debuff%", Double.toString(damagePenalty)));
            mp.setDivineEscapeDamageDebuff(divineEscapeEvent.getDamageIncreaseDebuff());
            mp.setDivineEscapeExpDebuff(divineEscapeEvent.getExpDebuff());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, cooldown);
            mp.addAbilityOnCooldown(UnlockedAbilities.DIVINE_ESCAPE, cal.getTimeInMillis());
            cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, mcrpgExpPenDur);
            mp.setDivineEscapeExpEnd(cal.getTimeInMillis());
            cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, damagePenDur);
            mp.setDivineEscapeDamageEnd(cal.getTimeInMillis());
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDeathMonitor(EntityDamageByEntityEvent e){
    if(e.getEntity() instanceof LivingEntity && !(e.getEntity() instanceof ArmorStand) && ((LivingEntity) e.getEntity()).getHealth() - e.getDamage() <= 0){
      LivingEntity len = (LivingEntity) e.getEntity();
      if(e.getDamager() instanceof Player){
        McRPGPlayer attacker;
        try{
          attacker = PlayerManager.getPlayer(e.getDamager().getUniqueId());
        }
        catch(McRPGPlayerNotFoundException exception){
          return;
        }
        if(UnlockedAbilities.BLOOD_FRENZY.isEnabled() && attacker.getAbilityLoadout().contains(UnlockedAbilities.BLOOD_FRENZY) && attacker.getBaseAbility(UnlockedAbilities.BLOOD_FRENZY).isToggled()){
          BloodFrenzy bloodFrenzy = (BloodFrenzy) attacker.getBaseAbility(UnlockedAbilities.BLOOD_FRENZY);
          FileConfiguration axesConfiguration = McRPG.getInstance().getFileManager().getFile(FileManager.Files.AXES_CONFIG);
          boolean affectMobs = axesConfiguration.getBoolean("BloodFrenzyConfig.ActiveOnMobs");
          if(len instanceof Player || affectMobs){
            String key = "BloodFrenzyConfig.Tier" + Methods.convertToNumeral(bloodFrenzy.getCurrentTier()) + ".";
            int hasteLevel = axesConfiguration.getInt(key + "HasteLevel") - 1;
            int hasteDuration = axesConfiguration.getInt(key + "HasteDuration");
            int regenLevel = axesConfiguration.getInt(key + "RegenerationLevel") - 1;
            int regenDuration = axesConfiguration.getInt(key + "RegenerationDuration");
            BloodFrenzyEvent bloodFrenzyEvent = new BloodFrenzyEvent(attacker, bloodFrenzy, hasteDuration, hasteLevel, regenDuration, regenLevel, len);
            Bukkit.getPluginManager().callEvent(bloodFrenzyEvent);
            if(!bloodFrenzyEvent.isCancelled()){
              Player p = attacker.getPlayer();
              if (p.hasPotionEffect(PotionEffectType.FAST_DIGGING)) {
                PotionEffect effect = p.getPotionEffect(PotionEffectType.FAST_DIGGING);
                if(effect.getDuration() < bloodFrenzyEvent.getHasteDuration() * 20 && effect.getAmplifier() <= bloodFrenzyEvent.getHasteLevel()){
                  p.removePotionEffect(PotionEffectType.FAST_DIGGING);
                  p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, bloodFrenzyEvent.getHasteDuration() * 20, bloodFrenzyEvent.getHasteLevel()));
                }
              }
              else {
                p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, bloodFrenzyEvent.getHasteDuration() * 20, bloodFrenzyEvent.getHasteLevel()));
              }
              if (p.hasPotionEffect(PotionEffectType.REGENERATION)) {
                PotionEffect effect = p.getPotionEffect(PotionEffectType.REGENERATION);
                if(effect.getDuration() < bloodFrenzyEvent.getRegenDuration() * 20 && effect.getAmplifier() <= bloodFrenzyEvent.getRegenLevel()){
                  p.removePotionEffect(PotionEffectType.REGENERATION);
                  p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, bloodFrenzyEvent.getRegenDuration() * 20, bloodFrenzyEvent.getRegenLevel()));
                }
              }
              else {
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, bloodFrenzyEvent.getRegenDuration() * 20, bloodFrenzyEvent.getRegenLevel()));
              }
            }
          }
        }
      }
    }
  }
  /**
   * Handle PlayerDeathEvents at the lowest priority.
   * <p>
   * These events are used to modify the death message of a player when
   * needed to correct issues potentially caused by the custom naming used
   * for mob healthbars.
   *
   * @param event The event to modify
   */
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onPlayerDeathLowest(PlayerDeathEvent event) {
    String deathMessage = event.getDeathMessage();

    if(deathMessage == null) {
      return;
    }

    Player player = event.getEntity();
    event.setDeathMessage(MobHealthbarUtils.fixDeathMessage(deathMessage, player));
  }
}
