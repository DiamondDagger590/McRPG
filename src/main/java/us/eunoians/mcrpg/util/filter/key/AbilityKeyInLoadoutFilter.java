package us.eunoians.mcrpg.util.filter.key;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.util.filter.core.McRPGPlayerContextFilter;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A {@link McRPGPlayerContextFilter} that filters out all {@link Ability Abilities} that can't be added to the
 * {@link McRPGPlayer}'s {@link us.eunoians.mcrpg.loadout.Loadout}.
 */
public class AbilityKeyInLoadoutFilter implements McRPGPlayerContextFilter<NamespacedKey> {

    @Override
    public Collection<NamespacedKey> filter(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Collection<NamespacedKey> list) {
        return list.stream().filter(namespacedKey -> mcRPGPlayer.asSkillHolder().getLoadout().canAbilityBeInLoadout(namespacedKey)).collect(Collectors.toSet());
    }
}
