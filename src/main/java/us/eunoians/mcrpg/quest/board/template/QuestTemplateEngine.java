package us.eunoians.mcrpg.quest.board.template;

import com.diamonddagger590.mccore.parser.Parser;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.template.condition.ConditionContext;
import us.eunoians.mcrpg.quest.board.template.variable.PoolVariable;
import us.eunoians.mcrpg.quest.board.template.variable.RangeVariable;
import us.eunoians.mcrpg.quest.board.template.variable.ResolvedPool;
import us.eunoians.mcrpg.quest.board.template.variable.TemplateVariable;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestRepeatMode;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Stateless engine that transforms a {@link QuestTemplate} and a rolled rarity into a concrete
 * {@link QuestDefinition}. All state is passed as arguments; registries are injected at construction.
 * <p>
 * The generation pipeline:
 * <ol>
 *     <li>Validate the rarity is supported by the template</li>
 *     <li>Resolve all template variables into a {@link ResolvedVariableContext}</li>
 *     <li>Build a fully materialized {@link QuestDefinition} from the template structure</li>
 *     <li>Serialize the definition to JSON via {@link GeneratedQuestDefinitionSerializer}</li>
 *     <li>Return a {@link GeneratedQuestResult} containing the definition, template key, and JSON</li>
 * </ol>
 */
public final class QuestTemplateEngine {

    private static final Logger LOGGER = Logger.getLogger(QuestTemplateEngine.class.getName());

    private final QuestRarityRegistry rarityRegistry;
    private final QuestObjectiveTypeRegistry objectiveTypeRegistry;
    private final QuestRewardTypeRegistry rewardTypeRegistry;

    public QuestTemplateEngine(@NotNull QuestRarityRegistry rarityRegistry,
                               @NotNull QuestObjectiveTypeRegistry objectiveTypeRegistry,
                               @NotNull QuestRewardTypeRegistry rewardTypeRegistry) {
        this.rarityRegistry = rarityRegistry;
        this.objectiveTypeRegistry = objectiveTypeRegistry;
        this.rewardTypeRegistry = rewardTypeRegistry;
    }

    /**
     * Generates a concrete quest definition from a template and a rolled rarity.
     * <p>
     * The full pipeline: validate rarity, resolve variables, build a materialized
     * {@link QuestDefinition}, serialize it to a JSON snapshot, and bundle everything
     * into a {@link GeneratedQuestResult}.
     *
     * @param template  the template to generate from
     * @param rarityKey the rolled rarity (must be in the template's supported rarities)
     * @param random    the random source (seeded for deterministic generation)
     * @return the generated result containing the definition, template key, and serialized JSON
     * @throws IllegalArgumentException if the template does not support the rolled rarity
     * @throws QuestGenerationException if an objective type or reward type is not registered,
     *                                  or if expression evaluation or config creation fails
     */
    @NotNull
    public GeneratedQuestResult generate(@NotNull QuestTemplate template,
                                         @NotNull NamespacedKey rarityKey,
                                         @NotNull Random random) {
        template.validateRaritySupported(rarityKey);

        ResolvedVariableContext variableContext = resolveVariables(template, rarityKey, random);

        ConditionContext conditionContext = ConditionContext.forTemplateGeneration(
                rarityKey, rarityRegistry, random, variableContext);

        List<TemplatePhaseDefinition> filteredPhases = filterPhases(
                template.getPhases(), conditionContext, random);

        if (filteredPhases.isEmpty()) {
            throw new QuestGenerationException("Template '" + template.getKey()
                    + "' generated zero phases after condition evaluation for rarity " + rarityKey,
                    template.getKey(), rarityKey, null);
        }

        Map<NamespacedKey, Map<String, Object>> objectiveConfigs = new LinkedHashMap<>();
        QuestDefinition definition = buildDefinition(
                template, rarityKey, variableContext, filteredPhases, random, objectiveConfigs);
        String json = GeneratedQuestDefinitionSerializer.serialize(
                definition, template.getKey(), rarityKey, variableContext, objectiveConfigs);
        return new GeneratedQuestResult(definition, template.getKey(), json);
    }

    /**
     * Resolves all template variables into concrete values using a two-pass strategy:
     * pool variables first (to compute pool difficulty), then range variables (scaled
     * by combined difficulty). The built-in {@code "difficulty"} variable is injected
     * after both passes.
     *
     * @param template  the template whose variables should be resolved
     * @param rarityKey the rolled rarity, used for pool weight lookups and difficulty/reward
     *                  multiplier retrieval
     * @param random    the seeded random source for pool selection and range generation
     * @return a snapshot of all resolved variable values and difficulty scalars
     */
    @NotNull
    ResolvedVariableContext resolveVariables(@NotNull QuestTemplate template,
                                            @NotNull NamespacedKey rarityKey,
                                            @NotNull Random random) {
        Map<String, Object> resolvedValues = new LinkedHashMap<>();
        double totalPoolDifficulty = 0.0;
        int poolCount = 0;

        for (TemplateVariable variable : template.getVariables().values()) {
            if (variable instanceof PoolVariable pool) {
                ResolvedPool resolved = pool.resolve(rarityKey, random);
                resolvedValues.put(pool.getName(), resolved.mergedValues());
                totalPoolDifficulty += resolved.averageDifficulty();
                poolCount++;
            }
        }

        double poolDifficulty = poolCount > 0 ? totalPoolDifficulty / poolCount : 1.0;
        double rarityDifficulty = template.getEffectiveDifficultyMultiplier(rarityKey, rarityRegistry);
        double difficulty = poolDifficulty * rarityDifficulty;

        for (TemplateVariable variable : template.getVariables().values()) {
            if (variable instanceof RangeVariable range) {
                double resolved = range.resolve(difficulty, random);
                resolvedValues.put(range.getName(), (long) Math.round(resolved));
            }
        }

        resolvedValues.put("difficulty", difficulty);

        return new ResolvedVariableContext(
                Map.copyOf(resolvedValues), poolDifficulty, rarityDifficulty, difficulty);
    }

    /**
     * Builds a concrete {@link QuestDefinition} from the template structure and resolved
     * variable context. Generates synthetic keys for the quest, stages, and objectives
     * following the pattern {@code mcrpg:gen_<template_suffix>_<8_char_hex>}.
     * <p>
     * Populates {@code objectiveConfigs} as a side-effect so the serializer can embed
     * resolved config maps in the JSON without requiring objective types to implement
     * a serialization method.
     *
     * @param template         the source template
     * @param rarityKey        the rolled rarity, used for reward multiplier lookup
     * @param context          the resolved variable context
     * @param random           the random source (consumed for hex suffix generation)
     * @param objectiveConfigs mutable map populated with each objective's resolved config
     *                         keyed by the objective's generated {@link NamespacedKey}
     * @return the fully materialized quest definition
     * @throws QuestGenerationException if an objective or reward type is not registered
     */
    @NotNull
    QuestDefinition buildDefinition(@NotNull QuestTemplate template,
                                    @NotNull NamespacedKey rarityKey,
                                    @NotNull ResolvedVariableContext context,
                                    @NotNull List<TemplatePhaseDefinition> filteredPhases,
                                    @NotNull Random random,
                                    @NotNull Map<NamespacedKey, Map<String, Object>> objectiveConfigs) {
        String hexSuffix = String.format("%08x", random.nextInt());
        String questKeyStr = "gen_" + template.getKey().getKey() + "_" + hexSuffix;
        NamespacedKey questKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), questKeyStr);

        List<QuestPhaseDefinition> phases = buildPhases(template, rarityKey, questKey, filteredPhases, context, objectiveConfigs);
        List<QuestRewardType> rewards = buildRewards(template, rarityKey, context);

        return new QuestDefinition(
                questKey,
                template.getScopeProviderKey(),
                null,
                phases,
                rewards,
                QuestRepeatMode.ONCE,
                null,
                -1,
                null,
                null,
                template.getRewardDistribution().orElse(null)
        );
    }

    /**
     * Builds phase definitions by iterating over template phases, creating stage and
     * objective definitions with synthesized keys and resolved config maps.
     *
     * @param template         the source template (provides phase structure)
     * @param rarityKey        the rolled rarity (for error context)
     * @param questKey         the generated quest key, used as prefix for stage/objective keys
     * @param context          the resolved variable context for expression evaluation
     * @param objectiveConfigs mutable map populated with resolved objective configs
     * @return the ordered list of materialized phase definitions
     */
    @NotNull
    private List<QuestPhaseDefinition> buildPhases(@NotNull QuestTemplate template,
                                                   @NotNull NamespacedKey rarityKey,
                                                   @NotNull NamespacedKey questKey,
                                                   @NotNull List<TemplatePhaseDefinition> filteredPhases,
                                                   @NotNull ResolvedVariableContext context,
                                                   @NotNull Map<NamespacedKey, Map<String, Object>> objectiveConfigs) {
        List<QuestPhaseDefinition> phases = new ArrayList<>();

        for (int phaseIdx = 0; phaseIdx < filteredPhases.size(); phaseIdx++) {
            TemplatePhaseDefinition templatePhase = filteredPhases.get(phaseIdx);
            List<QuestStageDefinition> stages = new ArrayList<>();

            for (int stageIdx = 0; stageIdx < templatePhase.stages().size(); stageIdx++) {
                TemplateStageDefinition templateStage = templatePhase.stages().get(stageIdx);
                NamespacedKey stageKey = new NamespacedKey(questKey.getNamespace(),
                        questKey.getKey() + "_p" + phaseIdx + "s" + stageIdx);

                List<QuestObjectiveDefinition> objectives = buildObjectives(
                        template, rarityKey, templateStage, questKey, phaseIdx, stageIdx, context, objectiveConfigs);
                stages.add(new QuestStageDefinition(stageKey, objectives, List.of(), null));
            }

            phases.add(new QuestPhaseDefinition(phaseIdx, templatePhase.completionMode(), stages, null));
        }

        return phases;
    }

    /**
     * Builds objective definitions for a single stage, resolving required progress
     * expressions and substituting variable references in config maps. Each objective's
     * resolved config is stored in {@code objectiveConfigs} for later serialization.
     *
     * @param template         the source template (for error context)
     * @param rarityKey        the rolled rarity (for error context)
     * @param templateStage    the template stage containing raw objective definitions
     * @param questKey         the generated quest key, used as prefix for objective keys
     * @param phaseIdx         the zero-based phase index (for key generation)
     * @param stageIdx         the zero-based stage index (for key generation)
     * @param context          the resolved variable context for expression evaluation
     * @param objectiveConfigs mutable map populated with resolved objective configs
     * @return the list of materialized objective definitions
     * @throws QuestGenerationException if an objective type is not registered or config creation fails
     */
    @NotNull
    private List<QuestObjectiveDefinition> buildObjectives(@NotNull QuestTemplate template,
                                                           @NotNull NamespacedKey rarityKey,
                                                           @NotNull TemplateStageDefinition templateStage,
                                                           @NotNull NamespacedKey questKey,
                                                           int phaseIdx,
                                                           int stageIdx,
                                                           @NotNull ResolvedVariableContext context,
                                                           @NotNull Map<NamespacedKey, Map<String, Object>> objectiveConfigs) {
        List<QuestObjectiveDefinition> objectives = new ArrayList<>();

        for (int objIdx = 0; objIdx < templateStage.objectives().size(); objIdx++) {
            TemplateObjectiveDefinition templateObj = templateStage.objectives().get(objIdx);
            NamespacedKey objectiveKey = new NamespacedKey(questKey.getNamespace(),
                    questKey.getKey() + "_p" + phaseIdx + "s" + stageIdx + "o" + objIdx);

            long requiredProgress = evaluateExpression(templateObj.requiredProgressExpression(), context);
            if (requiredProgress <= 0) {
                requiredProgress = 1;
            }

            Map<String, Object> resolvedConfig = resolveObjectiveConfig(templateObj.config(), context);

            QuestObjectiveType baseType = objectiveTypeRegistry.get(templateObj.typeKey())
                    .orElseThrow(() -> new QuestGenerationException(
                            "Unknown objective type '" + templateObj.typeKey()
                                    + "' referenced by template " + template.getKey()
                                    + ". Is the type registered in QuestObjectiveTypeRegistry?",
                            template.getKey(), rarityKey, templateObj.typeKey()));
            QuestObjectiveType configuredType = createConfiguredObjectiveType(
                    baseType, resolvedConfig, template.getKey(), rarityKey);
            objectiveConfigs.put(objectiveKey, Collections.unmodifiableMap(resolvedConfig));

            objectives.add(new QuestObjectiveDefinition(objectiveKey, configuredType, requiredProgress, List.of(), null));
        }

        return objectives;
    }

    /**
     * Builds reward types by resolving expression strings in config values and applying
     * the rarity reward multiplier to {@code "amount"} fields. Each reward's base type
     * is looked up from the registry and configured via
     * {@link QuestRewardType#fromSerializedConfig}.
     *
     * @param template  the source template (for error context and reward multiplier lookup)
     * @param rarityKey the rolled rarity, used for reward multiplier lookup
     * @param context   the resolved variable context for expression evaluation
     * @return the list of configured reward type instances
     * @throws QuestGenerationException if a reward type is not registered
     */
    @NotNull
    private List<QuestRewardType> buildRewards(@NotNull QuestTemplate template,
                                               @NotNull NamespacedKey rarityKey,
                                               @NotNull ResolvedVariableContext context) {
        double rewardMultiplier = template.getEffectiveRewardMultiplier(rarityKey, rarityRegistry);
        List<QuestRewardType> rewards = new ArrayList<>();

        for (TemplateRewardDefinition templateReward : template.getRewards()) {
            QuestRewardType baseType = rewardTypeRegistry.get(templateReward.typeKey())
                    .orElseThrow(() -> new QuestGenerationException(
                            "Unknown reward type '" + templateReward.typeKey()
                                    + "' referenced by template " + template.getKey()
                                    + ". Is the type registered in QuestRewardTypeRegistry?",
                            template.getKey(), rarityKey, templateReward.typeKey()));

            Map<String, Object> resolvedConfig = resolveRewardConfig(
                    templateReward.config(), context, rewardMultiplier);
            rewards.add(baseType.fromSerializedConfig(resolvedConfig));
        }

        return rewards;
    }

    /**
     * Evaluates a mathematical expression using {@link Parser} with all resolved
     * numeric variables from the context in scope. Non-numeric variables (e.g. pool
     * lists) are silently skipped.
     *
     * @param expression the expression string to evaluate (e.g. {@code "block_count * 5"})
     * @param context    the resolved variable context providing variable values
     * @return the evaluated result, rounded to the nearest {@code long}
     */
    private long evaluateExpression(@NotNull String expression,
                                    @NotNull ResolvedVariableContext context) {
        Parser parser = new Parser(expression);
        for (Map.Entry<String, Object> entry : context.resolvedValues().entrySet()) {
            if (entry.getValue() instanceof Number n) {
                parser.setVariable(entry.getKey(), n.doubleValue());
            }
        }
        return Math.round(parser.getValue());
    }

    /**
     * Resolves an objective config map by substituting string values that exactly
     * match a declared variable name with the resolved value (e.g. {@code "target_blocks"}
     * becomes {@code List<String>}). Non-matching strings are kept as literal values
     * (e.g. material names like {@code "MINING"}).
     *
     * @param templateConfig the raw config map from the template objective definition
     * @param context        the resolved variable context providing variable values
     * @return a new map with variable references substituted
     */
    @NotNull
    private Map<String, Object> resolveObjectiveConfig(@NotNull Map<String, Object> templateConfig,
                                                       @NotNull ResolvedVariableContext context) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : templateConfig.entrySet()) {
            if (entry.getValue() instanceof String s && context.resolvedValues().containsKey(s)) {
                resolved.put(entry.getKey(), context.resolvedValues().get(s));
            } else {
                resolved.put(entry.getKey(), entry.getValue());
            }
        }
        return resolved;
    }

    /**
     * Resolves a reward config map by substituting variable references and evaluating
     * expression strings through {@link Parser}. The rarity reward multiplier is applied
     * to any entry with key {@code "amount"} that resolves to a numeric value, whether
     * from a direct variable reference, an evaluated expression, or a raw numeric literal.
     *
     * @param templateConfig   the raw config map from the template reward definition
     * @param context          the resolved variable context providing variable values
     * @param rewardMultiplier the rarity-based reward scaling factor
     * @return a new map with expressions evaluated and the reward multiplier applied
     */
    @NotNull
    private Map<String, Object> resolveRewardConfig(@NotNull Map<String, Object> templateConfig,
                                                    @NotNull ResolvedVariableContext context,
                                                    double rewardMultiplier) {
        Set<String> numericVarNames = context.resolvedValues().entrySet().stream()
                .filter(e -> e.getValue() instanceof Number)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Map<String, Object> resolved = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : templateConfig.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String s) {
                if (context.resolvedValues().containsKey(s)) {
                    Object resolvedValue = context.resolvedValues().get(s);
                    resolved.put(key, applyRewardMultiplier(key, resolvedValue, rewardMultiplier));
                } else if (containsAnyVariable(s, numericVarNames)) {
                    long evaluated = evaluateExpression(s, context);
                    resolved.put(key, applyRewardMultiplier(key, evaluated, rewardMultiplier));
                } else {
                    resolved.put(key, s);
                }
            } else if (value instanceof Number n && "amount".equals(key)) {
                resolved.put(key, Math.round(n.doubleValue() * rewardMultiplier));
            } else {
                resolved.put(key, value);
            }
        }
        return resolved;
    }

    /**
     * Applies the rarity reward multiplier if the config key is {@code "amount"} and
     * the value is numeric. Non-amount keys or non-numeric values pass through unchanged.
     *
     * @param key        the config entry key
     * @param value      the resolved config value
     * @param multiplier the rarity reward multiplier
     * @return the value with the multiplier applied (if applicable), or the original value
     */
    @NotNull
    private Object applyRewardMultiplier(@NotNull String key, @NotNull Object value, double multiplier) {
        if ("amount".equals(key) && value instanceof Number n) {
            return Math.round(n.doubleValue() * multiplier);
        }
        return value;
    }

    /**
     * Checks whether the expression string contains at least one of the given
     * variable names as a substring, indicating it is likely an expression rather
     * than a plain literal.
     *
     * @param expression the string to check
     * @param varNames   the set of known numeric variable names
     * @return {@code true} if the expression references at least one variable
     */
    private boolean containsAnyVariable(@NotNull String expression, @NotNull Set<String> varNames) {
        for (String varName : varNames) {
            if (expression.contains(varName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a configured {@link QuestObjectiveType} by populating an in-memory
     * {@link YamlDocument} with the resolved config entries and calling
     * {@link QuestObjectiveType#parseConfig}. This bridges the gap between the
     * engine's resolved config maps and the YAML Section that objective types expect.
     *
     * @param baseType    the base (unconfigured) objective type from the registry
     * @param config      the resolved config map with variable substitutions applied
     * @param templateKey the template key for error context
     * @param rarityKey   the rolled rarity key for error context
     * @return a configured copy of the objective type
     * @throws QuestGenerationException if the in-memory document cannot be created
     */
    @NotNull
    private QuestObjectiveType createConfiguredObjectiveType(@NotNull QuestObjectiveType baseType,
                                                            @NotNull Map<String, Object> config,
                                                            @NotNull NamespacedKey templateKey,
                                                            @NotNull NamespacedKey rarityKey) {
        if (config.isEmpty()) {
            return baseType;
        }
        try {
            YamlDocument doc = YamlDocument.create(new ByteArrayInputStream("{}".getBytes()));
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                doc.set(entry.getKey(), entry.getValue());
            }
            return baseType.parseConfig(doc);
        } catch (IOException e) {
            throw new QuestGenerationException(
                    "Failed to create in-memory config for objective type "
                            + baseType.getKey() + " in template " + templateKey,
                    e, templateKey, rarityKey, baseType.getKey());
        }
    }

    @NotNull
    private List<TemplatePhaseDefinition> filterPhases(
            @NotNull List<TemplatePhaseDefinition> phases,
            @NotNull ConditionContext context,
            @NotNull Random random) {

        List<TemplatePhaseDefinition> result = new ArrayList<>();
        for (TemplatePhaseDefinition phase : phases) {
            if (phase.getCondition().map(c -> c.evaluate(context)).orElse(true)) {
                List<TemplateStageDefinition> filteredStages = filterStages(
                        phase.stages(), context, random);
                if (!filteredStages.isEmpty()) {
                    result.add(phase.withStages(filteredStages));
                }
            }
        }
        return result;
    }

    @NotNull
    private List<TemplateStageDefinition> filterStages(
            @NotNull List<TemplateStageDefinition> stages,
            @NotNull ConditionContext context,
            @NotNull Random random) {

        List<TemplateStageDefinition> result = new ArrayList<>();
        for (TemplateStageDefinition stage : stages) {
            if (stage.getCondition().map(c -> c.evaluate(context)).orElse(true)) {
                List<TemplateObjectiveDefinition> filteredObjectives = filterObjectives(
                        stage.objectives(), context);

                if (stage.getObjectiveSelection().isPresent()) {
                    ObjectiveSelectionConfig selConfig = stage.getObjectiveSelection().get();
                    if (selConfig.mode() == ObjectiveSelectionConfig.ObjectiveSelectionMode.WEIGHTED_RANDOM) {
                        try {
                            filteredObjectives = WeightedObjectiveSelector.select(
                                    filteredObjectives, selConfig, random);
                        } catch (IllegalStateException e) {
                            LOGGER.warning("[QuestTemplateEngine] Stage excluded: " + e.getMessage());
                            continue;
                        }
                    }
                }

                if (!filteredObjectives.isEmpty()) {
                    result.add(stage.withObjectives(filteredObjectives));
                }
            }
        }
        return result;
    }

    @NotNull
    private List<TemplateObjectiveDefinition> filterObjectives(
            @NotNull List<TemplateObjectiveDefinition> objectives,
            @NotNull ConditionContext context) {

        return objectives.stream()
                .filter(obj -> obj.getCondition().map(c -> c.evaluate(context)).orElse(true))
                .toList();
    }

}
