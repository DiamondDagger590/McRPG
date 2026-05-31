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
import us.eunoians.mcrpg.ability.impl.type.MobCastableAbility;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.GuardianAbilitiesConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.MobAbilityTriggerEvent;
import us.eunoians.mcrpg.event.ability.guardian.TsunamiWallActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.task.ability.guardian.TsunamiWallTask;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.ABILITY_DURATION;
import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.COOLDOWN;
import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.MANA_COST;

/**
 * Tsunami Wall summons a forward-facing particle wall that knocks back and slows
 * entities on contact.
 */
public final class TsunamiWall extends McRPGAbility
        implements ConfigurableAbility, UnlockableAbility,
        CooldownableAbility, ActiveAbility, ComboActivatable, MobCastableAbility {

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
        return comboActivate(abilityHolder);
    }

    @Override
    public boolean mobActivate(@NotNull AbilityHolder abilityHolder, @NotNull MobAbilityTriggerEvent mobEvent) {
        return spawnWall(abilityHolder, mobEvent.getCaster());
    }

    @Override
    public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
        if (!(Bukkit.getPlayer(abilityHolder.getUUID()) instanceof Player player)) {
            return false;
        }
        return spawnWall(abilityHolder, player);
    }

    /**
     * Fires the {@link TsunamiWallActivateEvent} and, if not cancelled, spawns a
     * forward-facing particle wall from the caster's location that travels outward
     * and applies knockback and slowness on contact.
     *
     * @param abilityHolder The {@link AbilityHolder} activating the ability.
     * @param caster        The {@link LivingEntity} whose location and facing direction
     *                      determine wall placement.
     * @return {@code true} if the ability executed, {@code false} if cancelled.
     */
    private boolean spawnWall(@NotNull AbilityHolder abilityHolder, @NotNull LivingEntity caster) {
        Location casterLoc = caster.getLocation();

        TsunamiWallActivateEvent event = new TsunamiWallActivateEvent(abilityHolder, casterLoc);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        Vector forward = casterLoc.getDirection().setY(0).normalize();
        Location spawnOrigin = casterLoc.clone().add(forward.clone().multiply(1.0));
        Location wallDestination = casterLoc.clone().add(forward.clone().multiply(getSpawnDistance()));
        Vector wallRight = new Vector(-forward.getZ(), 0, forward.getX()).normalize();

        UUID casterUUID = caster.getUniqueId();
        new TsunamiWallTask(getPlugin(), spawnOrigin, wallDestination, wallRight, getWidth(), getHeight(),
                getKnockbackStrength(), getSlownessAmplifier(), getSlownessDurationTicks(),
                forward, casterUUID, getDurationTicks(), getTravelSpeed()).runTask();

        caster.getWorld().playSound(spawnOrigin,
                Sound.ENTITY_GENERIC_SPLASH, 1.0f, 0.6f);

        return true;
    }

    /**
     * Gets the wall width in blocks.
     *
     * @return The configured width.
     */
    public int getWidth() {
        return getYamlDocument().getInt(GuardianAbilitiesConfigFile.TSUNAMI_WALL_WIDTH, 5);
    }

    /**
     * Gets the wall height in blocks.
     *
     * @return The configured height.
     */
    public int getHeight() {
        return getYamlDocument().getInt(GuardianAbilitiesConfigFile.TSUNAMI_WALL_HEIGHT, 3);
    }

    /**
     * Gets the wall duration in ticks.
     *
     * @return The configured duration in ticks.
     */
    public int getDurationTicks() {
        return getYamlDocument().getInt(GuardianAbilitiesConfigFile.TSUNAMI_WALL_DURATION_TICKS, 140);
    }

    /**
     * Gets the knockback strength applied on wall contact.
     *
     * @return The configured knockback strength.
     */
    public double getKnockbackStrength() {
        return getYamlDocument().getDouble(GuardianAbilitiesConfigFile.TSUNAMI_WALL_KNOCKBACK_STRENGTH, 1.5);
    }

    /**
     * Gets the slowness potion amplifier applied on wall contact.
     *
     * @return The configured slowness amplifier.
     */
    public int getSlownessAmplifier() {
        return getYamlDocument().getInt(GuardianAbilitiesConfigFile.TSUNAMI_WALL_SLOWNESS_AMPLIFIER, 2);
    }

    /**
     * Gets the slowness effect duration in ticks applied on wall contact.
     *
     * @return The configured slowness duration in ticks.
     */
    public int getSlownessDurationTicks() {
        return getYamlDocument().getInt(GuardianAbilitiesConfigFile.TSUNAMI_WALL_SLOWNESS_DURATION_TICKS, 60);
    }

    /**
     * Gets the distance in blocks ahead of the caster where the wall spawns.
     *
     * @return The configured spawn distance.
     */
    public double getSpawnDistance() {
        return getYamlDocument().getDouble(GuardianAbilitiesConfigFile.TSUNAMI_WALL_SPAWN_DISTANCE, 2.0);
    }

    /**
     * Gets the forward travel speed of the wall in blocks per tick.
     *
     * @return The configured travel speed.
     */
    public double getTravelSpeed() {
        return getYamlDocument().getDouble(GuardianAbilitiesConfigFile.TSUNAMI_WALL_TRAVEL_SPEED, 0.4);
    }

    @NotNull
    @Override
    public Map<String, String> getItemBuilderPlaceholders(@NotNull McRPGPlayer player) {
        Map<String, String> placeholders = new HashMap<>();
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
