package us.eunoians.mcrpg.ability.combo;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * Marks an ability as activatable via the combo input system.
 * <p>
 * Abilities implementing this interface are activated through click-combo sequences
 * (e.g. RRR, RRL, RLR) rather than the ready-state chain. Hunger is consumed on each
 * successful activation; if the player lacks sufficient hunger, the activation is denied
 * with feedback (sound + actionbar).
 * <p>
 * Abilities may still implement the legacy ready-state path as a fallback during the PoC.
 */
public interface ComboActivatable {

    /**
     * Activates this ability via the combo system, bypassing the ready-state chain.
     * <p>
     * Implementations should NOT call {@code abilityHolder.unreadyHolder()} since there
     * is no ready state to clear in the combo activation path.
     * Hunger deduction and cooldown management are handled by {@link us.eunoians.mcrpg.listener.ability.OnComboCompleteListener}
     * before this method is called.
     *
     * @param abilityHolder The {@link AbilityHolder} activating this ability.
     */
    void comboActivate(@NotNull AbilityHolder abilityHolder);

    /**
     * Returns the hunger cost (in Minecraft food-level half-points) required to activate
     * this ability via the combo system.
     * <p>
     * Minecraft food level runs from 0 to 20. One visible food bar = 2 half-points.
     * Sprint requires food level &gt;= 6 (3 visible bars), creating meaningful PvP tension.
     *
     * @param abilityHolder The {@link AbilityHolder} attempting to activate.
     * @return The hunger cost in half-points (food level units).
     */
    int getHungerCost(@NotNull AbilityHolder abilityHolder);
}
