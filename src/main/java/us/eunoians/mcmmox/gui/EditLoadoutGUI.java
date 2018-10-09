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
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;

import java.util.ArrayList;
import java.util.List;

public class EditLoadoutGUI extends GUI{

  @Getter
  private EditType editType;
  @Getter //Only access if type is the override
  private BaseAbility replaceAbility;
  private GUIInventoryFunction buildGUIFunction;
  @Getter
  private ArrayList<BaseAbility> abilities;

  public EditLoadoutGUI(McMMOPlayer player, EditType type){
    super(new GUIBuilder(player));
	this.editType = type;
	buildGUIFunction = (GUIBuilder builder) -> {
	  //FileConfiguration config = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.fromString(ability.getGenericAbility().getSkill()));
	  Inventory inv = Bukkit.createInventory(null, 9,
		  Methods.color("&eEdit your ability loadout"));
	  ArrayList<GUIItem> items = new ArrayList<>();

	  for(int i = 0; i < player.getAbilityLoadout().size(); i++){
		BaseAbility ability = player.getAbilityLoadout().get(i);

		FileConfiguration config = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.fromString(ability.getGenericAbility().getSkill()));
		String path = ability.getGenericAbility().getName() + "Config.Item.";
		ItemStack abilityItem = new ItemStack(Material.getMaterial(config.getString(path + "Material")),
			config.getInt(path + "Amount"));
		ItemMeta abilityMeta = abilityItem.getItemMeta();
		String tier = "Tier" + Methods.convertToNumeral(ability.getCurrentTier());
		abilityMeta.setDisplayName(Methods.color(config.getString(path + "DisplayName") + " " + tier));
		abilityMeta.setLore(Methods.colorLore(config.getStringList(path + "Lore")));
		abilityMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		List<String> newLore = new ArrayList<>();
		for(String s : abilityMeta.getLore()){
		  for(String value : config.getConfigurationSection(ability.getGenericAbility().getName() + "Config." + tier).getKeys(false)){
		    s = s.replace("%" + value + "%", config.getString(ability.getGenericAbility().getName() +"Config." + tier + "." + value));
		  }
		  newLore.add(s);
		}
		abilityMeta.setLore(newLore);
		abilityItem.setItemMeta(abilityMeta);
		if(ability.isToggled()){
		  abilityItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		}
		items.add(new GUIItem(abilityItem, i));
	  }
	  ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
	  ItemMeta fillerMeta = filler.getItemMeta();
	  fillerMeta.setDisplayName(" ");
	  filler.setItemMeta(fillerMeta);
	  inv = Methods.fillInventory(inv, filler, items);
	  return inv;
	};
	this.getGui().setBuildGUIFunction(buildGUIFunction);
	this.getGui().rebuildGUI();
	this.abilities = player.getAbilityLoadout();
  }

  public EditLoadoutGUI(McMMOPlayer player, EditType type, BaseAbility replaceAbility){
	super(new GUIBuilder(player));
	this.editType = type;
	this.replaceAbility = replaceAbility;
	buildGUIFunction = (GUIBuilder builder) -> {
	  String invName;
	  if(editType == EditType.ABILITY_OVERRIDE){
	    invName = "&eOverride an ability with " + replaceAbility.getGenericAbility().getName();
	  }
	  else if(editType == EditType.ABILITY_UPGRADE){
	    invName = "&Upgrade an ability! You have " + player.getAbilityPoints() + " points to spend.";
	  }
	  else{
	    invName = "&eEdit your ability loadout";
	  }
	  //FileConfiguration config = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.fromString(ability.getGenericAbility().getSkill()));
	  Inventory inv = Bukkit.createInventory(null, 9,
		  Methods.color(invName));
	  ArrayList<GUIItem> items = new ArrayList<>();

	  for(int i = 0; i < player.getAbilityLoadout().size(); i++){
		BaseAbility ability = player.getAbilityLoadout().get(i);

		FileConfiguration config = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.fromString(ability.getGenericAbility().getSkill()));
		String path = ability.getGenericAbility().getName() + "Config.Item.";
		ItemStack abilityItem = new ItemStack(Material.getMaterial(config.getString(path + "Material")),
			config.getInt(path + "Amount"));
		ItemMeta abilityMeta = abilityItem.getItemMeta();
		String tier = "Tier" + Methods.convertToNumeral(ability.getCurrentTier());
		abilityMeta.setDisplayName(Methods.color(config.getString(path + "DisplayName") + " " + tier));
		abilityMeta.setLore(Methods.colorLore(config.getStringList(path + "Lore")));
		abilityMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		List<String> newLore = new ArrayList<>();
		for(String s : abilityMeta.getLore()){
		  for(String value : config.getConfigurationSection(ability.getGenericAbility().getName() + "Config." + tier).getKeys(false)){
			s = s.replace("%" + value + "%", config.getString(ability.getGenericAbility().getName() +"Config." + tier + "." + value));
		  }
		  newLore.add(s);
		}
		abilityMeta.setLore(newLore);
		abilityItem.setItemMeta(abilityMeta);
		if(ability.isToggled()){
		  abilityItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		}
		items.add(new GUIItem(abilityItem, i));
	  }
	  ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
	  ItemMeta fillerMeta = filler.getItemMeta();
	  fillerMeta.setDisplayName(" ");
	  filler.setItemMeta(fillerMeta);
	  inv = Methods.fillInventory(inv, filler, items);
	  return inv;
	};
	this.getGui().setBuildGUIFunction(buildGUIFunction);
	this.getGui().rebuildGUI();
	this.abilities = player.getAbilityLoadout();
  }

  public BaseAbility getAbilityFromSlot(int slot){
    return abilities.get(slot);
  }

  public enum EditType{
    TOGGLE,
	ABILITY_OVERRIDE,
	ABILITY_UPGRADE
  }
}
