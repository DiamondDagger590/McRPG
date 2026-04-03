package us.eunoians.mcrpg.listener.statistic;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.statistic.PlayerStatisticData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.statistic.McRPGStatistic;

import java.util.Optional;

/**
 * Tracks damage dealt, damage taken, and mob kills.
 * <p>
 * Uses {@link EventPriority#MONITOR} to read the final damage values after
 * all other plugins have modified them.
 */
public class CombatStatisticListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(@NotNull EntityDamageByEntityEvent event) {
        // Track damage dealt
        if (event.getDamager() instanceof Player player) {
            getPlayer(player).ifPresent(mcRPGPlayer ->
                    mcRPGPlayer.getStatisticData().incrementDouble(
                            McRPGStatistic.DAMAGE_DEALT.getStatisticKey(), event.getFinalDamage()
                    )
            );
        }

        // Track damage taken
        if (event.getEntity() instanceof Player player) {
            getPlayer(player).ifPresent(mcRPGPlayer ->
                    mcRPGPlayer.getStatisticData().incrementDouble(
                            McRPGStatistic.DAMAGE_TAKEN.getStatisticKey(), event.getFinalDamage()
                    )
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) {
            return;
        }
        Player killer = entity.getKiller();
        if (killer == null) {
            return;
        }

        getPlayer(killer).ifPresent(mcRPGPlayer ->
                mcRPGPlayer.getStatisticData().incrementLong(
                        McRPGStatistic.MOBS_KILLED.getStatisticKey(), 1
                )
        );
    }

    @NotNull
    private Optional<McRPGPlayer> getPlayer(@NotNull Player player) {
        return McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER)
                .getPlayer(player.getUniqueId());
    }
}
