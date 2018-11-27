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
import us.eunoians.mcmmox.abilities.mining.RemoteTransfer;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.types.Skills;
import us.eunoians.mcmmox.types.UnlockedAbilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SelectReplaceGUI extends GUI{

  @Getter
  private ArrayList<UnlockedAbilities> abilities;

  private GUIInventoryFunction buildGUIFunction;

  public SelectReplaceGUI(McMMOPlayer player, Skills skill){
	super(new GUIBuilder(player));
	this.abilities = new ArrayList<>();
	buildGUIFunction = (GUIBuilder builder) -> {
	  FileConfiguration config = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.getSkillFile(skill));
	  Inventory inv = Bukkit.createInventory(null, 9,
		  Methods.color("&5" + skill.getName()));
	  ArrayList<GUIItem> items = new ArrayList<>();
	  List<String> enabledAbilities = new ArrayList<>(skill.getEnabledAbilities());
	  enabledAbilities.remove(skill.getDefaultAbility().getName().replace(" ", ""));
	  int counter = 0;
	  for(UnlockedAbilities ab : enabledAbilities.stream().map(UnlockedAbilities::fromString).collect(Collectors.toList())){
		if(ab == null){
		  continue;
		}
		BaseAbility baseAbility = player.getBaseAbility(ab);
		String tier = "";
		if(baseAbility.isUnlocked()){
		  tier = Methods.convertToNumeral(baseAbility.getCurrentTier());
		}
		String path = ab.getName().replaceAll(" ", "") + "Config.Item.";

		ItemStack abilityItem = new ItemStack(Material.getMaterial(config.getString(path + "Material")),
			config.getInt(path + "Amount"));

		ItemMeta abilityMeta = abilityItem.getItemMeta();

		abilityMeta.setDisplayName(Methods.color(config.getString(path + "DisplayName") + " " + tier));

		abilityMeta.setLore(Methods.colorLore(config.getStringList(path + "PlayerLore")));
		ArrayList<String> lore = (ArrayList) abilityMeta.getLore();
		List<String> newLore = new ArrayList<>();
		for(String s : abilityMeta.getLore()){
		  if(baseAbility.getCurrentTier() != 0){
			for(String value : config.getConfigurationSection(ab.getName() + "Config.Tier" + Methods.convertToNumeral(baseAbility.getCurrentTier())).getKeys(false)){
			  s = s.replace("%" + value + "%", config.get(ab.getName() + "Config.Tier" + Methods.convertToNumeral(baseAbility.getCurrentTier()) + "." + value).toString());
			}
			newLore.add(s);
		  }
		}
		if(baseAbility instanceof RemoteTransfer){
		  List<String> newNewLore = new ArrayList<>();
		  RemoteTransfer remoteTransfer = (RemoteTransfer) baseAbility;
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
		if(player.getBaseAbility(ab).isUnlocked()){
		  newLore.add(Methods.color("&aUnlocked!"));
		  if(player.getAbilityLoadout().contains(ab)){
		    newLore.add(Methods.color("&aCurrently in loadout"));
		  }
		  else{
			newLore.add(Methods.color("&cNot in loadout"));
		  }
		}
		else{
		  newLore.add(Methods.color("&bNot Unlocked!"));
		  abilityItem.setType(Material.RED_STAINED_GLASS_PANE);
		}
		abilities.add(ab);
		abilityMeta.setLore(newLore);
		abilityItem.setItemMeta(abilityMeta);
		GUIItem item = new GUIItem(abilityItem, counter);
		counter++;
		items.add(item);
	  }

	  ItemStack back = new ItemStack(Material.BARRIER);
	  ItemMeta backMeta = back.getItemMeta();
	  backMeta.setDisplayName(Methods.color("&bBack>>"));
	  backMeta.setLore(Methods.colorLore(Arrays.asList("&eClick this to go back")));
	  back.setItemMeta(backMeta);
	  items.add(new GUIItem(back, inv.getSize() - 1));

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
