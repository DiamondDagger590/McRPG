package us.eunoians.mcrpg.listener.ability;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.event.ability.AbilityCooldownExpireEvent;

/**
 * This listener handles notifying the player whenever their cooldown
 * is expired.
 */
public class OnAbilityCooldownExpireListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleExpire(AbilityCooldownExpireEvent abilityCooldownExpireEvent) {
        if (Bukkit.getEntity(abilityCooldownExpireEvent.getAbilityHolder().getUUID()) instanceof Player player) {
            MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
            Audience audience = McRPG.getInstance().getAdventure().player(player);
            audience.sendMessage(miniMessage.deserialize("<gold>" + abilityCooldownExpireEvent.getAbility().getDisplayName() + " <gray> is off cooldown."));
        }
    }
}
