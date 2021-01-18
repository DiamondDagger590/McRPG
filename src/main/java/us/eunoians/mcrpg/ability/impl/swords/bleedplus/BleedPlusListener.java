package us.eunoians.mcrpg.ability.impl.swords.bleedplus;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.swords.deeperwound.DeeperWound;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.swords.BleedActivateEvent;

/**
 * This listener handles activation of {@link BleedPlus}
 *
 * @author DiamondDagger590
 */
public class BleedPlusListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleBleed(BleedActivateEvent bleedActivateEvent) {

        AbilityHolder abilityHolder = bleedActivateEvent.getAbilityHolder();

        NamespacedKey id = Ability.getId(BleedPlus.class);
        if (abilityHolder.hasAbility(id)) {

            BleedPlus bleedPlus = (BleedPlus) abilityHolder.getAbility(id);
            bleedPlus.activate(abilityHolder, bleedActivateEvent);
        }
    }
}
