package us.eunoians.mcrpg.api.util.books;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.skills.Skill;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SkillBookFactory {

  private static final Random RANDOM = new Random();


  public ItemStack generateUnlockBook(){
    FileConfiguration file = McRPG.getInstance().getFileManager().getFile(FileManager.Files.UNLOCK_BOOKS);
    ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
    ItemMeta meta = book.getItemMeta();

    //Get the skill
    Skills skill = null;
    List<String> excludedSkills = file.getStringList("ExcludedSkills");
    while(skill == null){
      Skills temp = Skills.values()[RANDOM.nextInt(Skills.values().length)];
      if(excludedSkills.contains(temp.getName())){
        continue;
      }
      else{
        skill = temp;
      }
    }

    //Get the ability
    //TODO weighted system
    UnlockedAbilities ability = null;
    List<String> excludedAbilities = file.getStringList("ExcludedAbilities");
    List<String> tempList = skill.getEnabledAbilities();
    tempList.remove(skill.getDefaultAbility().getName());
    List<UnlockedAbilities> enabledAbilities = tempList.stream().map(UnlockedAbilities::fromString).collect(Collectors.toList());
    while(ability == null){

      if(excludedAbilities.contains(temp.getName())){
        continue;
      }
      else{
        ability = temp;
      }
    }

    int tier = 0;
    boolean requireLevel = false;
    List<String> requireLore;
    Skills requireSkill = null;
    int requireSkillLevel = 0;

    String key = file.contains(skill.getName() + "Format") ? skill.getName() + "Format." : "UniversalFormat.";
    String[] tierData = file.getString(key + "UnlockTierRange").split("-");
    int lowTier = Integer.parseInt(tierData[0]);
    int highTier = tierData.length > 1 ? Integer.parseInt(tierData[1]) : lowTier;
    tier = highTier > lowTier ? lowTier + RANDOM.nextInt(highTier - lowTier) : lowTier;

    requireLevel = file.getBoolean(key + "RequireLevel");
    requireLore = file.getStringList(key + "RequireLore");

    if(requireLevel){
      List<String> possibleLevelSkills = file.getStringList(key + "PossibleLevelSkills");
      String[] data = possibleLevelSkills.get(RANDOM.nextInt(possibleLevelSkills.size())).split(":");
      if(data[0].equalsIgnoreCase("ALL")){
        Skills[] possibleSkills = Skills.values();
        while(requireSkill == null){
          Skills temp = possibleSkills[RANDOM.nextInt(possibleSkills.length)];
          if(temp == skill){
            continue;
          }
          else{
            requireSkill = temp;
          }
        }
        String[] levelData = data[1].split("-");
        int lowLevel = Integer.parseInt(levelData[0]);
        int highLevel = levelData.length > 1 ? Integer.parseInt(levelData[1]) : lowLevel;
        requireSkillLevel = highLevel > lowLevel ? lowLevel + RANDOM.nextInt(highLevel - lowLevel) : lowLevel;
      }
    }

    if(!file.getString("BookFormat.DisplayName").equalsIgnoreCase("")){
      meta.setDisplayName(Methods.color(file.getString("BookFormat.DisplayName").replace("%Ability%", ability.getDisplayName()).replace("%Skill%", skill.getDisplayName())
              .replace("%Tier%", Integer.toString(tier))));
    }
    if(file.getStringList("BookFormat.Lore").size() > 0){
      List<String> lore = new ArrayList<>();
      for(String s : Methods.colorLore(file.getStringList("BookFormat.Lore"))){
        lore.add(s.replace("%Ability%", ability.getDisplayName()).replace("%Skill%", skill.getDisplayName()).replace("%Tier%", Integer.toString(tier)));
      }
      if(requireLevel){
        lore.addAll(requireLore);
      }
      meta.setLore(lore);
    }
    book.setItemMeta(meta);

    NBTItem item = new NBTItem(book);
    item.setBoolean("RequireLevel", requireLevel);
    if(requireLevel){
      item.setString("RequireSkill", requireSkill.getName());
      item.setInteger("RequireLevel", requireSkillLevel);
    }
    item.setString("Skill", skill.getName());
    item.setInteger("Tier", tier);
    item.setString("Ability", ability.getName());
    return book;
  }
}
