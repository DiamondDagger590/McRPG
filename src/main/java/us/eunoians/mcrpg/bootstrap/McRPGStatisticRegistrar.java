package us.eunoians.mcrpg.bootstrap;

import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.bootstrap.registrar.Registrar;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticRegistry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.statistic.McRPGStatistic;

/**
 * Registers all McRPG statistics with McCore's {@link StatisticRegistry}.
 * <p>
 * This includes all statically-defined statistics from {@link McRPGStatistic}
 * as well as dynamically-generated per-ability activation statistics from the
 * {@link AbilityRegistry}.
 */
final class McRPGStatisticRegistrar implements Registrar<McRPG> {

    @Override
    public void register(@NotNull BootstrapContext<McRPG> context) {
        McRPG plugin = context.plugin();
        StatisticRegistry statisticRegistry = plugin.registryAccess().registry(RegistryKey.STATISTIC);
        AbilityRegistry abilityRegistry = plugin.registryAccess().registry(McRPGRegistryKey.ABILITY);

        // Register all statically-defined statistics
        for (Statistic statistic : McRPGStatistic.ALL_STATIC_STATISTICS) {
            statisticRegistry.register(statistic);
        }

        // Register per-ability activation statistics dynamically
        for (NamespacedKey abilityKey : abilityRegistry.getAllAbilities()) {
            String abilityName = abilityRegistry.getRegisteredAbility(abilityKey).getName();
            statisticRegistry.register(McRPGStatistic.createAbilityActivationStatistic(abilityKey, abilityName));
        }
    }
}
