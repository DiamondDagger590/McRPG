package us.eunoians.mcrpg.quest.board.template;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.OnStartMessage;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestRepeatMode;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.board.distribution.DistributionRewardEntry;
import us.eunoians.mcrpg.quest.board.distribution.DistributionTierConfig;
import us.eunoians.mcrpg.quest.board.distribution.PotBehavior;
import us.eunoians.mcrpg.quest.board.distribution.RemainderStrategy;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardSplitMode;
import us.eunoians.mcrpg.quest.board.template.condition.ConditionParser;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;
import us.eunoians.mcrpg.quest.board.template.condition.QuestRewardEntry;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateConditionRegistry;
import us.eunoians.mcrpg.quest.board.template.condition.RewardFallback;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Serializes and deserializes template-generated {@link QuestDefinition}s to/from JSON.
 * <p>
 * The JSON snapshot captures the fully resolved definition at the moment of generation,
 * making the offering self-contained. On deserialization, objective types and reward types
 * are reconstructed from their respective registries.
 * <p>
 * Uses Gson (available transitively via Paper API).
 *
 * @see QuestTemplateEngine
 * @see GeneratedQuestResult
 */
public class GeneratedQuestDefinitionCodec {

    private static final Gson GSON = new GsonBuilder().create();

    private final QuestObjectiveTypeRegistry objectiveTypeRegistry;
    private final QuestRewardTypeRegistry rewardTypeRegistry;
    private final TemplateConditionRegistry conditionTypeRegistry;

    /**
     * Creates a new codec with the registries needed for deserialization.
     *
     * @param objectiveTypeRegistry registry for looking up objective types by key
     * @param rewardTypeRegistry    registry for looking up reward types by key
     * @param conditionTypeRegistry registry for looking up condition types by key
     */
    public GeneratedQuestDefinitionCodec(@NotNull QuestObjectiveTypeRegistry objectiveTypeRegistry,
                                         @NotNull QuestRewardTypeRegistry rewardTypeRegistry,
                                         @NotNull TemplateConditionRegistry conditionTypeRegistry) {
        this.objectiveTypeRegistry = objectiveTypeRegistry;
        this.rewardTypeRegistry = rewardTypeRegistry;
        this.conditionTypeRegistry = conditionTypeRegistry;
    }

    /**
     * Serializes a generated quest definition to JSON. The JSON contains the quest key,
     * template key, rarity key, scope, resolved variable values, phase/stage/objective tree
     * with concrete required-progress values, and reward configs with evaluated amounts.
     *
     * @param definition       the fully materialized quest definition
     * @param templateKey      the template that produced this definition
     * @param rarityKey        the rolled rarity
     * @param context          the resolved variable context containing all computed values
     * @param objectiveConfigs resolved config maps keyed by objective key, used to embed
     *                         objective type configuration in the JSON without requiring
     *                         objective types to implement a serialization method
     * @return the JSON string suitable for storage in the {@code generated_definition}
     *         database column
     */
    @NotNull
    public String serialize(@NotNull QuestDefinition definition,
                            @NotNull NamespacedKey templateKey,
                            @NotNull NamespacedKey rarityKey,
                            @NotNull ResolvedVariableContext context,
                            @NotNull Map<NamespacedKey, Map<String, Object>> objectiveConfigs) {
        JsonObject root = new JsonObject();
        root.addProperty("quest_key", definition.getQuestKey().toString());
        root.addProperty("template_key", templateKey.toString());
        root.addProperty("rarity_key", rarityKey.toString());
        root.addProperty("scope", definition.getScopeType().toString());

        JsonObject variables = new JsonObject();
        for (Map.Entry<String, Object> entry : context.resolvedValues().entrySet()) {
            variables.add(entry.getKey(), toJsonElement(entry.getValue()));
        }
        root.add("variables", variables);

        JsonArray phasesArray = new JsonArray();
        for (QuestPhaseDefinition phase : definition.getPhases()) {
            phasesArray.add(serializePhase(phase, objectiveConfigs));
        }
        root.add("phases", phasesArray);

        JsonArray rewardsArray = new JsonArray();
        for (QuestRewardEntry entry : definition.getRewardEntries()) {
            JsonObject rewardObj = new JsonObject();
            rewardObj.addProperty("type", entry.reward().getKey().toString());
            rewardObj.add("config", GSON.toJsonTree(entry.reward().serializeConfig()));
            if (entry.fallback() != null) {
                rewardObj.add("fallback", serializeRewardFallback(entry.fallback()));
            }
            rewardsArray.add(rewardObj);
        }
        root.add("rewards", rewardsArray);

        definition.getRewardDistribution()
                .ifPresent(dist -> root.add("reward_distribution", serializeDistribution(dist)));

        if (!definition.getInlineDisplay().isEmpty()) {
            JsonObject displayObj = new JsonObject();
            for (Map.Entry<String, String> entry : definition.getInlineDisplay().entrySet()) {
                displayObj.addProperty(entry.getKey(), entry.getValue());
            }
            root.add("inline_display", displayObj);
        }

        if (!definition.getOnStartMessages().isEmpty()) {
            JsonArray onStartArray = new JsonArray();
            for (OnStartMessage message : definition.getOnStartMessages()) {
                JsonObject messageObj = new JsonObject();
                message.localeKey().ifPresent(key -> messageObj.addProperty("locale_key", key));
                if (!message.inlineMessages().isEmpty()) {
                    JsonArray inlineArray = new JsonArray();
                    for (String line : message.inlineMessages()) {
                        inlineArray.add(line);
                    }
                    messageObj.add("inline_messages", inlineArray);
                }
                onStartArray.add(messageObj);
            }
            root.add("on_start_messages", onStartArray);
        }

        return GSON.toJson(root);
    }

    /**
     * Deserializes a JSON string back into a {@link QuestDefinition}, resolving objective
     * types, reward types, and condition types from the injected registries.
     *
     * @param json the JSON string produced by {@link #serialize}
     * @return the reconstructed quest definition
     * @throws QuestDeserializationException if the JSON is malformed, or references a
     *                                       type that is not registered
     */
    @NotNull
    public QuestDefinition deserialize(@NotNull String json) {
        String questKeyString = null;
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            questKeyString = root.get("quest_key").getAsString();
            NamespacedKey questKey = NamespacedKey.fromString(questKeyString);
            if (questKey == null) {
                throw new QuestDeserializationException(
                        "Invalid quest key: '" + questKeyString + "'",
                        questKeyString, "quest_key");
            }
            String scopeKeyString = root.get("scope").getAsString();
            NamespacedKey scopeKey = NamespacedKey.fromString(scopeKeyString);
            if (scopeKey == null) {
                throw new QuestDeserializationException(
                        "Invalid scope key: '" + scopeKeyString + "'",
                        questKeyString, "scope");
            }

            List<QuestPhaseDefinition> phases = deserializePhases(
                    root.getAsJsonArray("phases"), questKeyString);
            List<QuestRewardEntry> rewardEntries = deserializeRewardEntries(
                    root.getAsJsonArray("rewards"), questKeyString);

            RewardDistributionConfig rewardDistribution = root.has("reward_distribution")
                    ? deserializeDistribution(root.getAsJsonObject("reward_distribution"), questKeyString)
                    : null;

            Map<String, String> inlineDisplay = null;
            if (root.has("inline_display")) {
                inlineDisplay = new LinkedHashMap<>();
                JsonObject displayObj = root.getAsJsonObject("inline_display");
                for (Map.Entry<String, com.google.gson.JsonElement> entry : displayObj.entrySet()) {
                    inlineDisplay.put(entry.getKey(), entry.getValue().getAsString());
                }
            }

            List<OnStartMessage> onStartMessages = List.of();
            if (root.has("on_start_messages")) {
                onStartMessages = deserializeOnStartMessages(root.getAsJsonArray("on_start_messages"));
            }

            return new QuestDefinition.Builder(questKey, scopeKey, phases)
                    .rewardEntries(rewardEntries)
                    .rewardDistribution(rewardDistribution)
                    .inlineDisplay(inlineDisplay)
                    .onStartMessages(onStartMessages)
                    .build();
        } catch (QuestDeserializationException e) {
            throw e;
        } catch (Exception e) {
            throw new QuestDeserializationException(
                    "Failed to deserialize generated quest definition: " + e.getMessage(),
                    e, questKeyString, "JSON root");
        }
    }

    /**
     * Deserializes the {@code on_start_messages} JSON array into {@link OnStartMessage} entries.
     *
     * @param messagesArray the JSON array of on-start message objects
     * @return the deserialized on-start messages
     */
    @NotNull
    private List<OnStartMessage> deserializeOnStartMessages(@NotNull JsonArray messagesArray) {
        List<OnStartMessage> messages = new ArrayList<>();
        for (int i = 0; i < messagesArray.size(); i++) {
            JsonObject messageObj = messagesArray.get(i).getAsJsonObject();
            if (messageObj.has("locale_key")) {
                messages.add(OnStartMessage.fromLocaleKey(messageObj.get("locale_key").getAsString()));
            } else if (messageObj.has("inline_messages")) {
                List<String> lines = new ArrayList<>();
                for (JsonElement lineElement : messageObj.getAsJsonArray("inline_messages")) {
                    lines.add(lineElement.getAsString());
                }
                messages.add(OnStartMessage.fromInline(lines));
            } else {
                McRPG.getInstance().getLogger().warning(
                        "Malformed on_start_messages entry at index " + i
                                + " — missing both 'locale_key' and 'inline_messages', skipping");
            }
        }
        return messages;
    }

    /**
     * Serializes a single phase definition including its completion mode and all
     * child stages.
     *
     * @param phase            the phase to serialize
     * @param objectiveConfigs resolved objective configs for embedding in objective JSON
     * @return the JSON object representing this phase
     */
    @NotNull
    private JsonObject serializePhase(@NotNull QuestPhaseDefinition phase,
                                      @NotNull Map<NamespacedKey, Map<String, Object>> objectiveConfigs) {
        JsonObject phaseObj = new JsonObject();
        phaseObj.addProperty("completion_mode", phase.getCompletionMode().name());

        JsonArray stagesArray = new JsonArray();
        for (QuestStageDefinition stage : phase.getStages()) {
            stagesArray.add(serializeStage(stage, objectiveConfigs));
        }
        phaseObj.add("stages", stagesArray);

        if (!phase.getRewards().isEmpty()) {
            JsonArray rewardsArray = new JsonArray();
            for (QuestRewardType reward : phase.getRewards()) {
                JsonObject rewardObj = new JsonObject();
                rewardObj.addProperty("type", reward.getKey().toString());
                rewardObj.add("config", GSON.toJsonTree(reward.serializeConfig()));
                rewardsArray.add(rewardObj);
            }
            phaseObj.add("rewards", rewardsArray);
        }

        phase.getRewardDistribution()
                .ifPresent(dist -> phaseObj.add("reward_distribution", serializeDistribution(dist)));

        return phaseObj;
    }

    /**
     * Serializes a single stage definition including its key and all child objectives.
     *
     * @param stage            the stage to serialize
     * @param objectiveConfigs resolved objective configs for embedding in objective JSON
     * @return the JSON object representing this stage
     */
    @NotNull
    private JsonObject serializeStage(@NotNull QuestStageDefinition stage,
                                      @NotNull Map<NamespacedKey, Map<String, Object>> objectiveConfigs) {
        JsonObject stageObj = new JsonObject();
        stageObj.addProperty("key", stage.getStageKey().toString());

        JsonArray objectivesArray = new JsonArray();
        for (QuestObjectiveDefinition objective : stage.getObjectives()) {
            objectivesArray.add(serializeObjective(objective, objectiveConfigs));
        }
        stageObj.add("objectives", objectivesArray);

        stage.getRewardDistribution()
                .ifPresent(dist -> stageObj.add("reward_distribution", serializeDistribution(dist)));

        return stageObj;
    }

    /**
     * Serializes a single objective definition including its key, type, required
     * progress, and the resolved config map from the engine.
     *
     * @param objective        the objective to serialize
     * @param objectiveConfigs resolved objective configs keyed by objective key
     * @return the JSON object representing this objective
     */
    @NotNull
    private JsonObject serializeObjective(@NotNull QuestObjectiveDefinition objective,
                                          @NotNull Map<NamespacedKey, Map<String, Object>> objectiveConfigs) {
        JsonObject objObj = new JsonObject();
        objObj.addProperty("key", objective.getObjectiveKey().toString());
        objObj.addProperty("type", objective.getObjectiveType().getKey().toString());
        objObj.addProperty("required_progress", objective.getRequiredProgress());

        Map<String, Object> config = objectiveConfigs.getOrDefault(objective.getObjectiveKey(), Map.of());
        if (!config.isEmpty()) {
            objObj.add("config", GSON.toJsonTree(config));
        }

        objective.getRewardDistribution()
                .ifPresent(dist -> objObj.add("reward_distribution", serializeDistribution(dist)));

        return objObj;
    }

    /**
     * Deserializes the {@code phases} JSON array into an ordered list of
     * {@link QuestPhaseDefinition} objects, resolving each objective's type
     * from the registry.
     *
     * @param phasesArray    the JSON array of phase objects
     * @param questKeyString the quest key string for error context
     * @return the deserialized phase definitions
     * @throws QuestDeserializationException if an objective type cannot be resolved
     */
    @NotNull
    private List<QuestPhaseDefinition> deserializePhases(@NotNull JsonArray phasesArray,
                                                         @NotNull String questKeyString) {
        List<QuestPhaseDefinition> phases = new ArrayList<>();
        for (int phaseIdx = 0; phaseIdx < phasesArray.size(); phaseIdx++) {
            JsonObject phaseObj = phasesArray.get(phaseIdx).getAsJsonObject();
            PhaseCompletionMode completionMode = PhaseCompletionMode.valueOf(
                    phaseObj.get("completion_mode").getAsString());

            JsonArray stagesArray = phaseObj.getAsJsonArray("stages");
            List<QuestStageDefinition> stages = new ArrayList<>();

            for (JsonElement stageElement : stagesArray) {
                JsonObject stageObj = stageElement.getAsJsonObject();
                NamespacedKey stageKey = NamespacedKey.fromString(stageObj.get("key").getAsString());

                JsonArray objectivesArray = stageObj.getAsJsonArray("objectives");
                List<QuestObjectiveDefinition> objectives = new ArrayList<>();

                for (JsonElement objElement : objectivesArray) {
                    JsonObject objObj = objElement.getAsJsonObject();
                    objectives.add(deserializeObjective(objObj, questKeyString));
                }

                RewardDistributionConfig stageDist = stageObj.has("reward_distribution")
                        ? deserializeDistribution(stageObj.getAsJsonObject("reward_distribution"), questKeyString)
                        : null;

                stages.add(new QuestStageDefinition(stageKey, objectives, List.of(), stageDist));
            }

            List<QuestRewardType> phaseRewards = List.of();
            if (phaseObj.has("rewards")) {
                List<QuestRewardType> parsed = new ArrayList<>();
                for (JsonElement rewardElement : phaseObj.getAsJsonArray("rewards")) {
                    JsonObject rewardObj = rewardElement.getAsJsonObject();
                    NamespacedKey typeKey = NamespacedKey.fromString(rewardObj.get("type").getAsString());
                    QuestRewardType baseType = rewardTypeRegistry.get(typeKey)
                            .orElseThrow(() -> new QuestDeserializationException(
                                    "Unknown reward type '" + typeKey + "' in phase of quest " + questKeyString
                                            + ". Is the type registered?",
                                    questKeyString, "phase reward type " + typeKey));
                    Map<String, Object> configMap = jsonObjectToMap(rewardObj.getAsJsonObject("config"));
                    parsed.add(baseType.fromSerializedConfig(configMap));
                }
                phaseRewards = parsed;
            }

            RewardDistributionConfig phaseDist = phaseObj.has("reward_distribution")
                    ? deserializeDistribution(phaseObj.getAsJsonObject("reward_distribution"), questKeyString)
                    : null;

            phases.add(new QuestPhaseDefinition(phaseIdx, completionMode, stages, phaseRewards, phaseDist));
        }
        return phases;
    }

    /**
     * Deserializes a single objective from its JSON representation. Looks up the
     * base objective type in the registry and creates a configured instance from
     * the stored config map via an in-memory {@link YamlDocument}.
     *
     * @param objObj         the JSON object representing the objective
     * @param questKeyString the quest key string for error context
     * @return the deserialized objective definition
     * @throws QuestDeserializationException if the objective type is not registered
     */
    @NotNull
    private QuestObjectiveDefinition deserializeObjective(@NotNull JsonObject objObj,
                                                          @NotNull String questKeyString) {
        NamespacedKey objectiveKey = NamespacedKey.fromString(objObj.get("key").getAsString());
        NamespacedKey typeKey = NamespacedKey.fromString(objObj.get("type").getAsString());
        long requiredProgress = objObj.get("required_progress").getAsLong();

        QuestObjectiveType baseType = objectiveTypeRegistry.get(typeKey)
                .orElseThrow(() -> new QuestDeserializationException(
                        "Unknown objective type '" + typeKey + "' for objective '" + objectiveKey
                                + "' in quest " + questKeyString + ". Is the type registered?",
                        questKeyString, "objective type " + typeKey));

        QuestObjectiveType configuredType = baseType;
        if (objObj.has("config")) {
            Map<String, Object> configMap = jsonObjectToMap(objObj.getAsJsonObject("config"));
            configuredType = createConfiguredType(baseType, configMap, questKeyString);
        }

        RewardDistributionConfig objDist = objObj.has("reward_distribution")
                ? deserializeDistribution(objObj.getAsJsonObject("reward_distribution"), questKeyString)
                : null;

        return new QuestObjectiveDefinition(objectiveKey, configuredType, requiredProgress, List.of(), objDist);
    }

    /**
     * Deserializes the {@code rewards} JSON array into a list of configured
     * {@link QuestRewardType} instances by looking up base types in the registry
     * and calling {@link QuestRewardType#fromSerializedConfig}.
     *
     * @param rewardsArray   the JSON array of reward objects
     * @param questKeyString the quest key string for error context
     * @return the deserialized reward types
     * @throws QuestDeserializationException if a reward type is not registered
     */
    @NotNull
    private List<QuestRewardEntry> deserializeRewardEntries(
            @NotNull JsonArray rewardsArray,
            @NotNull String questKeyString) {
        List<QuestRewardEntry> entries = new ArrayList<>();
        for (JsonElement element : rewardsArray) {
            JsonObject rewardObj = element.getAsJsonObject();
            NamespacedKey typeKey = NamespacedKey.fromString(rewardObj.get("type").getAsString());

            QuestRewardType baseType = rewardTypeRegistry.get(typeKey)
                    .orElseThrow(() -> new QuestDeserializationException(
                            "Unknown reward type '" + typeKey + "' in quest " + questKeyString
                                    + ". Is the type registered?",
                            questKeyString, "reward type " + typeKey));

            Map<String, Object> configMap = jsonObjectToMap(rewardObj.getAsJsonObject("config"));
            QuestRewardType reward = baseType.fromSerializedConfig(configMap);

            RewardFallback fallback = null;
            if (rewardObj.has("fallback")) {
                fallback = deserializeFallback(rewardObj.getAsJsonObject("fallback"), questKeyString);
            }
            entries.add(new QuestRewardEntry(reward, fallback));
        }
        return entries;
    }

    /**
     * Serializes a {@link RewardFallback} to a JSON object.
     *
     * @param fallback the fallback to serialize
     * @return the JSON object representing this fallback
     */
    @NotNull
    private JsonObject serializeRewardFallback(@NotNull RewardFallback fallback) {
        JsonObject obj = new JsonObject();
        obj.addProperty("condition_type", fallback.condition().getKey().toString());
        obj.add("condition_config", GSON.toJsonTree(fallback.condition().serializeConfig()));
        obj.addProperty("fallback_reward_type", fallback.fallbackReward().getKey().toString());
        obj.add("fallback_reward_config", GSON.toJsonTree(fallback.fallbackReward().serializeConfig()));
        return obj;
    }

    /**
     * Deserializes a {@link RewardDistributionConfig} from its JSON representation.
     *
     * @param distObj        the JSON object containing the distribution config
     * @param questKeyString the quest key string for error context
     * @return the deserialized reward distribution config
     */
    @NotNull
    private RewardDistributionConfig deserializeDistribution(@NotNull JsonObject distObj,
                                                              @NotNull String questKeyString) {
        JsonArray tiersArray = distObj.getAsJsonArray("tiers");
        List<DistributionTierConfig> tiers = new ArrayList<>();

        for (JsonElement tierElement : tiersArray) {
            JsonObject tierObj = tierElement.getAsJsonObject();
            String tierKey = tierObj.get("tier_key").getAsString();
            NamespacedKey typeKey = NamespacedKey.fromString(tierObj.get("type").getAsString());
            RewardSplitMode splitMode = RewardSplitMode.valueOf(tierObj.get("split_mode").getAsString());

            Map<String, Object> typeParameters = tierObj.has("type_parameters")
                    ? jsonObjectToMap(tierObj.getAsJsonObject("type_parameters"))
                    : Map.of();

            List<DistributionRewardEntry> rewardEntries = deserializeDistributionRewardEntries(
                    tierObj.getAsJsonArray("rewards"), questKeyString);

            NamespacedKey minRarity = tierObj.has("min_rarity")
                    ? NamespacedKey.fromString(tierObj.get("min_rarity").getAsString())
                    : null;
            NamespacedKey requiredRarity = tierObj.has("required_rarity")
                    ? NamespacedKey.fromString(tierObj.get("required_rarity").getAsString())
                    : null;

            tiers.add(new DistributionTierConfig(tierKey, typeKey, splitMode, rewardEntries,
                    typeParameters, minRarity, requiredRarity));
        }

        return new RewardDistributionConfig(tiers);
    }

    /**
     * Deserializes reward entries within a distribution tier, reconstructing
     * {@link DistributionRewardEntry} instances including their {@link PotBehavior},
     * {@link RemainderStrategy}, min-scaled-amount, top-count, and optional
     * {@link RewardFallback}.
     * Falls back to defaults when fields are absent (backward compatibility with older snapshots).
     *
     * @param rewardsArray   the JSON array of reward entry objects
     * @param questKeyString the quest key string for error context
     * @return the list of distribution reward entries
     */
    @NotNull
    private List<DistributionRewardEntry> deserializeDistributionRewardEntries(
            @NotNull JsonArray rewardsArray,
            @NotNull String questKeyString) {
        List<DistributionRewardEntry> entries = new ArrayList<>();
        for (JsonElement element : rewardsArray) {
            JsonObject rObj = element.getAsJsonObject();
            NamespacedKey typeKey = NamespacedKey.fromString(rObj.get("type").getAsString());

            QuestRewardType baseType = rewardTypeRegistry.get(typeKey)
                    .orElseThrow(() -> new QuestDeserializationException(
                            "Unknown reward type '" + typeKey + "' in distribution tier of quest " + questKeyString
                                    + ". Is the type registered?",
                            questKeyString, "distribution reward type " + typeKey));

            Map<String, Object> configMap = jsonObjectToMap(rObj.getAsJsonObject("config"));
            QuestRewardType reward = baseType.fromSerializedConfig(configMap);

            PotBehavior potBehavior = rObj.has("pot_behavior")
                    ? PotBehavior.valueOf(rObj.get("pot_behavior").getAsString())
                    : PotBehavior.SCALE;
            RemainderStrategy remainderStrategy = rObj.has("remainder_strategy")
                    ? RemainderStrategy.valueOf(rObj.get("remainder_strategy").getAsString())
                    : RemainderStrategy.DISCARD;
            int minScaledAmount = rObj.has("min_scaled_amount") ? rObj.get("min_scaled_amount").getAsInt() : 1;
            int topCount = rObj.has("top_count") ? rObj.get("top_count").getAsInt() : 1;

            RewardFallback fallback = null;
            if (rObj.has("fallback")) {
                fallback = deserializeFallback(rObj.getAsJsonObject("fallback"), questKeyString);
            }

            entries.add(new DistributionRewardEntry(reward, potBehavior, remainderStrategy,
                    minScaledAmount, topCount, fallback));
        }
        return entries;
    }

    /**
     * Deserializes a {@link RewardFallback} from its JSON representation, reconstructing
     * the condition from the {@link TemplateConditionRegistry} and the fallback reward
     * from the {@link QuestRewardTypeRegistry}.
     *
     * @param fallbackObj    the JSON object containing condition and fallback reward data
     * @param questKeyString the quest key string for error context
     * @return the reconstructed {@link RewardFallback}
     * @throws QuestDeserializationException if the condition type or fallback reward type is not registered
     */
    @NotNull
    private RewardFallback deserializeFallback(@NotNull JsonObject fallbackObj,
                                               @NotNull String questKeyString) {
        NamespacedKey condTypeKey = NamespacedKey.fromString(fallbackObj.get("condition_type").getAsString());
        TemplateCondition baseCondition = conditionTypeRegistry.get(condTypeKey)
                .orElseThrow(() -> new QuestDeserializationException(
                        "Unknown condition type '" + condTypeKey + "' in reward fallback of quest " + questKeyString
                                + ". Is the type registered?",
                        questKeyString, "condition type " + condTypeKey));

        Map<String, Object> condConfig = jsonObjectToMap(fallbackObj.getAsJsonObject("condition_config"));
        TemplateCondition condition = createConfiguredCondition(baseCondition, condConfig, questKeyString);

        NamespacedKey fallbackTypeKey = NamespacedKey.fromString(fallbackObj.get("fallback_reward_type").getAsString());
        QuestRewardType baseFallbackReward = rewardTypeRegistry.get(fallbackTypeKey)
                .orElseThrow(() -> new QuestDeserializationException(
                        "Unknown fallback reward type '" + fallbackTypeKey + "' in quest " + questKeyString
                                + ". Is the type registered?",
                        questKeyString, "fallback reward type " + fallbackTypeKey));

        Map<String, Object> fallbackRewardConfig = jsonObjectToMap(fallbackObj.getAsJsonObject("fallback_reward_config"));
        QuestRewardType fallbackReward = baseFallbackReward.fromSerializedConfig(fallbackRewardConfig);

        return new RewardFallback(condition, fallbackReward);
    }

    /**
     * Creates a configured objective type by populating an in-memory {@link YamlDocument}
     * with the deserialized config entries and calling {@link QuestObjectiveType#parseConfig}.
     * This bridges the gap between JSON-stored config maps and the YAML Section that
     * objective types expect.
     *
     * @param baseType       the base (unconfigured) objective type from the registry
     * @param config         the config map deserialized from JSON
     * @param questKeyString the quest key string for error context
     * @return a configured copy of the objective type
     * @throws QuestDeserializationException if the in-memory document cannot be created
     */
    @NotNull
    private QuestObjectiveType createConfiguredType(@NotNull QuestObjectiveType baseType,
                                                    @NotNull Map<String, Object> config,
                                                    @NotNull String questKeyString) {
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
            throw new QuestDeserializationException(
                    "Failed to create in-memory config for objective type " + baseType.getKey()
                            + " in quest " + questKeyString,
                    e, questKeyString, "objective type config " + baseType.getKey());
        }
    }

    /**
     * Creates a configured condition by populating an in-memory {@link YamlDocument} with
     * the deserialized config entries and calling {@link TemplateCondition#fromConfig}.
     * Uses recursive document population to handle nested structures such as
     * {@link us.eunoians.mcrpg.quest.board.template.condition.CompoundCondition}.
     *
     * @param baseCondition  the base (unconfigured) condition type from the registry
     * @param config         the config map deserialized from JSON
     * @param questKeyString the quest key string for error context
     * @return a configured condition instance
     * @throws QuestDeserializationException if the in-memory document cannot be created
     */
    @NotNull
    private TemplateCondition createConfiguredCondition(@NotNull TemplateCondition baseCondition,
                                                        @NotNull Map<String, Object> config,
                                                        @NotNull String questKeyString) {
        if (config.isEmpty()) {
            return baseCondition;
        }
        try {
            YamlDocument doc = YamlDocument.create(new ByteArrayInputStream("{}".getBytes()));
            populateDocument(doc, config, "");
            var parser = new ConditionParser(conditionTypeRegistry);
            return baseCondition.fromConfig(doc, parser);
        } catch (IOException e) {
            throw new QuestDeserializationException(
                    "Failed to create in-memory config for condition type " + baseCondition.getKey()
                            + " in quest " + questKeyString,
                    e, questKeyString, "condition type config " + baseCondition.getKey());
        }
    }

    /**
     * Recursively populates a {@link YamlDocument} from a config map using dot-notation
     * routes. Nested {@link Map} values are flattened into nested YAML sections so that
     * {@link dev.dejvokep.boostedyaml.block.implementation.Section#getSection(String)} works
     * correctly when condition {@code fromConfig} implementations read child sections.
     *
     * @param doc    the document to populate
     * @param config the config map (possibly containing nested maps)
     * @param prefix the current route prefix (empty string for the root level)
     * @throws IOException if the document cannot be written to
     */
    private void populateDocument(@NotNull YamlDocument doc,
                                  @NotNull Map<String, Object> config,
                                  @NotNull String prefix) throws IOException {
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String route = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof Map<?, ?> nested) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) nested;
                populateDocument(doc, nestedMap, route);
            } else {
                doc.set(route, entry.getValue());
            }
        }
    }

    /**
     * Converts a {@link JsonObject} to a {@link Map}{@code <String, Object>} with
     * Java-native value types. Nested objects become nested maps, arrays become lists.
     *
     * @param jsonObject the JSON object to convert
     * @return an ordered map of string keys to Java-native values
     */
    @NotNull
    private Map<String, Object> jsonObjectToMap(@NotNull JsonObject jsonObject) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            map.put(entry.getKey(), jsonElementToJava(entry.getValue()));
        }
        return map;
    }

    /**
     * Converts a {@link JsonElement} to its Java-native equivalent. Primitives become
     * their boxed types (preferring {@code int} for whole numbers in 32-bit range,
     * {@code long} for larger whole numbers, {@code double} for decimals). Arrays
     * become {@code List<Object>} and objects become {@code Map<String, Object>}.
     *
     * @param element the JSON element to convert
     * @return the Java-native equivalent
     */
    @NotNull
    private Object jsonElementToJava(@NotNull JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive prim = element.getAsJsonPrimitive();
            if (prim.isNumber()) {
                double d = prim.getAsDouble();
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    long l = prim.getAsLong();
                    if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                        return (int) l;
                    }
                    return l;
                }
                return d;
            }
            if (prim.isBoolean()) {
                return prim.getAsBoolean();
            }
            return prim.getAsString();
        }
        if (element.isJsonArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonElement item : element.getAsJsonArray()) {
                list.add(jsonElementToJava(item));
            }
            return list;
        }
        if (element.isJsonObject()) {
            return jsonObjectToMap(element.getAsJsonObject());
        }
        return element.toString();
    }

    /**
     * Serializes a {@link RewardDistributionConfig} to a JSON object.
     *
     * @param config the distribution config to serialize
     * @return the JSON object representing this distribution config
     */
    @NotNull
    private JsonObject serializeDistribution(@NotNull RewardDistributionConfig config) {
        JsonArray tiersArray = new JsonArray();
        for (DistributionTierConfig tier : config.getTiers()) {
            JsonObject tierObj = new JsonObject();
            tierObj.addProperty("tier_key", tier.getTierKey());
            tierObj.addProperty("type", tier.getTypeKey().toString());
            tierObj.addProperty("split_mode", tier.getSplitMode().name());

            if (!tier.getTypeParameters().isEmpty()) {
                tierObj.add("type_parameters", GSON.toJsonTree(tier.getTypeParameters()));
            }

            JsonArray rewardsArray = new JsonArray();
            for (DistributionRewardEntry entry : tier.getRewardEntries()) {
                JsonObject rObj = new JsonObject();
                rObj.addProperty("type", entry.reward().getKey().toString());
                rObj.add("config", GSON.toJsonTree(entry.reward().serializeConfig()));
                rObj.addProperty("pot_behavior", entry.potBehavior().name());
                rObj.addProperty("remainder_strategy", entry.remainderStrategy().name());
                rObj.addProperty("min_scaled_amount", entry.minScaledAmount());
                rObj.addProperty("top_count", entry.topCount());
                if (entry.fallback() != null) {
                    RewardFallback fallback = entry.fallback();
                    JsonObject fallbackObj = new JsonObject();
                    fallbackObj.addProperty("condition_type", fallback.condition().getKey().toString());
                    fallbackObj.add("condition_config", GSON.toJsonTree(fallback.condition().serializeConfig()));
                    fallbackObj.addProperty("fallback_reward_type", fallback.fallbackReward().getKey().toString());
                    fallbackObj.add("fallback_reward_config", GSON.toJsonTree(fallback.fallbackReward().serializeConfig()));
                    rObj.add("fallback", fallbackObj);
                }
                rewardsArray.add(rObj);
            }
            tierObj.add("rewards", rewardsArray);

            tier.getMinRarity()
                    .ifPresent(k -> tierObj.addProperty("min_rarity", k.toString()));
            tier.getRequiredRarity()
                    .ifPresent(k -> tierObj.addProperty("required_rarity", k.toString()));

            tiersArray.add(tierObj);
        }
        JsonObject distObj = new JsonObject();
        distObj.add("tiers", tiersArray);
        return distObj;
    }

    /**
     * Converts a Java object to a {@link JsonElement} for serialization. Handles
     * numbers, strings, booleans, and lists recursively. Falls back to Gson's
     * default serialization for unrecognized types.
     *
     * @param value the Java value to convert
     * @return the corresponding JSON element
     */
    @NotNull
    private JsonElement toJsonElement(@NotNull Object value) {
        if (value instanceof Number n) {
            return new JsonPrimitive(n);
        }
        if (value instanceof String s) {
            return new JsonPrimitive(s);
        }
        if (value instanceof Boolean b) {
            return new JsonPrimitive(b);
        }
        if (value instanceof List<?> list) {
            JsonArray array = new JsonArray();
            for (Object item : list) {
                array.add(toJsonElement(item));
            }
            return array;
        }
        return GSON.toJsonTree(value);
    }
}
