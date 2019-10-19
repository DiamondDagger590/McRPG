package us.eunoians.mcrpg.api.util.brewing;


import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PotionRecipeManager {

  private Map<Material, Integer> fuelMaterials = new HashMap<>();

  boolean isFuel(ItemStack fuel){
    return fuelMaterials.containsKey(fuel.getType());
  }

  int getFuelAmount(ItemStack fuel){
    return fuelMaterials.get(fuel.getType());
  }
}
