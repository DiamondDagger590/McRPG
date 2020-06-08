package us.eunoians.mcrpg.gui;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.fishing.MagicTouch;
import us.eunoians.mcrpg.abilities.fishing.SeaGodsBlessing;
import us.eunoians.mcrpg.abilities.fishing.SunkenArmory;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.DefaultAbilities;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.Parser;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class ReplaceSkillsGUI extends GUI {

  private static FileManager fm = McRPG.getInstance().getFileManager();

  private static FileManager.Files file = FileManager.Files.REPLACE_SKILLS_GUI;

  private static GUIPlaceHolderFunction function = (GUIBuilder guiBuilder) -> {
    McRPGPlayer player = guiBuilder.getPlayer();
    if(guiBuilder.getRawPath().equalsIgnoreCase("ReplaceSkillsGUI")){
      skillsPlaceHolders(guiBuilder, player);
    }
  };

  public ReplaceSkillsGUI(McRPGPlayer p){
    super(new GUIBuilder("ReplaceSkillsGUI", fm.getFile(file), p));
    this.getGui().setReplacePlaceHoldersFunction(function);
    this.getGui().replacePlaceHolders();
    if(!GUITracker.isPlayerTracked(p)){
      GUITracker.trackPlayer(p, this);
    }
  }

  static void skillsPlaceHolders(GUIBuilder guiBuilder, McRPGPlayer player){
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMinimumIntegerDigits(1);
    nf.setMaximumFractionDigits(3);
    nf.setMinimumFractionDigits(2);
    for(int i = 0; i < guiBuilder.getInv().getSize(); i++){
      ItemStack item = guiBuilder.getInv().getItem(i);
      if(item == null){
        continue;
      }
      if(item.hasItemMeta() && item.getItemMeta().hasLore()){
        ItemMeta meta = item.getItemMeta();
        boolean isFish = meta.hasLore() && meta.getLore().stream().filter(s -> s.contains("%Fishing%")).count() >= 1;
        List<String> lore = new ArrayList<>();
        for(String s : meta.getLore()){
          for(Skills skill : Skills.values()){
            s = s.replaceAll("%" + skill.getName() + "_Level%", Integer.toString(player.getSkill(skill).getCurrentLevel()));
            Parser equation = skill.getDefaultAbility().getActivationEquation();
            equation.setVariable(skill.getName().toLowerCase() + "_level", player.getSkill(skill).getCurrentLevel());
            equation.setVariable("power_level", player.getPowerLevel());

            s = s.replaceAll("%" + skill.getDefaultAbility().getName().replaceAll(" ", "_") + "_Chance%", nf.format(equation.getValue()));
          }
          lore.add(s.replaceAll("%Power_Level%", Integer.toString(player.getPowerLevel()))
                  .replaceAll("%Ability_Points%", Integer.toString(player.getAbilityPoints())));
        }
        if(isFish){
          FileConfiguration fishingConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.FISHING_CONFIG);
          for(String category : fishingConfig.getConfigurationSection("CategoriesDefault").getKeys(false)){
            double c = fishingConfig.getDouble("CategoriesDefault." + category);
            String s2 = Methods.color(fishingConfig.getString("GreatRodConfig.Item.CategoryText")
                    .replace("%Category%", category).replace("%Chance%", Double.toString(c)));
            if(category.equalsIgnoreCase("Treasure") && DefaultAbilities.GREAT_ROD.isEnabled() && player.getBaseAbility(DefaultAbilities.GREAT_ROD).isToggled()){
              Parser equation = DefaultAbilities.GREAT_ROD.getActivationEquation();
              equation.setVariable("fishing_level", player.getSkill(Skills.FISHING).getCurrentLevel());
              s2 += Methods.color(" + " + nf.format(equation.getValue()) + "%");
            }
            lore.add(s2);
          }
          if(UnlockedAbilities.SEA_GODS_BLESSING.isEnabled() && player.getAbilityLoadout().contains(UnlockedAbilities.SEA_GODS_BLESSING) && player.getBaseAbility(UnlockedAbilities.SEA_GODS_BLESSING).isToggled()){
            SeaGodsBlessing seaGodsBlessing = (SeaGodsBlessing) player.getBaseAbility(UnlockedAbilities.SEA_GODS_BLESSING);
            String key = "SeaGodsBlessingConfig.Tier" + Methods.convertToNumeral(seaGodsBlessing.getCurrentTier()) + ".ExtraCategories";
            for(String category : fishingConfig.getConfigurationSection(key).getKeys(false)){
              double c = fishingConfig.getDouble(key + "." + category);
              String s2 = Methods.color(fishingConfig.getString("GreatRodConfig.Item.CategoryText")
                      .replace("%Category%", category).replace("%Chance%", Double.toString(c)));
              lore.add(s2);
            }
          }
          if(UnlockedAbilities.SUNKEN_ARMORY.isEnabled() && player.getAbilityLoadout().contains(UnlockedAbilities.SUNKEN_ARMORY) && player.getBaseAbility(UnlockedAbilities.SUNKEN_ARMORY).isToggled()){
            SunkenArmory sunkenArmory = (SunkenArmory) player.getBaseAbility(UnlockedAbilities.SUNKEN_ARMORY);
            String key = "SunkenArmoryConfig.Tier" + Methods.convertToNumeral(sunkenArmory.getCurrentTier()) + ".ExtraCategories";
            for(String category : fishingConfig.getConfigurationSection(key).getKeys(false)){
              double c = fishingConfig.getDouble(key + "." + category);
              String s2 = Methods.color(fishingConfig.getString("GreatRodConfig.Item.CategoryText")
                      .replace("%Category%", category).replace("%Chance%", Double.toString(c)));
              lore.add(s2);
            }
          }
          if(UnlockedAbilities.MAGIC_TOUCH.isEnabled() && player.getAbilityLoadout().contains(UnlockedAbilities.MAGIC_TOUCH) && player.getBaseAbility(UnlockedAbilities.MAGIC_TOUCH).isToggled()){
            MagicTouch magicTouch = (MagicTouch) player.getBaseAbility(UnlockedAbilities.MAGIC_TOUCH);
            String key = "MagicTouchConfig.Tier" + Methods.convertToNumeral(magicTouch.getCurrentTier()) + ".ExtraCategories";
            for(String category : fishingConfig.getConfigurationSection(key).getKeys(false)){
              double c = fishingConfig.getDouble(key + "." + category);
              String s2 = Methods.color(fishingConfig.getString("GreatRodConfig.Item.CategoryText")
                      .replace("%Category%", category).replace("%Chance%", Double.toString(c)));
              lore.add(s2);
            }
          }
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        guiBuilder.getInv().setItem(i, item);
      }
      continue;
    }
  }
}
