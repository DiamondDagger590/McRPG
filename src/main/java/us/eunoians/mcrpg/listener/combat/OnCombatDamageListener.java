package us.eunoians.mcrpg.listener.combat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.CombatTrackerManager;

import java.util.UUID;

/**
 * Handles {@link EntityDamageByEntityEvent} at {@link EventPriority#HIGHEST} priority — after most
 * damage modification plugins but before McRPG's {@code MONITOR}-priority ability listeners.
 * Resolves projectile shooters and delegates combat interactions to the {@link CombatTrackerManager}.
 */
public class OnCombatDamageListener implements Listener {

    private final CombatTrackerManager combatTrackerManager;

    /**
     * Constructs a new {@link OnCombatDamageListener}.
     *
     * @param combatTrackerManager The {@link CombatTrackerManager} to report events to.
     */
    public OnCombatDamageListener(@NotNull CombatTrackerManager combatTrackerManager) {
        this.combatTrackerManager = combatTrackerManager;
    }

    /**
     * Handles entity-on-entity damage events. Resolves the true source entity for projectiles
     * via {@link Projectile#getShooter()}, constructs {@link com.diamonddagger590.mccore.util.item.CustomEntityWrapper}s
     * for both entities, then reports the combat interaction to the manager.
     * <p>
     * Guards applied:
     * <ul>
     *     <li>Both source and target must be {@link LivingEntity}</li>
     *     <li>Source and target must not be the same entity (compared by UUID)</li>
     * </ul>
     *
     * @param event The damage event.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        // Resolve projectile shooter
        if (damager instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Entity shooterEntity) {
                damager = shooterEntity;
            } else {
                return;
            }
        }

        Entity target = event.getEntity();

        // Both source and target must be LivingEntity
        if (!(damager instanceof LivingEntity sourceEntity) || !(target instanceof LivingEntity targetEntity)) {
            return;
        }

        // Source and target must not be the same entity
        UUID sourceUUID = sourceEntity.getUniqueId();
        UUID targetUUID = targetEntity.getUniqueId();
        if (sourceUUID.equals(targetUUID)) {
            return;
        }

        // Pass the entities directly — the manager builds CustomEntityWrappers lazily and only when a
        // session or participant is actually created, avoiding wrapper construction on every hit.
        combatTrackerManager.handleCombatInteraction(sourceUUID, targetUUID, sourceEntity, targetEntity);
    }
}
