package us.eunoians.mcrpg.util.filter.ability;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityType;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.util.filter.core.McRPGPlayerContextFilter;

import java.util.Collection;

/**
 * Filters a collection of {@link Ability Abilities} to only those classified as
 * {@link AbilityType#ACTIVE} (combo-activated abilities).
 */
public class ActiveAbilityFilter implements McRPGPlayerContextFilter<Ability> {

    @NotNull
    @Override
    public Collection<Ability> filter(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Collection<Ability> collection) {
        return collection.stream().filter(ability -> ability.getAbilityType() == AbilityType.ACTIVE).toList();
    }
}
