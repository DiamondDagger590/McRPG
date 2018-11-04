package us.eunoians.mcmmox.events.vanilla;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.swords.Bleed;
import us.eunoians.mcmmox.abilities.swords.SerratedStrikes;
import us.eunoians.mcmmox.abilities.swords.TaintedBlade;
import us.eunoians.mcmmox.abilities.unarmed.*;
import us.eunoians.mcmmox.api.events.mcmmo.*;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;
import us.eunoians.mcmmox.players.PlayerReadyBit;
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
	if(e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity){
	  Player damager = (Player) e.getDamager();
	  McMMOPlayer mp = PlayerManager.getPlayer(damager.getUniqueId());
	  //Deal with unarmed
	  if(damager.getItemInHand() == null){
		if(!Skills.UNARMED.isEnabled()){
		  return;
		}
		config = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.UNARMED_CONFIG);
		e.setDamage(e.getDamage() + config.getInt("BonusDamage"));
		//Give exp
		int baseExp = 0;
		if(!config.contains("ExpAwardedPerMob." + e.getEntity().toString())){
		  baseExp = config.getInt("ExpAwardedPerMob.OTHER");
		}
		else{
		  baseExp = config.getInt("ExpAwardedPerMob." + e.getEntity().toString());
		}
		double dmg = e.getDamage();
		int expAwarded = (int) (dmg * baseExp);
		mp.getSkill(Skills.UNARMED).giveExp(expAwarded, GainReason.DAMAGE);
		if(mp.isCanSmite()){
		  if(!(e.getEntity().getFireTicks() > 0)){
			LivingEntity entity = (LivingEntity) e.getEntity();
			int chance = (int) mp.getSmitingFistData().getSmiteChance() * 1000;
			Random rand = new Random();
			int val = rand.nextInt(100000);
			if(chance >= val){
			  if(entity.hasPotionEffect(PotionEffectType.INVISIBILITY)){
				entity.removePotionEffect(PotionEffectType.INVISIBILITY);
			  }
			  entity.setFireTicks(mp.getSmitingFistData().getSmiteDuration());
			}
			mp.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
				Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.SmitingFist.Smited")));
			entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10, 1);
		  }
		}
		else if(mp.isCanDenseImpact()){
		  if(e.getEntity() instanceof Player){
		    Player damaged = (Player) e.getEntity();
		    for(ItemStack armour : damaged.getInventory().getArmorContents()){
		      armour.setDurability((short) (armour.getDurability() - mp.getArmourDmg()));
			}
		  }
		}
		if(mp.isReadying()){
		  PlayerReadyBit playerReadyBit = mp.getReadyingAbilityBit();
		  if(playerReadyBit.getAbilityReady().equals(UnlockedAbilities.BERSERK)){
			if(UnlockedAbilities.BERSERK.isEnabled() && mp.getBaseAbility(UnlockedAbilities.BERSERK).isToggled()){

			  Berserk berserk = (Berserk) mp.getBaseAbility(UnlockedAbilities.BERSERK);
			  double bonusChance = config.getDouble("BerserkConfig.Tier" + Methods.convertToNumeral(berserk.getCurrentTier()) + ".ActivationBoost");
			  int bonusDmg = config.getInt("BerserkConfig.Tier" + Methods.convertToNumeral(berserk.getCurrentTier()) + ".DamageBoost");
			  BerserkEvent event = new BerserkEvent(mp, (Berserk) mp.getBaseAbility(UnlockedAbilities.BERSERK), bonusChance, bonusDmg);
			  Bukkit.getPluginManager().callEvent(event);
			  if(!event.isCancelled()){
				//cancel the readying task and null the bit
				Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
				mp.setReadyingAbilityBit(null);
				//get the bleed ability and set the bonus chance
				Disarm disarm = (Disarm) mp.getSkill(Skills.UNARMED).getAbility(UnlockedAbilities.DISARM);
				disarm.setBonusChance(event.getBonusChance());
				e.setDamage(e.getDamage() + event.getBonusDamage());
				//Tell player ability started
				mp.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
					Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.Berserk.Activated")));
				mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 10, 1);
				new BukkitRunnable() {
				  @Override
				  public void run(){
					//Undo all the things that berserk did and set it on cooldown
					disarm.setBonusChance(0);
					mp.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
						Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.Berserk.Deactivated")));
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.SECOND,
						Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.UNARMED_CONFIG).getInt("BerserkConfig.Tier" + Methods.convertToNumeral(berserk.getCurrentTier()) + ".Cooldown"));
					mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.ENTITY_IRON_GOLEM_DEATH, 10, 1);
					mp.addAbilityOnCooldown(UnlockedAbilities.BERSERK, cal.getTimeInMillis());
				  }
				}.runTaskLater(Mcmmox.getInstance(), config.getInt("BerserkConfig.Tier" + Methods.convertToNumeral(berserk.getCurrentTier()) + ".Duration") * 20);
			  }
			}
		  }
		  else if(playerReadyBit.getAbilityReady().equals(UnlockedAbilities.SMITING_FIST)){
			if(UnlockedAbilities.SMITING_FIST.isEnabled() && mp.getBaseAbility(UnlockedAbilities.SMITING_FIST).isToggled()){
			  SmitingFist smitingFist = (SmitingFist) mp.getBaseAbility(UnlockedAbilities.SMITING_FIST);
			  //cancel the readying task and null the bit
			  Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
			  mp.setReadyingAbilityBit(null);

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
				mp.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
					Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.SmitingFist.Activated")));
				mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 10, 1);
				mp.setSmitingFistData(smitingFistEvent);
				mp.setCanSmite(true);
				if(smitingFistEvent.isRemoveDebuffs()){
				  for(PotionEffect effectType : damager.getActivePotionEffects()){
					if(Debuffs.isDebuff(effectType.getType())){
					  damager.removePotionEffect(effectType.getType());
					}
				  }
				}
				damager.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, smitingFistEvent.getDuration() * 20,smitingFistEvent.getAbsorptionLevel()));
				new BukkitRunnable() {
				  @Override
				  public void run(){
					//Undo all the things that smiting fist did and set it on cooldown
					mp.setCanSmite(false);
					mp.setSmitingFistData(null);
					mp.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
						Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.SmitingFist.Deactivated")));
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.SECOND,
						smitingFistEvent.getCooldown());
					mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.ENTITY_ILLUSIONER_DEATH, 10, 1);
					mp.addAbilityOnCooldown(UnlockedAbilities.SMITING_FIST, cal.getTimeInMillis());
				  }
				}.runTaskLater(Mcmmox.getInstance(), smitingFistEvent.getDuration() * 20);
			  }
			}
		  }
		  else if(playerReadyBit.getAbilityReady().equals(UnlockedAbilities.DENSE_IMPACT)){
		    if(UnlockedAbilities.DENSE_IMPACT.isEnabled() && mp.getBaseAbility(UnlockedAbilities.DENSE_IMPACT).isToggled()){
			  DenseImpact denseImpact = (DenseImpact) mp.getBaseAbility(UnlockedAbilities.DENSE_IMPACT);
			  int cooldown = config.getInt("DenseImpactConfig.Tier" + Methods.convertToNumeral(denseImpact.getCurrentTier()) + ".Cooldown");
			  int duration = config.getInt("DenseImpactConfig.Tier" + Methods.convertToNumeral(denseImpact.getCurrentTier()) + ".Duration");
			  int armourDmg = config.getInt("DenseImpactConfig.Tier" + Methods.convertToNumeral(denseImpact.getCurrentTier()) + ".ArmorDamage");
			  DenseImpactEvent denseImpactEvent = new DenseImpactEvent(mp, denseImpact, armourDmg);
			  Bukkit.getPluginManager().callEvent(denseImpactEvent);
			  if(!denseImpactEvent.isCancelled()){
				mp.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
					Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.DenseImpact.Activated")));
				mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 10, 1);
				mp.setCanDenseImpact(true);
				mp.setArmourDmg(denseImpactEvent.getArmourDmg());
				new BukkitRunnable() {
				  @Override
				  public void run(){
					//Undo all the things that dense impact did and set it on cooldown
					mp.setCanDenseImpact(false);
					mp.setArmourDmg(0);
					mp.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
						Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.DenseImpact.Deactivated")));
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.SECOND,
						cooldown);
					mp.getPlayer().getLocation().getWorld().playSound(mp.getPlayer().getLocation(), Sound.ENTITY_IRON_GOLEM_DEATH, 10, 1);
					mp.addAbilityOnCooldown(UnlockedAbilities.DENSE_IMPACT, cal.getTimeInMillis());
				  }
				}.runTaskLater(Mcmmox.getInstance(), duration * 20);
			  }
			}
		  }
		}
		//Manage disarm
		if(e.getEntity() instanceof Player && UnlockedAbilities.DISARM.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.DISARM) && mp.getBaseAbility(UnlockedAbilities.DISARM).isToggled()){
		  Disarm disarm = (Disarm) mp.getBaseAbility(UnlockedAbilities.DISARM);
		  Player damagedPlayer = (Player) e.getEntity();
		  McMMOPlayer damagedMcMMOPlayer = PlayerManager.getPlayer(damagedPlayer.getUniqueId());
		  if(damagedPlayer.getItemInHand() == null || damagedPlayer.getItemInHand().getType() == Material.AIR){
			return;
		  }
		  double disarmChance = config.getDouble("DisarmConfig.Tier" + Methods.convertToNumeral(disarm.getCurrentTier()) + ".ActivationChance") + disarm.getBonusChance();
		  int chance = (int) disarmChance * 1000;
		  Random rand = new Random();
		  int val = rand.nextInt(100000);
		  if(chance >= val){
			DisarmEvent disarmEvent = new DisarmEvent(mp, damagedMcMMOPlayer, disarm, damagedPlayer.getItemInHand());
			Bukkit.getPluginManager().callEvent(disarmEvent);
			if(!disarmEvent.isCancelled()){
			  int slot = damagedPlayer.getInventory().firstEmpty();
			  int heldSlot = damagedPlayer.getInventory().getHeldItemSlot();
			  if(slot == -1){
				damagedPlayer.getLocation().getWorld().dropItemNaturally(damagedPlayer.getLocation(), disarmEvent.getItemToDisarm());
			  }
			  else{
				damagedPlayer.getInventory().setItem(slot, disarmEvent.getItemToDisarm());
			  }
			  damagedPlayer.getInventory().getItem(heldSlot).setType(Material.AIR);
			  damager.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix()
				  + Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.Disarm.PlayerDisarmed").replaceAll("%Player%", damagedPlayer.getDisplayName())));
			  damagedPlayer.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.Disarm.BeenDisarmed")));
			}
		  }
		  return;
		}
		if(UnlockedAbilities.IRON_ARM.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.IRON_ARM) && mp.getBaseAbility(UnlockedAbilities.IRON_ARM).isToggled()){
		  IronArm ironArm = (IronArm) mp.getBaseAbility(UnlockedAbilities.IRON_ARM);
		  int chance = (int) config.getDouble("IronArmConfig.Tier" + Methods.convertToNumeral(ironArm.getCurrentTier()) + ".ActivationChance") * 1000;
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
		  return;
		}
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
			if(mp.getReadyingAbilityBit().getAbilityReady().getName().equalsIgnoreCase(UnlockedAbilities.SERRATED_STRIKES.getName())){
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
					mp.addAbilityOnCooldown(UnlockedAbilities.SERRATED_STRIKES, cal.getTimeInMillis());
				  }
				}.runTaskLater(Mcmmox.getInstance(), event.getDuration() * 20);
			  }
			}
			//If we need to use tainted blade
			else if(mp.getReadyingAbilityBit().getAbilityReady().getName().equalsIgnoreCase(UnlockedAbilities.TAINTED_BLADE.getName())){
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
				mp.addAbilityOnCooldown(UnlockedAbilities.TAINTED_BLADE, cal.getTimeInMillis());
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

  private enum Debuffs{
    BLINDNESS(PotionEffectType.BLINDNESS),
	WEAKNESS(PotionEffectType.WEAKNESS),
	SLOWNESS(PotionEffectType.SLOW),
	MINING_FATIGUE(PotionEffectType.SLOW_DIGGING),
	HUNGER(PotionEffectType.HUNGER),
	WITHER(PotionEffectType.WITHER);

    @Getter
	private PotionEffectType effectType;

    Debuffs(PotionEffectType effectType){
      this.effectType = effectType;
	}

	public static boolean isDebuff(PotionEffectType test){
      for(Debuffs debuff : values()){
        if(debuff.getEffectType() == test){
          return true;
		}
	  }
	  return false;
	}
  }
}
