package us.eunoians.mcrpg.ability.impl.guardian;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.GuardianAbilitiesConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.ability.MobAbilityTriggerEvent;
import us.eunoians.mcrpg.event.ability.guardian.TsunamiWallActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.task.ability.guardian.TsunamiWallTask;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tsunami Wall summons a forward-facing particle wall that knocks back and slows
 * entities on contact.
 */
public final class TsunamiWall extends McRPGAbility
        implements ConfigurableAbility, UnlockableAbility,
        CooldownableAbility, ActiveAbility, ComboActivatable {

    public static final NamespacedKey TSUNAMI_WALL_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "tsunami_wall");

    public TsunamiWall(@NotNull McRPG mcRPG) {
        super(mcRPG, TSUNAMI_WALL_KEY);
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "tsunami_wall";
    }

    @Override
    public int getManaCost(@NotNull AbilityHolder abilityHolder) {
        String formula = getYamlDocument().getString(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_MANA_COST, "50");
        return (int) new Parser(formula).getValue();
    }

    @Override
    public long getCooldown(@NotNull AbilityHolder abilityHolder) {
        String formula = getYamlDocument().getString(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_COOLDOWN, "15");
        return (long) new Parser(formula).getValue();
    }

    @Override
    public boolean activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        if (event instanceof MobAbilityTriggerEvent mobEvent) {
            return mobActivate(abilityHolder, mobEvent);
        }
        return comboActivate(abilityHolder);
    }

    /**
     * Activates Tsunami Wall for a MythicMobs mob. Spawns the wall in the caster's facing
     * direction using the same config values as player activation.
     *
     * @param abilityHolder The {@link AbilityHolder} representing the mob caster.
     * @param mobEvent      The {@link MobAbilityTriggerEvent} containing the caster entity.
     * @return {@code true} if the ability executed, {@code false} if cancelled.
     */
    private boolean mobActivate(@NotNull AbilityHolder abilityHolder, @NotNull MobAbilityTriggerEvent mobEvent) {
        LivingEntity caster = mobEvent.getCaster();
        Location casterLoc = caster.getLocation();

        TsunamiWallActivateEvent event = new TsunamiWallActivateEvent(abilityHolder, casterLoc);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        int width = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_WIDTH, 5);
        int height = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_HEIGHT, 3);
        int durationTicks = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_DURATION_TICKS, 140);
        double knockbackStrength = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_KNOCKBACK_STRENGTH, 1.5);
        int slownessAmplifier = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_SLOWNESS_AMPLIFIER, 2);
        int slownessDurationTicks = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_SLOWNESS_DURATION_TICKS, 60);
        double spawnDistance = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_SPAWN_DISTANCE, 2.0);
        double travelSpeed = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_TRAVEL_SPEED, 0.4);

        Vector forward = casterLoc.getDirection().setY(0).normalize();
        Location spawnOrigin = casterLoc.clone().add(forward.clone().multiply(1.0));
        Location wallDestination = casterLoc.clone().add(forward.clone().multiply(spawnDistance));
        Vector wallRight = new Vector(-forward.getZ(), 0, forward.getX()).normalize();

        UUID casterUUID = caster.getUniqueId();
        new TsunamiWallTask(getPlugin(), spawnOrigin, wallDestination, wallRight, width, height,
                knockbackStrength, slownessAmplifier, slownessDurationTicks,
                forward, casterUUID, durationTicks, travelSpeed).runTask();

        caster.getWorld().playSound(spawnOrigin,
                Sound.ENTITY_GENERIC_SPLASH, 1.0f, 0.6f);

        return true;
    }

    @Override
    public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
        if (!(Bukkit.getPlayer(abilityHolder.getUUID()) instanceof Player player)) {
            return false;
        }

        Location playerLoc = player.getLocation();

        TsunamiWallActivateEvent event = new TsunamiWallActivateEvent(abilityHolder, playerLoc);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        int width = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_WIDTH, 5);
        int height = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_HEIGHT, 3);
        int durationTicks = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_DURATION_TICKS, 140);
        double knockbackStrength = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_KNOCKBACK_STRENGTH, 1.5);
        int slownessAmplifier = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_SLOWNESS_AMPLIFIER, 2);
        int slownessDurationTicks = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_SLOWNESS_DURATION_TICKS, 60);
        double spawnDistance = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_SPAWN_DISTANCE, 2.0);
        double travelSpeed = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_TRAVEL_SPEED, 0.4);

        Vector forward = playerLoc.getDirection().setY(0).normalize();
        Location spawnOrigin = playerLoc.clone().add(forward.clone().multiply(1.0));
        Location wallDestination = playerLoc.clone().add(forward.clone().multiply(spawnDistance));
        Vector wallRight = new Vector(-forward.getZ(), 0, forward.getX()).normalize();

        UUID casterUUID = player.getUniqueId();
        new TsunamiWallTask(getPlugin(), spawnOrigin, wallDestination, wallRight, width, height,
                knockbackStrength, slownessAmplifier, slownessDurationTicks,
                forward, casterUUID, durationTicks, travelSpeed).runTask();

        player.getWorld().playSound(spawnOrigin,
                Sound.ENTITY_GENERIC_SPLASH, 1.0f, 0.6f);

        return true;
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
        return GuardianAbilitiesConfigFile.TSUNAMI_WALL_DISPLAY_ITEM;
    }

    @NotNull
    @Override
    public Route getAbilityEnabledRoute() {
        return GuardianAbilitiesConfigFile.TSUNAMI_WALL_ENABLED;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        Set<NamespacedKey> attributes = new HashSet<>(UnlockableAbility.super.getApplicableAttributes());
        attributes.addAll(CooldownableAbility.super.getApplicableAttributes());
        return Set.copyOf(attributes);
    }
}
