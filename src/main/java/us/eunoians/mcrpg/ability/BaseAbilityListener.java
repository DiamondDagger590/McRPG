package us.eunoians.mcrpg.ability;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.player.McRPGPlayer;

/**
 * A base ability listener that can be extended to create listeners for abilties. This class contains most of the
 * necessary objects to write ability listeners.
 *
 * @author OxKitsune
 */
public abstract class BaseAbilityListener implements Listener {

    /**
     * The ability this {@link BaseAbilityListener} is registered for.
     */
    @NotNull
    private final Ability ability;

    /**
     * The {@link Player} this {@link BaseAbilityListener} should focus on.
     */
    @NotNull
    private final LivingEntity entity;

    /**
     * The {@link McRPGPlayer} this {@link BaseAbilityListener} should focus on.
     */
    @NotNull

    // TODO: Change to ability holder
    private final McRPGPlayer mcRPGPlayer;

    /**
     * Construct a new {@link BaseAbilityListener}.
     *
     * @param ability the ability this listener will be created for
     * @param entity the player that this listener should focus on
     * @param mcRPGPlayer the McRPG player this listener should focus on
     */
    public BaseAbilityListener(@NotNull Ability ability, @NotNull LivingEntity entity, @NotNull McRPGPlayer mcRPGPlayer) {
        this.ability = ability;
        this.entity = entity;
        this.mcRPGPlayer = mcRPGPlayer;
    }

    /**
     * Get the {@link Ability} instance this {@link BaseAbilityListener} should focus on.
     *
     * @return the ability
     */
    @NotNull
    public Ability getAbility() {
        return ability;
    }

    /**
     * Get the {@link LivingEntity} instance this {@link BaseAbility} should focus on.
     *
     * @return the {@link LivingEntity}
     */
    @NotNull
    public LivingEntity getEntity() {
        return entity;
    }

    /**
     * Get the {@link McRPGPlayer} instance this {@link BaseAbilityListener} should focus on.
     *
     * @return the {@link McRPGPlayer}
     */
    @NotNull
    public McRPGPlayer getMcRPGPlayer() {
        return mcRPGPlayer;
    }
}
