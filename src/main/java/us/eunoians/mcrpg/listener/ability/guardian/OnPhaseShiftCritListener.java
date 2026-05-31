package us.eunoians.mcrpg.listener.ability.guardian;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.guardian.PhaseShift;
import us.eunoians.mcrpg.event.ability.guardian.PhaseShiftCritDamageEvent;

/**
 * Applies the guaranteed critical hit when a player attacks during their Phase Shift
 * crit window, tracked via a PDC tag on the player.
 */
public final class OnPhaseShiftCritListener implements Listener {

    private final PhaseShift phaseShift;

    /**
     * Creates a new listener that reads the crit damage multiplier from the given ability.
     *
     * @param phaseShift The Phase Shift ability instance.
     */
    public OnPhaseShiftCritListener(@NotNull PhaseShift phaseShift) {
        this.phaseShift = phaseShift;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerAttack(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        if (!player.getPersistentDataContainer().has(PhaseShift.CRIT_WINDOW_TAG, PersistentDataType.BOOLEAN)) {
            return;
        }

        double multiplier = phaseShift.getCritDamageMultiplier();
        double originalDamage = event.getDamage();
        double critDamage = originalDamage * multiplier;

        PhaseShiftCritDamageEvent critEvent = new PhaseShiftCritDamageEvent(
                player, target, originalDamage, critDamage, multiplier);
        Bukkit.getPluginManager().callEvent(critEvent);

        if (!critEvent.isCancelled()) {
            event.setDamage(critEvent.getCritDamage());
        }

        player.getPersistentDataContainer().remove(PhaseShift.CRIT_WINDOW_TAG);

        target.getWorld().spawnParticle(Particle.CRIT,
                target.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.1);
        player.getWorld().playSound(target.getLocation(),
                Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
    }
}
