package us.eunoians.mcrpg.listener.entity.holder;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.entity.AbilityHolderUnreadyEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * This listener handles notifying players that they are no longer ready for ability activation.
 */
public class OnAbilityHolderUnreadyListener implements Listener {

    @EventHandler
    public void onAbilityUnready(AbilityHolderUnreadyEvent event) {
        AbilityHolder abilityHolder = event.getAbilityHolder();
        Player player = Bukkit.getPlayer(abilityHolder.getUUID());
        var playerOptional = RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(abilityHolder.getUUID());
        if (playerOptional.isPresent() && event.didReadyAutoExpire()) {
            player.sendMessage(event.getReadyData().getUnreadyMessage(playerOptional.get()));
        }
    }
}
