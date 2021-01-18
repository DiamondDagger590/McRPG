package us.eunoians.mcrpg.api.event.taming;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.taming.gore.Gore;
import us.eunoians.mcrpg.api.event.AbilityActivateEvent;
import us.eunoians.mcrpg.player.McRPGPlayer;

/**
 * This event is called whenever a {@link org.bukkit.entity.Wolf} attacks a {@link org.bukkit.entity.LivingEntity}
 * and activates {@link Gore}
 *
 * @author DiamondDagger590
 */
public class GoreActivateEvent extends AbilityActivateEvent {

    /**
     * @param mcRPGPlayer The {@link McRPGPlayer} that is activating the event
     * @param ability     The {@link Ability} being activated
     */
    public GoreActivateEvent(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Gore ability) {
        super(mcRPGPlayer, ability, AbilityEventType.COMBAT);
    }

    /**
     * The {@link Ability} that is being activated by this event
     *
     * @return The {@link Ability} that is being activated by this event
     */
    @Override
    @NotNull
    public Gore getAbility() {
        return (Gore) super.getAbility();
    }
}
