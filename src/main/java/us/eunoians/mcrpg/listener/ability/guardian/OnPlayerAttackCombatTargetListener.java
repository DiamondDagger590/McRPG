package us.eunoians.mcrpg.listener.ability.guardian;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Records the last entity attacked by each player for Phase Shift targeting.
 */
public final class OnPlayerAttackCombatTargetListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerAttack(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(player.getUniqueId())
                .ifPresent(mcRPGPlayer -> mcRPGPlayer.getOrCreateCombatTargetState()
                        .recordAttack(event.getEntity().getUniqueId(), System.currentTimeMillis()));
    }
}
