package us.eunoians.mcmmox.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;

import java.util.ArrayList;
import java.util.Arrays;

public class AcceptAbilityGUI extends GUI{

  @Getter
  private BaseAbility ability;
  @Getter
  AcceptType acceptType;

  private GUIInventoryFunction buildGUIFunction;


  public AcceptAbilityGUI(McMMOPlayer p, BaseAbility ability, AcceptType acceptType){
	super(new GUIBuilder(p));
	this.ability = ability;
	this.acceptType = acceptType;
	if(!GUITracker.isPlayerTracked(p)){
	  GUITracker.trackPlayer(p, this);
	}
	if(acceptType == AcceptType.ACCEPT_ABILITY){
	  buildGUIFunction = (GUIBuilder builder) -> {
		FileConfiguration config = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.fromString(ability.getGenericAbility().getSkill()));
		Inventory inv = Bukkit.createInventory(null, 27,
			Methods.color("&eAccept/Decline &5" + ability.getGenericAbility().getName()));
		ArrayList<GUIItem> items = new ArrayList<>();
		String path = ability.getGenericAbility().getName() + "Config.Item.";

		ItemStack confirmItem = new ItemStack(Material.LIME_CONCRETE, 1);
		ItemMeta confirmMeta = confirmItem.getItemMeta();
		confirmMeta.setDisplayName(Methods.color("&aAccept this ability"));
		confirmMeta.setLore(Methods.colorLore(config.getStringList(path + "MenuLore")));
		confirmItem.setItemMeta(confirmMeta);
		items.add(new GUIItem(confirmItem, 10));

		ItemStack denyItem = new ItemStack(Material.RED_CONCRETE, 1);
		ItemMeta denyMeta = denyItem.getItemMeta();
		denyMeta.setDisplayName(Methods.color("&cDeny this ability"));
		denyMeta.setLore(Methods.colorLore(Arrays.asList("&cYou can replace an old ability with this one later")));
		denyItem.setItemMeta(denyMeta);
		items.add(new GUIItem(denyItem, 16));

		ItemStack abilityItem = new ItemStack(Material.getMaterial(config.getString(path + "Material")),
			config.getInt(path + "Amount"));
		ItemMeta abilityMeta = abilityItem.getItemMeta();
		abilityMeta.setDisplayName(Methods.color(config.getString(path + "DisplayName")));
		abilityMeta.setLore(Methods.colorLore(Arrays.asList("&eConfirm if you want this ability in", "&eyour loadout. If you confirm, ", "&ethe ability will go into any empty slot.",
			"&eOtherwise you will be asked", "&eto replace an ability you currently have")));
		abilityItem.setItemMeta(abilityMeta);
		items.add(new GUIItem(abilityItem, 13));

		ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
		ItemMeta fillerMeta = filler.getItemMeta();
		fillerMeta.setDisplayName(" ");
		filler.setItemMeta(fillerMeta);
		inv = Methods.fillInventory(inv, filler, items);
		return inv;
	  };
	}
	else{
	  buildGUIFunction = (GUIBuilder builder) -> {
		FileConfiguration config = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.fromString(ability.getGenericAbility().getSkill()));
		Inventory inv = Bukkit.createInventory(null, 27,
			Methods.color("&eUpgrade &5" + ability.getGenericAbility().getName() + " &eto Tier " + Methods.convertToNumeral(ability.getCurrentTier() + 1)));
		ArrayList<GUIItem> items = new ArrayList<>();

		ItemStack confirmItem = new ItemStack(Material.LIME_CONCRETE, 1);
		ItemMeta confirmMeta = confirmItem.getItemMeta();
		confirmMeta.setDisplayName(Methods.color("&aAccept this upgrade"));
		confirmItem.setItemMeta(confirmMeta);
		items.add(new GUIItem(confirmItem, 10));

		ItemStack denyItem = new ItemStack(Material.RED_CONCRETE, 1);
		ItemMeta denyMeta = denyItem.getItemMeta();
		denyMeta.setDisplayName(Methods.color("&cDeny this upgrade"));
		denyItem.setItemMeta(denyMeta);
		items.add(new GUIItem(denyItem, 16));

		String path = ability.getGenericAbility().getName() + "Config.Item.";
		ItemStack abilityItem = new ItemStack(Material.getMaterial(config.getString(path + "Material")),
			config.getInt(path + "Amount"));
		ItemMeta abilityMeta = abilityItem.getItemMeta();
		abilityMeta.setDisplayName(Methods.color(config.getString(path + "DisplayName")));
		abilityMeta.setLore(Methods.colorLore(Arrays.asList("&eUpgrade this ability by one tier.")));
		abilityItem.setItemMeta(abilityMeta);
		items.add(new GUIItem(abilityItem, 13));

		ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
		ItemMeta fillerMeta = filler.getItemMeta();
		fillerMeta.setDisplayName(" ");
		filler.setItemMeta(fillerMeta);
		inv = Methods.fillInventory(inv, filler, items);
		return inv;
	  };
	}
	this.getGui().setBuildGUIFunction(buildGUIFunction);
	this.getGui().rebuildGUI();
  }

  public enum AcceptType{
	ACCEPT_UPGRADE,
	ACCEPT_ABILITY
  }
}
