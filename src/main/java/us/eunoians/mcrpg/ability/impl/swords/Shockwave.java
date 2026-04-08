package us.eunoians.mcrpg.ability.impl.swords;

import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.ActiveAbility;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.combo.ComboConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.swords.ShockwaveActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;

/**
 * Shockwave is a PoC combo-only active ability for the Swords skill.
 * <p>
 * Activated via the combo system, it knocks all nearby entities away from the player
 * within a configurable radius. Hunger is the sole activation gate — no cooldown — to
 * stress-test hunger-as-resource-gate in PvP scenarios.
 */
public final class Shockwave extends McRPGAbility implements ComboActivatable, ActiveAbility, UnlockableAbility, SkillAbility {

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

            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 1.0f, 0.8f);

            // Knock all nearby living entities outward
            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (!(entity instanceof LivingEntity) || isNPC(entity) || entity.equals(player)) {
                    continue;
                }
                Vector outward = entity.getLocation().toVector()
                        .subtract(player.getLocation().toVector())
                        .normalize()
                        .multiply(knockbackForce)
                        .setY(0.4);
                entity.setVelocity(outward);
            }

            // Expanding ground ring: radiates outward over several ticks
            Location center = player.getLocation().add(0, 0.15, 0);
            double maxRadius = radius;
            new BukkitRunnable() {
                double currentRadius = 1.0;

                @Override
                public void run() {
                    if (currentRadius > maxRadius) {
                        cancel();
                        return;
                    }
                    int points = (int) (currentRadius * 8);
                    for (int i = 0; i < points; i++) {
                        double angle = Math.toRadians(i * (360.0 / points));
                        double x = Math.cos(angle) * currentRadius;
                        double z = Math.sin(angle) * currentRadius;
                        player.getWorld().spawnParticle(Particle.CLOUD, center.clone().add(x, 0, z), 1, 0, 0, 0, 0);
                    }
                    currentRadius += 1.5;
                }
            }.runTaskTimer(getPlugin(), 0, 2);
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
    public int getUnlockLevel() {
        return 0;
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
        var item = new ItemStack(Material.WIND_CHARGE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Shockwave", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Knockback nova — blast nearby enemies away", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Hunger: " + getHungerCost(player.asSkillHolder()), NamedTextColor.DARK_GREEN).decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return new AbilityItemBuilder(item, player, this);
    }

    private boolean isNPC(@NotNull Entity entity) {
        return entity.hasMetadata("NPC") || entity instanceof NPC || entity instanceof ArmorStand;
    }
}
