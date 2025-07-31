package us.eunoians.mcrpg.util.filter.key;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.util.filter.core.McRPGPlayerContextFilter;

import java.util.Collection;

/**
 * A {@link McRPGPlayerContextFilter} that filters out all {@link Ability Abilities}
 * that aren't unlockable abilities.
 */
public class AbilityKeyUnlockedFilter implements McRPGPlayerContextFilter<NamespacedKey> {

    @NotNull
    @Override
    public Collection<NamespacedKey> filter(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Collection<NamespacedKey> collection) {
        return collection.stream()
                .filter(namespacedKey -> McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).registered(namespacedKey))
                .filter(namespacedKey -> McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(namespacedKey) instanceof UnlockableAbility)
                .toList();
    }
}
