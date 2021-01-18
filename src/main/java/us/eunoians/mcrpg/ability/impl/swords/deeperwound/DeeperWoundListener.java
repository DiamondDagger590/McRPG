package us.eunoians.mcrpg.ability.impl.swords.deeperwound;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.swords.BleedActivateEvent;

public class DeeperWoundListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleBleed(BleedActivateEvent bleedActivateEvent) {

        AbilityHolder abilityHolder = bleedActivateEvent.getAbilityHolder();

        NamespacedKey id = Ability.getId(DeeperWound.class);
        if (abilityHolder.hasAbility(id)) {

            DeeperWound deeperWound = (DeeperWound) abilityHolder.getAbility(id);
            deeperWound.activate(abilityHolder, bleedActivateEvent);
        }
    }
}
