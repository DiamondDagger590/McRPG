package us.eunoians.mcrpg.listener.ability.guardian;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.guardian.WaterloggedStrike;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.GuardianAbilitiesConfigFile;
import us.eunoians.mcrpg.event.ability.guardian.WaterloggedStrikeImpactEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Handles Waterlogged Strike projectile impacts, applying damage and Slowness to
 * the hit entity.
 */
public final class OnWaterloggedStrikeImpactListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(@NotNull ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) {
            return;
        }
        if (!Boolean.TRUE.equals(snowball.getPersistentDataContainer()
                .get(WaterloggedStrike.PROJECTILE_TAG, PersistentDataType.BOOLEAN))) {
            return;
        }
        if (event.getHitEntity() == null || !(event.getHitEntity() instanceof LivingEntity target)) {
            return;
        }
        if (!(snowball.getShooter() instanceof Player player)) {
            return;
        }

        YamlDocument config = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.GUARDIAN_ABILITIES_CONFIG);

        double damage = config.getDouble(
                GuardianAbilitiesConfigFile.WATERLOGGED_STRIKE_DAMAGE, 3.0);
        int slownessAmplifier = config.getInt(
                GuardianAbilitiesConfigFile.WATERLOGGED_STRIKE_SLOWNESS_AMPLIFIER, 1);
        int slownessDurationTicks = config.getInt(
                GuardianAbilitiesConfigFile.WATERLOGGED_STRIKE_SLOWNESS_DURATION_TICKS, 60);

        WaterloggedStrikeImpactEvent impactEvent = new WaterloggedStrikeImpactEvent(
                player, target, damage, slownessAmplifier, slownessDurationTicks);
        Bukkit.getPluginManager().callEvent(impactEvent);

        if (impactEvent.isCancelled()) {
            return;
        }

        target.damage(impactEvent.getDamage(), player);
        target.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS, impactEvent.getSlownessDurationTicks(),
                impactEvent.getSlownessAmplifier(), false, true, true));

        Location hitLoc = target.getLocation();
        hitLoc.getWorld().spawnParticle(Particle.SPLASH, hitLoc, 20, 0.3, 0.5, 0.3, 0.1);
        hitLoc.getWorld().playSound(hitLoc, Sound.ENTITY_GENERIC_SPLASH, 1.0f, 1.2f);
    }
}
