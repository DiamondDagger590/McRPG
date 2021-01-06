package us.eunoians.mcrpg.ability;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.player.McRPGPlayer;

/**
 * This interface represents an {@link us.eunoians.mcrpg.ability.Ability} that is only usable by a player to help
 * in the future when bosses or custom mobs are coded and allow the activation of abilities.
 *
 * Implementing this interface ensures that this {@link Ability} can only be activated from a player so it is safe
 * to assume activators in events for this would be players
 *
 * @author DiamondDagger590
 */
public interface PlayerAbility {

    /**
     * Gets the {@link McRPGPlayer} that this {@link Ability} belongs to.
     *
     * @return The {@link McRPGPlayer} that this {@link Ability} belongs to
     */
    @NotNull
    public McRPGPlayer getPlayer();
}
