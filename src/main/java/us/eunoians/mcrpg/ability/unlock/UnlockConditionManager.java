package us.eunoians.mcrpg.ability.unlock;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableAbility;
import us.eunoians.mcrpg.exception.UnlockConditionParseException;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resolves and caches the effective {@link UnlockConditionType} list for each
 * {@link UnlockableAbility}. Owns:
 * <ul>
 *   <li><b>Resolution:</b> a config {@code unlock-conditions} section, when present and non-empty,
 *       <i>replaces</i> the ability's {@link UnlockableAbility#getDefaultUnlockConditions()
 *       programmatic default}. The override is logged.</li>
 *   <li><b>Caching:</b> resolved lists are cached by ability {@link NamespacedKey}. The cache
 *       is the home for resolved config-state — ability singletons stay stateless.
 *       {@link #reload()} clears and repopulates the cache.</li>
 *   <li><b>Recursive parsing:</b> {@link #parseSection(Section)} parses a single named-keyed
 *       map into a {@code List<UnlockConditionType>}. Used both at the top level and by
 *       composite types ({@code mcrpg:all_of} / {@code mcrpg:any_of}).</li>
 *   <li><b>Startup validation:</b> {@link #reload()} runs once after types, abilities, and
 *       configs are loaded; abilities that resolve to zero met-able conditions <i>and</i> zero
 *       hints log a warning so server owners know the ability is undiscoverable.</li>
 * </ul>
 * <p>
 * This is intentionally a manager-level cache rather than a {@code ReloadableContent} field on
 * each ability. {@code ReloadableContent} models a single {@code (YamlDocument, Route, callback)}
 * triple, but unlock-condition resolution is cross-cutting: it aggregates named sections from
 * multiple skill config files with per-ability Java-default fallbacks. A single manager-level
 * {@link #reload()} entry point is the correct granularity.
 */
public class UnlockConditionManager extends Manager<McRPG> {

    private final Map<NamespacedKey, List<UnlockConditionType>> cache;
    private final Logger logger;

    public UnlockConditionManager(@NotNull McRPG plugin) {
        super(plugin);
        this.cache = new ConcurrentHashMap<>();
        this.logger = plugin.getLogger();
    }

    /**
     * Resolves the effective unlock condition list for the given ability, caching the result.
     * Config takes precedence over the Java default; when config supplies an
     * {@code unlock-conditions} section the Java default is fully replaced and a warning is
     * logged.
     *
     * @param ability the ability whose conditions to resolve
     * @return the resolved condition list, never null (possibly empty)
     */
    @NotNull
    public List<UnlockConditionType> resolve(@NotNull UnlockableAbility ability) {
        return cache.computeIfAbsent(ability.getAbilityKey(), key -> resolveUncached(ability, key));
    }

    /**
     * Performs the actual resolution for a single ability without consulting the cache.
     * Checks the ability's YAML config for an {@code unlock-conditions} section first; if
     * present and non-empty it fully replaces the programmatic default (with a warning when
     * a default existed). Otherwise falls back to
     * {@link UnlockableAbility#getDefaultUnlockConditions()}.
     *
     * @param ability the ability to resolve conditions for
     * @param key     the ability's namespaced key (passed separately because
     *                {@code computeIfAbsent} already extracted it)
     * @return an immutable list of resolved conditions
     */
    @NotNull
    private List<UnlockConditionType> resolveUncached(@NotNull UnlockableAbility ability, @NotNull NamespacedKey key) {
        Optional<Section> sectionOptional = getUnlockConditionsSection(ability);
        if (sectionOptional.isPresent() && !sectionOptional.get().getRoutesAsStrings(false).isEmpty()) {
            List<UnlockConditionType> fromConfig = parseSection(sectionOptional.get());
            List<UnlockConditionType> defaults = ability.getDefaultUnlockConditions();
            if (!defaults.isEmpty()) {
                logger.warning(() -> "Ability " + key + " declares unlock-conditions in config; "
                        + "this REPLACES its " + defaults.size()
                        + " programmatic default condition(s).");
            }
            return List.copyOf(fromConfig);
        }
        return List.copyOf(ability.getDefaultUnlockConditions());
    }

    /**
     * Parses a single named-keyed map of unlock-condition entries into a list of configured
     * {@link UnlockConditionType}s. Each entry must carry a {@code type} key naming a registered
     * type. Entries with missing types, unknown types, or malformed config are skipped with a
     * warning so a single bad entry can't break the rest of the list.
     *
     * @param parent the section containing one named entry per condition
     * @return the parsed list, in iteration order
     */
    @NotNull
    public List<UnlockConditionType> parseSection(@NotNull Section parent) {
        UnlockConditionTypeRegistry typeRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.UNLOCK_CONDITION_TYPE);
        List<UnlockConditionType> conditions = new ArrayList<>();
        for (String conditionId : parent.getRoutesAsStrings(false)) {
            Optional<Section> entryOptional = parent.getOptionalSection(conditionId);
            if (entryOptional.isEmpty()) {
                continue;
            }
            Section entry = entryOptional.get();
            NamespacedKey typeKey = McRPGMethods.parseNamespacedKey(entry.getString("type"));
            if (typeKey == null) {
                logger.warning("Unlock condition '" + conditionId
                        + "' is missing a 'type' key; skipping.");
                continue;
            }
            Optional<UnlockConditionType> typeOptional = typeRegistry.get(typeKey);
            if (typeOptional.isEmpty()) {
                logger.warning("Unknown unlock condition type '" + typeKey + "' for entry '"
                        + conditionId + "'; skipping. Is the providing expansion installed?");
                continue;
            }
            try {
                conditions.add(typeOptional.get().parseConfig(entry));
            } catch (UnlockConditionParseException e) {
                logger.log(Level.WARNING, "Failed to parse unlock condition '" + conditionId
                        + "' of type " + typeKey + "; skipping.", e);
            }
        }
        return conditions;
    }

    /**
     * Clears the cache and then eagerly resolves every registered {@link UnlockableAbility},
     * repopulating the cache and emitting the empty-display startup warning for any ability
     * whose resolved list is entirely empty (no real conditions and no hints). Called once
     * at bootstrap after types, abilities, and configs are registered, and again from the
     * reload command after the file manager re-reads YAML.
     */
    public void reload() {
        cache.clear();
        AbilityRegistry abilityRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.ABILITY);
        for (NamespacedKey abilityKey : abilityRegistry.getAllAbilities()) {
            Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
            if (!(ability instanceof UnlockableAbility unlockable)) {
                continue;
            }
            List<UnlockConditionType> resolved = resolve(unlockable);
            if (resolved.isEmpty()) {
                logger.warning(() -> "UnlockableAbility " + abilityKey
                        + " resolved to zero unlock conditions and zero hints — players have"
                        + " no advertised way to unlock it. Add an 'unlock-conditions' entry or"
                        + " a programmatic default.");
            }
        }
    }

    /**
     * Looks up the {@code unlock-conditions} YAML section for the given ability. Only
     * {@link ConfigurableAbility} instances have a YAML document to query; non-configurable
     * abilities return empty. The ability key's underscores are replaced with hyphens to
     * match the YAML convention used by all skill config files (e.g. ability key
     * {@code deeper_wound} maps to config section {@code deeper-wound}).
     *
     * @param ability the ability whose config section to locate
     * @return the section if it exists, otherwise empty
     */
    @NotNull
    private Optional<Section> getUnlockConditionsSection(@NotNull UnlockableAbility ability) {
        if (!(ability instanceof ConfigurableAbility configurable)) {
            return Optional.empty();
        }
        Route route = Route.from("ability-configuration",
                ability.getAbilityKey().getKey().replace('_', '-'), "unlock-conditions");
        return configurable.getYamlDocument().getOptionalSection(route);
    }
}
