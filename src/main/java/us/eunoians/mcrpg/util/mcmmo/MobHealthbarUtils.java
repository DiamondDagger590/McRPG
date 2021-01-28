package us.eunoians.mcrpg.util.mcmmo;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import us.eunoians.mcrpg.player.McRPGPlayer;

import java.util.Arrays;

/**
 * This code is not mine. It is copyright from the original mcMMO allowed for use by their license.
 * This code has been modified from it source material
 * It was released under the GPLv3 license
 */

public final class MobHealthbarUtils {

  private MobHealthbarUtils(){}

  /**
   * Fix issues with death messages caused by the mob healthbars.
   *
   * @param deathMessage The original death message
   * @param player       The player who died
   * @return the fixed death message
   */
  public static String fixDeathMessage(String deathMessage, Player player){
    EntityDamageEvent lastDamageCause = player.getLastDamageCause();
    String replaceString = lastDamageCause instanceof EntityDamageByEntityEvent ? getPrettyEntityTypeString(((EntityDamageByEntityEvent) lastDamageCause).getDamager().getType()) : "a mob";

    return deathMessage.replaceAll("(?:\u00A7(?:[0-9A-FK-ORa-fk-or]){1}(?:[\u2764\u25A0]{1,10})){1,2}", replaceString);
  }

  /**
   * Handle the creation of mob healthbars.
   *
   * p      the attacking player
   * target the targetted entity
   * @param damage damage done by the attack triggering this
   */
  /*
  public static void handleMobHealthbars(Player p, LivingEntity target, double damage){
    if(McRPG.getInstance().isHealthBarPluginEnabled() || !McRPG.getInstance().getConfig().getBoolean("Configuration.HealthBarEnabled")){
      return;
    }

    if(isBoss(target)){
      return;
    }
    if(!p.isOnline() || !PlayerManager.isPlayerStored(p.getUniqueId())){
      return;
    }
    McRPGPlayer mp;
    try{
      mp = PlayerManager.getPlayer(p.getUniqueId());
    } catch(McRPGPlayerNotFoundException exception){
      return;
    }
    //TODO Player settings
	/*PlayerProfile profile = UserManager.getPlayer(player).getProfile();

	if (profile.getMobHealthbarType() == null) {
	  profile.setMobHealthbarType(Config.getInstance().getMobHealthbarDefault());
	}

    if(mp.getHealthbarType() == MobHealthbarType.DISABLED){
      return;
    }

    String oldName = target.getCustomName();

    if(oldName == null){
      oldName = "";
    }

    boolean oldNameVisible = target.isCustomNameVisible();
    String newName = createHealthDisplay(mp, target, damage);

    target.setCustomName(newName);
    target.setCustomNameVisible(true);

    int displayTime = McRPG.getInstance().getConfig().getInt("Configuration.HealthBarDisplayTime");

    if(displayTime != -1){
      boolean updateName = !ChatColor.stripColor(oldName).equalsIgnoreCase(ChatColor.stripColor(newName));

      if(updateName){
        target.setMetadata(McRPG.getInstance().getCustomNameKey(), new FixedMetadataValue(McRPG.getInstance(), oldName));
        target.setMetadata(McRPG.getInstance().getCustomVisibleKey(), new FixedMetadataValue(McRPG.getInstance(), oldNameVisible));
      }
      else if(!target.hasMetadata(McRPG.getInstance().getCustomNameKey())){
        target.setMetadata(McRPG.getInstance().getCustomNameKey(), new FixedMetadataValue(McRPG.getInstance(), ""));
        target.setMetadata(McRPG.getInstance().getCustomVisibleKey(), new FixedMetadataValue(McRPG.getInstance(), false));
      }

      new MobHealthDisplayUpdaterTask(target).runTaskLater(McRPG.getInstance(), displayTime * 20); // Clear health display after 3 seconds
    }
  }*/

  private static String createHealthDisplay(McRPGPlayer player, LivingEntity entity, double damage){
    double maxHealth = entity.getMaxHealth();
    double currentHealth = Math.max(entity.getHealth() - damage, 0);
    double healthPercentage = (currentHealth / maxHealth) * 100.0D;

    int fullDisplay = 0;
    ChatColor color = ChatColor.BLACK;
    String symbol = null;

    /*
    switch(player.getHealthbarType()){
      case HEARTS:
        fullDisplay = Math.min((int) (maxHealth / 2), 10);
        color = ChatColor.DARK_RED;
        symbol = "❤";
        break;

      case BAR:
        fullDisplay = 10;

        if(healthPercentage >= 85){
          color = ChatColor.DARK_GREEN;
        }
        else if(healthPercentage >= 70){
          color = ChatColor.GREEN;
        }
        else if(healthPercentage >= 55){
          color = ChatColor.GOLD;
        }
        else if(healthPercentage >= 40){
          color = ChatColor.YELLOW;
        }
        else if(healthPercentage >= 25){
          color = ChatColor.RED;
        }
        else if(healthPercentage >= 0){
          color = ChatColor.DARK_RED;
        }

        symbol = "■";
        break;

      default:
        return null;
    }*/

    int coloredDisplay = (int) Math.ceil(fullDisplay * (healthPercentage / 100.0D));
    int grayDisplay = fullDisplay - coloredDisplay;

    String healthbar = color + "";

    for(int i = 0; i < coloredDisplay; i++){
      healthbar += symbol;
    }

    healthbar += ChatColor.GRAY;

    for(int i = 0; i < grayDisplay; i++){
      healthbar += symbol;
    }

    return healthbar;
  }

  /**
   * Check if a given LivingEntity is a boss.
   *
   * @param livingEntity The {@link LivingEntity} of the livingEntity to check
   * @return true if the livingEntity is a boss, false otherwise
   */
  private static boolean isBoss(LivingEntity livingEntity){
    switch(livingEntity.getType()){
      case ENDER_DRAGON:
      case WITHER:
        return true;

      default:
        return false;
    }
  }

  public static String getPrettyEntityTypeString(EntityType entity){
    return createPrettyEnumString(entity.toString());
  }

  private static String createPrettyEnumString(String baseString){
    String[] substrings = baseString.split("_");
    String prettyString = "";
    int size = 1;

    for(String string : substrings){
      prettyString = prettyString.concat(getCapitalized(string));

      if(size < substrings.length){
        prettyString = prettyString.concat(" ");
      }

      size++;
    }

    return prettyString;
  }

  private static String getCapitalized(String target){
    return target.substring(0, 1).toUpperCase() + target.substring(1).toLowerCase();
  }

  public enum MobHealthbarType {
    HEARTS("Hearts"),
    BAR("Bar"),
    DISABLED("Disabled");

    @Getter
    String name;

    MobHealthbarType(String name){
      this.name = name;
    }

    public static MobHealthbarType fromString(String type){
      return Arrays.stream(values()).filter(healthbarType -> healthbarType.getName().equalsIgnoreCase(type)).findFirst().orElse(null);
    }
  }
}
