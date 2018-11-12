package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.abilities.herbalism.NaturesWrath;
import us.eunoians.mcmmox.api.events.mcmmo.NaturesWrathEvent;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;
import us.eunoians.mcmmox.players.PlayerReadyBit;
import us.eunoians.mcmmox.types.Skills;
import us.eunoians.mcmmox.types.UnlockedAbilities;

import java.util.ArrayList;
import java.util.UUID;

public class CheckReadyEvent implements Listener {

  private static final ArrayList<UUID> playersToIgnore = new ArrayList<>();

  @EventHandler(priority = EventPriority.MONITOR)
  public void checkReady(PlayerInteractEvent e){

	Player p = e.getPlayer();
	McMMOPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
	ItemStack heldItem = e.getItem();
	if(e.isCancelled() && e.getAction() != Action.RIGHT_CLICK_AIR){
	  return;
	}
	Block target = e.getClickedBlock();
	Material type;
	if(target == null){
	  type = Material.AIR;
	}
	else{
	  type = target.getType();
	}
	if(heldItem == null){
	  heldItem = new ItemStack(Material.AIR);
	}
	if(type == Material.CHEST || type == Material.ENDER_CHEST || type == Material.TRAPPED_CHEST
		|| type == Material.BEACON){
	  return;
	}
	if((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && UnlockedAbilities.NATURES_WRATH.isEnabled() &&
		mp.getAbilityLoadout().contains(UnlockedAbilities.NATURES_WRATH) && mp.getBaseAbility(UnlockedAbilities.NATURES_WRATH).isToggled()){
	  //verify they have a weapon or tool. prevents annoying food bug
	  if(Methods.getSkillsItem(p.getInventory().getItemInMainHand()) != null && !playersToIgnore.contains(p.getUniqueId())){
		NaturesWrath naturesWrath = (NaturesWrath) mp.getBaseAbility(UnlockedAbilities.NATURES_WRATH);
		FileConfiguration herbalism = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.HERBALISM_CONFIG);
		String key = "NaturesWrathConfig.Tier" + Methods.convertToNumeral(naturesWrath.getCurrentTier()) + ".";
		ItemStack offHand = p.getInventory().getItemInOffHand();
		if(offHand != null && offHand.getType() != Material.AIR){
		  Material itemType = offHand.getType();
		  if(itemType == Material.POPPY && herbalism.getBoolean(key + itemType.toString())){
			int hungerLost = herbalism.getInt(key + "HungerLost");
			int hungerLimit = herbalism.getInt(key + "HungerLimit");
			if(p.getFoodLevel() - hungerLost >= hungerLimit){
			  int modifier = herbalism.getInt(key + "Modifier");
			  int duration = herbalism.getInt(key + "Duration");
			  PotionEffectType effectType = PotionEffectType.INCREASE_DAMAGE;
			  NaturesWrathEvent naturesWrathEvent = new NaturesWrathEvent(mp, naturesWrath, hungerLost, modifier, effectType, duration);
			  Bukkit.getPluginManager().callEvent(naturesWrathEvent);
			  if(!naturesWrathEvent.isCancelled()){
				p.getLocation().getWorld().spawnParticle(Particle.ITEM_CRACK, p.getEyeLocation(), 1, 0, 0, 0, new ItemStack(Material.POPPY));
				p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 10, 1);
				p.getInventory().getItemInOffHand().setAmount(p.getInventory().getItemInOffHand().getAmount() - 1);
				p.updateInventory();
				p.setFoodLevel(p.getFoodLevel() - naturesWrathEvent.getHungerLost());
				p.addPotionEffect(new PotionEffect(naturesWrathEvent.getEffectType(), naturesWrathEvent.getDuration() * 20, naturesWrathEvent.getModifier() - 1));
			  }
			}
		  }
		  else if(itemType == Material.DANDELION && herbalism.getBoolean(key + itemType.toString())){
			int hungerLost = herbalism.getInt(key + "HungerLost");
			int hungerLimit = herbalism.getInt(key + "HungerLimit");
			if(p.getFoodLevel() - hungerLost >= hungerLimit){
			  int modifier = herbalism.getInt(key + "Modifier");
			  int duration = herbalism.getInt(key + "Duration");
			  PotionEffectType effectType = PotionEffectType.FAST_DIGGING;
			  NaturesWrathEvent naturesWrathEvent = new NaturesWrathEvent(mp, naturesWrath, hungerLost, modifier, effectType, duration);
			  Bukkit.getPluginManager().callEvent(naturesWrathEvent);
			  if(!naturesWrathEvent.isCancelled()){
				p.getLocation().getWorld().spawnParticle(Particle.ITEM_CRACK, p.getEyeLocation(), 1, 0, 0, 0, new ItemStack(Material.DANDELION));
				p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 10, 1);
				p.getInventory().getItemInOffHand().setAmount(p.getInventory().getItemInOffHand().getAmount() - 1);
				p.updateInventory();
				p.setFoodLevel(p.getFoodLevel() - naturesWrathEvent.getHungerLost());
				p.addPotionEffect(new PotionEffect(naturesWrathEvent.getEffectType(), naturesWrathEvent.getDuration() * 20, naturesWrathEvent.getModifier() - 1));
			  }
			}
		  }
		  else if(itemType == Material.BLUE_ORCHID && herbalism.getBoolean(key + itemType.toString())){
			int hungerLost = herbalism.getInt(key + "HungerLost");
			int hungerLimit = herbalism.getInt(key + "HungerLimit");
			if(p.getFoodLevel() - hungerLost >= hungerLimit){
			  int modifier = herbalism.getInt(key + "Modifier");
			  int duration = herbalism.getInt(key + "Duration");
			  PotionEffectType effectType = PotionEffectType.SPEED;
			  NaturesWrathEvent naturesWrathEvent = new NaturesWrathEvent(mp, naturesWrath, hungerLost, modifier, effectType, duration);
			  Bukkit.getPluginManager().callEvent(naturesWrathEvent);
			  if(!naturesWrathEvent.isCancelled()){
				p.getLocation().getWorld().spawnParticle(Particle.ITEM_CRACK, p.getEyeLocation(), 1, 0, 0, 0, new ItemStack(Material.BLUE_ORCHID));
				p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 10, 1);
				p.getInventory().getItemInOffHand().setAmount(p.getInventory().getItemInOffHand().getAmount() - 1);
				p.updateInventory();
				p.setFoodLevel(p.getFoodLevel() - naturesWrathEvent.getHungerLost());
				p.addPotionEffect(new PotionEffect(naturesWrathEvent.getEffectType(), naturesWrathEvent.getDuration() * 20, naturesWrathEvent.getModifier() - 1));
			  }
			}
		  }
		  else if(itemType == Material.LILAC && herbalism.getBoolean(key + itemType.toString())){
			int hungerLost = herbalism.getInt(key + "HungerLost");
			int hungerLimit = herbalism.getInt(key + "HungerLimit");
			if(p.getFoodLevel() - hungerLost >= hungerLimit){
			  int modifier = herbalism.getInt(key + "Modifier");
			  int duration = herbalism.getInt(key + "Duration");
			  PotionEffectType effectType = PotionEffectType.DAMAGE_RESISTANCE;
			  NaturesWrathEvent naturesWrathEvent = new NaturesWrathEvent(mp, naturesWrath, hungerLost, modifier, effectType, duration);
			  Bukkit.getPluginManager().callEvent(naturesWrathEvent);
			  if(!naturesWrathEvent.isCancelled()){
				p.getLocation().getWorld().spawnParticle(Particle.ITEM_CRACK, p.getEyeLocation(), 1, 0, 0, 0, new ItemStack(Material.LILAC));
				p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 10, 1);
				p.getInventory().getItemInOffHand().setAmount(p.getInventory().getItemInOffHand().getAmount() - 1);
				p.updateInventory();
				p.setFoodLevel(p.getFoodLevel() - naturesWrathEvent.getHungerLost());
				p.addPotionEffect(new PotionEffect(naturesWrathEvent.getEffectType(), naturesWrathEvent.getDuration() * 20, naturesWrathEvent.getModifier() - 1));
			  }
			}
		  }
		  playersToIgnore.add(p.getUniqueId());
		  Bukkit.getScheduler().runTaskLater(Mcmmox.getInstance(), () -> playersToIgnore.remove(p.getUniqueId()), 3 * 20);
		}
	  }
	}
	//Verify that the player is in a state able to ready abilities
	//If the slot is not their hand or they arent right clicking or the player is charging or if their gamemode isnt survival or if they are holding air
	if(e.getHand() == null || e.getHand() != EquipmentSlot.HAND || (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) || ShiftToggle.isPlayerCharging(p) || p.getGameMode() != GameMode.SURVIVAL){
	  return;
	}
	else{
	  //If they are already readying we dont need to do anything
	  if(mp.isReadying()){
		return;
	  }
	  else{
		//Get the skill from the material of the item
		Skills skillType = Methods.getSkillsItem(heldItem);
		if(skillType == null){
		  return;
		}
		if(p.getInventory().getItemInOffHand().getType() == Material.POPPY || p.getInventory().getItemInOffHand().getType() == Material.DANDELION
			|| p.getInventory().getItemInOffHand().getType() == Material.LILAC || p.getInventory().getItemInOffHand().getType() == Material.BLUE_ORCHID){
		  if(UnlockedAbilities.NATURES_WRATH.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.NATURES_WRATH) && mp.getBaseAbility(UnlockedAbilities.NATURES_WRATH).isToggled()){
		    if(!playersToIgnore.contains(p.getUniqueId())){
		      return;
			}
		  }
		}
		if(Mcmmox.getInstance().getConfig().getBoolean("Configuration.RequireEmptyOffHand") && p.getInventory().getItemInOffHand().getType() != Material.AIR){
		  return;
		}
		if(mp.getCooldown(skillType) != -1){
		  p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
			  Mcmmox.getInstance().getLangFile().getString("Messages.Players.CooldownActive").replace("%Skill%", skillType.getName())
				  .replace("%Time%", Integer.toString((int) mp.getCooldown(skillType)))));
		  return;
		}
		readyHandler(p, mp, skillType);
	  }
	}
  }

  private void readyHandler(Player p, McMMOPlayer mp, Skills skillType){
	if(mp.doesPlayerHaveActiveAbilityFromSkill(skillType)){
	  BaseAbility ab = mp.getBaseAbility(mp.getActiveAbilityForSkill(skillType));
	  if(!ab.isToggled() || !ab.getGenericAbility().isEnabled()){
		return;
	  }
	  String skill = "";
	  if(skillType == Skills.SWORDS){
		skill = "Sword";
	  }
	  else if(skillType == Skills.MINING){
		skill = "Pickaxe";
	  }
	  else if(skillType == Skills.UNARMED){
		skill = "Fist";
	  }
	  else if(skillType == Skills.HERBALISM){
		skill = "Hoe";
	  }
	  p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
		  Mcmmox.getInstance().getLangFile().getString("Messages.Players.PlayerReady").replace("%Skill_Item%", skill)));
	  PlayerReadyBit bit = new PlayerReadyBit(mp.getActiveAbilityForSkill(skillType), mp);
	  mp.setReadyingAbilityBit(bit);
	  mp.setReadying(true);
	}
  }
}
