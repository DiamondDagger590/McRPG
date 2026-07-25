package us.eunoians.mcrpg.listener.combat;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.CombatTrackerManager;

/**
 * Handles {@link EntityDamageByEntityEvent} at {@link EventPriority#HIGHEST} priority — after most
 * damage modification plugins but before McRPG's {@code MONITOR}-priority ability listeners.
 * Delegates combat interactions to the {@link CombatTrackerManager} for events that
 * {@link CombatDamageResolver} accepts as combat.
 */
public class OnCombatDamageListener implements Listener {

    private final CombatTrackerManager combatTrackerManager;
    private final CombatDamageResolver combatDamageResolver;

    /**
     * Constructs a new {@link OnCombatDamageListener}.
     *
     * @param combatTrackerManager The {@link CombatTrackerManager} to report events to.
     * @param combatDamageResolver The resolver deciding which damage events count as combat.
     */
    public OnCombatDamageListener(@NotNull CombatTrackerManager combatTrackerManager,
                                  @NotNull CombatDamageResolver combatDamageResolver) {
        this.combatTrackerManager = combatTrackerManager;
        this.combatDamageResolver = combatDamageResolver;
    }

    /**
     * Handles entity-on-entity damage events. Resolves the combatants via
     * {@link CombatDamageResolver#resolve(EntityDamageByEntityEvent)} — which unwraps projectile
     * shooters, requires both sides to be {@link LivingEntity}, and rejects self-damage — then
     * reports the combat interaction to the manager.
     *
     * @param event The damage event.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        // Pass the entities directly — the manager builds CustomEntityWrappers lazily and only when a
        // session or participant is actually created, avoiding wrapper construction on every hit.
        combatDamageResolver.resolve(event).ifPresent(combatants ->
                combatTrackerManager.handleCombatInteraction(combatants.sourceUUID(), combatants.targetUUID(),
                        combatants.source(), combatants.target()));
    }
}
