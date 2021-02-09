package us.eunoians.mcrpg.ability;

import org.bukkit.potion.PotionEffect;

import java.util.Set;

/**
 * This interface has generic methods for exposing potion effects that implementations provide
 *
 * @author DiamondDagger590
 */
public interface PotionEffectableAbility extends Ability {

    /**
     * Gets the {@link Set} of {@link PotionEffect}s that should be given to either
     * the user of the {@link Ability} or the target of such {@link Ability}
     *
     * @return The {@link Set} of {@link PotionEffect}s
     */
    public Set<PotionEffect> getPotionEffects();
}
