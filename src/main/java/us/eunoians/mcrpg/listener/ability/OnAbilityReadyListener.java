package us.eunoians.mcrpg.listener.ability;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.event.ability.AbilityReadyEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

public class OnAbilityReadyListener implements Listener {

    @EventHandler
    public void onAbilityReady(AbilityReadyEvent event) {
        AbilityHolder abilityHolder = event.getAbilityHolder();
        Player player = Bukkit.getPlayer(abilityHolder.getUUID());
        if (player != null) {
            MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
            BukkitAudiences adventure = McRPG.getInstance().getAdventure();
            Audience audience = adventure.player(player);
            audience.sendMessage(miniMessage.deserialize("<gray>You raise your sword."));
        }
    }

}
