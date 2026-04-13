package us.eunoians.mcrpg.external.mythicmobs;

import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillTrigger;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.skills.mechanics.CustomMechanic;
import io.lumine.mythic.core.skills.mechanics.MetaSkillMechanic;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
 *       and tier are extracted directly from the mechanic instance.</li>
 *   <li><strong>Nested skill references:</strong> for {@link MetaSkillMechanic} entries
 *       ({@code skill:SkillName}), the parser resolves the referenced {@link Skill} and
 *       reads its raw YAML {@code Skills} config lines, searching for {@code mcrpg_ability}
 *       patterns. Nested {@code skill:} references are recursively resolved with cycle detection.</li>
 * </ol>
 * <p>
 * Parsed results are cached per instance, keyed by mob type internal name, since skill trees
 * are static per type. The cache should be invalidated via {@link #clearCache()} whenever
 * MythicMobs reloads its configuration.
 */
public class MythicMobAbilityParser {

    /**
     * Pattern to match {@code mcrpg_ability{...}} in raw YAML skill lines.
     */
    private static final Pattern ABILITY_MECHANIC_PATTERN = Pattern.compile("mcrpg_ability\\{([^}]+)}");

    /**
     * Pattern to match {@code skill:SkillName} references in raw YAML skill lines.
     * Captures the skill name (everything until a space, semicolon, or end of string).
     */
    private static final Pattern SKILL_REFERENCE_PATTERN = Pattern.compile("^\\s*-?\\s*skill:([^\\s;{]+)");

    /**
     * Maximum recursion depth for resolving nested skill references.
     */
    private static final int MAX_RECURSION_DEPTH = 10;

    private final Map<String, List<ParsedAbilityInfo>> cache = new ConcurrentHashMap<>();

    /**
     * Holds a parsed McRPG ability key and its configured tier from a MythicMobs skill definition.
     *
     * @param abilityKey The McRPG ability {@link NamespacedKey}
     * @param tier       The configured tier (defaults to 1 if not specified)
     */
    public record ParsedAbilityInfo(@NotNull NamespacedKey abilityKey, int tier) {
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
        return cache.computeIfAbsent(mythicMob.getInternalName(), name -> parseAbilitiesInternal(mythicMob));
    }

    /**
     * Clears the parsed ability cache. Should be called when MythicMobs reloads
     * its configuration so that skill tree changes are picked up on next mob spawn.
     */
    public void clearCache() {
        cache.clear();
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
                    mcrpgMechanic.getAbilityKey().ifPresent(key ->
                            results.add(new ParsedAbilityInfo(key, mcrpgMechanic.getTier()))
                    );
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
                parseAbilityParams(abilityMatcher.group(1)).ifPresent(results::add);
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
     * Parses the parameter string from an {@code mcrpg_ability{...}} mechanic definition.
     * Expected format: {@code ability=mcrpg:phase_shift;tier=1} or {@code ability=mcrpg:phase_shift}.
     *
     * @param params The raw parameter string between curly braces
     * @return An {@link Optional} containing a {@link ParsedAbilityInfo} if parsing succeeds,
     *         or empty if the ability key is missing/invalid
     */
    @NotNull
    private Optional<ParsedAbilityInfo> parseAbilityParams(@NotNull String params) {
        String abilityStr = null;
        int tier = 1;

        for (String param : params.split(";")) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                if ("ability".equals(key)) {
                    abilityStr = value;
                } else if ("tier".equals(key)) {
                    try {
                        tier = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        McRPG.getInstance().getLogger().log(Level.WARNING,
                                "Invalid tier value in mcrpg_ability config: " + value, e);
                    }
                }
            }
        }

        if (abilityStr == null) {
            return Optional.empty();
        }

        NamespacedKey key = NamespacedKey.fromString(abilityStr);
        if (key == null) {
            return Optional.empty();
        }

        return Optional.of(new ParsedAbilityInfo(key, tier));
    }
}
