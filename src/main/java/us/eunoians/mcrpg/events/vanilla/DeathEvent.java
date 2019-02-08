package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import us.eunoians.mcrpg.util.mcmmo.MobHealthbarUtils;

public class DeathEvent implements Listener {
/**
 * This code is not mine. It is copyright from the original mcMMO allowed for use by their license.
 * This code has been modified from it source material
 * It was released under the GPLv3 license
 */
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
