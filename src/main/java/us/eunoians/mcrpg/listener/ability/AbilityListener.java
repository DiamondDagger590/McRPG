package us.eunoians.mcrpg.listener.ability;

import com.diamonddagger590.mccore.player.PlayerManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.BaseAbility;
import us.eunoians.mcrpg.ability.impl.CooldownableAbility;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.setting.impl.RequireEmptyOffhandSetting;

import java.util.Set;
import java.util.UUID;

/**
 * This interface provides two methods that are generally used by other
 * listeners that are needed to provide a source for ability activation/readying.
 * <p>
 * By providing these methods, adding a new source for activation or readying becomes
 * incredibly trivial as you only need to pass in the {@link UUID} of whoever would be involved
 * and the {@link Event} itself.
 */
public interface AbilityListener extends Listener {

    /**
     * Goes through a list of all valid {@link us.eunoians.mcrpg.ability.impl.Ability Abilities}
     * for an {@link us.eunoians.mcrpg.entity.holder.AbilityHolder} that maps to the provided {@link UUID} while
     * checking for activation logic and finally activating said abilities.
     *
     * @param uuid  The {@link UUID} of the user involved in this event
     * @param event The {@link Event} to activate from
     */
    default void activateAbilities(@NotNull UUID uuid, @NotNull Event event) {
        McRPG mcRPG = McRPG.getInstance();
        EntityManager entityManager = mcRPG.getEntityManager();
        AbilityRegistry abilityRegistry = mcRPG.getAbilityRegistry();

        entityManager.getAbilityHolder(uuid).ifPresent(abilityHolder -> {

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
            allAbilities.stream()
                    .map(ability -> (BaseAbility) abilityRegistry.getRegisteredAbility(ability))
                    .filter(ability -> ability.canEventActivateAbility(event))
                    .filter(ability -> ability.checkIfComponentFailsActivation(abilityHolder, event).isEmpty())
                    .filter(ability -> !(ability instanceof CooldownableAbility cooldownableAbility) || !cooldownableAbility.isAbilityOnCooldown(abilityHolder))
                    .forEach(ability -> ability.activateAbility(abilityHolder, event));
        });
    }

    /**
     * Goes through a list of all valid {@link us.eunoians.mcrpg.ability.impl.Ability Abilities}
     * for an {@link us.eunoians.mcrpg.entity.holder.AbilityHolder} that maps to the provided {@link UUID} while
     * checking for ready logic and finally readying said abilities.
     *
     * @param uuid  The {@link UUID} of the user involved in this event
     * @param event The {@link Event} to ready from
     */
    default void readyAbilities(@NotNull UUID uuid, @NotNull Event event) {
        McRPG mcRPG = McRPG.getInstance();
        EntityManager entityManager = mcRPG.getEntityManager();
        PlayerManager playerManager = mcRPG.getPlayerManager();
        AbilityRegistry abilityRegistry = mcRPG.getAbilityRegistry();

        entityManager.getAbilityHolder(uuid).ifPresent(abilityHolder -> {

            // Check if the player requires empty offhand
            var playerOptional = playerManager.getPlayer(uuid);
            if (playerOptional.isPresent() && playerOptional.get() instanceof McRPGPlayer mcRPGPlayer && mcRPGPlayer.getAsBukkitPlayer().isPresent()) {
                var settingOptional = mcRPGPlayer.getPlayerSetting(RequireEmptyOffhandSetting.SETTING_KEY);
                Player player = mcRPGPlayer.getAsBukkitPlayer().get();
                // If the player has the setting enabled and their offhand isn't empty, don't even try to ready any abilities
                if (settingOptional.isPresent()
                        && settingOptional.get() instanceof RequireEmptyOffhandSetting requireEmptyOffhandSetting
                        && requireEmptyOffhandSetting == RequireEmptyOffhandSetting.ENABLED
                        && player.getInventory().getItemInOffHand().getType() != Material.AIR) {
                    return;
                }
            }

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
            allAbilities.stream()
                    .map(ability -> (BaseAbility) abilityRegistry.getRegisteredAbility(ability))
                    .filter(ability -> ability.canEventReadyAbility(event))
                    .filter(ability -> ability.checkIfComponentFailsReady(abilityHolder, event).isEmpty())
                    .filter(ability -> {
                        if (ability instanceof CooldownableAbility cooldownableAbility && cooldownableAbility.isAbilityOnCooldown(abilityHolder)) {
                            cooldownableAbility.notifyCooldownActive(abilityHolder);
                            return false;
                        }
                        return true;
                    })
                    .forEach(abilityHolder::readyAbility);
        });
    }
}
