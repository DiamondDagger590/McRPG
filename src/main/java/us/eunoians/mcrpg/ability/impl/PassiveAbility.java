package us.eunoians.mcrpg.ability.impl;

/**
 * An interface that represents an {@link Ability} that will passively
 * activate based on various conditions.
 */
public interface PassiveAbility extends Ability {

    @Override
    default boolean isPassive() {
        return true;
    }
}
