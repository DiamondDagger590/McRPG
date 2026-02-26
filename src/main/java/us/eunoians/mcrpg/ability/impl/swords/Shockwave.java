package us.eunoians.mcrpg.ability.impl.swords;

import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.combo.ComboConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.swords.ShockwaveActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.util.McRPGMethods;

/**
 * Shockwave is a PoC combo-only active ability for the Swords skill.
 * <p>
 * Activated by combo slot 1 (RRR), it knocks all nearby entities away from the player
 * within a configurable radius. Hunger is the sole activation gate — no cooldown — to
 * stress-test hunger-as-resource-gate in PvP scenarios.
 * <p>
 * This ability does NOT implement {@link us.eunoians.mcrpg.ability.impl.type.UnlockableAbility},
 * making it a "default ability" available to all Swords players automatically.
 */
public final class Shockwave extends McRPGAbility implements ComboActivatable, SkillAbility {

    public static final NamespacedKey SHOCKWAVE_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "shockwave");

    public Shockwave(@NotNull McRPG mcRPG) {
        super(mcRPG, SHOCKWAVE_KEY);
        // No activatable/readyable components — combo-only ability for PoC
    }

    @Override
    @NotNull
    public NamespacedKey getSkillKey() {
        return Swords.SWORDS_KEY;
    }

    @Override
    @NotNull
    public String getDatabaseName() {
        return "shockwave";
    }

    @Override
    public void comboActivate(@NotNull AbilityHolder abilityHolder) {
        ShockwaveActivateEvent shockwaveActivateEvent = new ShockwaveActivateEvent(abilityHolder);
        Bukkit.getPluginManager().callEvent(shockwaveActivateEvent);

        if (!shockwaveActivateEvent.isCancelled() && Bukkit.getPlayer(abilityHolder.getUUID()) instanceof Player player) {
            double radius = getPlugin().registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.FILE).getFile(FileType.COMBO_CONFIG)
                    .getDouble(ComboConfigFile.SHOCKWAVE_RADIUS, 6.0);
            double knockbackForce = getPlugin().registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.FILE).getFile(FileType.COMBO_CONFIG)
                    .getDouble(ComboConfigFile.SHOCKWAVE_KNOCKBACK_FORCE, 2.5);

            // Visual feedback — sweep particles and a concussive boom
            player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation().add(0, 1, 0), 8, 0.5, 0.5, 0.5, 0.1);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.2f);

            // Knock all nearby living entities outward
            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (!(entity instanceof LivingEntity) || isNPC(entity) || entity.equals(player)) {
                    continue;
                }
                Vector outward = entity.getLocation().toVector()
                        .subtract(player.getLocation().toVector())
                        .normalize()
                        .multiply(knockbackForce)
                        .setY(0.4); // slight upward arc
                entity.setVelocity(outward);
            }
        }
    }

    @Override
    public int getHungerCost(@NotNull AbilityHolder abilityHolder) {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE).getFile(FileType.COMBO_CONFIG)
                .getInt(ComboConfigFile.SHOCKWAVE_HUNGER_COST, 8);
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        // Combo-only ability for PoC — activation is handled exclusively via comboActivate()
    }

    @Override
    public boolean isPassive() {
        return false;
    }

    // getName() and getDisplayName() are hardcoded for the PoC.
    // TODO Phase 2: wire these to a localization key in the swords locale file.

    @Override
    @NotNull
    public String getName(@NotNull McRPGPlayer player) {
        return "Shockwave";
    }

    @Override
    @NotNull
    public String getName() {
        return "Shockwave";
    }

    @Override
    @NotNull
    public Component getDisplayName(@NotNull McRPGPlayer player) {
        return Component.text("Shockwave");
    }

    @Override
    @NotNull
    public Component getDisplayName() {
        return Component.text("Shockwave");
    }

    @Override
    @NotNull
    public AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
        // TODO Phase 2: add display item config in swords_configuration.yml or localization file
        throw new UnsupportedOperationException("Shockwave display item not yet configured — PoC ability");
    }

    private boolean isNPC(@NotNull Entity entity) {
        return entity.hasMetadata("NPC") || entity instanceof NPC || entity instanceof ArmorStand;
    }
}
