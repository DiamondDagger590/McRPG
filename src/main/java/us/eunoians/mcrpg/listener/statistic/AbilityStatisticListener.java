package us.eunoians.mcrpg.listener.statistic;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.statistic.PlayerStatisticData;
import com.diamonddagger590.mccore.statistic.StatisticRegistry;
import org.bukkit.NamespacedKey;
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
 * Per-ability activation statistics are registered through the
 * {@link us.eunoians.mcrpg.expansion.ContentExpansion} system via
 * {@link us.eunoians.mcrpg.expansion.content.StatisticContentPack}. Native McRPG abilities
 * include their activation statistics by default via
 * {@link us.eunoians.mcrpg.ability.impl.type.ActiveAbility#getDefaultStatistics()}.
 * Third-party expansions should include their abilities' default statistics in their own
 * {@link us.eunoians.mcrpg.expansion.content.StatisticContentPack} to have them tracked here.
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
        StatisticRegistry statisticRegistry = McRPG.getInstance().registryAccess().registry(RegistryKey.STATISTIC);

        // Increment global ability activation count
        stats.incrementLong(McRPGStatistic.ABILITIES_ACTIVATED.getStatisticKey(), 1);

        // Increment per-ability activation count — only if the expansion registered the statistic
        NamespacedKey activationKey = McRPGStatistic.getAbilityActivationKey(event.getAbility().getAbilityKey());
        if (statisticRegistry.getStatistic(activationKey).isPresent()) {
            stats.incrementLong(activationKey, 1);
        }
    }
}
