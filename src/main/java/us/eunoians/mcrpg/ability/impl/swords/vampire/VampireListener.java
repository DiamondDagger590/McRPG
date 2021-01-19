package us.eunoians.mcrpg.ability.impl.swords.vampire;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.swords.BleedActivateEvent;

/**
 * Handles activation of {@link Vampire}
 *
 * @author DiamondDagger590
 */
public class VampireListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleBleed(BleedActivateEvent bleedActivateEvent) {

        AbilityHolder abilityHolder = bleedActivateEvent.getAbilityHolder();

        NamespacedKey id = Ability.getId(Vampire.class);
        if (abilityHolder.hasAbility(id)) {

            Vampire vampire = (Vampire) abilityHolder.getAbility(id);
            vampire.activate(abilityHolder, bleedActivateEvent);
        }
    }
}
