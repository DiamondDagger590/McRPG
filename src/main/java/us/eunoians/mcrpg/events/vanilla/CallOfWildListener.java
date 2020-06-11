package us.eunoians.mcrpg.events.vanilla;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.types.Skills;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class CallOfWildListener implements Listener{
  
  public static final NamespacedKey ANTI_BREEDABLE_KEY = new NamespacedKey(McRPG.getInstance(),"call-of-wild-anti-breed");
  public static final NamespacedKey SUMMONED_TIME = new NamespacedKey(McRPG.getInstance(), "call-of-wild-summon-time");
  private static final Set<EntityType> summonableMobTypes = new HashSet<>();
  
  static {
    summonableMobTypes.add(EntityType.CAT);
    summonableMobTypes.add(EntityType.DONKEY);
    summonableMobTypes.add(EntityType.HORSE);
    summonableMobTypes.add(EntityType.SKELETON_HORSE);
    summonableMobTypes.add(EntityType.ZOMBIE_HORSE);
    summonableMobTypes.add(EntityType.MULE);
    summonableMobTypes.add(EntityType.WOLF);
    summonableMobTypes.add(EntityType.PARROT);
    summonableMobTypes.add(EntityType.LLAMA);
    summonableMobTypes.add(EntityType.TROPICAL_FISH);
    summonableMobTypes.add(EntityType.PANDA);
  }
  
  private Map<Material, CallOfWildWrapper> wrappers = new HashMap<>();
  
  public CallOfWildListener(){
    FileConfiguration tamingConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.TAMING_CONFIG);
    if(tamingConfig.contains("CallOfWild")){
      for(String entityTypeString : tamingConfig.getConfigurationSection("CallOfWild").getKeys(false)){
        EntityType entityType = EntityType.fromName(entityTypeString);
        if(entityType == null || !summonableMobTypes.contains(entityType)){
          Bukkit.getLogger().log(Level.WARNING, "Call Of Wild: " + entityTypeString + " is not a valid mob.");
          continue;
        }
        Map<Material, Integer> summonableMaterials = new HashMap<>();
        for(String materialID : tamingConfig.getConfigurationSection("CallOfWild." + entityTypeString + ".SummonableMaterials").getKeys(false)){
          Material material = Material.getMaterial(materialID);
          if(material == null){
            Bukkit.getLogger().log(Level.WARNING, "Call Of Wild: " + materialID + " is not a valid material.");
            continue;
          }
          summonableMaterials.put(material, Math.min(64, tamingConfig.getInt("CallOfWild." + entityTypeString + ".SummonableMaterials." + materialID)));
        }
        double health = tamingConfig.getDouble("CallOfWild." + entityTypeString + ".Health", 10d);
        boolean breedable = tamingConfig.getBoolean("CallOfWild." + entityTypeString + ".CanBreed", false);
        int radius = tamingConfig.getInt("CallOfWild." + entityTypeString + ".RadiusCheck.Radius", 0);
        int amountInRadius = tamingConfig.getInt("CallOfWild." + entityTypeString + ".RadiusCheck.AmountAllowed", 0);
        CallOfWildWrapper callOfWildWrapper = new CallOfWildWrapper(summonableMaterials, entityType, health, breedable, radius, amountInRadius);
        for(Material material : summonableMaterials.keySet()){
          wrappers.put(material, callOfWildWrapper);
        }
      }
    }
  }
  
  @EventHandler(priority = EventPriority.LOW)
  public void handleCallOfWild(PlayerInteractEvent event){
    if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
      Player player = event.getPlayer();
      if(event.getHand() == EquipmentSlot.HAND && player.isSneaking() && Skills.TAMING.isEnabled() && wrappers.containsKey(player.getInventory().getItemInMainHand().getType())){
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        CallOfWildWrapper callOfWildWrapper = wrappers.get(mainHand.getType());
        if(mainHand.getAmount() >= callOfWildWrapper.getSummonableMaterials().get(mainHand.getType())){
          if(callOfWildWrapper.getAmountInRadius() > 0 && callOfWildWrapper.getRadius() > 0){
            if(player.getNearbyEntities(callOfWildWrapper.getRadius(), callOfWildWrapper.getRadius(), callOfWildWrapper.getAmountInRadius())
                 .stream().filter(entity -> entity.getType() == callOfWildWrapper.getEntityToSummon()).count() >= callOfWildWrapper.getAmountInRadius()){
              player.sendMessage(Methods.color(player, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Misc.CallOfWild.TooManyEntities")));
              return;
            }
          }
          player.getInventory().getItemInMainHand().setAmount(mainHand.getAmount() - callOfWildWrapper.getSummonableMaterials().get(mainHand.getType()));
          player.updateInventory();
          FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
          player.getLocation().getWorld().playSound(player.getLocation(), Sound.valueOf(soundFile.getString("Sounds.Taming.CallOfWild.Sound")),
            Float.parseFloat(soundFile.getString("Sounds.Taming.CallOfWild.Volume")), Float.parseFloat(soundFile.getString("Sounds.Taming.CallOfWild.Pitch")));          LivingEntity livingEntity = (LivingEntity) player.getWorld().spawnEntity(player.getLocation(), callOfWildWrapper.getEntityToSummon());
          livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(callOfWildWrapper.getHealth());
          livingEntity.setHealth(callOfWildWrapper.getHealth());
          if(livingEntity instanceof Tameable){
            ((Tameable) livingEntity).setOwner(player);
          }
          if(!callOfWildWrapper.isBreedable()){
            livingEntity.getPersistentDataContainer().set(ANTI_BREEDABLE_KEY, PersistentDataType.STRING, "true");
          }
          livingEntity.getPersistentDataContainer().set(SUMMONED_TIME, PersistentDataType.LONG, Calendar.getInstance().getTimeInMillis());
          event.setCancelled(true);
        }
      }
    }
  }
  
  private class CallOfWildWrapper{
    
    @Getter
    private Map<Material, Integer> summonableMaterials;
    @Getter
    private EntityType entityToSummon;
    @Getter
    private double health;
    @Getter
    private boolean breedable;
    @Getter
    private int radius;
    @Getter
    private int amountInRadius;
    
    private CallOfWildWrapper(Map<Material, Integer> summonableMaterials, EntityType entityToSummon, double health, boolean breedable, int radius, int amountInRadius){
      this.summonableMaterials = summonableMaterials;
      this.entityToSummon = entityToSummon;
      this.health = health;
      this.breedable = breedable;
      this.radius = radius;
      this.amountInRadius = amountInRadius;
    }
  }
}
