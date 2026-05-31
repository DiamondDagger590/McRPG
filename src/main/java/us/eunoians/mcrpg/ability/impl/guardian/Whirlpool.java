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
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.MobAbilityTriggerEvent;
import us.eunoians.mcrpg.event.ability.guardian.WhirlpoolActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.task.ability.guardian.WhirlpoolZoneTask;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.ABILITY_DURATION;
import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.COOLDOWN;
import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.MANA_COST;
import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.RADIUS;

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
        return spawnWhirlpool(abilityHolder, mobEvent.getCaster());
    }

    @Override
    public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
        if (!(Bukkit.getPlayer(abilityHolder.getUUID()) instanceof Player player)) {
            return false;
        }
        return spawnWhirlpool(abilityHolder, player);
    }

    /**
     * Fires the {@link WhirlpoolActivateEvent} and, if not cancelled, spawns a
     * whirlpool zone at the caster's location that pulls and slows nearby entities.
     *
     * @param abilityHolder The {@link AbilityHolder} activating the ability.
     * @param caster        The {@link LivingEntity} whose location becomes the whirlpool center.
     * @return {@code true} if the ability executed, {@code false} if cancelled.
     */
    private boolean spawnWhirlpool(@NotNull AbilityHolder abilityHolder, @NotNull LivingEntity caster) {
        Location center = caster.getLocation().clone();

        WhirlpoolActivateEvent event = new WhirlpoolActivateEvent(abilityHolder, center);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        UUID casterUUID = caster.getUniqueId();
        new WhirlpoolZoneTask(getPlugin(), center, getRadius(), getPullVelocity(),
                getSlownessAmplifier(), getSlownessDurationTicks(), casterUUID,
                getDurationTicks(), getTickInterval(), getExpansionTicks()).runTask();

        caster.getWorld().playSound(center, Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0f, 0.5f);

        return true;
    }

    /**
     * Gets the whirlpool zone radius in blocks.
     *
     * @return The configured radius.
     */
    public double getRadius() {
        return getYamlDocument().getDouble(GuardianAbilitiesConfigFile.WHIRLPOOL_RADIUS, 4.0);
    }

    /**
     * Gets the whirlpool zone duration in ticks.
     *
     * @return The configured duration in ticks.
     */
    public int getDurationTicks() {
        return getYamlDocument().getInt(GuardianAbilitiesConfigFile.WHIRLPOOL_DURATION_TICKS, 100);
    }

    /**
     * Gets the velocity magnitude applied when pulling entities toward the center.
     *
     * @return The configured pull velocity.
     */
    public double getPullVelocity() {
        return getYamlDocument().getDouble(GuardianAbilitiesConfigFile.WHIRLPOOL_PULL_VELOCITY, 0.1);
    }

    /**
     * Gets the slowness potion amplifier applied to entities in the whirlpool.
     *
     * @return The configured slowness amplifier.
     */
    public int getSlownessAmplifier() {
        return getYamlDocument().getInt(GuardianAbilitiesConfigFile.WHIRLPOOL_SLOWNESS_AMPLIFIER, 0);
    }

    /**
     * Gets the slowness effect duration in ticks applied to entities in the whirlpool.
     *
     * @return The configured slowness duration in ticks.
     */
    public int getSlownessDurationTicks() {
        return getYamlDocument().getInt(GuardianAbilitiesConfigFile.WHIRLPOOL_SLOWNESS_DURATION_TICKS, 40);
    }

    /**
     * Gets the tick interval between whirlpool pull/effect applications.
     *
     * @return The configured tick interval.
     */
    public int getTickInterval() {
        return getYamlDocument().getInt(GuardianAbilitiesConfigFile.WHIRLPOOL_TICK_INTERVAL, 4);
    }

    /**
     * Gets the number of ticks over which the whirlpool expands to full radius.
     *
     * @return The configured expansion duration in ticks.
     */
    public int getExpansionTicks() {
        return getYamlDocument().getInt(GuardianAbilitiesConfigFile.WHIRLPOOL_EXPANSION_TICKS, 40);
    }

    @NotNull
    @Override
    public Map<String, String> getItemBuilderPlaceholders(@NotNull McRPGPlayer player) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(RADIUS.getKey(),
                getPlugin().registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getDisplayDecimalFormatter().formatDisplayDecimal(player, getRadius()));
        placeholders.put(ABILITY_DURATION.getKey(), Integer.toString(getDurationTicks()));
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
