package us.eunoians.mcrpg.external.mythicmobs;

import com.diamonddagger590.mccore.registry.RegistryKey;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.ability.MobAbilityTriggerEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * A custom MythicMobs mechanic that delegates ability execution to McRPG.
 * <p>
 * MythicMobs owns AI (when to fire, conditions, cooldowns, targeting).
 * McRPG owns execution (damage, effects, scaling, events).
 * <p>
 * This mechanic fires a {@link MobAbilityTriggerEvent} through Bukkit's event system.
 * A separate listener handles the actual ability activation, keeping the MM integration
 * decoupled from McRPG's activation logic.
 * <p>
 * Abilities are eagerly registered on the mob's {@link AbilityHolder} at spawn time by
 * {@link MythicMobAbilityParser}. This mechanic also includes a fallback that lazily registers
 * the ability if it was somehow missed during spawn parsing (e.g., dynamically added skills).
 * The holder itself is created at {@code MythicMobSpawnEvent} time and cleaned up on death/despawn.
 * <p>
 * Usage in MythicMobs YAML:
 * <pre>
 *   Skills:
 *   - mcrpg_ability{ability=mcrpg:phase_shift} @target
 *   - mcrpg_ability{ability=mcrpg:rage_spike;tier=2} @target
 * </pre>
 */
public class McRPGAbilityMechanic implements ITargetedEntitySkill {

    private final NamespacedKey abilityKey;
    private final int tier;

    /**
     * Gets the McRPG ability key this mechanic targets.
     *
     * @return The ability {@link NamespacedKey}, or {@code null} if the config was invalid
     */
    @org.jetbrains.annotations.Nullable
    public NamespacedKey getAbilityKey() {
        return abilityKey;
    }

    /**
     * Gets the configured tier for this mechanic (defaults to 1).
     *
     * @return The tier value
     */
    public int getTier() {
        return tier;
    }

    /**
     * Creates a new ability mechanic from a MythicMobs line config.
     *
     * @param config The MythicMobs line config containing the {@code ability} parameter
     *               and optional {@code tier} parameter (defaults to 1)
     */
    public McRPGAbilityMechanic(@NotNull MythicLineConfig config) {
        String keyString = config.getString("ability", "");
        this.abilityKey = NamespacedKey.fromString(keyString);
        this.tier = config.getInteger("tier", 1);
    }

    @Override
    @NotNull
    public SkillResult castAtEntity(@NotNull SkillMetadata data,
                                     @NotNull AbstractEntity target) {
        if (abilityKey == null) {
            return SkillResult.CONDITION_FAILED;
        }

        AbilityRegistry abilityRegistry = McRPG.getInstance().registryAccess()
                .registry(McRPGRegistryKey.ABILITY);
        if (!abilityRegistry.registered(abilityKey)) {
            return SkillResult.CONDITION_FAILED;
        }

        AbstractEntity casterEntity = data.getCaster().getEntity();
        if (!(BukkitAdapter.adapt(casterEntity) instanceof LivingEntity bukkitCaster)) {
            return SkillResult.CONDITION_FAILED;
        }
        if (!(BukkitAdapter.adapt(target) instanceof LivingEntity bukkitTarget)) {
            return SkillResult.CONDITION_FAILED;
        }

        Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
        AbilityHolder mobHolder = getOrCreateHolder(bukkitCaster);
        ensureAbilityRegistered(mobHolder, ability);

        MobAbilityTriggerEvent triggerEvent = new MobAbilityTriggerEvent(
                mobHolder, ability, bukkitCaster, bukkitTarget);
        Bukkit.getPluginManager().callEvent(triggerEvent);

        return SkillResult.SUCCESS;
    }

    /**
     * Gets the tracked {@link AbilityHolder} for the caster, or creates and tracks a new one
     * if none exists (edge case — e.g., spawn event was missed).
     *
     * @param caster The caster entity
     * @return The tracked {@link AbilityHolder}
     */
    @NotNull
    private AbilityHolder getOrCreateHolder(@NotNull LivingEntity caster) {
        EntityManager entityManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.ENTITY);

        return entityManager.getAbilityHolder(caster.getUniqueId())
                .orElseGet(() -> {
                    AbilityHolder holder = new AbilityHolder(McRPG.getInstance(),
                            caster.getUniqueId());
                    entityManager.trackAbilityHolder(holder);
                    return holder;
                });
    }

    /**
     * Ensures the ability is registered on the holder with the configured tier.
     * If the ability is already registered, this is a no-op.
     *
     * @param holder  The mob's ability holder
     * @param ability The ability to register
     */
    private void ensureAbilityRegistered(@NotNull AbilityHolder holder,
                                         @NotNull Ability ability) {
        if (holder.getAvailableAbilities().contains(ability.getAbilityKey())) {
            return;
        }
        holder.addAvailableAbility(ability.getAbilityKey());
        AbilityData abilityData = new AbilityData(ability.getAbilityKey(),
                new AbilityTierAttribute(tier));
        holder.addAbilityData(abilityData);
    }
}
