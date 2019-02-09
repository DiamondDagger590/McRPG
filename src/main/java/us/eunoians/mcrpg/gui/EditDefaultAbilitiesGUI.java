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
import java.util.List;

public class EditDefaultAbilitiesGUI extends GUI {

  private GUIInventoryFunction buildGUIFunction;
  @Getter
  private ArrayList<BaseAbility> defaultAbilityList = new ArrayList<>();

  public EditDefaultAbilitiesGUI(McRPGPlayer player) {
    super(new GUIBuilder(player));
    buildGUIFunction = (GUIBuilder builder) -> {
      FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.EDIT_DEFAULT_ABILITIES_GUI);
      String title = Methods.color(player.getPlayer(), guiConfig.getString("Title"));
      Inventory inv = Bukkit.createInventory(null, guiConfig.getInt("Size"),
              title);
      ArrayList<GUIItem> items = new ArrayList<>();
      Skills[] skills = Skills.values();
      for(int i = 0; i < skills.length; i++) {
        if(skills[i] == Skills.ARCHERY) {
          continue;
        }
        Skill skill = player.getSkill(skills[i]);
        BaseAbility ability = skill.getDefaultAbility();
        defaultAbilityList.add(ability);
        FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.fromString(skill.getName()));
        String path = ability.getGenericAbility().getName().replace(" ", "").replace("_", "") + "Config.Item.";
        ItemStack abilityItem = new ItemStack(Material.getMaterial(config.getString(path + "Material")),
                config.getInt(path + "Amount"));
        ItemMeta abilityMeta = abilityItem.getItemMeta();
        abilityMeta.setDisplayName(Methods.color(player.getPlayer(), config.getString(path + "DisplayName")));
        abilityMeta.setLore(Methods.colorLore(config.getStringList(path + "MenuLore")));
        List<String> lore = (abilityMeta.getLore());
        abilityMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        abilityMeta.setLore(lore);
        abilityItem.setItemMeta(abilityMeta);
        if(ability.isToggled()) {
          lore.add(Methods.color(player.getPlayer(), guiConfig.getString("AbilityItems.ToggledOn")));
          abilityMeta.setLore(lore);
          abilityItem.setItemMeta(abilityMeta);
          abilityItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        }
        else {
          lore.add(Methods.color(guiConfig.getString("AbilityItems.ToggledOff")));
          abilityMeta.setLore(lore);
          abilityItem.setItemMeta(abilityMeta);
        }
        items.add(new GUIItem(abilityItem, i));
      }
      ItemStack filler = new ItemStack(Material.valueOf(guiConfig.getString("FillerItem.Material")), guiConfig.getInt("FillerItem.Amount"));
      ItemMeta fillerMeta = filler.getItemMeta();
      fillerMeta.setDisplayName(Methods.color(player.getPlayer(), guiConfig.getString("FillerItem.DisplayName")));
      fillerMeta.setLore(Methods.colorLore(guiConfig.getStringList("FillerItem.Lore")));
      filler.setItemMeta(fillerMeta);

      ItemStack back = new ItemStack(Material.valueOf(guiConfig.getString("BackButton.Material")));
      ItemMeta backMeta = back.getItemMeta();
      backMeta.setDisplayName(Methods.color(player.getPlayer(), guiConfig.getString("BackButton.DisplayName")));
      backMeta.setLore(Methods.colorLore(guiConfig.getStringList("BackButton.Lore")));
      back.setItemMeta(backMeta);
      items.add(new GUIItem(back, guiConfig.getInt("BackButton.Slot")));

      return Methods.fillInventory(inv, filler, items);
    };
    this.getGui().setBuildGUIFunction(buildGUIFunction);
    this.getGui().rebuildGUI();
  }
}
