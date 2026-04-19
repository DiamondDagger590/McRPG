package us.eunoians.mcrpg.ability.combo;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * Marks an ability as activatable via the combo input system.
 * <p>
 * Abilities implementing this interface are activated through click-combo sequences
 * (e.g. RRR, RRL, RLR). Mana is consumed on each successful activation;
 * if the player lacks sufficient mana, the activation is denied
 * with feedback (sound + action bar + chat).
 */
public interface ComboActivatable {

    /**
     * Activates this ability via the combo system, bypassing the ready-state chain.
     * <p>
     * Implementations should NOT call {@code abilityHolder.unreadyHolder()} since there
     * is no ready state to clear in the combo activation path.
     * Mana deduction and cooldown management are handled by {@link us.eunoians.mcrpg.listener.ability.OnComboCompleteListener}
     * before this method is called.
     *
     * @param abilityHolder The {@link AbilityHolder} activating this ability.
     */
    void comboActivate(@NotNull AbilityHolder abilityHolder);

    /**
     * Returns the mana cost required to activate this ability via the combo system.
     *
     * @param abilityHolder The {@link AbilityHolder} attempting to activate.
     * @return The mana cost (in mana points).
     */
    int getManaCost(@NotNull AbilityHolder abilityHolder);
}
