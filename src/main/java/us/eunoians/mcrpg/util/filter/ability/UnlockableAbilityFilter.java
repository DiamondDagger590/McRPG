package us.eunoians.mcrpg.util.filter.ability;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.util.PlayerContextFilter;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.UnlockableAbility;

import java.util.Collection;

/**
 * This filter is used to filter a collection of {@link Ability Abilities} so only abilities that can be unlocked
 * are left.
 */
public class UnlockableAbilityFilter implements PlayerContextFilter<Ability> {

    @Override
    public Collection<Ability> filter(@NotNull CorePlayer corePlayer, @NotNull Collection<Ability> collection) {
        return collection.stream().filter(ability -> ability instanceof UnlockableAbility).toList();
    }
}
