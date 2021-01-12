package us.eunoians.mcrpg.ability.impl.swords.bleed;

import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.ability.creation.AbilityCreationData;
import us.eunoians.mcrpg.ability.creation.ToggleableCreationData;

/**
 * This is an implementation of {@link AbilityCreationData} to create an instance of {@link Bleed}
 *
 * @author DiamondDagger590
 */
public class BleedCreationData extends AbilityCreationData implements ToggleableCreationData {

    /**
     * Represents the toggled state of if the ability is enabled or not
     * for the {@link AbilityHolder} who owns it.
     */
    private final boolean toggled;

    public BleedCreationData(AbilityHolder abilityHolder, boolean toggled) {
        super(abilityHolder);
        this.toggled = toggled;
    }

    /**
     * Gets if the {@link Ability} represented by the {@link AbilityCreationData}
     * is toggled
     *
     * @return {@code true} if the {@link Ability} represented by the {@link AbilityCreationData}
     * is toggled
     */
    @Override
    public boolean isToggled() {
        return this.toggled;
    }
}
