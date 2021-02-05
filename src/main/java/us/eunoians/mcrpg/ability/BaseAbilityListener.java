package us.eunoians.mcrpg.ability;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.player.McRPGPlayer;

/**
 * A base ability listener that can be extended to create listeners for abilities. This class contains most of the
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
    private final AbilityHolder abilityHolder;

    /**
     * Construct a new {@link BaseAbilityListener}.
     *
     * @param ability the ability this listener will be created for
     * @param entity the entity that this listener should focus on
     */
    public BaseAbilityListener(@NotNull Ability ability, @NotNull LivingEntity entity) {
        this.ability = ability;
        this.entity = entity;
        this.abilityHolder = AbilityHolder.getFromEntity(entity);
    }

    /**
     * Construct a new {@link BaseAbilityListener}.
     *
     * @param ability the ability this listener will be created for
     * @param mcRPGPlayer the McRPG player this listener should focus on
     */
    public BaseAbilityListener(@NotNull Ability ability, @NotNull McRPGPlayer mcRPGPlayer) {
        this.ability = ability;
        this.entity = Bukkit.getPlayer(mcRPGPlayer.getUniqueId());
        this.abilityHolder = mcRPGPlayer;
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
     * Get the {@link AbilityHolder} instance this {@link BaseAbilityListener} should focus on.
     *
     * @return the {@link AbilityHolder}
     */
    @NotNull
    public AbilityHolder getAbilityHolder() {
        return abilityHolder;
    }
}
