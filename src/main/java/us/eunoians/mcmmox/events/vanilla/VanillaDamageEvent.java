package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.swords.Bleed;
import us.eunoians.mcmmox.abilities.swords.SerratedStrikes;
import us.eunoians.mcmmox.abilities.swords.TaintedBlade;
import us.eunoians.mcmmox.api.events.mcmmo.BleedEvent;
import us.eunoians.mcmmox.api.events.mcmmo.SerratedStrikesEvent;
import us.eunoians.mcmmox.api.events.mcmmo.TaintedBladeEvent;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;
import us.eunoians.mcmmox.skills.Skill;
import us.eunoians.mcmmox.types.DefaultAbilities;
import us.eunoians.mcmmox.types.GainReason;
import us.eunoians.mcmmox.types.Skills;
import us.eunoians.mcmmox.types.UnlockedAbilities;
import us.eunoians.mcmmox.util.Parser;

import java.util.Calendar;
import java.util.Random;

public class VanillaDamageEvent implements Listener {

  @EventHandler
  public void damageEvent(EntityDamageByEntityEvent e){
	FileConfiguration config;
	if(e.getDamager() instanceof Player){
	  Player damager = (Player) e.getDamager();
	  McMMOPlayer mp = PlayerManager.getPlayer(damager.getUniqueId());
	  if(damager.getItemInHand() == null){
		//UNARMED
	  }
	  else{
		Material weapon = damager.getItemInHand().getType();
		if(weapon.name().contains("SWORD")){
		  Skill playersSkill = mp.getSkill(Skills.SWORDS);
		  if(!Skills.SWORDS.isEnabled()){
			return;
		  }
		  //If the player is readying for an ability
		  if(mp.isReadying()){
			//If we need to use serrated strikes (We can preemptively assume that its enabled as we have checked earlier on in the code)
			if(mp.getReadyingAbilityBit().getAbilityReady().getName() == UnlockedAbilities.SERRATED_STRIKES.getName()){
			  //call api event
			  SerratedStrikesEvent event = new SerratedStrikesEvent(mp, (SerratedStrikes) mp.getBaseAbility(UnlockedAbilities.SERRATED_STRIKES));
			  Bukkit.getPluginManager().callEvent(event);
			  if(!event.isCancelled()){
				//cancel the readying task and null the bit
				Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
				mp.setReadyingAbilityBit(null);
				//get the bleed ability and set the bonus chance
				Bleed bleed = (Bleed) mp.getSkill(Skills.SWORDS).getAbility(DefaultAbilities.BLEED);
				bleed.setBonusChance(event.getActivationRateBoost());
				//Tell player ability started
				mp.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
					Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.SerratedStrikes.Activated")));
				new BukkitRunnable() {
				  @Override
				  public void run(){
					//Undo all the things that serrated strikes did and set it on cooldown
					bleed.setBonusChance(0);
					mp.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
						Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.SerratedStrikes.Deactivated")));
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.SECOND,
						event.getCooldown());
					mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.ENTITY_SHULKER_HURT, 10, 1);
					mp.addAbilityOnCooldown(UnlockedAbilities.SERRATED_STRIKES, event.getCooldown());
				  }
				}.runTaskLater(Mcmmox.getInstance(), event.getDuration() * 20);
			  }
			}
			//If we need to use tainted blade
			else if(mp.getReadyingAbilityBit().getAbilityReady().getName() == UnlockedAbilities.TAINTED_BLADE.getName()){
			  TaintedBladeEvent event = new TaintedBladeEvent(mp, (TaintedBlade) mp.getBaseAbility(UnlockedAbilities.TAINTED_BLADE));
			  Bukkit.getPluginManager().callEvent(event);
			  if(!event.isCancelled()){
			    Player p = mp.getPlayer();
			    p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
					Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.TaintedBlade.Activated")));
				PotionEffect strength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, event.getStrengthDuration() * 20, 1);
				PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, event.getResistanceDuration() * 20, 1);
				PotionEffect hunger = new PotionEffect(PotionEffectType.HUNGER, event.getHungerDuration() * 20, 2);
				p.addPotionEffect(strength);
				p.addPotionEffect(resistance);
				p.addPotionEffect(hunger);
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.SECOND,
					event.getCooldown());
				p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_SKELETON_HURT, 10, 1);
				mp.addAbilityOnCooldown(UnlockedAbilities.TAINTED_BLADE, event.getCooldown());
			  }
			}
		  }
		  if(Skills.SWORDS.getEnabledAbilities().contains("Bleed")){
			if(playersSkill.getAbility(DefaultAbilities.BLEED).isToggled()){
			  Bleed bleed = (Bleed) playersSkill.getAbility(DefaultAbilities.BLEED);
			  Parser parser = DefaultAbilities.BLEED.getActivationEquation();
			  if(e.getEntity() instanceof Player){
				Player damagedPlayer = (Player) e.getEntity();
				McMMOPlayer dmged = PlayerManager.getPlayer(damagedPlayer.getUniqueId());
				if(!dmged.isHasBleedImmunity() && bleed.canTarget()){
				  parser.setVariable("swords_level", playersSkill.getCurrentLevel());
				  parser.setVariable("power_level", mp.getPowerLevel());
				  int chance = (int) parser.getValue() * 1000;
				  Random rand = new Random();
				  int val = rand.nextInt(100000);
				  if(chance >= val){
					BleedEvent event = new BleedEvent(mp, e.getEntity(), bleed);
					Bukkit.getPluginManager().callEvent(event);
				  }
				}
			  }
			  else{
				parser.setVariable("swords_level", playersSkill.getCurrentLevel());
				parser.setVariable("power_level", mp.getPowerLevel());
				Random rand = new Random();
				int chance = (int) parser.getValue() * 1000;
				int val = rand.nextInt(100000);
				if(chance >= val){
				  BleedEvent event = new BleedEvent(mp, e.getEntity(), bleed);
				  Bukkit.getPluginManager().callEvent(event);
				}
			  }
			}
		  }
		  config = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG);
		  double multiplier = config.getDouble("MaterialBonus." + weapon);
		  int baseExp = 0;
		  if(!config.contains("ExpAwardedPerMob." + e.getEntity().toString())){
			baseExp = config.getInt("ExpAwardedPerMob.OTHER");
		  }
		  else{
			baseExp = config.getInt("ExpAwardedPerMob." + e.getEntity().toString());
		  }
		  double dmg = e.getDamage();
		  int expAwarded = (int) (dmg * baseExp * multiplier);
		  mp.getSkill(Skills.SWORDS).giveExp(expAwarded, GainReason.DAMAGE);
		  return;
		}
	  }
	}
	else{
	  return;
	}
  }
}
