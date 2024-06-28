package us.eunoians.mcrpg.listener.ability;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.event.ability.AbilityUnreadyEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

public class OnAbilityUnreadyListener implements Listener {

    @EventHandler
    public void onAbilityUnready(AbilityUnreadyEvent event) {
        AbilityHolder abilityHolder = event.getAbilityHolder();
        Player player = Bukkit.getPlayer(abilityHolder.getUUID());
        if (player != null) {
            MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
            BukkitAudiences adventure = McRPG.getInstance().getAdventure();
            Audience audience = adventure.player(player);
            audience.sendMessage(miniMessage.deserialize("<gray>You lower your sword."));
        }
    }
}
