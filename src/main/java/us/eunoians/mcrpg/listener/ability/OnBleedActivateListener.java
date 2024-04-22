package us.eunoians.mcrpg.listener.ability;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.api.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;

import java.util.Set;

/**
 * This listener handles activating any abilities that rly on {@link BleedActivateEvent}
 * for activation
 */
public class OnBleedActivateListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleBleedActivate(BleedActivateEvent bleedActivateEvent) {

        McRPG mcRPG = McRPG.getInstance();
        EntityManager entityManager = mcRPG.getEntityManager();
        AbilityRegistry abilityRegistry = mcRPG.getAbilityRegistry();

        AbilityHolder abilityHolder = bleedActivateEvent.getAbilityHolder();

        Set<NamespacedKey> allAbilities = abilityHolder instanceof LoadoutHolder loadoutHolder ?
                loadoutHolder.getAvailableAbilitiesToUse() : abilityHolder.getAvailableAbilities();

        //We can do this safely because we assume that the only abilities in the loadout are registered ones.
        allAbilities.stream().map(abilityRegistry::getRegisteredAbility)
                .filter(ability -> ability.canEventActivateAbility(bleedActivateEvent))
                .filter(ability -> ability.checkIfComponentFailsActivation(abilityHolder, bleedActivateEvent).isEmpty())
                .forEach(ability -> ability.activateAbility(abilityHolder, bleedActivateEvent));
    }
}
