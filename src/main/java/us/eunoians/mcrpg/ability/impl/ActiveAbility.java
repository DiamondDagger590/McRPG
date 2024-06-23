package us.eunoians.mcrpg.ability.impl;

/**
 * An interface that represents an {@link Ability} that requires some sort of
 * action by an {@link us.eunoians.mcrpg.entity.holder.AbilityHolder} in order to activate.
 */
public interface ActiveAbility extends Ability {

    @Override
    default boolean isActivePassive() {
        return false;
    }
}
