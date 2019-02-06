package us.eunoians.mcrpg.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SubSkillGUI extends GUI{

  @Getter
  private Skills storedSkill;

  private GUIInventoryFunction buildGUIFunction;

  public SubSkillGUI(McRPGPlayer player, Skills skill){
    super(new GUIBuilder(player));
    this.storedSkill = skill;
	buildGUIFunction = (GUIBuilder builder) -> {
	  FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.fromString(skill.getName()));
	  FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SUBSKILL_GUI);
	  Inventory inv = Bukkit.createInventory(null, guiConfig.getInt("Size"),
		  Methods.color(guiConfig.getString("Title").replace("%Skill", skill.getName())));
	  ArrayList<GUIItem> items = new ArrayList<>();
	  List<String> enabledAbilities = new ArrayList<>(skill.getEnabledAbilities());
	  enabledAbilities.remove(skill.getDefaultAbility().getName().replace(" ", ""));
	  int counter = 0;
	  for(UnlockedAbilities ab : enabledAbilities.stream().map(UnlockedAbilities::fromString).collect(Collectors.toList())){
	    if(ab == null){
	      continue;
		}
		String path = ab.getName().replaceAll(" ", "") + "Config.Item.";
		ItemStack abilityItem = new ItemStack(Material.getMaterial(config.getString(path + "Material")),
			config.getInt(path + "Amount"));
		ItemMeta abilityMeta = abilityItem.getItemMeta();
		abilityMeta.setDisplayName(Methods.color(config.getString(path + "DisplayName")));
		List<String> lore = Methods.colorLore(config.getStringList(path + "MenuLore"));
		abilityMeta.setLore(lore);
		abilityItem.setItemMeta(abilityMeta);
		GUIItem item = new GUIItem(abilityItem, counter);
		counter++;
		items.add(item);
	  }

	  ItemStack back = new ItemStack(Material.valueOf(guiConfig.getString("BackButton.Material")));
	  ItemMeta backMeta = back.getItemMeta();
	  backMeta.setDisplayName(Methods.color(guiConfig.getString("BackButton.DisplayName")));
	  backMeta.setLore(Methods.colorLore(guiConfig.getStringList("BackButton.Lore")));
	  back.setItemMeta(backMeta);
	  items.add(new GUIItem(back, guiConfig.getInt("BackButton.Slot")));

	  ItemStack filler = new ItemStack(Material.valueOf(guiConfig.getString("FillerItem.Material")), guiConfig.getInt("FillerItem.Slot"));
	  ItemMeta fillerMeta = filler.getItemMeta();
	  fillerMeta.setDisplayName(Methods.color(guiConfig.getString("FillerItem.DisplayName")));
	  fillerMeta.setLore(Methods.colorLore(guiConfig.getStringList("FillerItem.Lore")));
	  filler.setItemMeta(fillerMeta);
	  return Methods.fillInventory(inv, filler, items);
	};
	this.getGui().setBuildGUIFunction(buildGUIFunction);
	this.getGui().rebuildGUI();
  }
}
