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
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.ArrayList;
import java.util.List;

public class EditLoadoutGUI extends GUI{
  
  @Getter
  private EditType editType;
  @Getter //Only access if type is the override
  private BaseAbility replaceAbility;
  private GUIInventoryFunction buildGUIFunction;
  @Getter
  private ArrayList<UnlockedAbilities> abilities;
  
  public EditLoadoutGUI(McRPGPlayer player, EditType type){
    super(new GUIBuilder(player));
    this.editType = type;
    buildGUIFunction = (GUIBuilder builder) -> {
      FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.EDIT_LOADOUT_GUI);
      //FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.fromString(ability.getGenericAbility().getSkill()));
      String title = "";
      if(type == EditType.TOGGLE){
        title = Methods.color(player.getPlayer(), guiConfig.getString("Title.Toggle"));
      }
      else if(type == EditType.ABILITY_UPGRADE){
        title = Methods.color(player.getPlayer(), guiConfig.getString("Title.Upgrade").replace("%AbilityPoints%", Integer.toString(player.getAbilityPoints())));
      }
      int totAbilities = McRPG.getInstance().getConfig().getInt("PlayerConfiguration.AmountOfTotalAbilities");
      int size = totAbilities % 9 != 0 ? totAbilities - (totAbilities % 9) + 9 : totAbilities;
      Inventory inv = Bukkit.createInventory(null, size,
        title);
      ArrayList<GUIItem> items = new ArrayList<>();
      
      for(int i = 0; i < player.getAbilityLoadout().size(); i++){
        UnlockedAbilities unlockedAbilities = player.getAbilityLoadout().get(i);
        BaseAbility ability = player.getBaseAbility(unlockedAbilities);
        
        FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.fromString(ability.getGenericAbility().getSkill().getName()));
        String path = ability.getGenericAbility().getName() + "Config.Item.";
        ItemStack abilityItem = new ItemStack(Material.getMaterial(config.getString(path + "Material")),
          config.getInt(path + "Amount"));
        ItemMeta abilityMeta = abilityItem.getItemMeta();
        String tier = "Tier" + Methods.convertToNumeral(ability.getCurrentTier());
        abilityMeta.setDisplayName(Methods.color(player.getPlayer(), config.getString(path + "DisplayName") + " " + tier));
        abilityMeta.setLore(Methods.colorLore(config.getStringList(path + "PlayerLore")));
        List<String> lore = abilityMeta.getLore();
		/*for(String s : config.getConfigurationSection(ability.getGenericAbility().getName() + "Config." + tier).getKeys(false)){
		  for(int j = 0; j < lore.size(); j++){
			String l = lore.get(j).replace("%" + s + "%", config.getString(ability.getGenericAbility().getName() + "Config." + tier + "." + s));
			lore.set(j, l);
		  }
		}*/
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
          if(ability.getCurrentTier() >= ((UnlockedAbilities) ability.getGenericAbility()).getMaxTier()){
            for(String s : guiConfig.getStringList("AbilityItem.MaxedLore")){
              newLore.add(Methods.color(player.getPlayer(), s));
            }
          }
          else{
            for(String s : guiConfig.getStringList("AbilityItem.LevelPromptLore")){
              s = s.replace("%Level%", Integer.toString(((UnlockedAbilities) ability.getGenericAbility()).tierUnlockLevel(ability.getCurrentTier() + 1)));
              s = s.replace("%Tier%", Methods.convertToNumeral(ability.getCurrentTier() + 1));
              s = s.replace("%Skill%", ability.getGenericAbility().getSkill().getDisplayName());
              newLore.add(Methods.color(player.getPlayer(), s));
            }
          }
        }
        abilityMeta.setLore(newLore);
        abilityItem.setItemMeta(abilityMeta);
        if(ability.isToggled()){
          for(String s : guiConfig.getStringList("AbilityItem.ToggledOnLore")){
            newLore.add(Methods.color(player.getPlayer(), s));
          }
          abilityMeta.setLore(newLore);
          abilityItem.setItemMeta(abilityMeta);
          abilityItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
          
        }
        else{
          for(String s : guiConfig.getStringList("AbilityItem.ToggledOffLore")){
            newLore.add(Methods.color(player.getPlayer(), s));
          }
          abilityMeta.setLore(newLore);
          abilityItem.setItemMeta(abilityMeta);
        }
        items.add(new GUIItem(abilityItem, i));
      }
      ItemStack filler = new ItemStack(Material.AIR);
      if(guiConfig.contains("FillerItem")){
        filler = new ItemStack(Material.valueOf(guiConfig.getString("FillerItem.Material")), guiConfig.getInt("FillerItem.Amount"));
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(Methods.color(player.getPlayer(), guiConfig.getString("FillerItem.DisplayName")));
        fillerMeta.setLore(Methods.colorLore(guiConfig.getStringList("FillerItem.Lore")));
        filler.setItemMeta(fillerMeta);
      }
      return Methods.fillInventory(inv, filler, items);
    };
    this.getGui().setBuildGUIFunction(buildGUIFunction);
    this.getGui().rebuildGUI();
    this.abilities = player.getAbilityLoadout();
  }
  
  public EditLoadoutGUI(McRPGPlayer player, EditType type, BaseAbility replaceAbility){
    super(new GUIBuilder(player));
    this.editType = type;
    this.replaceAbility = replaceAbility;
    buildGUIFunction = (GUIBuilder builder) -> {
      String invName;
      if(editType == EditType.ABILITY_OVERRIDE || editType == EditType.ABILITY_REPLACE){
        invName = "&eOverride an ability with " + replaceAbility.getGenericAbility().getName();
      }
      else if(editType == EditType.ABILITY_UPGRADE){
        invName = "&Upgrade an ability! You have " + player.getAbilityPoints() + " points to spend.";
      }
      else{
        invName = "&eEdit your ability loadout";
      }
      //FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.fromString(ability.getGenericAbility().getSkill()));
      int totAbilities = McRPG.getInstance().getConfig().getInt("PlayerConfiguration.AmountOfTotalAbilities");
      int size = totAbilities % 9 != 0 ? totAbilities - (totAbilities % 9) + 9 : totAbilities;
      Inventory inv = Bukkit.createInventory(null, size,
        Methods.color(player.getPlayer(), invName));
      ArrayList<GUIItem> items = new ArrayList<>();
      
      for(int i = 0; i < player.getAbilityLoadout().size(); i++){
        UnlockedAbilities unlockedAbilities = player.getAbilityLoadout().get(i);
        BaseAbility ability = player.getBaseAbility(unlockedAbilities);
        
        FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.fromString(ability.getGenericAbility().getSkill().getName()));
        String path = ability.getGenericAbility().getName() + "Config.Item.";
        ItemStack abilityItem = new ItemStack(Material.getMaterial(config.getString(path + "Material")),
          config.getInt(path + "Amount"));
        ItemMeta abilityMeta = abilityItem.getItemMeta();
        String tier = "Tier" + Methods.convertToNumeral(ability.getCurrentTier());
        abilityMeta.setDisplayName(Methods.color(player.getPlayer(), config.getString(path + "DisplayName") + " " + tier));
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
  
  public enum EditType{
    TOGGLE,
    ABILITY_OVERRIDE,
    ABILITY_UPGRADE,
    ABILITY_REPLACE
  }
}
