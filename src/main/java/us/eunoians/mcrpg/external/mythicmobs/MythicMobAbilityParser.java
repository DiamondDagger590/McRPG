package us.eunoians.mcrpg.external.mythicmobs;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.configuration.common.ReloadableInteger;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillTrigger;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.config.MythicLineConfigImpl;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.skills.mechanics.CustomMechanic;
import io.lumine.mythic.core.skills.mechanics.MetaSkillMechanic;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses {@code mcrpg_ability} mechanics from a {@link MythicMob}'s skill tree at spawn time,
 * so that the mob's {@link us.eunoians.mcrpg.entity.holder.AbilityHolder} can be eagerly populated
 * with all abilities rather than lazily on first fire.
 * <p>
 * The parser works in two passes:
 * <ol>
 *   <li><strong>Direct mechanics:</strong> iterates top-level {@link SkillMechanic} entries
 *       across all {@link SkillTrigger} triggers and timer skills. If a mechanic is a
 *       {@link CustomMechanic} wrapping a {@link McRPGAbilityMechanic}, the ability key
 *       and attributes are extracted directly from the mechanic instance.</li>
 *   <li><strong>Nested skill references:</strong> for {@link MetaSkillMechanic} entries
 *       ({@code skill:SkillName}), the parser resolves the referenced {@link Skill} and
 *       reads its raw YAML {@code Skills} config lines, matching {@code mcrpg_ability{...}}
 *       patterns. Matched lines are fed through MythicMobs' own {@link MythicLineConfigImpl}
 *       parser and then through {@link McRPGAbilityMechanic}'s constructor, so the mechanic
 *       itself is the single source of truth for param parsing — adding a new field to
 *       {@link McRPGAbilityMechanic} requires no corresponding change here. Nested
 *       {@code skill:} references are recursively resolved with cycle detection.</li>
 * </ol>
 * <p>
 * Parsed results are cached per instance in a {@link Caffeine} cache keyed by mob type
 * internal name. Entries expire after a configurable TTL (see
 * {@link MainConfigFile#MYTHICMOBS_ABILITY_PARSER_CACHE_TTL_MINUTES}) of no reads so the
 * cache does not grow unbounded on long-running servers where obscure mob types are only
 * spawned rarely. The TTL is reloadable: when the reloadable content fires, the Caffeine
 * cache is rebuilt with the new TTL (Caffeine's TTL is fixed at build time). The cache is
 * also explicitly cleared via {@link #clearCache()} on MythicMobs reload.
 */
public class MythicMobAbilityParser {

    /**
     * Fallback TTL (in minutes) applied when the configured value is missing or non-positive.
     */
    private static final int DEFAULT_CACHE_TTL_MINUTES = 60;

    /**
     * Pattern to match {@code mcrpg_ability{...}} in raw YAML skill lines. Group 0 returns
     * the full matched {@code mcrpg_ability{params}} text, which is then handed to
     * {@link MythicLineConfigImpl#of(String)} for parsing.
     */
    private static final Pattern ABILITY_MECHANIC_PATTERN = Pattern.compile("mcrpg_ability\\{[^}]+}");

    /**
     * Pattern to match {@code skill:SkillName} references in raw YAML skill lines.
     * Captures the skill name (everything until a space, semicolon, or end of string).
     */
    private static final Pattern SKILL_REFERENCE_PATTERN = Pattern.compile("^\\s*-?\\s*skill:([^\\s;{]+)");

    /**
     * Maximum recursion depth for resolving nested skill references.
     */
    private static final int MAX_RECURSION_DEPTH = 10;

    /**
     * Reloadable wrapper around the cache TTL config value. Overrides
     * {@link ReloadableContent#reloadContent()} to rebuild the Caffeine cache, since
     * Caffeine's {@code expireAfterAccess} is fixed at build time and cannot be mutated
     * in place.
     */
    private final ReloadableInteger cacheTtlMinutes;

    /**
     * The cache of parsed ability info per mob type. Rebuilt whenever the TTL reloads,
     * so the reference is {@code volatile} rather than {@code final}.
     */
    private volatile Cache<String, List<ParsedAbilityInfo>> cache;

    /**
     * Holds a parsed McRPG ability key and the attributes extracted from a MythicMobs skill
     * definition. Attributes are populated by matching registered
     * {@link us.eunoians.mcrpg.ability.attribute.AbilityAttribute}s from the
     * {@link us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry}.
     *
     * @param abilityKey The McRPG ability {@link NamespacedKey}
     * @param attributes The extracted ability attributes (may be empty if none were configured)
     */
    public record ParsedAbilityInfo(@NotNull NamespacedKey abilityKey,
                                    @NotNull List<AbilityAttribute<?>> attributes) {
    }

    /**
     * Creates a new parser backed by a Caffeine cache whose TTL is read from and
     * reloadable via the provided main config document.
     *
     * @param mainConfig The McRPG main config document providing the cache TTL route
     */
    public MythicMobAbilityParser(@NotNull YamlDocument mainConfig) {
        // Anonymous override rebuilds the Caffeine cache whenever the TTL value reloads,
        // since Caffeine's expireAfterAccess is fixed at build time. Calling getContent()
        // on `this` (the reloadable) is safe here because super.reloadContent() has just
        // assigned it; we avoid touching the outer `cacheTtlMinutes` field because it
        // hasn't been assigned yet while the super constructor is still running.
        this.cacheTtlMinutes = new ReloadableInteger(mainConfig,
                MainConfigFile.MYTHICMOBS_ABILITY_PARSER_CACHE_TTL_MINUTES) {
            @Override
            public void reloadContent() {
                super.reloadContent();
                rebuildCache(getContent());
            }
        };
    }

    /**
     * Returns the reloadable wrapping the cache TTL value. Callers should register this
     * with the {@link com.diamonddagger590.mccore.configuration.ReloadableContentManager}
     * so that {@code /mcrpg reload} picks up runtime changes to the TTL.
     *
     * @return The reloadable content backing the cache TTL
     */
    @NotNull
    public ReloadableContent<Integer> getCacheTtlReloadable() {
        return cacheTtlMinutes;
    }

    /**
     * Rebuilds the Caffeine cache with the given TTL in minutes. Called during construction
     * (via the reloadable's initial reload) and on every subsequent reload. Values of
     * {@code 0} or below fall back to {@link #DEFAULT_CACHE_TTL_MINUTES}.
     *
     * @param ttlMinutes The TTL in minutes from the reloadable content; non-positive values
     *                   fall back to the default
     */
    private void rebuildCache(@NotNull Integer ttlMinutes) {
        int effective = ttlMinutes > 0 ? ttlMinutes : DEFAULT_CACHE_TTL_MINUTES;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(effective))
                .build();
    }

    /**
     * Extracts all McRPG ability definitions from a {@link MythicMob}'s skill tree.
     * Results are cached by the mob type's internal name.
     *
     * @param mythicMob The MythicMob type to parse abilities from
     * @return An unmodifiable list of parsed ability info, or an empty list if none found
     */
    @NotNull
    public List<ParsedAbilityInfo> parseAbilities(@NotNull MythicMob mythicMob) {
        return cache.get(mythicMob.getInternalName(), name -> parseAbilitiesInternal(mythicMob));
    }

    /**
     * Clears the parsed ability cache so that skill tree changes are picked up
     * on next mob spawn.
     */
    public void clearCache() {
        cache.invalidateAll();
    }

    /**
     * Internal parsing logic that traverses all trigger-based and timer-based skills.
     */
    @NotNull
    private List<ParsedAbilityInfo> parseAbilitiesInternal(@NotNull MythicMob mythicMob) {
        List<ParsedAbilityInfo> results = new ArrayList<>();
        Set<String> visitedSkills = new HashSet<>();

        // Check all trigger-based skills
        for (SkillTrigger<?> trigger : SkillTrigger.values()) {
            Queue<SkillMechanic> mechanics = mythicMob.getSkills(trigger);
            if (mechanics != null) {
                for (SkillMechanic mechanic : mechanics) {
                    collectFromMechanic(mechanic, results, visitedSkills, 0);
                }
            }
        }

        // Check timer skills
        Queue<SkillMechanic> timerSkills = mythicMob.getTimerSkills();
        if (timerSkills != null) {
            for (SkillMechanic mechanic : timerSkills) {
                collectFromMechanic(mechanic, results, visitedSkills, 0);
            }
        }

        return Collections.unmodifiableList(results);
    }

    /**
     * Examines a single {@link SkillMechanic} and collects any McRPG ability info from it.
     *
     * @param mechanic      The mechanic to examine
     * @param results       The accumulator list for found abilities
     * @param visitedSkills Set of already-visited skill names for cycle detection
     * @param depth         Current recursion depth
     */
    private void collectFromMechanic(@NotNull SkillMechanic mechanic,
                                     @NotNull List<ParsedAbilityInfo> results,
                                     @NotNull Set<String> visitedSkills,
                                     int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            return;
        }

        // Case 1: Direct mcrpg_ability mechanic (wrapped in CustomMechanic by MM)
        if (mechanic instanceof CustomMechanic customMechanic) {
            customMechanic.getMechanic().ifPresent(inner -> {
                if (inner instanceof McRPGAbilityMechanic mcrpgMechanic) {
                    results.add(new ParsedAbilityInfo(mcrpgMechanic.getAbilityKey(), mcrpgMechanic.getAttributes()));
                }
            });
        }

        // Case 2: skill: reference (MetaSkillMechanic) — resolve and parse the referenced skill
        if (mechanic instanceof MetaSkillMechanic metaSkillMechanic) {
            try {
                Skill skill = metaSkillMechanic.getSkill();
                if (skill != null && visitedSkills.add(skill.getInternalName())) {
                    parseSkillConfig(skill, results, visitedSkills, depth + 1);
                }
            } catch (Exception e) {
                McRPG.getInstance().getLogger().log(Level.WARNING,
                        "Failed to resolve MetaSkillMechanic for McRPG ability parsing", e);
            }
        }
    }

    /**
     * Parses a resolved {@link Skill}'s YAML config to find {@code mcrpg_ability} mechanics
     * and nested {@code skill:} references.
     *
     * @param skill         The resolved named skill
     * @param results       The accumulator list for found abilities
     * @param visitedSkills Set of already-visited skill names for cycle detection
     * @param depth         Current recursion depth
     */
    private void parseSkillConfig(@NotNull Skill skill,
                                  @NotNull List<ParsedAbilityInfo> results,
                                  @NotNull Set<String> visitedSkills,
                                  int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            return;
        }

        MythicConfig config = skill.getConfig();
        if (config == null) {
            return;
        }

        List<String> skillLines = config.getStringList("Skills");
        if (skillLines == null) {
            return;
        }

        for (String line : skillLines) {
            // Check for mcrpg_ability mechanic
            Matcher abilityMatcher = ABILITY_MECHANIC_PATTERN.matcher(line);
            if (abilityMatcher.find()) {
                buildAbilityInfoFromLine(abilityMatcher.group()).ifPresent(results::add);
                continue;
            }

            // Check for nested skill: references
            Matcher skillRefMatcher = SKILL_REFERENCE_PATTERN.matcher(line);
            if (skillRefMatcher.find()) {
                String skillName = skillRefMatcher.group(1);
                if (visitedSkills.add(skillName)) {
                    MythicBukkit.inst().getSkillManager().getSkill(skillName).ifPresent(
                            nestedSkill -> parseSkillConfig(nestedSkill, results, visitedSkills, depth + 1)
                    );
                }
            }
        }
    }

    /**
     * Converts a raw {@code mcrpg_ability{...}} YAML fragment into a {@link ParsedAbilityInfo}
     * by delegating to {@link MythicLineConfigImpl} (MythicMobs' own param parser) and then
     * {@link McRPGAbilityMechanic}'s constructor. This means the mechanic is the single source
     * of truth for supported config fields — adding a new param here requires no changes to
     * the parser, only to the mechanic's constructor.
     *
     * @param rawMechanicText The full matched {@code mcrpg_ability{params}} text from the YAML line
     * @return An {@link Optional} containing the parsed info, or empty if the line
     *         failed to parse
     */
    @NotNull
    private Optional<ParsedAbilityInfo> buildAbilityInfoFromLine(@NotNull String rawMechanicText) {
        try {
            MythicLineConfig lineConfig = MythicLineConfigImpl.of(rawMechanicText);
            McRPGAbilityMechanic mechanic = new McRPGAbilityMechanic(lineConfig);
            return Optional.of(new ParsedAbilityInfo(mechanic.getAbilityKey(), mechanic.getAttributes()));
        } catch (IllegalArgumentException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING,
                    "Invalid mcrpg_ability config in MetaSkill: " + rawMechanicText + " — " + e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            McRPG.getInstance().getLogger().log(Level.WARNING,
                    "Failed to parse mcrpg_ability config in MetaSkill: " + rawMechanicText, e);
            return Optional.empty();
        }
    }
}
