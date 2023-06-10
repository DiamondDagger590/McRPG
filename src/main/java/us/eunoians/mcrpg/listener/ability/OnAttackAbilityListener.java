package us.eunoians.mcrpg.listener.ability;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.entity.AbilityHolderTracker;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;

import java.util.Set;

public class OnAttackAbilityListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleOnAttackAbilities(EntityDamageByEntityEvent entityDamageByEntityEvent) {

        McRPG mcRPG = McRPG.getInstance();
        AbilityHolderTracker abilityHolderTracker = mcRPG.getEntityManager();
        AbilityRegistry abilityRegistry = mcRPG.getAbilityRegistry();

        Entity damager = entityDamageByEntityEvent.getDamager();
        Entity damaged = entityDamageByEntityEvent.getEntity();

        abilityHolderTracker.getAbilityHolder(damager.getUniqueId()).ifPresent(damagerAbilityHolder -> {

            /*
             * We can do this without too much of an impact on performance due to two assumptions.
             *
             * Assumption 1) A non-player entity will have a highly limited list of available abilities
             * Assumption 2) A player entity will be a loadout holder which will return a limited list of
             * what abilities
             */
            Set<NamespacedKey> allAbilities = damagerAbilityHolder instanceof LoadoutHolder loadoutHolder ?
                    loadoutHolder.getAvailableAbilitiesToUse() : damagerAbilityHolder.getAvailableAbilities();

            //We can do this safely because we assume that the only abilities in the loadout are registered ones.
            allAbilities.stream().map(abilityRegistry::getRegisteredAbility)
                    .filter(ability -> ability.canEventActivateAbility(entityDamageByEntityEvent))
                    .filter(ability -> ability.checkIfComponentFailsActivation(damagerAbilityHolder, entityDamageByEntityEvent).isEmpty())
                    .forEach(ability -> ability.activateAbility(damagerAbilityHolder, entityDamageByEntityEvent));
        });
    }
}
