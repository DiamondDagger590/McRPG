package us.eunoians.mcrpg.ability.impl.swords.bleedplus;

import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.creation.AbilityCreationData;
import us.eunoians.mcrpg.ability.creation.TierableCreationData;
import us.eunoians.mcrpg.ability.creation.ToggleableCreationData;
import us.eunoians.mcrpg.ability.creation.UnlockableCreationData;
import us.eunoians.mcrpg.api.AbilityHolder;

/**
 * Contains data needed to construct {@link BleedPlus}
 *
 * @author DiamondDagger590
 */
public class BleedPlusCreationData extends AbilityCreationData implements UnlockableCreationData, TierableCreationData, ToggleableCreationData {

    /**
     * The tier of the ability
     */
    private final int tier;

    /**
     * If the ability is toggled on or off
     */
    private final boolean toggled;

    /**
     * If the ability is unlocked or not
     */
    private final boolean unlocked;

    public BleedPlusCreationData(AbilityHolder abilityHolder, int tier, boolean toggled, boolean unlocked) {
        super(abilityHolder);
        this.tier = tier;
        this.toggled = toggled;
        this.unlocked = unlocked;
    }

    /**
     * Gets the tier of the {@link Ability} being created
     *
     * @return The tier of the {@link Ability} being created
     */
    @Override
    public int getTier() {
        return this.tier;
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

    /**
     * Gets if the {@link Ability} being created
     * is unlocked.
     *
     * @return {@code true} if the {@link Ability} being created
     * is unlocked.
     */
    @Override
    public boolean isUnlocked() {
        return this.unlocked;
    }
}
