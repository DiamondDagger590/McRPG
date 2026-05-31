package us.eunoians.mcrpg.listener.ability.guardian;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.GuardianAbilitiesConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.guardian.PhaseShiftCritDamageEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Applies the guaranteed critical hit when a player attacks during their Phase Shift
 * crit window.
 */
public final class OnPhaseShiftCritListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerAttack(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        var playerOpt = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(player.getUniqueId());
        if (playerOpt.isEmpty()) {
            return;
        }
        McRPGPlayer mcRPGPlayer = playerOpt.get();
        if (!mcRPGPlayer.hasCritWindow()) {
            return;
        }

        YamlDocument config = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.GUARDIAN_ABILITIES_CONFIG);
        double multiplier = config.getDouble(
                GuardianAbilitiesConfigFile.PHASE_SHIFT_CRIT_DAMAGE_MULTIPLIER, 1.5);

        double originalDamage = event.getDamage();
        double critDamage = originalDamage * multiplier;

        PhaseShiftCritDamageEvent critEvent = new PhaseShiftCritDamageEvent(
                player, target, originalDamage, critDamage, multiplier);
        Bukkit.getPluginManager().callEvent(critEvent);

        if (!critEvent.isCancelled()) {
            event.setDamage(critEvent.getCritDamage());
        }

        mcRPGPlayer.consumeCritWindow();

        target.getWorld().spawnParticle(Particle.CRIT,
                target.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.1);
        player.getWorld().playSound(target.getLocation(),
                Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
    }
}
