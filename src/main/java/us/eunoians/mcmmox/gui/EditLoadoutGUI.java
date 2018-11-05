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
import us.eunoians.mcmmox.types.UnlockedAbilities;

import java.util.ArrayList;
import java.util.List;

public class EditLoadoutGUI extends GUI {

  @Getter
  private EditType editType;
  @Getter //Only access if type is the override
  private BaseAbility replaceAbility;
  private GUIInventoryFunction buildGUIFunction;
  @Getter
  private ArrayList<UnlockedAbilities> abilities;

  public EditLoadoutGUI(McMMOPlayer player, EditType type){
	super(new GUIBuilder(player));
	this.editType = type;
	buildGUIFunction = (GUIBuilder builder) -> {
	  //FileConfiguration config = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.fromString(ability.getGenericAbility().getSkill()));
	  String title = "";
	  if(type == EditType.TOGGLE){
		title = Methods.color("&eToggle abilities");
	  }
	  else if(type == EditType.ABILITY_UPGRADE){
		title = Methods.color("&eUpgrade Abilities: &a" + player.getAbilityPoints() + " &epoint(s)");
	  }
	  Inventory inv = Bukkit.createInventory(null, 9,
		  title);
	  ArrayList<GUIItem> items = new ArrayList<>();

	  for(int i = 0; i < player.getAbilityLoadout().size(); i++){
		UnlockedAbilities unlockedAbilities = player.getAbilityLoadout().get(i);
		BaseAbility ability = player.getBaseAbility(unlockedAbilities);

		FileConfiguration config = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.fromString(ability.getGenericAbility().getSkill()));
		String path = ability.getGenericAbility().getName() + "Config.Item.";
		ItemStack abilityItem = new ItemStack(Material.getMaterial(config.getString(path + "Material")),
			config.getInt(path + "Amount"));
		ItemMeta abilityMeta = abilityItem.getItemMeta();
		String tier = "Tier" + Methods.convertToNumeral(ability.getCurrentTier());
		abilityMeta.setDisplayName(Methods.color(config.getString(path + "DisplayName") + " " + tier));
		abilityMeta.setLore(Methods.colorLore(config.getStringList(path + "PlayerLore")));
		ArrayList<String> lore = (ArrayList) abilityMeta.getLore();
		for(String s : config.getConfigurationSection(ability.getGenericAbility().getName() + "Config." + tier).getKeys(false)){
		  for(int j = 0; j < lore.size(); j++){
			String l = lore.get(j).replace("%" + s + "%", config.getString(ability.getGenericAbility().getName() + "Config." + tier + "." + s));
			lore.set(j, l);
		  }
		}
		abilityMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		List<String> newLore = new ArrayList<>();
		for(String s : abilityMeta.getLore()){
		  for(String value : config.getConfigurationSection(ability.getGenericAbility().getName() + "Config." + tier).getKeys(false)){
			s = s.replace("%" + value + "%", config.get(ability.getGenericAbility().getName() + "Config." + tier + "." + value).toString());
		  }
		  newLore.add(s);
		}
		if(ability instanceof RemoteTransfer){
		  List<String> newNewLore = new ArrayList<>();
		  RemoteTransfer remoteTransfer = (RemoteTransfer) ability;
		  if(remoteTransfer.getLinkedChestLocation() == null){
			for(String s : newLore){
			  s = s.replace("%Location%", "None");
			  newNewLore.add(s);
			}
		  }
		  else{
			for(String s : newLore){
			  s = s.replace("%Location%", "X:" + remoteTransfer.getLinkedChestLocation().getBlockX() + " Y:" + remoteTransfer.getLinkedChestLocation().getBlockY()
			  + " Z:" + remoteTransfer.getLinkedChestLocation().getBlockZ());
			  newNewLore.add(s);
			}
		  }
		  newLore = newNewLore;
		}
		if(type == EditType.ABILITY_UPGRADE){
		  if(ability.getCurrentTier() == 5){
			newLore.add(Methods.color("&5You have maxed this ability out!"));
		  }
		  else{
			newLore.add(Methods.color("&6You must be at least level &a" + ((UnlockedAbilities) ability.getGenericAbility()).tierUnlockLevel(ability.getCurrentTier() + 1)));
			newLore.add(Methods.color("&6to upgrade this ability to Tier " + Methods.convertToNumeral(ability.getCurrentTier() + 1)));
		  }
		}
		abilityMeta.setLore(newLore);
		abilityItem.setItemMeta(abilityMeta);
		if(ability.isToggled()){
		  newLore.add(Methods.color("&eToggled: &2&lON"));
		  abilityMeta.setLore(newLore);
		  abilityItem.setItemMeta(abilityMeta);
		  abilityItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

		}
		else{
		  newLore.add(Methods.color("&eToggled: &c&lOFF"));
		  abilityMeta.setLore(newLore);
		  abilityItem.setItemMeta(abilityMeta);
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
		UnlockedAbilities unlockedAbilities = player.getAbilityLoadout().get(i);
		BaseAbility ability = player.getBaseAbility(unlockedAbilities);

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
			s = s.replace("%" + value + "%", config.getString(ability.getGenericAbility().getName() + "Config." + tier + "." + value));
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

  public UnlockedAbilities getAbilityFromSlot(int slot){
	return abilities.get(slot);
  }

  public enum EditType {
	TOGGLE,
	ABILITY_OVERRIDE,
	ABILITY_UPGRADE
  }
}
