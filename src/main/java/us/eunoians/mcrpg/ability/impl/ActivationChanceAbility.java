package us.eunoians.mcrpg.ability.impl;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

public interface ActivationChanceAbility extends Ability {

    double getActivationChance(@NotNull AbilityHolder abilityHolder);
}
