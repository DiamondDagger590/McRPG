package us.eunoians.mcrpg.listener.entity.holder;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.event.entity.AbilityHolderReadyEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This listener notifies players whenever they ready for an ability.
 */
public class OnAbilityHolderReadyListener implements Listener {

    @EventHandler
    public void onAbilityReady(AbilityHolderReadyEvent event) {
        AbilityHolder abilityHolder = event.getAbilityHolder();
        Player player = Bukkit.getPlayer(abilityHolder.getUUID());
        if (player != null) {
            MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
            BukkitAudiences adventure = McRPG.getInstance().getAdventure();
            Audience audience = adventure.player(player);
            audience.sendMessage(miniMessage.deserialize(event.getReadyData().getReadyMessage()));
        }
    }

}
