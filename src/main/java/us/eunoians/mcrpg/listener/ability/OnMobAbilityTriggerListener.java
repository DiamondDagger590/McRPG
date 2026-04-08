package us.eunoians.mcrpg.listener.ability;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.ability.MobAbilityTriggerEvent;

/**
 * Listens for {@link MobAbilityTriggerEvent} fired by the MythicMobs {@code mcrpg_ability}
 * mechanic and delegates to McRPG's ability activation system.
 * <p>
 * MythicMobs owns AI decisions (when to fire, cooldowns, targeting).
 * This listener bridges those decisions into McRPG's
 * {@link us.eunoians.mcrpg.ability.Ability#activateAbility} call so that McRPG owns
 * execution (damage, effects, scaling, events).
 * <p>
 * Component checks are intentionally bypassed — MythicMobs has already decided
 * the ability should fire, and the mob's transient {@link us.eunoians.mcrpg.entity.holder.AbilityHolder}
 * does not carry the same component state as a player holder.
 */
public class OnMobAbilityTriggerListener implements Listener {

    /**
     * Handles a mob ability trigger by directly activating the ability carried in the event.
     *
     * @param event The mob ability trigger event fired by the {@code mcrpg_ability} mechanic
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleMobAbilityTrigger(@NotNull MobAbilityTriggerEvent event) {
        event.getAbility().activateAbility(event.getAbilityHolder(), event);
    }
}
