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
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.ability.MobAbilityTriggerEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.registry.plugin.McRPGPluginHookKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
 * Abilities are eagerly added to the mob's {@link AbilityHolder} at spawn time by
 * {@link MythicMobAbilityParser}. This mechanic also includes a fallback that lazily
 * attaches the ability if it was somehow missed during spawn parsing (e.g., dynamically
 * added skills). The holder itself is created at {@code MythicMobSpawnEvent} time and
 * cleaned up on death/despawn.
 * <p>
 * This mechanic is also the canonical parser for {@code mcrpg_ability} config lines.
 * {@link MythicMobAbilityParser} constructs instances of this class directly when scanning
 * nested {@code MetaSkill} configs, so any new config fields added here are automatically
 * picked up by spawn-time parsing — there is no secondary YAML parser to keep in sync.
 * <p>
 * Attribute extraction is pluggable via {@link MechanicAttributeExtractor}s registered
 * on {@link MythicMobsHook}. McRPG ships a built-in extractor for {@code tier}; third-party
 * plugins register their own for custom parameters.
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
    private final List<AbilityAttribute<?>> attributes;

    /**
     * Creates a new ability mechanic from a MythicMobs line config.
     * <p>
     * The {@code ability} parameter is <b>required</b>. If it is missing or not a valid
     * {@link NamespacedKey}, this constructor throws {@link IllegalArgumentException}
     * so that MythicMobs fails mechanic registration loudly at config load time rather
     * than at mob cast time.
     * <p>
     * All other parameters are extracted by the registered
     * {@link MechanicAttributeExtractor}s on the {@link MythicMobsHook}. Each extractor
     * is invoked against the config; non-empty results are collected as attributes.
     *
     * @param config The MythicMobs line config containing the {@code ability} parameter
     *               and any additional parameters supported by registered extractors
     * @throws IllegalArgumentException If the {@code ability} parameter is missing or invalid
     */
    public McRPGAbilityMechanic(@NotNull MythicLineConfig config) {
        String keyString = config.getString("ability", "");
        NamespacedKey parsedKey = NamespacedKey.fromString(keyString);
        if (parsedKey == null) {
            throw new IllegalArgumentException(
                    "mcrpg_ability mechanic requires a valid 'ability' parameter (got: '" + keyString + "')");
        }
        this.abilityKey = parsedKey;
        this.attributes = extractAttributes(config);
    }

    /**
     * Gets the McRPG ability key this mechanic targets. Guaranteed non-null — a missing
     * or invalid key causes construction to fail.
     *
     * @return The ability {@link NamespacedKey}
     */
    @NotNull
    public NamespacedKey getAbilityKey() {
        return abilityKey;
    }

    /**
     * Gets the list of attributes extracted from the config by the registered
     * {@link MechanicAttributeExtractor}s.
     *
     * @return An unmodifiable list of extracted ability attributes
     */
    @NotNull
    public List<AbilityAttribute<?>> getAttributes() {
        return attributes;
    }

    @Override
    @NotNull
    public SkillResult castAtEntity(@NotNull SkillMetadata data,
                                     @NotNull AbstractEntity target) {
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
        addAbilityToHolderIfAbsent(mobHolder, ability);

        MobAbilityTriggerEvent triggerEvent = new MobAbilityTriggerEvent(
                mobHolder, ability, bukkitCaster, bukkitTarget);
        Bukkit.getPluginManager().callEvent(triggerEvent);

        return SkillResult.SUCCESS;
    }

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

    private void addAbilityToHolderIfAbsent(@NotNull AbilityHolder holder,
                                            @NotNull Ability ability) {
        if (holder.getAvailableAbilities().contains(ability.getAbilityKey())) {
            return;
        }
        holder.addAvailableAbility(ability.getAbilityKey());
        AbilityData abilityData = new AbilityData(ability.getAbilityKey(), attributes);
        holder.addAbilityData(abilityData);
    }

    @NotNull
    private static List<AbilityAttribute<?>> extractAttributes(@NotNull MythicLineConfig config) {
        MythicMobsHook hook = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.PLUGIN_HOOK)
                .<MythicMobsHook>pluginHook(McRPGPluginHookKey.MYTHIC_MOBS)
                .orElseThrow();
        List<AbilityAttribute<?>> result = new ArrayList<>();
        for (MechanicAttributeExtractor extractor : hook.getMechanicAttributeExtractors().values()) {
            extractor.extract(config).ifPresent(result::add);
        }
        return Collections.unmodifiableList(result);
    }
}
