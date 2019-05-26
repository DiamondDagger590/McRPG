package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.fitness.DivineEscape;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.mcmmo.MobHealthbarUtils;

public class DeathEvent implements Listener {
  /**
   * This code is not mine. It is copyright from the original mcMMO allowed for use by their license.
   * This code has been modified from it source material
   * It was released under the GPLv3 license
   */


  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerDeathHighest(EntityDamageEvent e) {
    if(e instanceof Player ) {
      Player p = (Player) e.getEntity();
      if(p.getHealth() - e.getDamage() <= 0 && p.getBedSpawnLocation() != null) {
        McRPGPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
        if(UnlockedAbilities.DIVINE_ESCAPE.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.DIVINE_ESCAPE)
        && mp.getBaseAbility(UnlockedAbilities.DIVINE_ESCAPE).isToggled()){
          FileConfiguration fitnessConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.FITNESS_CONFIG);
          DivineEscape divineEscape = (DivineEscape) mp.getBaseAbility(UnlockedAbilities.DIVINE_ESCAPE);
          String key = "DivineEscapeConfig.Tier" + Methods.convertToNumeral(divineEscape.getCurrentTier()) + ".";
          double mcrpgExpPen = fitnessConfig.getDouble(key + "McRPGExpPenalty");
          int mcrpgExpPenDur = fitnessConfig.getInt(key + "McRPGExpPenaltyDuration");
          double damagePenalty = fitnessConfig.getDouble(key + "DamagePenalty");
          int damagePenDur = fitnessConfig.getInt(key + "DamagePenaltyDuration");
          int cooldown = fitnessConfig.getInt(key + "Cooldown");
        }
      }
    }
  }

  /**
   * Handle PlayerDeathEvents at the lowest priority.
   * <p>
   * These events are used to modify the death message of a player when
   * needed to correct issues potentially caused by the custom naming used
   * for mob healthbars.
   *
   * @param event The event to modify
   */
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onPlayerDeathLowest(PlayerDeathEvent event) {
    String deathMessage = event.getDeathMessage();

    if(deathMessage == null) {
      return;
    }

    Player player = event.getEntity();
    event.setDeathMessage(MobHealthbarUtils.fixDeathMessage(deathMessage, player));
  }
}
