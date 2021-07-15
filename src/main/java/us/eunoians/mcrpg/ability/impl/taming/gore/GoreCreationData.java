package us.eunoians.mcrpg.ability.impl.taming.gore;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.creation.AbilityCreationData;
import us.eunoians.mcrpg.ability.creation.ToggleableCreationData;
import us.eunoians.mcrpg.api.AbilityHolder;

/**
 * This is used to pass data into the construction of {@link Gore}
 *
 * @author DiamondDagger590
 */
public class GoreCreationData extends AbilityCreationData implements ToggleableCreationData {

    /**
     * If the ability is toggled or not
     */
    private final boolean toggled;

    public GoreCreationData(@NotNull AbilityHolder abilityHolder, boolean toggled) {
        super(abilityHolder, null);
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
