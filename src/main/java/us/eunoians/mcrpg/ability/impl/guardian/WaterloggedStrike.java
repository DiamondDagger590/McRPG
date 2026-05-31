package us.eunoians.mcrpg.ability.impl.guardian;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event;
import org.bukkit.persistence.PersistentDataType;
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
import us.eunoians.mcrpg.event.ability.guardian.WaterloggedStrikeActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.task.ability.guardian.WaterloggedStrikeTrailTask;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashSet;
import java.util.Set;

/**
 * Waterlogged Strike fires an invisible water projectile that damages and slows
 * the first entity it hits.
 */
public final class WaterloggedStrike extends McRPGAbility
        implements ConfigurableAbility, UnlockableAbility,
        CooldownableAbility, ActiveAbility, ComboActivatable, MobCastableAbility {

    public static final NamespacedKey WATERLOGGED_STRIKE_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "waterlogged_strike");

    public static final NamespacedKey PROJECTILE_TAG =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "waterlogged_strike_projectile");

    public WaterloggedStrike(@NotNull McRPG mcRPG) {
        super(mcRPG, WATERLOGGED_STRIKE_KEY);
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "waterlogged_strike";
    }

    @Override
    public int getManaCost(@NotNull AbilityHolder abilityHolder) {
        String formula = getYamlDocument().getString(
                GuardianAbilitiesConfigFile.WATERLOGGED_STRIKE_MANA_COST, "15");
        return (int) new Parser(formula).getValue();
    }

    @Override
    public long getCooldown(@NotNull AbilityHolder abilityHolder) {
        String formula = getYamlDocument().getString(
                GuardianAbilitiesConfigFile.WATERLOGGED_STRIKE_COOLDOWN, "1");
        return (long) new Parser(formula).getValue();
    }

    @Override
    public boolean activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        return comboActivate(abilityHolder);
    }

    @Override
    public boolean mobActivate(@NotNull AbilityHolder abilityHolder, @NotNull MobAbilityTriggerEvent mobEvent) {
        return launchStrike(abilityHolder, mobEvent.getCaster());
    }

    @Override
    public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
        if (!(Bukkit.getPlayer(abilityHolder.getUUID()) instanceof Player player)) {
            return false;
        }
        return launchStrike(abilityHolder, player);
    }

    /**
     * Fires the {@link WaterloggedStrikeActivateEvent} and, if not cancelled, launches
     * a tagged invisible snowball projectile from the caster with a water trail.
     *
     * @param abilityHolder The {@link AbilityHolder} activating the ability.
     * @param caster        The {@link LivingEntity} launching the projectile.
     * @return {@code true} if the ability executed, {@code false} if cancelled.
     */
    private boolean launchStrike(@NotNull AbilityHolder abilityHolder, @NotNull LivingEntity caster) {
        WaterloggedStrikeActivateEvent activateEvent = new WaterloggedStrikeActivateEvent(abilityHolder);
        Bukkit.getPluginManager().callEvent(activateEvent);
        if (activateEvent.isCancelled()) {
            return false;
        }

        double speed = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.WATERLOGGED_STRIKE_PROJECTILE_SPEED, 1.5);
        Snowball projectile = caster.launchProjectile(Snowball.class,
                caster.getLocation().getDirection().normalize().multiply(speed));
        projectile.setInvisible(true);
        projectile.getPersistentDataContainer().set(
                PROJECTILE_TAG, PersistentDataType.BOOLEAN, true);

        int maxRange = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.WATERLOGGED_STRIKE_MAX_RANGE, 28);
        new WaterloggedStrikeTrailTask(getPlugin(), projectile, caster.getLocation(), maxRange)
                .runTask();

        caster.getWorld().playSound(caster.getLocation(),
                Sound.ENTITY_FISHING_BOBBER_THROW, 1.0f, 0.8f);

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
        return GuardianAbilitiesConfigFile.WATERLOGGED_STRIKE_DISPLAY_ITEM;
    }

    @NotNull
    @Override
    public Route getAbilityEnabledRoute() {
        return GuardianAbilitiesConfigFile.WATERLOGGED_STRIKE_ENABLED;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        Set<NamespacedKey> attributes = new HashSet<>(UnlockableAbility.super.getApplicableAttributes());
        attributes.addAll(CooldownableAbility.super.getApplicableAttributes());
        return Set.copyOf(attributes);
    }
}
