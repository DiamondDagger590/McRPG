package us.eunoians.mcrpg.ability;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.event.ability.AbilityRegisterEvent;
import us.eunoians.mcrpg.api.event.ability.AbilityUnregisterEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The central ability registry for McRPG.
 * <p>
 * This is where abilities will be registered and unregistered, as well the central location for
 * the creation of {@link AbilityData} for {@link us.eunoians.mcrpg.entity.AbilityHolder ability holders}.
 */
public class AbilityRegistry {

    private final McRPG mcRPG;
    private final Map<NamespacedKey, Ability> abilities;
    private final List<EntityAlliedFunction> entityAlliedFunctions;

    public AbilityRegistry(@NotNull McRPG mcRPG) {
        this.mcRPG = mcRPG;
        abilities = new HashMap<>();
        entityAlliedFunctions = new ArrayList<>();
    }

    /**
     * Registers the {@link Ability} for use in McRPG. This includes internal storage and registering it as a
     * listener via {@link org.bukkit.plugin.PluginManager#registerEvents(Listener, Plugin)}.
     * <p>
     * Once registered, developers can create instances of {@link AbilityData} for the {@link Ability}
     * that can then be used by {@link us.eunoians.mcrpg.entity.AbilityHolder ability holders}.
     * <p>
     * This method also calls a {@link AbilityRegisterEvent} after the ability has been registered.
     *
     * @param ability The {@link Ability} to register
     */
    public void registerAbility(@NotNull Ability ability) {
        abilities.put(ability.getAbilityKey(), ability);
        Bukkit.getPluginManager().registerEvents(ability, mcRPG);

        Bukkit.getPluginManager().callEvent(new AbilityRegisterEvent(ability));
    }

    /**
     * Checks to see if the provided {@link Ability} is registered by using {@link Ability#getAbilityKey()}
     * and calling {@link #isAbilityRegistered(NamespacedKey)}.
     *
     * @param ability The {@link Ability} to check
     * @return {@code true} if the provided {@link Ability} is registered or {@code false} otherwise.
     */
    public boolean isAbilityRegistered(@NotNull Ability ability) {
        return isAbilityRegistered(ability.getAbilityKey());
    }

    /**
     * Checks to see if the provided {@link NamespacedKey} matches an {@link Ability} that is
     * currently registered.
     *
     * @param abilityKey The {@link NamespacedKey} to check
     * @return {@code true} if the provided {@link NamespacedKey} matches a registered {@link Ability} or {@code false} otherwise.
     */
    public boolean isAbilityRegistered(@NotNull NamespacedKey abilityKey) {
        return abilities.containsKey(abilityKey);
    }

    /**
     * Unregisters the provided {@link Ability} from McRPG by calling {@link #unregisterAbility(NamespacedKey)} using the
     * {@link NamespacedKey} from {@link Ability#getAbilityKey()}. This process includes removing it from local storage
     * and unregistering any listeners that the {@link Ability} contains, provided the {@link Ability} was registered already.
     * <p>
     * Note that this does not remove {@link AbilityData} from existing {@link us.eunoians.mcrpg.entity.AbilityHolder ability holders},
     * and will still allow the ability to be saved and exist in loadouts. This just prevents the ability from being loaded for future holders
     * and the ability will not be able to activate at all.
     * <p>
     * This method will also result in the calling of an {@link AbilityUnregisterEvent} after the unregistration
     * has finished ONLY if the ability was registered in the first place.
     *
     * @param ability The {@link Ability} to unregister
     */
    public void unregisterAbility(@NotNull Ability ability) {
        unregisterAbility(ability.getAbilityKey());
    }

    /**
     * Unregisters the provided {@link NamespacedKey} from McRPG. This process includes removing it from local storage
     * and unregistering any listeners that the {@link Ability} associated with this {@link NamespacedKey} contains,
     * provided the {@link Ability} was registered already.
     * <p>
     * Note that this does not remove {@link AbilityData} from existing {@link us.eunoians.mcrpg.entity.AbilityHolder ability holders},
     * and will still allow the ability to be saved and exist in loadouts. This just prevents the ability from being loaded for future holders
     * and the ability will not be able to activate at all.
     * <p>
     * This method will also result in the calling of an {@link AbilityUnregisterEvent} after the unregistration
     * has finished ONLY if the ability was registered in the first place.
     *
     * @param abilityKey The {@link NamespacedKey} to unregister
     */
    public void unregisterAbility(@NotNull NamespacedKey abilityKey) {
        Ability ability = abilities.remove(abilityKey);

        if (ability != null) {
            HandlerList.unregisterAll(ability);
            Bukkit.getPluginManager().callEvent(new AbilityUnregisterEvent(ability));
        }
    }

    /**
     * Register the provided {@link EntityAlliedFunction} to be checked when {@link #areEntitiesAllied(Entity, Entity)} is called
     *
     * @param entityAlliedFunction The {@link EntityAlliedFunction} to register
     */
    public void registerEntityAlliedFunction(@NotNull EntityAlliedFunction entityAlliedFunction) {
        entityAlliedFunctions.add(entityAlliedFunction);
    }

    /**
     * Checks to see if the two provided {@link Entity entities} are allies or not using registered {@link EntityAlliedFunction EntityAlliedFunctions}.
     * <p>
     * This allows 3rd party plugins to anonymously register handling for their specific definition of what an "ally" is.
     * <p>
     * The order of the two entities should not matter as well.
     *
     * @param entity1 The first {@link Entity} to check
     * @param entity2 The second {@link Entity} to check
     * @return {@code true} if the two {@link Entity entities} are considered allies by any registered {@link EntityAlliedFunction EntityAlliedFunctions}.
     */
    public boolean areEntitiesAllied(@NotNull Entity entity1, @NotNull Entity entity2) {

        for (EntityAlliedFunction entityAlliedFunction : entityAlliedFunctions) {

            //We don't care about any others, something considers them allies so stop early
            if (entityAlliedFunction.areAllies(entity1, entity2)) {
                return true;
            }
        }

        return false;
    }

}
