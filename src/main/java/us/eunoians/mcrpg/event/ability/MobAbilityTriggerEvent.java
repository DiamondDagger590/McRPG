package us.eunoians.mcrpg.event.ability;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * Fired when a MythicMobs mechanic ({@code mcrpg_ability}) triggers an McRPG ability
 * on behalf of a non-player entity.
 * <p>
 * This event carries the caster (mob) and target entities so that ability implementations
 * can use them in {@link Ability#activateAbility(AbilityHolder, org.bukkit.event.Event)}.
 * MythicMobs owns AI decisions (when to fire, cooldowns, targeting); McRPG owns execution.
 */
public class MobAbilityTriggerEvent extends AbilityActivateEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final LivingEntity caster;
    private final LivingEntity target;

    /**
     * Creates a new mob ability trigger event.
     *
     * @param abilityHolder The {@link AbilityHolder} representing the mob caster
     * @param ability       The {@link Ability} being triggered
     * @param caster        The mob entity casting the ability
     * @param target        The entity being targeted
     */
    public MobAbilityTriggerEvent(@NotNull AbilityHolder abilityHolder,
                                  @NotNull Ability ability,
                                  @NotNull LivingEntity caster,
                                  @NotNull LivingEntity target) {
        super(abilityHolder, ability);
        this.caster = caster;
        this.target = target;
    }

    /**
     * Gets the mob entity that is casting the ability.
     *
     * @return The casting {@link LivingEntity}
     */
    @NotNull
    public LivingEntity getCaster() {
        return caster;
    }

    /**
     * Gets the entity being targeted by the ability.
     *
     * @return The target {@link LivingEntity}
     */
    @NotNull
    public LivingEntity getTarget() {
        return target;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
