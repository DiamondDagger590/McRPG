package us.eunoians.mcrpg.util.filter.ability;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.util.filter.core.McRPGPlayerContextFilter;

import java.util.Collection;

/**
 * This filter is used to filter a collection of {@link Ability Abilities} so that only unlockable abilities
 * that aren't passive are left.
 */
public class ActiveAbilityFilter implements McRPGPlayerContextFilter<Ability> {

    @NotNull
    @Override
    public Collection<Ability> filter(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Collection<Ability> collection) {
        return collection.stream().filter(ability -> ability instanceof UnlockableAbility && !ability.isPassive()).toList();
    }
}
