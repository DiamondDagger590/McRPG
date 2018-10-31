package us.eunoians.mcmmox.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.abilities.mining.RemoteTransfer;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RemoteTransferGUI extends GUI {

  @Getter
  private RemoteTransfer remoteTransfer;

  private GUIInventoryFunction buildGUIFunction;


  public RemoteTransferGUI(McMMOPlayer p, BaseAbility ability){
	super(new GUIBuilder(p));
	this.remoteTransfer = (RemoteTransfer) ability;
	if(!GUITracker.isPlayerTracked(p)){
	  GUITracker.trackPlayer(p, this);
	}

	buildGUIFunction = (GUIBuilder builder) -> {
	  FileConfiguration config = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.fromString(remoteTransfer.getGenericAbility().getSkill()));
	  Inventory inv = Bukkit.createInventory(null, config.getInt("RemoteTransferConfig.Tier" + Methods.convertToNumeral(remoteTransfer.getCurrentTier()) + ".InvSize"),
		  Methods.color("&eRemote Transfer Settings"));
	  ArrayList<GUIItem> items = new ArrayList<>();
	  HashMap<Material, Boolean> itemsToSync = remoteTransfer.getItemsToSync();
	  int counter = 0;
	  for(String cat : config.getStringList("RemoteTransferConfig.Tier" + Methods.convertToNumeral(remoteTransfer.getCurrentTier()) + ".Categories")){
		List<Material> blocksInCat = config.getStringList("RemoteTransferConfig.Categories." + cat).stream().map(Material::getMaterial).collect(Collectors.toList());
		for(Material mat : blocksInCat){
		  if(itemsToSync.containsKey(mat)){
			ItemStack displayItem = new ItemStack(mat, 1);
			ItemMeta meta = displayItem.getItemMeta();
			meta.setLore(Arrays.asList(Methods.color("&eToggle if this block should"), Methods.color("&ebe teleported by Remote Transfer")));
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			displayItem.setItemMeta(meta);
			if(itemsToSync.get(mat)){
			  displayItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
			}
			items.add(new GUIItem(displayItem, counter));
		  }
		  else{
			itemsToSync.put(mat, true);
			ItemStack displayItem = new ItemStack(mat, 1);
			ItemMeta meta = displayItem.getItemMeta();
			meta.setLore(Arrays.asList(Methods.color("&eToggle if this block should"), Methods.color("&ebe teleported by Remote Transfer")));
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			displayItem.setItemMeta(meta);
			displayItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
			items.add(new GUIItem(displayItem, counter));
		  }
		  counter++;
		}
	  }
	  p.saveData();


	  String path = ability.getGenericAbility().getName() + "Config.Item.";
	  ItemStack abilityItem = new ItemStack(Material.getMaterial(config.getString(path + "Material")),
		  config.getInt(path + "Amount"));
	  ItemMeta abilityMeta = abilityItem.getItemMeta();
	  abilityMeta.setDisplayName(Methods.color(config.getString(path + "DisplayName")));
	  abilityMeta.setLore(Methods.colorLore(Arrays.asList("&eToggle this ability")));
	  abilityMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
	  abilityItem.setItemMeta(abilityMeta);
	  if(remoteTransfer.isToggled()){
		abilityItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
	  }

	  items.add(new GUIItem(abilityItem, inv.getSize() - 1));
	  ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
	  ItemMeta fillerMeta = filler.getItemMeta();
	  fillerMeta.setDisplayName(" ");
	  filler.setItemMeta(fillerMeta);
	  inv = Methods.fillInventory(inv, filler, items);
	  return inv;
	};

	this.getGui().setBuildGUIFunction(buildGUIFunction);
	this.getGui().rebuildGUI();
  }
}
