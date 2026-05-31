package us.eunoians.mcrpg.ability.impl.guardian;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.ActiveAbility;
import us.eunoians.mcrpg.ability.impl.type.CooldownableAbility;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.GuardianAbilitiesConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.CombatTargetState;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.guardian.PhaseShiftActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.task.ability.guardian.PhaseShiftCritWindowTask;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Phase Shift teleports the player behind their last-attacked target, resets the attack
 * timer, and grants a guaranteed critical hit window on the next attack.
 */
public final class PhaseShift extends McRPGAbility
        implements ConfigurableAbility, UnlockableAbility,
        CooldownableAbility, ActiveAbility, ComboActivatable {

    public static final NamespacedKey PHASE_SHIFT_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "phase_shift");

    public PhaseShift(@NotNull McRPG mcRPG) {
        super(mcRPG, PHASE_SHIFT_KEY);
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "phase_shift";
    }

    @Override
    public int getManaCost(@NotNull AbilityHolder abilityHolder) {
        String formula = getYamlDocument().getString(
                GuardianAbilitiesConfigFile.PHASE_SHIFT_MANA_COST, "40");
        return (int) new Parser(formula).getValue();
    }

    @Override
    public long getCooldown(@NotNull AbilityHolder abilityHolder) {
        String formula = getYamlDocument().getString(
                GuardianAbilitiesConfigFile.PHASE_SHIFT_COOLDOWN, "12");
        return (long) new Parser(formula).getValue();
    }

    @Override
    public boolean activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        return comboActivate(abilityHolder);
    }

    @Override
    public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
        Optional<McRPGPlayer> playerOpt = getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(abilityHolder.getUUID());
        if (playerOpt.isEmpty()) {
            return false;
        }
        McRPGPlayer mcRPGPlayer = playerOpt.get();
        Optional<Player> bukkitPlayerOpt = mcRPGPlayer.getAsBukkitPlayer();
        if (bukkitPlayerOpt.isEmpty()) {
            return false;
        }
        Player player = bukkitPlayerOpt.get();

        Optional<Entity> targetOpt = resolveTarget(mcRPGPlayer, player);
        if (targetOpt.isEmpty()) {
            return false;
        }
        Entity target = targetOpt.get();

        PhaseShiftActivateEvent event = new PhaseShiftActivateEvent(abilityHolder, target);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        teleportBehindTarget(player, target);
        grantCritWindow(mcRPGPlayer);
        playTeleportEffects(player);

        return true;
    }

    /**
     * Resolves the combat target for Phase Shift, validating recency, range, self-targeting,
     * and liveness.
     *
     * @param mcRPGPlayer The McRPG player activating the ability.
     * @param player      The Bukkit player.
     * @return An {@link Optional} containing the valid target, or empty if no valid target exists.
     */
    @NotNull
    private Optional<Entity> resolveTarget(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Player player) {
        Optional<CombatTargetState> stateOpt = mcRPGPlayer.getCombatTargetState();
        if (stateOpt.isEmpty()) {
            return Optional.empty();
        }
        CombatTargetState state = stateOpt.get();
        long windowMillis = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.PHASE_SHIFT_LAST_HIT_WINDOW_SECONDS, 5) * 1000L;
        if (!state.hasRecentTarget(System.currentTimeMillis(), windowMillis)) {
            return Optional.empty();
        }

        UUID targetUUID = state.getLastAttackedEntityUUID();
        if (targetUUID == null || targetUUID.equals(player.getUniqueId())) {
            return Optional.empty();
        }
        Entity target = player.getWorld().getEntity(targetUUID);
        if (target == null || target.isDead()) {
            return Optional.empty();
        }

        double maxRange = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.PHASE_SHIFT_MAX_RANGE, 12.0);
        if (player.getLocation().distanceSquared(target.getLocation()) > maxRange * maxRange) {
            return Optional.empty();
        }

        return Optional.of(target);
    }

    /**
     * Teleports the player behind the target entity, facing toward it.
     *
     * @param player The player to teleport.
     * @param target The target entity to teleport behind.
     */
    private void teleportBehindTarget(@NotNull Player player, @NotNull Entity target) {
        double offset = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.PHASE_SHIFT_TELEPORT_OFFSET, 1.5);
        Location destination = calculateBehindTarget(target, offset);
        if (!isSafeLocation(destination)) {
            destination = target.getLocation().clone();
        }
        destination.setYaw(calculateFacingYaw(destination, target.getLocation()));
        destination.setPitch(0);

        player.teleportAsync(destination);
        player.resetCooldown();
    }

    /**
     * Grants the player a guaranteed critical hit window for a configured duration.
     *
     * @param mcRPGPlayer The McRPG player to grant the crit window to.
     */
    private void grantCritWindow(@NotNull McRPGPlayer mcRPGPlayer) {
        int critWindowTicks = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.PHASE_SHIFT_CRIT_WINDOW_TICKS, 60);
        mcRPGPlayer.activateCritWindow();
        new PhaseShiftCritWindowTask(getPlugin(), mcRPGPlayer, critWindowTicks).runTask();
    }

    /**
     * Plays the teleport sound and spawns water-themed particles at the player's location.
     *
     * @param player The player whose location is used for effects.
     */
    private void playTeleportEffects(@NotNull Player player) {
        spawnTeleportParticles(player.getLocation());
        player.getWorld().playSound(player.getLocation(),
                Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE).getFile(FileType.GUARDIAN_ABILITIES_CONFIG);
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return GuardianAbilitiesConfigFile.PHASE_SHIFT_DISPLAY_ITEM;
    }

    @NotNull
    @Override
    public Route getAbilityEnabledRoute() {
        return GuardianAbilitiesConfigFile.PHASE_SHIFT_ENABLED;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        Set<NamespacedKey> attributes = new HashSet<>(UnlockableAbility.super.getApplicableAttributes());
        attributes.addAll(CooldownableAbility.super.getApplicableAttributes());
        return Set.copyOf(attributes);
    }

    /**
     * Calculates the location behind the target entity based on its facing direction.
     *
     * @param target The target entity.
     * @param offset The distance behind the target.
     * @return The calculated location behind the target.
     */
    @NotNull
    private Location calculateBehindTarget(@NotNull Entity target, double offset) {
        Location targetLoc = target.getLocation();
        Vector direction = targetLoc.getDirection().normalize();
        return targetLoc.clone().subtract(direction.multiply(offset));
    }

    /**
     * Calculates the yaw angle to face from one location toward another.
     *
     * @param from The source location.
     * @param to   The target location to face.
     * @return The yaw angle in degrees.
     */
    private float calculateFacingYaw(@NotNull Location from, @NotNull Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }

    /**
     * Checks if a location is safe for teleportation (passable feet/head, solid ground).
     *
     * @param location The location to check.
     * @return True if the location is safe to teleport to.
     */
    private boolean isSafeLocation(@NotNull Location location) {
        Block feet = location.getBlock();
        Block head = feet.getRelative(BlockFace.UP);
        Block ground = feet.getRelative(BlockFace.DOWN);
        return feet.isPassable() && head.isPassable() && !ground.isPassable();
    }

    /**
     * Spawns water-themed teleport particles at the given location.
     *
     * @param location The location to spawn particles at.
     */
    private void spawnTeleportParticles(@NotNull Location location) {
        location.getWorld().spawnParticle(Particle.SPLASH, location, 30, 0.5, 1.0, 0.5, 0.1);
        location.getWorld().spawnParticle(Particle.DRIPPING_WATER, location, 15, 0.3, 0.8, 0.3, 0.05);
    }
}
