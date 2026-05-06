package us.eunoians.mcrpg.ability.combo;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.type.ManaAbility;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * Marks an ability as activatable via the combo input system.
 * <p>
 * Abilities implementing this interface are activated through click-combo sequences
 * (e.g. RRR, RRL, RLR). Mana is consumed on each successful activation via
 * {@link ManaAbility#getManaCost(AbilityHolder)}; if the player lacks sufficient mana,
 * the activation is denied with feedback (sound + action bar + chat).
 */
public interface ComboActivatable extends ManaAbility {

    /**
     * Activates this ability via the combo system, bypassing the ready-state chain.
     * <p>
     * Implementations should NOT call {@code abilityHolder.unreadyHolder()} since there
     * is no ready state to clear in the combo activation path.
     * Mana deduction and cooldown management are handled by
     * {@link us.eunoians.mcrpg.listener.ability.OnComboCompleteListener} before this
     * method is called.
     *
     * @param abilityHolder The {@link AbilityHolder} activating this ability.
     * @return {@code true} if the activation completed (or was never cancellable),
     *         {@code false} if the ability's internal Bukkit event was cancelled — the caller
     *         should refund mana when this returns {@code false}.
     */
    boolean comboActivate(@NotNull AbilityHolder abilityHolder);
}
