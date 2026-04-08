package us.eunoians.mcrpg.external.mythicmobs;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

/**
 * A custom MythicMobs mechanic that delegates ability execution to McRPG.
 * <p>
 * MythicMobs owns AI (when to fire, conditions, cooldowns, targeting).
 * McRPG owns execution (damage, effects, scaling, events).
 * <p>
 * Usage in MythicMobs YAML:
 * <pre>
 *   Skills:
 *   - mcrpg_ability{ability=mcrpg:phase_shift} @target
 * </pre>
 *
 * @see McRPG
 * @see Ability#executeMobAbility
 */
public class McRPGAbilityMechanic implements ITargetedEntitySkill {

    private final NamespacedKey abilityKey;

    /**
     * Creates a new ability mechanic from a MythicMobs line config.
     *
     * @param config The MythicMobs line config containing the {@code ability} parameter
     */
    public McRPGAbilityMechanic(@NotNull MythicLineConfig config) {
        String keyString = config.getString("ability", "");
        this.abilityKey = NamespacedKey.fromString(keyString);
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

        Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
        if (!ability.supportsMobExecution()) {
            return SkillResult.CONDITION_FAILED;
        }

        AbstractEntity casterEntity = data.getCaster().getEntity();
        if (!(BukkitAdapter.adapt(casterEntity) instanceof LivingEntity bukkitCaster)) {
            return SkillResult.CONDITION_FAILED;
        }
        if (!(BukkitAdapter.adapt(target) instanceof LivingEntity bukkitTarget)) {
            return SkillResult.CONDITION_FAILED;
        }

        // Create a transient AbilityHolder for the mob caster
        AbilityHolder mobHolder = new AbilityHolder(McRPG.getInstance(),
                bukkitCaster.getUniqueId());

        ability.executeMobAbility(mobHolder, bukkitCaster, bukkitTarget);

        return SkillResult.SUCCESS;
    }
}
