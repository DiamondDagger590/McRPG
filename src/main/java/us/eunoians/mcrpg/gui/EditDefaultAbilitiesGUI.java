package us.eunoians.mcrpg.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.skills.Skill;
import us.eunoians.mcrpg.types.Skills;

import java.util.ArrayList;

public class EditDefaultAbilitiesGUI extends GUI {

  private GUIInventoryFunction buildGUIFunction;
  @Getter
  private ArrayList<BaseAbility> defaultAbilityList = new ArrayList<>();

  public EditDefaultAbilitiesGUI(McRPGPlayer player){
	super(new GUIBuilder(player));
	buildGUIFunction = (GUIBuilder builder) -> {
	  String title = Methods.color("&eToggle Default Abilities");
	  int size = Skills.values().length;
	  if(size < 9){
	    size = 9;
	  }
	  int remainder = size % 9;
	  if(remainder != 0){
		size += (9 - remainder);
	  }
	  Inventory inv = Bukkit.createInventory(null, size,
		  title);
	  ArrayList<GUIItem> items = new ArrayList<>();
	  Skills[] skills = Skills.values();
	  for(int i = 0; i < skills.length; i++){
		Skill skill = player.getSkill(skills[i]);
		BaseAbility ability = skill.getDefaultAbility();
		defaultAbilityList.add(ability);
		FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.fromString(skill.getName()));
		String path = ability.getGenericAbility().getName().replaceAll(" " , "").replaceAll("_", "") + "Config.Item.";
		ItemStack abilityItem = new ItemStack(Material.getMaterial(config.getString(path + "Material")),
			config.getInt(path + "Amount"));
		ItemMeta abilityMeta = abilityItem.getItemMeta();
		abilityMeta.setDisplayName(Methods.color(config.getString(path + "DisplayName")));
		abilityMeta.setLore(Methods.colorLore(config.getStringList(path + "MenuLore")));
		ArrayList<String> lore = (ArrayList) abilityMeta.getLore();
		abilityMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		abilityMeta.setLore(lore);
		abilityItem.setItemMeta(abilityMeta);
		if(ability.isToggled()){
		  lore.add(Methods.color("&eToggled: &2&lON"));
		  abilityMeta.setLore(lore);
		  abilityItem.setItemMeta(abilityMeta);
		  abilityItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

		}
		else{
		  lore.add(Methods.color("&eToggled: &c&lOFF"));
		  abilityMeta.setLore(lore);
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
  }
}
