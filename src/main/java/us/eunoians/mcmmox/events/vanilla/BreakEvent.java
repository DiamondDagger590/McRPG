package us.eunoians.mcmmox.events.vanilla;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.mining.DoubleDrop;
import us.eunoians.mcmmox.abilities.mining.ItsATriple;
import us.eunoians.mcmmox.abilities.mining.RemoteTransfer;
import us.eunoians.mcmmox.abilities.mining.RicherOres;
import us.eunoians.mcmmox.api.events.mcmmo.DoubleDropEvent;
import us.eunoians.mcmmox.api.events.mcmmo.ItsATripleEvent;
import us.eunoians.mcmmox.api.events.mcmmo.RicherOresEvent;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;
import us.eunoians.mcmmox.types.DefaultAbilities;
import us.eunoians.mcmmox.types.GainReason;
import us.eunoians.mcmmox.types.Skills;
import us.eunoians.mcmmox.types.UnlockedAbilities;
import us.eunoians.mcmmox.util.Parser;

import java.util.*;
import java.util.stream.Collectors;

public class BreakEvent implements Listener {

  @EventHandler
  @SuppressWarnings("Duplicates")
  public void breakEvent(BlockBreakEvent event){
	if(!event.isCancelled()){

	  Player p = event.getPlayer();
	  Block block = event.getBlock();
	  McMMOPlayer mp = PlayerManager.getPlayer((p).getUniqueId());
	  FileConfiguration mining = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.MINING_CONFIG);

	  //Deal with mining
	  if(mining.getBoolean("MiningEnabled")){
		if(!Mcmmox.getPlaceStore().isTrue(block)){
		  if(p.getItemInHand().getType().toString().contains("PICK") && mining.contains("ExpAwardedPerBlock." + block.getType().toString())){
			int expWorth = mining.getInt("ExpAwardedPerBlock." + block.getType().toString());

			mp.giveExp(Skills.MINING, expWorth, GainReason.BREAK);
		  }
		}

		int dropMultiplier = 1;
		if(DefaultAbilities.DOUBLE_DROP.isEnabled() && mp.getSkill(Skills.MINING).getAbility(DefaultAbilities.DOUBLE_DROP).isToggled()){
		  DoubleDrop doubleDrop = (DoubleDrop) mp.getSkill(Skills.MINING).getAbility(DefaultAbilities.DOUBLE_DROP);
		  if(UnlockedAbilities.RICHER_ORES.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.RICHER_ORES)
			  && mp.getSkill(Skills.MINING).getAbility(UnlockedAbilities.RICHER_ORES).isToggled()){
			RicherOres richerOres = (RicherOres) mp.getSkill(Skills.MINING).getAbility(UnlockedAbilities.RICHER_ORES);
			RicherOresEvent richerOresEvent = new RicherOresEvent(mp, richerOres);
			Bukkit.getPluginManager().callEvent(richerOresEvent);
			if(!richerOresEvent.isCancelled()){
			  double boost = mining.getDouble("RicherOresConfig.Tier" + Methods.convertToNumeral(richerOres.getCurrentTier()) + ".ActivationBoost");
			  doubleDrop.setBonusChance(doubleDrop.getBonusChance() + boost);
			}
		  }

		  Parser parser = DefaultAbilities.DOUBLE_DROP.getActivationEquation();
		  parser.setVariable("swords_level", mp.getSkill(Skills.MINING).getCurrentLevel());
		  parser.setVariable("power_level", mp.getPowerLevel());
		  int chance = (int) (parser.getValue() + doubleDrop.getBonusChance()) * 1000;
		  Random rand = new Random();
		  int val = rand.nextInt(100000);
		  if(chance >= val){
			DoubleDropEvent doubleDropEvent = new DoubleDropEvent(mp, doubleDrop);
			Bukkit.getPluginManager().callEvent(doubleDropEvent);
			if(!doubleDropEvent.isCancelled()){
			  dropMultiplier = 2;
			}
		  }
		}

		if(UnlockedAbilities.ITS_A_TRIPLE.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.ITS_A_TRIPLE)
			&& mp.getSkill(Skills.MINING).getAbility(UnlockedAbilities.ITS_A_TRIPLE).isToggled()){
		  ItsATriple itsATriple = (ItsATriple) mp.getSkill(Skills.MINING).getAbility(UnlockedAbilities.ITS_A_TRIPLE);
		  int chance = (int) mining.getDouble("ItsATripleConfig.Tier" + Methods.convertToNumeral(itsATriple.getCurrentTier()) + ".ActivationChance") * 1000;
		  Random rand = new Random();
		  int val = rand.nextInt(100000);
		  if(chance >= val){
			ItsATripleEvent itsATripleEvent = new ItsATripleEvent(mp, itsATriple);
			Bukkit.getPluginManager().callEvent(itsATripleEvent);
			if(!itsATripleEvent.isCancelled()){
			  dropMultiplier = 3;
			}
		  }
		}
		//Check if the block is tracked by remote transfer
		if(block.getType() == Material.CHEST && Mcmmox.getInstance().getRemoteTransferTracker().isTracked(event.getBlock().getLocation())){
		  UUID uuid = Mcmmox.getInstance().getRemoteTransferTracker().getUUID(event.getBlock().getLocation());
		  if(p.getUniqueId().equals(uuid)){
			if(!Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.MINING_CONFIG).getBoolean("RemoteTransferConfig.UnlinkAndBreakOnMine")){
			  event.setCancelled(true);
			}
			RemoteTransfer remoteTransfer = (RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER);
			remoteTransfer.setLinkedChestLocation(null);
			mp.setLinkedToRemoteTransfer(false);
			Mcmmox.getInstance().getRemoteTransferTracker().removeLocation(uuid);
			p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.RemoteTransfer.Unlinked")));
		  }
		  else{
			//TODO will need to add support for admins to remove these such as towny mayors
			if(p.hasPermission("mcadmin.*") || p.hasPermission("mcadmin.unlink")){
			  McMMOPlayer target;
			  OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
			  if(offlinePlayer.isOnline()){
				target = PlayerManager.getPlayer(uuid);
				target.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.RemoteTransfer.AdminUnlinked")));
			  }
			  else{
				target = new McMMOPlayer(uuid);
			  }
			  target.setLinkedToRemoteTransfer(false);
			  ((RemoteTransfer) target.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER)).setLinkedChestLocation(null);
			  Mcmmox.getInstance().getRemoteTransferTracker().removeLocation(uuid);
			  if(!Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.MINING_CONFIG).getBoolean("RemoteTransferConfig.UnlinkAndBreakOnMine")){
				event.setCancelled(true);
			  }
			}
			else{
			  event.setCancelled(true);
			  p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.RemoteTransfer.IsLinked")
				  .replace("%Player%", Bukkit.getOfflinePlayer(uuid).getName())));
			  return;
			}
		  }
		}

		else if(UnlockedAbilities.REMOTE_TRANSFER.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.REMOTE_TRANSFER)
			&& mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER).isToggled() && mp.isLinkedToRemoteTransfer()){
		  RemoteTransfer transfer = (RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER);
		  int tier = transfer.getCurrentTier();
		  int range = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.MINING_CONFIG).getInt("RemoteTransferConfig.Tier" + Methods.convertToNumeral(tier) + ".Range");
		  if(!(block.getLocation().distance(transfer.getLinkedChestLocation()) <= range)){
			return;
		  }
		  //Get the chest or unlink if it doesnt exist.
		  Chest chest;
		  Block block2 = transfer.getLinkedChestLocation().getBlock();
		  if(block2.getType() == Material.CHEST){
			chest = (Chest) block2.getState();
		  }
		  else{
			p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.RemoteTransfer.ChestMissing")));
			transfer.setLinkedChestLocation(null);
			mp.setLinkedToRemoteTransfer(false);
			mp.saveData();
			return;
		  }
		  //Get chest inv
		  Inventory inv = chest.getBlockInventory();
		  Collection<ItemStack> itemsToDrop = block.getDrops(p.getItemInHand());
		  //Iterate across dropped materials
		  for(Material mat : itemsToDrop.stream().map(ItemStack::getType).collect(Collectors.toList())){
			//If the item needs to be transferred and is toggled for transferring
			if(transfer.getItemsToSync().keySet().contains(mat) && transfer.getItemsToSync().get(mat)){
			  //Apply fortune and silk touch
			  ItemStack item = getDropsFromMaterial(mat, p.getItemInHand(), dropMultiplier);
			  //Get the material of the item we are putting in the chest and the amount
			  if(mat != Material.COBBLESTONE){
				mat = item.getType();
			  }
			  int amount = item.getAmount();
			  //if the chest contents are full, check if there are any stacks we can increase before dropping
			  for(int i = 0; i < inv.getSize(); i++){
				//if the amount is no longer positive then we are done with this item
				if(amount <= 0){
				  break;
				}
				//Get the current item per iteration
				ItemStack currentItem = inv.getItem(i);
				//If the slot is empty
				if(currentItem == null || currentItem.getType() == Material.AIR){
				  ItemStack newStack = new ItemStack(mat);
				  //if the amount is greater than a stack
				  if(amount > 64){
					newStack.setAmount(64);
					amount -= 64;
					inv.setItem(i, newStack);
					continue;
				  }
				  //Otherwise just slap the item in there and break since we dont need to put it anywhere else
				  else{
					newStack.setAmount(amount);
					inv.setItem(i, newStack);
					amount = 0;
					break;
				  }
				}
				else if(currentItem.getType() == mat){
				  if(currentItem.getAmount() == 64){
					continue;
				  }
				  else{
					if(currentItem.getAmount() + amount > 64){
					  amount -= 64 - currentItem.getAmount();
					  currentItem.setAmount(64);
					  continue;
					}
					else{
					  currentItem.setAmount(currentItem.getAmount() + amount);
					  amount = 0;
					  break;
					}
				  }
				}
				else{
				  continue;
				}
			  }
			  block2.getState().update();
			  block.setType(Material.AIR);
			  //Drop leftovers
			  if(amount > 0){
				block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(mat, amount));
			  }
			  event.setDropItems(false);
			}
		  }
		}
	  }


	}

  }

  private ItemStack getDropsFromMaterial(Material mat, ItemStack tool, int multiplier){
	ItemStack returnItem = new ItemStack(mat, 1);
	Map<Enchantment, Integer> enchants = tool.getEnchantments();
	if(enchants.keySet().contains(Enchantment.LOOT_BONUS_BLOCKS) && FortuneBlocks.isFortunable(mat)){
	  int level = enchants.get(Enchantment.LOOT_BONUS_BLOCKS);
	  int dropAmount = getDropCount(mat, level, new Random()) * multiplier;
	  returnItem.setAmount(dropAmount);
	}
	if(enchants.keySet().contains(Enchantment.SILK_TOUCH) && SilkBlocks.isSilked(mat)){
	  Material newMat = SilkBlocks.getSilkBlock(mat);
	  returnItem.setType(newMat);
	}

	return returnItem;
  }


  private int a(Material material, Random random){
	return material == Material.LAPIS_ORE ? 4 + random.nextInt(5) : 1;
  }

  private int getDropCount(Material mat, int fortuneLevel, Random random){
	if((fortuneLevel > 0)){
	  int drops = random.nextInt(fortuneLevel + 2) - 1;
	  if(drops < 0){
		drops = 0;
	  }
	  return a(mat, random) * (drops + 1);
	}
	return a(mat, random);
  }

  private enum FortuneBlocks {
	COAL_ORE(Material.COAL),
	DIAMOND_ORE(Material.DIAMOND),
	EMERALD_ORE(Material.EMERALD),
	LAPIS_ORE(Material.LAPIS_LAZULI),
	REDSTONE_ORE(Material.REDSTONE);

	@Getter
	private Material material;

	FortuneBlocks(Material mat){
	  this.material = mat;
	}

	public static boolean isFortunable(Material mat){
	  return (Arrays.stream(values()).anyMatch(fortuneBlocks -> fortuneBlocks.material.equals(mat)));
	}
  }

  private enum SilkBlocks {
	COAL_ORE(Material.COAL, Material.COAL_ORE),
	DIAMOND_ORE(Material.DIAMOND, Material.DIAMOND_ORE),
	EMERALD_ORE(Material.EMERALD, Material.EMERALD_ORE),
	LAPIS_ORE(Material.LAPIS_LAZULI, Material.LAPIS_ORE),
	REDSTONE_ORE(Material.REDSTONE, Material.REDSTONE_ORE),
	STONE(Material.COBBLESTONE, Material.STONE);

	@Getter
	private Material unsilkedMat;

	@Getter
	private Material silkedMat;

	SilkBlocks(Material mat, Material silkedMat){
	  this.unsilkedMat = mat;
	  this.silkedMat = silkedMat;
	}

	public static boolean isSilked(Material mat){
	  return (Arrays.stream(values()).anyMatch(silkBlocks -> silkBlocks.unsilkedMat.equals(mat)));
	}

	public static Material getSilkBlock(Material mat){
	  Material result = Objects.requireNonNull(Arrays.stream(values()).filter(silkBlocks -> silkBlocks.unsilkedMat.equals(mat)).findFirst().orElse(null)).getSilkedMat();
	  Bukkit.broadcastMessage(result.toString());
	  return result;
	}
  }

}
