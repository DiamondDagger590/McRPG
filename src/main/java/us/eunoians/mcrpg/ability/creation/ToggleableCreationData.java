package us.eunoians.mcrpg.ability.creation;

/**
 * This interface represents something that an implementation of {@link AbilityCreationData} can implement
 * to pass in extra data properly.
 * <p>
 * This should be used for constructing an ability that can be toggled on and off
 *
 * @author DiamondDagger590
 */
public interface ToggleableCreationData {

    /**
     * Gets if the {@link us.eunoians.mcrpg.ability.Ability} represented by the {@link AbilityCreationData}
     * is toggled
     *
     * @return {@code true} if the {@link us.eunoians.mcrpg.ability.Ability} represented by the {@link AbilityCreationData}
     * is toggled
     */
    public boolean isToggled();
}
