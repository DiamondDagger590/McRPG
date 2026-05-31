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
import us.eunoians.mcrpg.event.ability.MobAbilityTriggerEvent;
import us.eunoians.mcrpg.event.ability.guardian.WhirlpoolActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.task.ability.guardian.WhirlpoolZoneTask;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Whirlpool creates a stationary AoE zone at the player's location that pulls nearby
 * entities toward its center and applies Slowness.
 */
public final class Whirlpool extends McRPGAbility
        implements ConfigurableAbility, UnlockableAbility,
        CooldownableAbility, ActiveAbility, ComboActivatable, MobCastableAbility {

    public static final NamespacedKey WHIRLPOOL_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "whirlpool");

    public Whirlpool(@NotNull McRPG mcRPG) {
        super(mcRPG, WHIRLPOOL_KEY);
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "whirlpool";
    }

    @Override
    public int getManaCost(@NotNull AbilityHolder abilityHolder) {
        String formula = getYamlDocument().getString(
                GuardianAbilitiesConfigFile.WHIRLPOOL_MANA_COST, "25");
        return (int) new Parser(formula).getValue();
    }

    @Override
    public long getCooldown(@NotNull AbilityHolder abilityHolder) {
        String formula = getYamlDocument().getString(
                GuardianAbilitiesConfigFile.WHIRLPOOL_COOLDOWN, "12");
        return (long) new Parser(formula).getValue();
    }

    @Override
    public boolean activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        return comboActivate(abilityHolder);
    }

    @Override
    public boolean mobActivate(@NotNull AbilityHolder abilityHolder, @NotNull MobAbilityTriggerEvent mobEvent) {
        LivingEntity caster = mobEvent.getCaster();
        Location center = caster.getLocation().clone();

        WhirlpoolActivateEvent event = new WhirlpoolActivateEvent(abilityHolder, center);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        double radius = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.WHIRLPOOL_RADIUS, 4.0);
        int durationTicks = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.WHIRLPOOL_DURATION_TICKS, 100);
        double pullVelocity = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.WHIRLPOOL_PULL_VELOCITY, 0.1);
        int slownessAmplifier = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.WHIRLPOOL_SLOWNESS_AMPLIFIER, 0);
        int slownessDurationTicks = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.WHIRLPOOL_SLOWNESS_DURATION_TICKS, 40);
        int tickInterval = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.WHIRLPOOL_TICK_INTERVAL, 4);
        int expansionTicks = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.WHIRLPOOL_EXPANSION_TICKS, 40);

        UUID casterUUID = caster.getUniqueId();
        new WhirlpoolZoneTask(getPlugin(), center, radius, pullVelocity,
                slownessAmplifier, slownessDurationTicks, casterUUID,
                durationTicks, tickInterval, expansionTicks).runTask();

        caster.getWorld().playSound(center, Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0f, 0.5f);

        return true;
    }

    @Override
    public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
        if (!(Bukkit.getPlayer(abilityHolder.getUUID()) instanceof Player player)) {
            return false;
        }

        Location center = player.getLocation().clone();

        WhirlpoolActivateEvent event = new WhirlpoolActivateEvent(abilityHolder, center);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        double radius = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.WHIRLPOOL_RADIUS, 4.0);
        int durationTicks = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.WHIRLPOOL_DURATION_TICKS, 100);
        double pullVelocity = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.WHIRLPOOL_PULL_VELOCITY, 0.1);
        int slownessAmplifier = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.WHIRLPOOL_SLOWNESS_AMPLIFIER, 0);
        int slownessDurationTicks = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.WHIRLPOOL_SLOWNESS_DURATION_TICKS, 40);
        int tickInterval = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.WHIRLPOOL_TICK_INTERVAL, 4);
        int expansionTicks = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.WHIRLPOOL_EXPANSION_TICKS, 40);

        UUID casterUUID = player.getUniqueId();
        new WhirlpoolZoneTask(getPlugin(), center, radius, pullVelocity,
                slownessAmplifier, slownessDurationTicks, casterUUID,
                durationTicks, tickInterval, expansionTicks).runTask();

        player.getWorld().playSound(center, Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0f, 0.5f);

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
        return GuardianAbilitiesConfigFile.WHIRLPOOL_DISPLAY_ITEM;
    }

    @NotNull
    @Override
    public Route getAbilityEnabledRoute() {
        return GuardianAbilitiesConfigFile.WHIRLPOOL_ENABLED;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        Set<NamespacedKey> attributes = new HashSet<>(UnlockableAbility.super.getApplicableAttributes());
        attributes.addAll(CooldownableAbility.super.getApplicableAttributes());
        return Set.copyOf(attributes);
    }
}
