package us.eunoians.mcrpg.configuration;

import com.diamonddagger590.mccore.parser.Parser;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.board.template.ObjectiveSelectionConfig;
import us.eunoians.mcrpg.quest.board.template.QuestTemplate;
import us.eunoians.mcrpg.quest.board.template.RarityOverride;
import us.eunoians.mcrpg.quest.board.template.TemplateObjectiveDefinition;
import us.eunoians.mcrpg.quest.board.template.TemplatePhaseDefinition;
import us.eunoians.mcrpg.quest.board.template.TemplateRewardDefinition;
import us.eunoians.mcrpg.quest.board.template.TemplateStageDefinition;
import us.eunoians.mcrpg.quest.board.template.condition.ConditionParser;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;
import us.eunoians.mcrpg.quest.board.template.variable.Pool;
import us.eunoians.mcrpg.quest.board.template.variable.PoolVariable;
import us.eunoians.mcrpg.quest.board.template.variable.RangeVariable;
import us.eunoians.mcrpg.quest.board.template.variable.TemplateVariable;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Loads {@link QuestTemplate} definitions from YAML files.
 * <p>
 * Recursively scans directories for {@code .yml}/{@code .yaml} files. Each file can
 * contain one or more templates under a top-level {@code quest-templates} key. Follows
 * the same directory-scanning pattern as {@link QuestConfigLoader}.
 * <p>
 * Variable names in YAML use underscores ({@code target_blocks}, {@code block_count})
 * because the {@link Parser} treats {@code -} as subtraction. All other YAML keys use
 * hyphens per normal convention.
 * <p>
 * Expression syntax validation is performed at load time: all {@code required-progress}
 * and reward {@code amount} expressions are trial-parsed with dummy variable values to
 * catch syntax errors early.
 */
public final class QuestTemplateConfigLoader {

    private final Logger logger;

    public QuestTemplateConfigLoader(@NotNull Logger logger) {
        this.logger = logger;
    }

    /**
     * Loads templates from a single directory (recursively scanning for .yml/.yaml).
     *
     * @param templatesDirectory the directory to scan
     * @return an ordered map of template key to parsed template
     */
    @NotNull
    public Map<NamespacedKey, QuestTemplate> loadTemplatesFromDirectory(@NotNull File templatesDirectory) {
        Map<NamespacedKey, QuestTemplate> templates = new LinkedHashMap<>();

        if (!templatesDirectory.exists() || !templatesDirectory.isDirectory()) {
            return templates;
        }

        try (Stream<Path> paths = Files.walk(templatesDirectory.toPath())) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.endsWith(".yml") || name.endsWith(".yaml");
                    })
                    .sorted()
                    .forEach(path -> loadTemplatesFromFile(path.toFile(), templates));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to walk templates directory: " + templatesDirectory.getAbsolutePath(), e);
        }

        logger.info("Loaded " + templates.size() + " quest template(s) from " + templatesDirectory.getName() + "/");
        return templates;
    }

    /**
     * Loads templates from all provided directories, merging the results.
     * Duplicate keys across directories cause a warning log; the last-loaded wins.
     *
     * @param directories the directories to scan
     * @return a merged ordered map of all loaded templates
     */
    @NotNull
    public Map<NamespacedKey, QuestTemplate> loadTemplatesFromDirectories(@NotNull List<File> directories) {
        Map<NamespacedKey, QuestTemplate> merged = new LinkedHashMap<>();
        for (File dir : directories) {
            if (!dir.isDirectory()) {
                continue;
            }
            Map<NamespacedKey, QuestTemplate> loaded = loadTemplatesFromDirectory(dir);
            for (Map.Entry<NamespacedKey, QuestTemplate> entry : loaded.entrySet()) {
                if (merged.containsKey(entry.getKey())) {
                    logger.warning("Duplicate template key '" + entry.getKey()
                            + "' across directories; last-loaded from " + dir.getName() + "/ wins");
                }
                merged.put(entry.getKey(), entry.getValue());
            }
        }
        return merged;
    }

    /**
     * Parses all template definitions from a single YAML file and adds them to the
     * accumulator map. Reads the file into memory via byte array to avoid holding
     * file handles open (BoostedYaml can leak handles on Windows).
     *
     * @param file      the YAML file to parse
     * @param templates the accumulator map; duplicate keys within a directory are skipped
     *                  with a warning (first-loaded wins)
     */
    private void loadTemplatesFromFile(@NotNull File file,
                                       @NotNull Map<NamespacedKey, QuestTemplate> templates) {
        YamlDocument yaml;
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            yaml = YamlDocument.create(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load YAML file " + file.getName(), e);
            return;
        }

        Section templatesSection = yaml.getSection("quest-templates");
        if (templatesSection == null) {
            return;
        }

        for (String keyString : templatesSection.getRoutesAsStrings(false)) {
            Optional<NamespacedKey> templateKeyOpt = parseNamespacedKey(keyString);
            if (templateKeyOpt.isEmpty()) {
                logger.warning("Invalid template key '" + keyString + "' in " + file.getName() + ", skipping");
                continue;
            }
            NamespacedKey templateKey = templateKeyOpt.get();

            if (templates.containsKey(templateKey)) {
                logger.warning("Duplicate template key '" + templateKey + "' in " + file.getName()
                        + ", skipping (first-loaded wins)");
                continue;
            }

            Section section = templatesSection.getSection(keyString);
            if (section == null) {
                logger.warning("Template '" + keyString + "' in " + file.getName()
                        + " has no configuration, skipping");
                continue;
            }

            try {
                QuestTemplate template = parseTemplate(templateKey, section, file.getName());
                templates.put(templateKey, template);
            } catch (Exception e) {
                logger.warning("Failed to parse template '" + keyString + "' in " + file.getName()
                        + ": " + e.getMessage());
            }
        }
    }

    /**
     * Parses a single quest template from a BoostedYaml section. Handles all top-level
     * fields ({@code display-name-route}, {@code board-eligible}, {@code scope}, etc.),
     * delegates variable/phase/reward parsing to dedicated methods, then runs both
     * variable reference and expression syntax validation before returning.
     *
     * @param key      the parsed namespaced key for the template
     * @param section  the BoostedYaml section containing the template's configuration
     * @param fileName the source file name (for log messages)
     * @return the parsed quest template
     * @throws IllegalArgumentException if required fields are missing or validation fails
     */
    @NotNull
    private QuestTemplate parseTemplate(@NotNull NamespacedKey key,
                                        @NotNull Section section,
                                        @NotNull String fileName) {
        String displayNameRouteStr = section.getString("display-name-route");
        if (displayNameRouteStr == null || displayNameRouteStr.isBlank()) {
            throw new IllegalArgumentException("Missing 'display-name-route'");
        }
        Route displayNameRoute = Route.fromString(displayNameRouteStr);

        boolean boardEligible = section.getBoolean("board-eligible", true);

        String scopeStr = section.getString("scope", "mcrpg:single_player");
        NamespacedKey scopeProviderKey = parseNamespacedKey(scopeStr)
                .orElseThrow(() -> new IllegalArgumentException("Invalid scope: " + scopeStr));

        Set<NamespacedKey> supportedRarities = parseSupportedRarities(section);
        if (supportedRarities.isEmpty()) {
            throw new IllegalArgumentException("Template must have at least one supported rarity");
        }

        Map<NamespacedKey, RarityOverride> rarityOverrides = parseRarityOverrides(section);
        Map<String, TemplateVariable> variables = parseVariables(section, key.toString());

        List<TemplatePhaseDefinition> phases = parsePhases(section, key.toString());
        if (phases.isEmpty()) {
            throw new IllegalArgumentException("Template must have at least one phase");
        }

        List<TemplateRewardDefinition> rewards = parseRewardDefinitions(section);

        RewardDistributionConfig rewardDistribution = QuestConfigLoader.parseRewardDistribution(
                section, fileName, key.toString()).orElse(null);

        TemplateCondition prerequisite = ConditionParser.parsePrerequisiteBlock(section);

        validateExpressions(variables, phases, rewards, key.toString());

        return new QuestTemplate(key, displayNameRoute, boardEligible, scopeProviderKey,
                supportedRarities, rarityOverrides, variables, phases, rewards, rewardDistribution, prerequisite, null);
    }

    /**
     * Parses the {@code supported-rarities} string list from a template section. Each
     * entry is resolved as a {@link NamespacedKey} via {@link #parseNamespacedKey}.
     *
     * @param section the template section
     * @return the ordered set of supported rarity keys (empty if the field is absent)
     */
    @NotNull
    private Set<NamespacedKey> parseSupportedRarities(@NotNull Section section) {
        Set<NamespacedKey> rarities = new LinkedHashSet<>();
        if (!section.contains("supported-rarities")) {
            return rarities;
        }
        for (String raw : section.getStringList("supported-rarities")) {
            parseNamespacedKey(raw).ifPresent(rarities::add);
        }
        return rarities;
    }

    /**
     * Parses the optional {@code rarity-overrides} map from a template section. Each
     * entry maps a rarity key to optional {@code difficulty-multiplier} and
     * {@code reward-multiplier} overrides.
     *
     * @param section the template section
     * @return the rarity override map (empty if the field is absent)
     */
    @NotNull
    private Map<NamespacedKey, RarityOverride> parseRarityOverrides(@NotNull Section section) {
        Map<NamespacedKey, RarityOverride> overrides = new LinkedHashMap<>();
        Section overridesSection = section.getSection("rarity-overrides");
        if (overridesSection == null) {
            return overrides;
        }
        for (String rawKey : overridesSection.getRoutesAsStrings(false)) {
            Optional<NamespacedKey> rarityKeyOpt = parseNamespacedKey(rawKey);
            if (rarityKeyOpt.isEmpty()) {
                continue;
            }
            NamespacedKey rarityKey = rarityKeyOpt.get();
            Section overrideSection = overridesSection.getSection(rawKey);
            if (overrideSection == null) {
                continue;
            }
            Double difficultyMul = overrideSection.contains("difficulty-multiplier")
                    ? overrideSection.getDouble("difficulty-multiplier") : null;
            Double rewardMul = overrideSection.contains("reward-multiplier")
                    ? overrideSection.getDouble("reward-multiplier") : null;
            overrides.put(rarityKey, new RarityOverride(difficultyMul, rewardMul));
        }
        return overrides;
    }

    /**
     * Parses the optional {@code variables} section. Each child key is a variable name
     * (underscored by convention), and its {@code type} field determines whether it is
     * parsed as a {@link PoolVariable} or {@link RangeVariable}.
     *
     * @param section     the template section
     * @param templateKey the template's key string (for error messages)
     * @return the ordered map of variable name to parsed variable (empty if absent)
     * @throws IllegalArgumentException if a variable is missing its {@code type} or has
     *                                  an unrecognized type value
     */
    @NotNull
    private Map<String, TemplateVariable> parseVariables(@NotNull Section section,
                                                         @NotNull String templateKey) {
        Map<String, TemplateVariable> variables = new LinkedHashMap<>();
        Section variablesSection = section.getSection("variables");
        if (variablesSection == null) {
            return variables;
        }
        for (String varName : variablesSection.getRoutesAsStrings(false)) {
            Section varSection = variablesSection.getSection(varName);
            if (varSection == null) {
                continue;
            }
            String type = varSection.getString("type");
            if (type == null) {
                throw new IllegalArgumentException("Variable '" + varName + "' is missing 'type'");
            }
            TemplateVariable variable = switch (type.toUpperCase()) {
                case "POOL" -> parsePoolVariable(varName, varSection);
                case "RANGE" -> parseRangeVariable(varName, varSection);
                default -> throw new IllegalArgumentException("Unknown variable type '" + type
                        + "' for variable '" + varName + "' in template " + templateKey
                        + ". Valid types: POOL, RANGE");
            };
            variables.put(varName, variable);
        }
        return variables;
    }

    /**
     * Parses a {@code POOL}-type variable. Reads {@code min-selections},
     * {@code max-selections}, and the {@code pools} map. Each pool entry contains a
     * {@code difficulty} scalar, per-rarity {@code weight} map, and a {@code values} list.
     *
     * @param name    the variable name
     * @param section the variable's YAML section
     * @return the parsed pool variable
     * @throws IllegalArgumentException if the {@code pools} subsection is missing
     */
    @NotNull
    private PoolVariable parsePoolVariable(@NotNull String name, @NotNull Section section) {
        int minSelections = section.getInt("min-selections", 1);
        int maxSelections = section.getInt("max-selections", 1);

        Section poolsSection = section.getSection("pools");
        if (poolsSection == null) {
            throw new IllegalArgumentException("Pool variable '" + name + "' is missing 'pools'");
        }

        List<Pool> pools = new ArrayList<>();
        for (String poolName : poolsSection.getRoutesAsStrings(false)) {
            Section poolSection = poolsSection.getSection(poolName);
            if (poolSection == null) {
                continue;
            }
            double difficulty = poolSection.getDouble("difficulty", 1.0);

            Map<NamespacedKey, Integer> weights = new HashMap<>();
            Section weightSection = poolSection.getSection("weight");
            if (weightSection != null) {
                for (String rarityRaw : weightSection.getRoutesAsStrings(false)) {
                    parseNamespacedKey(rarityRaw)
                            .ifPresent(k -> weights.put(k, weightSection.getInt(rarityRaw, 0)));
                }
            }

            List<String> values = poolSection.getStringList("values");
            pools.add(new Pool(poolName, difficulty, weights, values));
        }

        return new PoolVariable(name, minSelections, maxSelections, pools);
    }

    /**
     * Parses a {@code RANGE}-type variable. Reads {@code base.min} and {@code base.max}
     * from the variable's section. The {@link RangeVariable} constructor enforces that
     * both values are positive.
     *
     * @param name    the variable name
     * @param section the variable's YAML section
     * @return the parsed range variable
     * @throws IllegalArgumentException if the {@code base} section is missing or
     *                                  min/max constraints are violated
     */
    @NotNull
    private RangeVariable parseRangeVariable(@NotNull String name, @NotNull Section section) {
        Section baseSection = section.getSection("base");
        if (baseSection == null) {
            throw new IllegalArgumentException("Range variable '" + name + "' is missing 'base' section");
        }
        double min = baseSection.getDouble("min");
        double max = baseSection.getDouble("max");
        return new RangeVariable(name, min, max);
    }

    /**
     * Parses the {@code phases} section of a template. Each phase has a
     * {@code completion-mode} (defaulting to {@code ALL}) and a {@code stages} subsection.
     *
     * @param section     the template section
     * @param templateKey the template's key string (for error messages)
     * @return the ordered list of phase definitions (empty if the section is absent)
     * @throws IllegalArgumentException if a phase has no stages or an invalid
     *                                  completion mode
     */
    @NotNull
    private List<TemplatePhaseDefinition> parsePhases(@NotNull Section section,
                                                      @NotNull String templateKey) {
        Section phasesSection = section.getSection("phases");
        if (phasesSection == null) {
            return List.of();
        }

        List<TemplatePhaseDefinition> phases = new ArrayList<>();
        for (String phaseLabel : phasesSection.getRoutesAsStrings(false)) {
            Section phaseSection = phasesSection.getSection(phaseLabel);
            if (phaseSection == null) {
                continue;
            }

            String modeStr = phaseSection.getString("completion-mode", "ALL");
            PhaseCompletionMode mode;
            try {
                mode = PhaseCompletionMode.valueOf(modeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid completion-mode '" + modeStr
                        + "' in phase '" + phaseLabel + "' of template " + templateKey);
            }

            Section stagesSection = phaseSection.getSection("stages");
            if (stagesSection == null) {
                throw new IllegalArgumentException("Phase '" + phaseLabel + "' in template "
                        + templateKey + " must have at least one stage");
            }

            TemplateCondition phaseCondition = ConditionParser.parseConditionBlock(phaseSection);

            List<TemplateStageDefinition> stages = new ArrayList<>();
            for (String stageLabel : stagesSection.getRoutesAsStrings(false)) {
                Section stageSection = stagesSection.getSection(stageLabel);
                if (stageSection == null) {
                    continue;
                }
                stages.add(parseStageDefinition(stageSection, templateKey, phaseLabel, stageLabel));
            }

            if (stages.isEmpty()) {
                throw new IllegalArgumentException("Phase '" + phaseLabel + "' in template "
                        + templateKey + " must have at least one stage");
            }

            phases.add(new TemplatePhaseDefinition(mode, stages, phaseCondition));
        }
        return phases;
    }

    /**
     * Parses a single stage definition from within a phase. Each stage contains an
     * {@code objectives} subsection; every objective must have a {@code type} and
     * {@code required-progress} (number or expression string).
     *
     * @param stageSection the BoostedYaml section for this stage
     * @param templateKey  the template's key string (for error messages)
     * @param phaseLabel   the parent phase's YAML label (for error messages)
     * @param stageLabel   the stage's YAML label (for error messages)
     * @return the parsed stage definition
     * @throws IllegalArgumentException if objectives are missing or invalid
     */
    @NotNull
    private TemplateStageDefinition parseStageDefinition(@NotNull Section stageSection,
                                                         @NotNull String templateKey,
                                                         @NotNull String phaseLabel,
                                                         @NotNull String stageLabel) {
        TemplateCondition stageCondition = ConditionParser.parseConditionBlock(stageSection);
        ObjectiveSelectionConfig selectionConfig = parseObjectiveSelectionConfig(stageSection);

        Section objectivesSection = stageSection.getSection("objectives");
        if (objectivesSection == null) {
            throw new IllegalArgumentException("Stage '" + stageLabel + "' in phase '" + phaseLabel
                    + "' of template " + templateKey + " must have at least one objective");
        }

        List<TemplateObjectiveDefinition> objectives = new ArrayList<>();
        for (String objLabel : objectivesSection.getRoutesAsStrings(false)) {
            Section objSection = objectivesSection.getSection(objLabel);
            if (objSection == null) {
                continue;
            }

            String typeStr = objSection.getString("type");
            if (typeStr == null || typeStr.isBlank()) {
                throw new IllegalArgumentException("Objective '" + objLabel + "' in stage '" + stageLabel
                        + "' of template " + templateKey + " is missing 'type'");
            }
            NamespacedKey typeKey = parseNamespacedKey(typeStr)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid objective type '" + typeStr + "' in template " + templateKey));

            Object progressObj = objSection.get("required-progress");
            String requiredProgressExpression;
            if (progressObj instanceof Number n) {
                requiredProgressExpression = String.valueOf(n);
            } else if (progressObj instanceof String s && !s.isBlank()) {
                requiredProgressExpression = s;
            } else {
                throw new IllegalArgumentException("Objective '" + objLabel + "' in template "
                        + templateKey + " is missing 'required-progress'");
            }

            TemplateCondition objCondition = ConditionParser.parseConditionBlock(objSection);
            int weight = objSection.getInt("weight", 1);

            Map<String, Object> config = parseConfigMap(objSection.getSection("config"));
            objectives.add(new TemplateObjectiveDefinition(
                    typeKey, requiredProgressExpression, config, objCondition, weight));
        }

        if (objectives.isEmpty()) {
            throw new IllegalArgumentException("Stage '" + stageLabel + "' in phase '" + phaseLabel
                    + "' of template " + templateKey + " must have at least one objective");
        }

        return new TemplateStageDefinition(objectives, stageCondition, selectionConfig);
    }

    /**
     * Parses an optional {@code objective-selection} block from a stage section.
     *
     * @param stageSection the stage's YAML section
     * @return the parsed selection config, or null if absent
     */
    @Nullable
    private ObjectiveSelectionConfig parseObjectiveSelectionConfig(@NotNull Section stageSection) {
        Section selSection = stageSection.getSection("objective-selection");
        if (selSection == null) {
            return null;
        }
        String modeStr = selSection.getString("mode", "ALL");
        ObjectiveSelectionConfig.ObjectiveSelectionMode mode;
        try {
            mode = ObjectiveSelectionConfig.ObjectiveSelectionMode.valueOf(modeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid objective-selection mode: " + modeStr);
        }
        int minCount = selSection.getInt("min-count", 1);
        int maxCount = selSection.getInt("max-count", minCount);
        return new ObjectiveSelectionConfig(mode, minCount, maxCount);
    }

    /**
     * Parses the optional {@code rewards} section of a template. Each reward has a
     * {@code type} key and an arbitrary config map whose values may contain expression
     * strings referencing template variables.
     *
     * @param section the template section
     * @return the ordered list of reward definitions (empty if the section is absent)
     */
    @NotNull
    private List<TemplateRewardDefinition> parseRewardDefinitions(@NotNull Section section) {
        Section rewardsSection = section.getSection("rewards");
        if (rewardsSection == null) {
            return List.of();
        }

        List<TemplateRewardDefinition> rewards = new ArrayList<>();
        for (String rewardLabel : rewardsSection.getRoutesAsStrings(false)) {
            Section rewardSection = rewardsSection.getSection(rewardLabel);
            if (rewardSection == null) {
                continue;
            }

            String typeStr = rewardSection.getString("type");
            if (typeStr == null || typeStr.isBlank()) {
                continue;
            }
            Optional<NamespacedKey> typeKeyOpt = parseNamespacedKey(typeStr);
            if (typeKeyOpt.isEmpty()) {
                continue;
            }
            NamespacedKey typeKey = typeKeyOpt.get();

            Map<String, Object> config = new LinkedHashMap<>();
            for (String key : rewardSection.getRoutesAsStrings(false)) {
                if ("type".equals(key)) {
                    continue;
                }
                config.put(key, rewardSection.get(key));
            }

            rewards.add(new TemplateRewardDefinition(typeKey, config));
        }
        return rewards;
    }

    /**
     * Converts a BoostedYaml {@link Section} into a flat {@code Map<String, Object>}.
     * Used for objective {@code config} blocks where values may be strings (variable
     * references or literals), numbers, or lists.
     *
     * @param section the config section, or {@code null} if absent
     * @return an ordered map of config key to raw value (empty if section is null)
     */
    @NotNull
    private Map<String, Object> parseConfigMap(@Nullable Section section) {
        if (section == null) {
            return Map.of();
        }
        Map<String, Object> config = new LinkedHashMap<>();
        for (String key : section.getRoutesAsStrings(false)) {
            config.put(key, section.get(key));
        }
        return config;
    }

    /**
     * Validates all expression strings in objective {@code required-progress} fields and
     * reward config values by trial-parsing them through the actual {@link Parser} with
     * all declared variables (plus the built-in {@code difficulty}) set to dummy values.
     * <p>
     * This single pass catches both undeclared variable references and syntax errors
     * (e.g. trailing operators, mismatched parentheses). The {@link Parser} already knows
     * its own built-in functions, so no separate function-name allowlist is needed.
     *
     * @param variables   the declared template variables
     * @param phases      the template's phase definitions
     * @param rewards     the template's reward definitions
     * @param templateKey the template's key string (for error messages)
     * @throws IllegalArgumentException if any expression fails to parse
     */
    private void validateExpressions(@NotNull Map<String, TemplateVariable> variables,
                                     @NotNull List<TemplatePhaseDefinition> phases,
                                     @NotNull List<TemplateRewardDefinition> rewards,
                                     @NotNull String templateKey) {
        Set<String> declaredNames = new HashSet<>(variables.keySet());
        declaredNames.add("difficulty");

        Map<String, String> expressionsToValidate = new LinkedHashMap<>();

        for (TemplatePhaseDefinition phase : phases) {
            for (TemplateStageDefinition stage : phase.stages()) {
                for (TemplateObjectiveDefinition obj : stage.objectives()) {
                    expressionsToValidate.put(obj.requiredProgressExpression(), "required-progress");
                }
            }
        }

        for (TemplateRewardDefinition reward : rewards) {
            for (Map.Entry<String, Object> entry : reward.config().entrySet()) {
                if (entry.getValue() instanceof String s && !s.isEmpty() && referencesAnyVariable(s, declaredNames)) {
                    expressionsToValidate.put(s, "reward." + entry.getKey());
                }
            }
        }

        for (Map.Entry<String, String> entry : expressionsToValidate.entrySet()) {
            String expression = entry.getKey();
            String fieldPath = entry.getValue();
            try {
                Parser parser = new Parser(expression);
                for (String varName : variables.keySet()) {
                    parser.setVariable(varName, 1.0);
                }
                parser.setVariable("difficulty", 1.0);
                parser.getValue();

                for (String parsedVar : parser.getParsedVariables()) {
                    if (!declaredNames.contains(parsedVar)) {
                        throw new IllegalArgumentException("references undeclared variable '" + parsedVar + "'");
                    }
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Template " + templateKey + " field '" + fieldPath
                        + "' has invalid expression: '" + expression + "' -- " + e.getMessage());
            }
        }
    }

    /**
     * Checks whether a string value references any declared template variable name.
     * Used to distinguish plain literal config values (e.g. {@code "SWORDS"}) from
     * expression strings that should be validated (e.g. {@code "base_count * 5"}).
     */
    private boolean referencesAnyVariable(@NotNull String value, @NotNull Set<String> declaredNames) {
        for (String varName : declaredNames) {
            if (value.contains(varName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses a {@link NamespacedKey} from a string. If the string contains a colon,
     * it is parsed as {@code namespace:key}. Otherwise, the McRPG namespace is used
     * with the input lowercased and hyphens replaced by underscores.
     *
     * @param input the string to parse
     * @return an {@link Optional} containing the parsed key, or empty if the input
     *         is null, empty, or invalid
     */
    @NotNull
    private Optional<NamespacedKey> parseNamespacedKey(@Nullable String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }
        if (input.contains(":")) {
            return Optional.ofNullable(NamespacedKey.fromString(input.toLowerCase()));
        }
        return Optional.of(new NamespacedKey(McRPGMethods.getMcRPGNamespace(), input.toLowerCase().replace('-', '_')));
    }
}
