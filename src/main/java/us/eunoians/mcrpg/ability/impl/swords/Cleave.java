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
import us.eunoians.mcrpg.event.ability.swords.CleaveActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.util.McRPGMethods;

/**
 * Cleave is a PoC combo-only active ability for the Swords skill.
 * <p>
 * Activated by combo slot 3 (RLR), it deals configurable damage to all living entities
 * within a tight radius, respecting armor and enchantments naturally via Bukkit's damage API.
 * Hunger is the sole activation gate — no cooldown.
 * <p>
 * This ability does NOT implement {@link us.eunoians.mcrpg.ability.impl.type.UnlockableAbility},
 * making it a "default ability" available to all Swords players automatically.
 */
public final class Cleave extends McRPGAbility implements ComboActivatable, SkillAbility {

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

            // Auditory feedback
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);

            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (!(entity instanceof LivingEntity livingEntity) || isNPC(entity) || entity.equals(player)) {
                    continue;
                }
                // damage() respects armor, enchantments, and protection natively
                livingEntity.damage(damage, player);
                // Crit particles at each hit entity to signal the hit visually
                player.getWorld().spawnParticle(Particle.CRIT, livingEntity.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.1);
            }
        }
    }

    @Override
    public int getHungerCost(@NotNull AbilityHolder abilityHolder) {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE).getFile(FileType.COMBO_CONFIG)
                .getInt(ComboConfigFile.CLEAVE_HUNGER_COST, 6);
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
        // TODO Phase 2: add display item config in swords_configuration.yml or localization file
        throw new UnsupportedOperationException("Cleave display item not yet configured — PoC ability");
    }

    private boolean isNPC(@NotNull Entity entity) {
        return entity.hasMetadata("NPC") || entity instanceof NPC || entity instanceof ArmorStand;
    }
}
