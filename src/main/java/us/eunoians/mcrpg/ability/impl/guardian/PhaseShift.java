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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.ActiveAbility;
import us.eunoians.mcrpg.ability.impl.type.CooldownableAbility;
import us.eunoians.mcrpg.ability.impl.type.MobCastableAbility;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.GuardianAbilitiesConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.CombatTargetState;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.MobAbilityTriggerEvent;
import us.eunoians.mcrpg.event.ability.guardian.PhaseShiftActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.task.ability.guardian.PhaseShiftCritWindowTask;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.COOLDOWN;
import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.MANA_COST;
import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.RANGE;

/**
 * Phase Shift teleports the player behind their last-attacked target, resets the attack
 * timer, and grants a guaranteed critical hit window on the next attack.
 */
public final class PhaseShift extends McRPGAbility
        implements ConfigurableAbility, UnlockableAbility,
        CooldownableAbility, ActiveAbility, ComboActivatable, MobCastableAbility {

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
    public boolean mobActivate(@NotNull AbilityHolder abilityHolder, @NotNull MobAbilityTriggerEvent mobEvent) {
        LivingEntity caster = mobEvent.getCaster();
        LivingEntity target = mobEvent.getTarget();

        PhaseShiftActivateEvent event = new PhaseShiftActivateEvent(abilityHolder, target);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        if (!teleportBehindTarget(caster, target)) {
            return false;
        }
        playTeleportEffects(caster);

        return true;
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

        if (!teleportBehindTarget(player, target)) {
            return false;
        }
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
        long windowMillis = getLastHitWindowSeconds() * 1000L;
        if (!state.hasRecentTarget(System.currentTimeMillis(), windowMillis)) {
            return Optional.empty();
        }

        Optional<UUID> targetUUIDOpt = state.getLastAttackedEntityUUID();
        if (targetUUIDOpt.isEmpty() || targetUUIDOpt.get().equals(player.getUniqueId())) {
            return Optional.empty();
        }
        Entity target = player.getWorld().getEntity(targetUUIDOpt.get());
        if (target == null || target.isDead()) {
            return Optional.empty();
        }

        double maxRange = getMaxRange();
        if (player.getLocation().distanceSquared(target.getLocation()) > maxRange * maxRange) {
            return Optional.empty();
        }

        return Optional.of(target);
    }

    /**
     * Teleports the entity behind the target, facing toward it. If the entity is a
     * {@link Player}, the attack cooldown is also reset upon successful teleport.
     *
     * @param entity The entity to teleport.
     * @param target The target entity to teleport behind.
     * @return {@code true} if the teleport succeeded, {@code false} if it was blocked.
     */
    private boolean teleportBehindTarget(@NotNull LivingEntity entity, @NotNull Entity target) {
        double offset = getTeleportOffset();
        Location destination = calculateBehindTarget(target, offset);
        if (!isSafeLocation(destination)) {
            destination = target.getLocation().clone();
        }
        destination.setYaw(calculateFacingYaw(destination, target.getLocation()));
        destination.setPitch(0);

        if (!entity.teleport(destination)) {
            return false;
        }
        if (entity instanceof Player player) {
            player.resetCooldown();
        }
        return true;
    }

    /**
     * Grants the player a guaranteed critical hit window for a configured duration.
     *
     * @param mcRPGPlayer The McRPG player to grant the crit window to.
     */
    private void grantCritWindow(@NotNull McRPGPlayer mcRPGPlayer) {
        int critWindowTicks = getCritWindowTicks();
        mcRPGPlayer.activateCritWindow();
        new PhaseShiftCritWindowTask(getPlugin(), mcRPGPlayer, critWindowTicks).runTask();
    }

    /**
     * Plays the teleport sound and spawns water-themed particles at the entity's location.
     *
     * @param entity The entity whose location is used for effects.
     */
    private void playTeleportEffects(@NotNull LivingEntity entity) {
        spawnTeleportParticles(entity.getLocation());
        entity.getWorld().playSound(entity.getLocation(),
                Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
    }

    /**
     * Gets the distance behind the target where the caster teleports.
     *
     * @return The configured teleport offset in blocks.
     */
    public double getTeleportOffset() {
        return getYamlDocument().getDouble(GuardianAbilitiesConfigFile.PHASE_SHIFT_TELEPORT_OFFSET, 1.5);
    }

    /**
     * Gets the maximum range at which Phase Shift can target an entity.
     *
     * @return The configured max range in blocks.
     */
    public double getMaxRange() {
        return getYamlDocument().getDouble(GuardianAbilitiesConfigFile.PHASE_SHIFT_MAX_RANGE, 12.0);
    }

    /**
     * Gets the time window in seconds during which a recent attack qualifies for Phase Shift.
     *
     * @return The configured last-hit window in seconds.
     */
    public int getLastHitWindowSeconds() {
        return getYamlDocument().getInt(GuardianAbilitiesConfigFile.PHASE_SHIFT_LAST_HIT_WINDOW_SECONDS, 5);
    }

    /**
     * Gets the duration in ticks of the guaranteed critical hit window after teleporting.
     *
     * @return The configured crit window duration in ticks.
     */
    public int getCritWindowTicks() {
        return getYamlDocument().getInt(GuardianAbilitiesConfigFile.PHASE_SHIFT_CRIT_WINDOW_TICKS, 60);
    }

    /**
     * Gets the damage multiplier applied during the critical hit window.
     *
     * @return The configured crit damage multiplier.
     */
    public double getCritDamageMultiplier() {
        return getYamlDocument().getDouble(GuardianAbilitiesConfigFile.PHASE_SHIFT_CRIT_DAMAGE_MULTIPLIER, 1.5);
    }

    @NotNull
    @Override
    public Map<String, String> getItemBuilderPlaceholders(@NotNull McRPGPlayer player) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(RANGE.getKey(),
                getPlugin().registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getDisplayDecimalFormatter().formatDisplayDecimal(player, getMaxRange()));
        placeholders.put(COOLDOWN.getKey(), Long.toString(getCooldown(player.asSkillHolder())));
        placeholders.put(MANA_COST.getKey(), Integer.toString(getManaCost(player.asSkillHolder())));
        return placeholders;
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
