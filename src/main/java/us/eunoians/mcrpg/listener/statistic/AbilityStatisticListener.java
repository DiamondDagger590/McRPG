package us.eunoians.mcrpg.listener.statistic;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.statistic.PlayerStatisticData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.statistic.McRPGStatistic;

import java.util.Optional;

/**
 * Listens to ability activation events and increments the global and
 * per-ability activation count statistics.
 * <p>
 * Per-ability activation statistics are auto-registered via
 * {@link us.eunoians.mcrpg.bootstrap.McRPGStatisticRegistrar} when an ability is
 * registered through the {@link us.eunoians.mcrpg.expansion.ContentExpansion} system.
 * This means third-party abilities that use the standard registration path automatically
 * get their activation statistics tracked here.
 */
public class AbilityStatisticListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAbilityActivate(@NotNull AbilityActivateEvent event) {
        Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER)
                .getPlayer(event.getAbilityHolder().getUUID());
        if (playerOptional.isEmpty()) {
            return;
        }

        PlayerStatisticData stats = playerOptional.get().getStatisticData();

        // Increment global ability activation count
        stats.incrementLong(McRPGStatistic.ABILITIES_ACTIVATED.getStatisticKey(), 1);

        // Increment per-ability activation count — the statistic is auto-registered
        // for any ability that goes through the ContentExpansion registration path
        stats.incrementLong(McRPGStatistic.getAbilityActivationKey(event.getAbility().getAbilityKey()), 1);
    }
}
