package us.eunoians.mcrpg.listener.combat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Resolves the two combatants involved in an {@link EntityDamageByEntityEvent} for the combat
 * tracker's damage listeners. Extracted so the session-management listener
 * ({@link OnCombatDamageListener}) and the statistics listener ({@link OnCombatDamageStatListener})
 * cannot drift apart on which events they consider valid combat.
 */
final class CombatDamageResolution {

    private CombatDamageResolution() {
    }

    /**
     * Resolves the source and target combatants from a damage event. A {@link Projectile} damager is
     * unwrapped to its shooter, so the archer rather than the arrow is credited as the source.
     * <p>
     * The event is rejected (empty result) when:
     * <ul>
     *     <li>the damager is a projectile whose shooter is not an {@link Entity} (dispenser-fired
     *     arrows, for example)</li>
     *     <li>either resolved side is not a {@link LivingEntity}</li>
     *     <li>the source and target are the same entity — self-damage is not combat</li>
     * </ul>
     *
     * @param event The damage event to resolve.
     * @return An {@link Optional} containing the resolved {@link Combatants}, or empty if any guard rejects the event.
     */
    @NotNull
    static Optional<Combatants> resolve(@NotNull EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (damager instanceof Projectile projectile) {
            if (!(projectile.getShooter() instanceof Entity shooterEntity)) {
                return Optional.empty();
            }
            damager = shooterEntity;
        }

        if (!(damager instanceof LivingEntity sourceEntity) || !(event.getEntity() instanceof LivingEntity targetEntity)) {
            return Optional.empty();
        }

        if (sourceEntity.getUniqueId().equals(targetEntity.getUniqueId())) {
            return Optional.empty();
        }

        return Optional.of(new Combatants(sourceEntity, targetEntity));
    }

    /**
     * The two living entities resolved from a damage event.
     *
     * @param source The entity that dealt the damage (the shooter, for projectile damage).
     * @param target The entity that took the damage.
     */
    record Combatants(@NotNull LivingEntity source, @NotNull LivingEntity target) {

        /**
         * Gets the UUID of the damage source.
         *
         * @return The source entity's UUID.
         */
        @NotNull
        UUID sourceUUID() {
            return source.getUniqueId();
        }

        /**
         * Gets the UUID of the damage target.
         *
         * @return The target entity's UUID.
         */
        @NotNull
        UUID targetUUID() {
            return target.getUniqueId();
        }
    }
}
