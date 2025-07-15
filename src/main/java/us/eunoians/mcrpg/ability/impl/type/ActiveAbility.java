package us.eunoians.mcrpg.ability.impl.type;

import us.eunoians.mcrpg.ability.Ability;

/**
 * An interface that represents an {@link Ability} that requires some sort of
 * action by an {@link us.eunoians.mcrpg.entity.holder.AbilityHolder} in order to activate.
 */
public interface ActiveAbility extends Ability {

    @Override
    default boolean isPassive() {
        return false;
    }
}
