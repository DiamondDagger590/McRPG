package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.herbalism.FarmersDiet;
import us.eunoians.mcmmox.api.events.mcmmo.FarmersDietEvent;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;
import us.eunoians.mcmmox.types.UnlockedAbilities;

public class PlayerNomNomEvent implements Listener {

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onFoodLevelChange(FoodLevelChangeEvent event){
	Entity entity = event.getEntity();

	if(!(entity instanceof Player)){
	  return;
	}

	Player player = (Player) entity;
	int currentFoodLevel = player.getFoodLevel();
	int newFoodLevel = event.getFoodLevel();
	int foodChange = newFoodLevel - currentFoodLevel;

	if(foodChange <= 0){
	  return;
	}
	McMMOPlayer mp = PlayerManager.getPlayer(player.getUniqueId());
	if(UnlockedAbilities.FARMERS_DIET.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.FARMERS_DIET) && mp.getBaseAbility(UnlockedAbilities.FARMERS_DIET).isToggled()){
	  FarmersDiet farmersDiet = (FarmersDiet) mp.getBaseAbility(UnlockedAbilities.FARMERS_DIET);
	  FileConfiguration config = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.HERBALISM_CONFIG);
	  String key = "FarmersDietConfig.Tier" + Methods.convertToNumeral(farmersDiet.getCurrentTier()) + ".";
	  int foodRestorationBonus = config.getInt(key + "FoodRestorationBonus");
	  double saturationBonus = config.getDouble(key + "SaturationBonus");
	  ItemStack mainHand = player.getInventory().getItemInMainHand();
	  ItemStack offHand = player.getInventory().getItemInOffHand();
	  boolean useMainHand = config.getStringList("FarmersDietConfig.FoodForFarmersDiet").contains(mainHand.getType().toString());
	  boolean useOffHand = config.getStringList("FarmersDietConfig.FoodForFarmersDiet").contains(offHand.getType().toString());
	  if(!(useMainHand || useOffHand)){
	    return;
	  }
	  Material foodType;
	  if(useMainHand){
	    foodType = mainHand.getType();
	  }
	  else{
	    foodType = offHand.getType();
	  }
	  FarmersDietEvent farmersDietEvent = new FarmersDietEvent(mp, farmersDiet, foodRestorationBonus, saturationBonus, foodType);
	  Bukkit.getPluginManager().callEvent(farmersDietEvent);
	  if(!farmersDietEvent.isCancelled()){
	    player.setSaturation((float) (player.getSaturation() + farmersDietEvent.getSaturationBonus()));
	    event.setFoodLevel(newFoodLevel + farmersDietEvent.getFoodRestorationBonus());
	  }
	}
  }
}
