package us.eunoians.mcrpg.gui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
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
import java.util.stream.Collectors;

public class SelectReplaceGUI extends GUI {

  @Getter
  private ArrayList<UnlockedAbilities> abilities;

  private GUIInventoryFunction buildGUIFunction;

  public SelectReplaceGUI(McRPGPlayer player, Skills skill) {
    super(new GUIBuilder(player));
    this.abilities = new ArrayList<>();
    buildGUIFunction = (GUIBuilder builder) -> {
      FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.getSkillFile(skill));
      FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SELECT_REPLACE_SKILLS_GUI);
      Inventory inv = Bukkit.createInventory(null, guiConfig.getInt("Size"),
              Methods.color(player.getPlayer(), guiConfig.getString("Title").replace("%Skill%", skill.getDisplayName())));
      ArrayList<GUIItem> items = new ArrayList<>();

      List<String> enabledAbilities = new ArrayList<>(skill.getEnabledAbilities());
      //Default abilities dont matter so exclude it
      enabledAbilities.remove(skill.getDefaultAbility().getName().replace(" ", ""));

      int counter = 0;
      for(UnlockedAbilities ab : enabledAbilities.stream().map(UnlockedAbilities::fromString).collect(Collectors.toList())) {
        if(ab == null) {
          continue;
        }
        BaseAbility baseAbility = player.getBaseAbility(ab);
        String tier = "";
        if(baseAbility.isUnlocked()) {
          tier = Methods.convertToNumeral(baseAbility.getCurrentTier());
        }
        String path = ab.getName().replaceAll(" ", "") + "Config.Item.";
        ItemStack abilityItem = new ItemStack(Material.getMaterial(config.getString(path + "Material")),
                config.getInt(path + "Amount"));
        ItemMeta abilityMeta = abilityItem.getItemMeta();

        abilityMeta.setDisplayName(Methods.color(player.getPlayer(), config.getString(path + "DisplayName") + " " + tier));

        abilityMeta.setLore(Methods.colorLore(config.getStringList(path + "PlayerLore")));
        List<String> newLore = new ArrayList<>();
        //Move strings into new array
        for(String s : abilityMeta.getLore()) {
          if(baseAbility.getCurrentTier() != 0) {
            for(String value : config.getConfigurationSection(ab.getName() + "Config.Tier" + Methods.convertToNumeral(baseAbility.getCurrentTier())).getKeys(false)) {
              s = s.replace("%" + value + "%", config.get(ab.getName() + "Config.Tier" + Methods.convertToNumeral(baseAbility.getCurrentTier()) + "." + value).toString());
            }
            newLore.add(s);
          }
        }
        //Handle special ability
        if(baseAbility instanceof RemoteTransfer) {
          List<String> newNewLore = new ArrayList<>();
          RemoteTransfer remoteTransfer = (RemoteTransfer) baseAbility;
          if(remoteTransfer.getLinkedChestLocation() == null) {
            for(String s : newLore) {
              s = s.replace("%Location%", guiConfig.getString("RemoteTransfer.NoLocation"));
              newNewLore.add(s);
            }
          }
          else {
            for(String s : newLore) {
              s = s.replace("%Location%", "X:" + remoteTransfer.getLinkedChestLocation().getBlockX() + " Y:" + remoteTransfer.getLinkedChestLocation().getBlockY()
                      + " Z:" + remoteTransfer.getLinkedChestLocation().getBlockZ());
              newNewLore.add(s);
            }
          }
          newLore = newNewLore;
        }
        //Append gui specific lore
        if(player.getBaseAbility(ab).isUnlocked()) {
          newLore.add(Methods.color(player.getPlayer(), guiConfig.getString("Ability.IsUnlocked")));
          if(player.getAbilityLoadout().contains(ab)) {
            newLore.add(Methods.color(player.getPlayer(), guiConfig.getString("Ability.IsInLoadout")));
          }
          else {
            newLore.add(Methods.color(player.getPlayer(), guiConfig.getString("Ability.IsNotInLoadout")));
          }
        }
        else {
          newLore.add(Methods.color(player.getPlayer(), guiConfig.getString("Ability.IsNotUnlocked")));
          abilityItem = new ItemStack(Material.valueOf(guiConfig.getString("Ability.NotUnlockedMaterial")));
          String displayName = abilityMeta.getDisplayName();
          abilityMeta = abilityItem.getItemMeta();
          abilityMeta.setDisplayName(displayName);

          //abilityItem.setType(Material.valueOf(guiConfig.getString("Ability.NotUnlockedMaterial")));
        }

        //Debugg
        abilities.add(ab);
        abilityMeta.setLore(newLore);
        abilityItem.setItemMeta(abilityMeta);
        GUIItem item = new GUIItem(abilityItem, counter);
        counter++;
        items.add(item);
      }

      //TODO custom back button
      ItemStack back = new ItemStack(Material.valueOf(guiConfig.getString("BackButton.Material")));
      ItemMeta backMeta = back.getItemMeta();
      backMeta.setDisplayName(Methods.color(player.getPlayer(), guiConfig.getString("BackButton.DisplayName")));
      backMeta.setLore(Methods.colorLore(guiConfig.getStringList("BackButton.Lore")));
      back.setItemMeta(backMeta);
      items.add(new GUIItem(back, guiConfig.getInt("BackButton.Slot")));

      ItemStack filler = new ItemStack(Material.valueOf(guiConfig.getString("FillerItem.Material")), guiConfig.getInt("FillerItem.Amount"));
      ItemMeta fillerMeta = filler.getItemMeta();
      fillerMeta.setDisplayName(guiConfig.getString("FillerItem.DisplayName"));
      fillerMeta.setLore(Methods.colorLore(guiConfig.getStringList("FillerItem.Lore")));
      filler.setItemMeta(fillerMeta);

      return Methods.fillInventory(inv, filler, items);
    };
    this.getGui().setBuildGUIFunction(buildGUIFunction);
    this.getGui().rebuildGUI();
  }
}
