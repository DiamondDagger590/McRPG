package us.eunoians.mcrpg.gui;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.DefaultAbilities;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.util.Parser;

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

  static void skillsPlaceHolders(GUIBuilder guiBuilder, McRPGPlayer player){
	for(int i = 0; i < guiBuilder.getInv().getSize(); i++){
	  ItemStack item = guiBuilder.getInv().getItem(i);
	  if(item.hasItemMeta() && item.getItemMeta().hasLore()){
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<>();
		for(String s : meta.getLore()){
		  for(Skills skill : Skills.values()){
		    //TODO remove this
		    if(skill == Skills.ARCHERY){
		      continue;
			}
			s = s.replaceAll("%" + skill.getName() + "_Level%", Integer.toString(player.getSkill(skill).getCurrentLevel()));
			DefaultAbilities ability = DefaultAbilities.getSkillsDefaultAbility(skill.getName());
			Parser equation = ability.getActivationEquation();
			equation.setVariable(skill.getName().toLowerCase() + "_level", player.getSkill(skill).getCurrentLevel());
			equation.setVariable("power_level", player.getPowerLevel());
			s = s.replaceAll("%" + ability.getName().replaceAll(" ", "_") + "_Chance%", Double.toString(equation.getValue()));
		  }
		  lore.add(s.replaceAll("%Power_Level%", Integer.toString(player.getPowerLevel()))
			  .replaceAll("%Ability_Points%", Integer.toString(player.getAbilityPoints())));
		  meta.setLore(lore);
		  item.setItemMeta(meta);
		  guiBuilder.getInv().setItem(i, item);
		}
	  }
	  continue;
	}
  }

  public ReplaceSkillsGUI(McRPGPlayer p){
	super(new GUIBuilder("ReplaceSkillsGUI", fm.getFile(file), p));
	this.getGui().setReplacePlaceHoldersFunction(function);
	this.getGui().replacePlaceHolders();
	if(!GUITracker.isPlayerTracked(p)){
	  GUITracker.trackPlayer(p, this);
	}
  }

}
