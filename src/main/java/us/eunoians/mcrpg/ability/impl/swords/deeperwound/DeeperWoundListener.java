package us.eunoians.mcrpg.ability.impl.swords.deeperwound;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.player.McRPGPlayer;

/**
 * This listener handles activation of {@link DeeperWound}
 *
 * @author DiamondDagger590
 */
public class DeeperWoundListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleBleed(BleedActivateEvent bleedActivateEvent) {

        AbilityHolder abilityHolder = bleedActivateEvent.getAbilityHolder();

        NamespacedKey id = Ability.getId(DeeperWound.class);
        if (abilityHolder instanceof McRPGPlayer ? abilityHolder.getLoadout().contains(bleedActivateEvent.getAbility()) : abilityHolder.hasAbility(id)) {

            DeeperWound deeperWound = (DeeperWound) abilityHolder.getAbility(id);

            if(deeperWound.isToggled()) {
                deeperWound.activate(abilityHolder, bleedActivateEvent);
            }
        }
    }
}
