package us.eunoians.mcrpg.ability;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.util.Parser;

/**
 * This interface represents an ability that is unlocked and always equipped.
 * <p>
 * The various activation rates for this ability scales with the level of the ability
 *
 * @author DiamondDagger590
 */
public interface DefaultAbility extends Ability {

    /**
     * Gets the {@link Parser} that represents the equation needed to activate this ability.
     * <p>
     * Provided that there is an invalid equation offered in the configuration file, the equation will
     * always result in 0.
     *
     * @return The {@link Parser} that represents the equation needed to activate this ability
     */
    @NotNull
    public Parser getActivationEquation();

}
