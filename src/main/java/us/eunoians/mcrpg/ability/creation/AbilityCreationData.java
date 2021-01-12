package us.eunoians.mcrpg.ability.creation;

import us.eunoians.mcrpg.api.AbilityHolder;

/**
 * This represents an object that can be used to pass in data for the construction of {@link us.eunoians.mcrpg.ability.Ability}s.
 * <p>
 * Any implementation should implement required interfaces such as {@link ToggleableCreationData} for their specific use cases.
 *
 * @author DiamondDagger590
 */
public abstract class AbilityCreationData {

    /**
     * The {@link AbilityHolder} that has data being created for
     */
    private AbilityHolder abilityHolder;

    public AbilityCreationData(AbilityHolder abilityHolder) {
        this.abilityHolder = abilityHolder;
    }

    /**
     * Gets the {@link AbilityHolder} that has data being created for
     *
     * @return The {@link AbilityHolder} that has data being created for.
     */
    public AbilityHolder getAbilityHolder() {
        return abilityHolder;
    }
}
