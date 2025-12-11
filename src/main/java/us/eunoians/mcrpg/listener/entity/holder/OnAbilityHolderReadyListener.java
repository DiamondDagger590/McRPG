package us.eunoians.mcrpg.listener.entity.holder;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.entity.AbilityHolderReadyEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * This listener notifies players whenever they ready for an ability.
 */
public class OnAbilityHolderReadyListener implements Listener {

    @EventHandler
    public void onAbilityReady(AbilityHolderReadyEvent event) {
        AbilityHolder abilityHolder = event.getAbilityHolder();
        Player player = Bukkit.getPlayer(abilityHolder.getUUID());
        var playerOptional = RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(abilityHolder.getUUID());
        playerOptional.ifPresent(mcRPGPlayer -> player.sendMessage(event.getReadyData().getReadyMessage(mcRPGPlayer)));
    }

}
