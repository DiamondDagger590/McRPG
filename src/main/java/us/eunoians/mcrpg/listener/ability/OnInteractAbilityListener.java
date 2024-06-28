package us.eunoians.mcrpg.listener.ability;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.BaseAbility;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;

import java.util.Set;

public class OnInteractAbilityListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleOnInteractActivate(PlayerInteractEvent playerInteractEvent) {

        McRPG mcRPG = McRPG.getInstance();
        EntityManager entityManager = mcRPG.getEntityManager();
        AbilityRegistry abilityRegistry = mcRPG.getAbilityRegistry();

        Player player = playerInteractEvent.getPlayer();

        entityManager.getAbilityHolder(player.getUniqueId()).ifPresent(abilityHolder -> {

            /*
             * We can do this without too much of an impact on performance due to two assumptions.
             *
             * Assumption 1) A non-player entity will have a highly limited list of available abilities
             * Assumption 2) A player entity will be a loadout holder which will return a limited list of
             * what abilities
             */
            Set<NamespacedKey> allAbilities = abilityHolder instanceof LoadoutHolder loadoutHolder ?
                    loadoutHolder.getAvailableAbilitiesToUse() : abilityHolder.getAvailableAbilities();

            //We can do this safely because we assume that the only abilities in the loadout are registered ones.
            allAbilities.stream().map(abilityRegistry::getRegisteredAbility)
                    .map(ability -> (BaseAbility) ability)
                    .filter(ability -> ability.canEventActivateAbility(playerInteractEvent))
                    .filter(ability -> ability.checkIfComponentFailsActivation(abilityHolder, playerInteractEvent).isEmpty())
                    .forEach(ability -> ability.activateAbility(abilityHolder, playerInteractEvent));
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleOnInteractActivate(PlayerInteractEntityEvent playerInteractEvent) {

        McRPG mcRPG = McRPG.getInstance();
        EntityManager entityManager = mcRPG.getEntityManager();
        AbilityRegistry abilityRegistry = mcRPG.getAbilityRegistry();

        Player player = playerInteractEvent.getPlayer();

        entityManager.getAbilityHolder(player.getUniqueId()).ifPresent(abilityHolder -> {

            /*
             * We can do this without too much of an impact on performance due to two assumptions.
             *
             * Assumption 1) A non-player entity will have a highly limited list of available abilities
             * Assumption 2) A player entity will be a loadout holder which will return a limited list of
             * what abilities
             */
            Set<NamespacedKey> allAbilities = abilityHolder instanceof LoadoutHolder loadoutHolder ?
                    loadoutHolder.getAvailableAbilitiesToUse() : abilityHolder.getAvailableAbilities();

            //We can do this safely because we assume that the only abilities in the loadout are registered ones.
            allAbilities.stream().map(abilityRegistry::getRegisteredAbility)
                    .map(ability -> (BaseAbility) ability)
                    .filter(ability -> ability.canEventActivateAbility(playerInteractEvent))
                    .filter(ability -> ability.checkIfComponentFailsActivation(abilityHolder, playerInteractEvent).isEmpty())
                    .forEach(ability -> ability.activateAbility(abilityHolder, playerInteractEvent));
        });
    }

    // Handle ready events

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleOnInteractReady(PlayerInteractEvent playerInteractEvent) {

        McRPG mcRPG = McRPG.getInstance();
        EntityManager entityManager = mcRPG.getEntityManager();
        AbilityRegistry abilityRegistry = mcRPG.getAbilityRegistry();

        Player player = playerInteractEvent.getPlayer();

        entityManager.getAbilityHolder(player.getUniqueId()).ifPresent(abilityHolder -> {

            /*
             * We can do this without too much of an impact on performance due to two assumptions.
             *
             * Assumption 1) A non-player entity will have a highly limited list of available abilities
             * Assumption 2) A player entity will be a loadout holder which will return a limited list of
             * what abilities
             */
            Set<NamespacedKey> allAbilities = abilityHolder instanceof LoadoutHolder loadoutHolder ?
                    loadoutHolder.getAvailableAbilitiesToUse() : abilityHolder.getAvailableAbilities();

            //We can do this safely because we assume that the only abilities in the loadout are registered ones.
            allAbilities.stream().map(abilityRegistry::getRegisteredAbility)
                    .map(ability -> (BaseAbility) ability)
                    .filter(ability -> ability.canEventReadyAbility(playerInteractEvent))
                    .filter(ability -> ability.checkIfComponentFailsReady(abilityHolder, playerInteractEvent).isEmpty())
                    .forEach(abilityHolder::readyAbility);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleOnInteractReady(PlayerInteractEntityEvent playerInteractEvent) {

        McRPG mcRPG = McRPG.getInstance();
        EntityManager entityManager = mcRPG.getEntityManager();
        AbilityRegistry abilityRegistry = mcRPG.getAbilityRegistry();

        Player player = playerInteractEvent.getPlayer();

        entityManager.getAbilityHolder(player.getUniqueId()).ifPresent(abilityHolder -> {

            /*
             * We can do this without too much of an impact on performance due to two assumptions.
             *
             * Assumption 1) A non-player entity will have a highly limited list of available abilities
             * Assumption 2) A player entity will be a loadout holder which will return a limited list of
             * what abilities
             */
            Set<NamespacedKey> allAbilities = abilityHolder instanceof LoadoutHolder loadoutHolder ?
                    loadoutHolder.getAvailableAbilitiesToUse() : abilityHolder.getAvailableAbilities();

            //We can do this safely because we assume that the only abilities in the loadout are registered ones.
            allAbilities.stream().map(abilityRegistry::getRegisteredAbility)
                    .map(ability -> (BaseAbility) ability)
                    .filter(ability -> ability.canEventReadyAbility(playerInteractEvent))
                    .filter(ability -> ability.checkIfComponentFailsReady(abilityHolder, playerInteractEvent).isEmpty())
                    .forEach(abilityHolder::readyAbility);
        });
    }
}
