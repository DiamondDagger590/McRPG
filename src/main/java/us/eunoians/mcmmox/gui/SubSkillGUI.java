package us.eunoians.mcmmox.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.types.Skills;
import us.eunoians.mcmmox.types.UnlockedAbilities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SubSkillGUI extends GUI{

  @Getter
  private Skills storedSkill;

  private GUIInventoryFunction buildGUIFunction;

  public SubSkillGUI(McMMOPlayer player, Skills skill){
    super(new GUIBuilder(player));
    this.storedSkill = skill;
	buildGUIFunction = (GUIBuilder builder) -> {
	  FileConfiguration config = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.fromString(skill.getName()));
	  Inventory inv = Bukkit.createInventory(null, 9,
		  Methods.color("&5" + skill.getName()));
	  ArrayList<GUIItem> items = new ArrayList<>();
	  List<String> enabledAbilities = new ArrayList<>(skill.getEnabledAbilities());
	  enabledAbilities.remove(skill.getDefaultAbility().getName());
	  int counter = 0;
	  for(UnlockedAbilities ab : enabledAbilities.stream().map(UnlockedAbilities::fromString).collect(Collectors.toList())){
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
