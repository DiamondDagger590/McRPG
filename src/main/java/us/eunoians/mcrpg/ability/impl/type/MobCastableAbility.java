package us.eunoians.mcrpg.ability.impl.type;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.ability.MobAbilityTriggerEvent;

/**
 * An {@link Ability} that supports activation by MythicMobs mobs via the {@code mcrpg_ability}
 * mechanic. Abilities that implement this interface opt in to mob casting; the
 * {@link us.eunoians.mcrpg.listener.ability.OnMobAbilityTriggerListener} will call
 * {@link #mobActivate(AbilityHolder, MobAbilityTriggerEvent)} instead of the generic
 * {@link Ability#activateAbility(AbilityHolder, org.bukkit.event.Event)} path.
 * <p>
 * Abilities that do not implement this interface are silently skipped when a mob
 * attempts to cast them.
 */
public interface MobCastableAbility extends Ability {

    /**
     * Activates this ability on behalf of a MythicMobs mob. The event carries the
     * caster and target entities resolved by MythicMobs' targeting system.
     * <p>
     * Implementations should fire the ability's custom Bukkit activate event, check
     * cancellation, and execute the effect. Component checks and cooldowns are
     * intentionally skipped — MythicMobs owns those decisions.
     *
     * @param abilityHolder The {@link AbilityHolder} representing the mob caster.
     * @param mobEvent      The {@link MobAbilityTriggerEvent} containing caster, target, and ability.
     * @return {@code true} if the ability executed successfully, {@code false} if it was
     *         cancelled by an event listener or failed internally.
     */
    boolean mobActivate(@NotNull AbilityHolder abilityHolder, @NotNull MobAbilityTriggerEvent mobEvent);
}
