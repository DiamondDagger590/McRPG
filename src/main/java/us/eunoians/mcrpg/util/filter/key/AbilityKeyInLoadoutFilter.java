package us.eunoians.mcrpg.util.filter.key;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.util.filter.core.McRPGPlayerContextFilter;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A {@link McRPGPlayerContextFilter} that filters out all {@link Ability Abilities} that can't be added to the
 * {@link McRPGPlayer}'s {@link us.eunoians.mcrpg.loadout.Loadout}.
 */
public class AbilityKeyInLoadoutFilter implements McRPGPlayerContextFilter<NamespacedKey> {

    private final Loadout loadout;
    private final NamespacedKey abilityBeingReplaced;

    public AbilityKeyInLoadoutFilter(@NotNull Loadout loadout, @Nullable NamespacedKey abilityBeingReplaced) {
        this.loadout = loadout;
        this.abilityBeingReplaced = abilityBeingReplaced;
    }

    @Override
    public Collection<NamespacedKey> filter(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Collection<NamespacedKey> list) {
        return list.stream().filter(abilityKey -> abilityBeingReplaced == null ? loadout.canAbilityBeAddedToLoadout(abilityKey)
                : loadout.canAbilityBeReplacedIntoLoadout(abilityBeingReplaced, abilityKey)).collect(Collectors.toSet());
    }
}
