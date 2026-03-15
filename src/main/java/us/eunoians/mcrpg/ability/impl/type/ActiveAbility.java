package us.eunoians.mcrpg.ability.impl.type;

import com.diamonddagger590.mccore.statistic.Statistic;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.statistic.McRPGStatistic;

import java.util.Set;

/**
 * An interface that represents an {@link Ability} that requires some sort of
 * action by an {@link us.eunoians.mcrpg.entity.holder.AbilityHolder} in order to activate.
 */
public interface ActiveAbility extends Ability {

    @Override
    default boolean isPassive() {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Active abilities provide a default activation count statistic. Concrete abilities
     * can override this to add additional custom statistics alongside the default.
     */
    @Override
    @NotNull
    default Set<Statistic> getDefaultStatistics() {
        return Set.of(McRPGStatistic.createAbilityActivationStatistic(getAbilityKey(), getName()));
    }
}
