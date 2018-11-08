package us.eunoians.mcmmox.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;

import java.util.ArrayList;

public class EditLoadoutSelectGUI extends GUI {


  private GUIInventoryFunction buildGUIFunction;
  @Getter
  private ArrayList<BaseAbility> defaultAbilityList = new ArrayList<>();

  public EditLoadoutSelectGUI(McMMOPlayer player){
	super(new GUIBuilder(player));
	buildGUIFunction = (GUIBuilder builder) -> {
	  String title = Methods.color("&eSelect what to edit");
	  Inventory inv = Bukkit.createInventory(null, 27,
		  title);
	  ArrayList<GUIItem> items = new ArrayList<>();

	  ItemStack defaultAbilities = new ItemStack(Material.DIAMOND, 1);
	  ItemMeta defaultMeta = defaultAbilities.getItemMeta();
	  defaultMeta.setDisplayName(Methods.color("&bToggle Default Abilities"));
	  defaultAbilities.setItemMeta(defaultMeta);
	  items.add(new GUIItem(defaultAbilities, 10));

	  ItemStack replaceAbilties = new ItemStack(Material.CRAFTING_TABLE, 1);
	  ItemMeta replaceMeta = replaceAbilties.getItemMeta();
	  replaceMeta.setDisplayName(Methods.color("&bReplace Abilities"));
	  replaceAbilties.setItemMeta(replaceMeta);
	  items.add(new GUIItem(replaceAbilties, 13));

	  ItemStack unlockedAbilities = new ItemStack(Material.DIAMOND_BLOCK, 1);
	  ItemMeta unlockedMeta = unlockedAbilities.getItemMeta();
	  unlockedMeta.setDisplayName(Methods.color("&bToggle Unlocked Abilities"));
	  unlockedAbilities.setItemMeta(unlockedMeta);
	  items.add(new GUIItem(unlockedAbilities, 16));

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
