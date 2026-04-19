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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
import us.eunoians.mcrpg.event.ability.swords.CleaveActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;

/**
 * Cleave is a PoC combo-only active ability for the Swords skill.
 * <p>
 * Activated via the combo system, it deals configurable damage to all living entities
 * within a tight radius, respecting armor and enchantments naturally via Bukkit's damage API.
 * Hunger is the sole activation gate — no cooldown.
 */
public final class Cleave extends McRPGAbility implements ComboActivatable, ActiveAbility, UnlockableAbility, SkillAbility {

    public static final NamespacedKey CLEAVE_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "cleave");

    public Cleave(@NotNull McRPG mcRPG) {
        super(mcRPG, CLEAVE_KEY);
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
        return "cleave";
    }

    @Override
    public void comboActivate(@NotNull AbilityHolder abilityHolder) {
        CleaveActivateEvent cleaveActivateEvent = new CleaveActivateEvent(abilityHolder);
        Bukkit.getPluginManager().callEvent(cleaveActivateEvent);

        if (!cleaveActivateEvent.isCancelled() && Bukkit.getPlayer(abilityHolder.getUUID()) instanceof Player player) {
            double radius = getPlugin().registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.FILE).getFile(FileType.COMBO_CONFIG)
                    .getDouble(ComboConfigFile.CLEAVE_RADIUS, 3.0);
            double damage = getPlugin().registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.FILE).getFile(FileType.COMBO_CONFIG)
                    .getDouble(ComboConfigFile.CLEAVE_DAMAGE, 4.0);

            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);

            // Sweep ring: evenly spaced SWEEP_ATTACK particles in a circle at chest height
            Location center = player.getLocation().add(0, 1, 0);
            int points = 16;
            for (int i = 0; i < points; i++) {
                double angle = Math.toRadians(i * (360.0 / points));
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, center.clone().add(x, 0, z), 1, 0, 0, 0, 0);
            }

            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (!(entity instanceof LivingEntity livingEntity) || isNPC(entity) || entity.equals(player)) {
                    continue;
                }
                livingEntity.damage(damage, player);
                player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, livingEntity.getLocation().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0.02);
            }
        }
    }

    @Override
    public int getManaCost(@NotNull AbilityHolder abilityHolder) {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE).getFile(FileType.COMBO_CONFIG)
                .getInt(ComboConfigFile.CLEAVE_MANA_COST, 20);
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
        return "Cleave";
    }

    @Override
    @NotNull
    public String getName() {
        return "Cleave";
    }

    @Override
    @NotNull
    public Component getDisplayName(@NotNull McRPGPlayer player) {
        return Component.text("Cleave");
    }

    @Override
    @NotNull
    public Component getDisplayName() {
        return Component.text("Cleave");
    }

    @Override
    @NotNull
    public AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
        var item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Cleave", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("AoE melee damage around you", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Mana: " + getManaCost(player.asSkillHolder()), NamedTextColor.DARK_GREEN).decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return new AbilityItemBuilder(item, player, this);
    }

    private boolean isNPC(@NotNull Entity entity) {
        return entity.hasMetadata("NPC") || entity instanceof NPC || entity instanceof ArmorStand;
    }
}
