package us.eunoians.mcrpg.ability.impl.type;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * Marks an ability as having a mana cost on activation.
 * <p>
 * The mana check, {@link us.eunoians.mcrpg.event.stat.PlayerStatConsumeEvent} firing,
 * and deduction are handled by the activation dispatcher
 * ({@link us.eunoians.mcrpg.listener.ability.OnComboCompleteListener} for combo abilities,
 * {@link us.eunoians.mcrpg.listener.ability.AbilityListener} for passive/event-driven abilities).
 * Implementations must not deduct mana themselves.
 * <p>
 * Third-party abilities implementing this interface will automatically participate in the mana
 * system — no additional registration is needed beyond implementing the interface.
 */
public interface ManaAbility {

    /**
     * Returns the mana cost required to activate this ability for the given holder.
     * <p>
     * The cost may vary by tier (using the {@link com.diamonddagger590.mccore.util.Parser}
     * with a {@code tier} variable) or be fixed. Costs must never be negative.
     * The global minimum from
     * {@link us.eunoians.mcrpg.configuration.file.MainConfigFile#MANA_MINIMUM_ABILITY_COST}
     * is enforced by the dispatcher, not by this method.
     *
     * @param abilityHolder The {@link AbilityHolder} attempting to activate.
     * @return The mana cost in mana points (non-negative).
     */
    int getManaCost(@NotNull AbilityHolder abilityHolder);
}
