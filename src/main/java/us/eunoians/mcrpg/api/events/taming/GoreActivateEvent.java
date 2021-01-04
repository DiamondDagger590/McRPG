package us.eunoians.mcrpg.api.events.taming;

import us.eunoians.mcrpg.abilities.Ability;
import us.eunoians.mcrpg.abilities.impl.taming.Gore;
import us.eunoians.mcrpg.api.events.AbilityActivateEvent;
import us.eunoians.mcrpg.player.McRPGPlayer;

/**
 * This event is called whenever a {@link org.bukkit.entity.Wolf} attacks a {@link org.bukkit.entity.LivingEntity}
 * and activates {@link us.eunoians.mcrpg.abilities.impl.taming.Gore}
 *
 * @author DiamondDagger590
 */
public class GoreActivateEvent extends AbilityActivateEvent {

    /**
     * @param mcRPGPlayer The {@link McRPGPlayer} that is activating the event
     * @param ability     The {@link Ability} being activated
     */
    public GoreActivateEvent(McRPGPlayer mcRPGPlayer, Gore ability) {
        super(mcRPGPlayer, ability, AbilityEventType.COMBAT);
    }

    /**
     * The {@link Ability} that is being activated by this event
     *
     * @return The {@link Ability} that is being activated by this event
     */
    @Override
    public Gore getAbility() {
        return (Gore) super.getAbility();
    }
}
