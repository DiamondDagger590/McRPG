package us.eunoians.mcrpg.bootstrap;

import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.bootstrap.registrar.Registrar;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.event.ability.AbilityRegisterEvent;
import us.eunoians.mcrpg.statistic.McRPGStatistic;

/**
 * Registers all McRPG statistics with McCore's {@link StatisticRegistry}.
 * <p>
 * This includes all statically-defined statistics from {@link McRPGStatistic}
 * as well as dynamically-generated per-ability activation statistics. Per-ability
 * statistics are registered automatically via an {@link AbilityRegisterEvent} listener,
 * which means third-party {@link us.eunoians.mcrpg.expansion.ContentExpansion} plugins
 * that register abilities through the standard content expansion system will get
 * per-ability activation statistics for free — no additional setup required.
 * <p>
 * This registrar must be registered <b>before</b>
 * {@link McRPGExpansionRegistrar} in the bootstrap sequence so that the event listener
 * is active when abilities are registered during content expansion processing.
 */
final class McRPGStatisticRegistrar implements Registrar<McRPG> {

    @Override
    public void register(@NotNull BootstrapContext<McRPG> context) {
        McRPG plugin = context.plugin();
        StatisticRegistry statisticRegistry = plugin.registryAccess().registry(RegistryKey.STATISTIC);

        // Register all statically-defined statistics
        for (Statistic statistic : McRPGStatistic.ALL_STATIC_STATISTICS) {
            statisticRegistry.register(statistic);
        }

        // Listen for ability registrations (both native and third-party) and
        // auto-register a per-ability activation statistic for each one
        Bukkit.getPluginManager().registerEvents(new AbilityStatisticAutoRegistrar(statisticRegistry), plugin);
    }

    /**
     * Listens for {@link AbilityRegisterEvent} and automatically registers a per-ability
     * activation statistic. This ensures that any ability registered through the
     * {@link us.eunoians.mcrpg.expansion.ContentExpansion} system — including those from
     * third-party plugins — gets a corresponding activation count statistic without
     * requiring manual registration.
     */
    private static class AbilityStatisticAutoRegistrar implements Listener {

        private final StatisticRegistry statisticRegistry;

        AbilityStatisticAutoRegistrar(@NotNull StatisticRegistry statisticRegistry) {
            this.statisticRegistry = statisticRegistry;
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onAbilityRegister(@NotNull AbilityRegisterEvent event) {
            statisticRegistry.register(McRPGStatistic.createAbilityActivationStatistic(
                    event.getAbility().getAbilityKey(), event.getAbility().getName()));
        }
    }
}
