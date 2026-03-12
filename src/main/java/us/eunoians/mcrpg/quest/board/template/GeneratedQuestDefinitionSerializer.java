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
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestRepeatMode;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.board.distribution.DistributionTierConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardSplitMode;
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
public final class GeneratedQuestDefinitionSerializer {

    private static final Gson GSON = new GsonBuilder().create();

    private GeneratedQuestDefinitionSerializer() {}

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
    public static String serialize(@NotNull QuestDefinition definition,
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
        for (QuestRewardType reward : definition.getRewards()) {
            JsonObject rewardObj = new JsonObject();
            rewardObj.addProperty("type", reward.getKey().toString());
            rewardObj.add("config", GSON.toJsonTree(reward.serializeConfig()));
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

        return GSON.toJson(root);
    }

    /**
     * Deserializes a JSON string back into a {@link QuestDefinition}, resolving objective
     * types and reward types from their registries.
     *
     * @param json                  the JSON string produced by {@link #serialize}
     * @param objectiveTypeRegistry registry for looking up objective types by key
     * @param rewardTypeRegistry    registry for looking up reward types by key
     * @return the reconstructed quest definition
     * @throws QuestDeserializationException if the JSON is malformed, or references an
     *                                       objective type or reward type that is not
     *                                       registered
     */
    @NotNull
    public static QuestDefinition deserialize(@NotNull String json,
                                              @NotNull QuestObjectiveTypeRegistry objectiveTypeRegistry,
                                              @NotNull QuestRewardTypeRegistry rewardTypeRegistry) {
        String questKeyString = null;
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            questKeyString = root.get("quest_key").getAsString();
            NamespacedKey questKey = NamespacedKey.fromString(questKeyString);
            NamespacedKey scopeKey = NamespacedKey.fromString(root.get("scope").getAsString());

            List<QuestPhaseDefinition> phases = deserializePhases(
                    root.getAsJsonArray("phases"), objectiveTypeRegistry, rewardTypeRegistry, questKeyString);
            List<QuestRewardType> rewards = deserializeRewards(
                    root.getAsJsonArray("rewards"), rewardTypeRegistry, questKeyString);

            RewardDistributionConfig rewardDistribution = root.has("reward_distribution")
                    ? deserializeDistribution(root.getAsJsonObject("reward_distribution"), rewardTypeRegistry, questKeyString)
                    : null;

            Map<String, String> inlineDisplay = null;
            if (root.has("inline_display")) {
                inlineDisplay = new java.util.LinkedHashMap<>();
                JsonObject displayObj = root.getAsJsonObject("inline_display");
                for (Map.Entry<String, com.google.gson.JsonElement> entry : displayObj.entrySet()) {
                    inlineDisplay.put(entry.getKey(), entry.getValue().getAsString());
                }
            }

            return new QuestDefinition(
                    questKey,
                    scopeKey,
                    null,
                    phases,
                    rewards,
                    QuestRepeatMode.ONCE,
                    null,
                    -1,
                    null,
                    null,
                    rewardDistribution,
                    inlineDisplay
            );
        } catch (QuestDeserializationException e) {
            throw e;
        } catch (Exception e) {
            throw new QuestDeserializationException(
                    "Failed to deserialize generated quest definition: " + e.getMessage(),
                    e, questKeyString, "JSON root");
        }
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
    private static JsonObject serializePhase(@NotNull QuestPhaseDefinition phase,
                                             @NotNull Map<NamespacedKey, Map<String, Object>> objectiveConfigs) {
        JsonObject phaseObj = new JsonObject();
        phaseObj.addProperty("completion_mode", phase.getCompletionMode().name());

        JsonArray stagesArray = new JsonArray();
        for (QuestStageDefinition stage : phase.getStages()) {
            stagesArray.add(serializeStage(stage, objectiveConfigs));
        }
        phaseObj.add("stages", stagesArray);

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
    private static JsonObject serializeStage(@NotNull QuestStageDefinition stage,
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
    private static JsonObject serializeObjective(@NotNull QuestObjectiveDefinition objective,
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
     * @param phasesArray           the JSON array of phase objects
     * @param objectiveTypeRegistry registry for looking up objective types
     * @param rewardTypeRegistry    registry for looking up reward types (used for distribution tiers)
     * @param questKeyString        the quest key string for error context
     * @return the deserialized phase definitions
     * @throws QuestDeserializationException if an objective type cannot be resolved
     */
    @NotNull
    private static List<QuestPhaseDefinition> deserializePhases(@NotNull JsonArray phasesArray,
                                                                @NotNull QuestObjectiveTypeRegistry objectiveTypeRegistry,
                                                                @NotNull QuestRewardTypeRegistry rewardTypeRegistry,
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
                    objectives.add(deserializeObjective(objObj, objectiveTypeRegistry, rewardTypeRegistry, questKeyString));
                }

                RewardDistributionConfig stageDist = stageObj.has("reward_distribution")
                        ? deserializeDistribution(stageObj.getAsJsonObject("reward_distribution"), rewardTypeRegistry, questKeyString)
                        : null;

                stages.add(new QuestStageDefinition(stageKey, objectives, List.of(), stageDist));
            }

            RewardDistributionConfig phaseDist = phaseObj.has("reward_distribution")
                    ? deserializeDistribution(phaseObj.getAsJsonObject("reward_distribution"), rewardTypeRegistry, questKeyString)
                    : null;

            phases.add(new QuestPhaseDefinition(phaseIdx, completionMode, stages, phaseDist));
        }
        return phases;
    }

    /**
     * Deserializes a single objective from its JSON representation. Looks up the
     * base objective type in the registry and creates a configured instance from
     * the stored config map via an in-memory {@link YamlDocument}.
     *
     * @param objObj                the JSON object representing the objective
     * @param objectiveTypeRegistry registry for looking up objective types
     * @param questKeyString        the quest key string for error context
     * @return the deserialized objective definition
     * @throws QuestDeserializationException if the objective type is not registered
     */
    @NotNull
    private static QuestObjectiveDefinition deserializeObjective(@NotNull JsonObject objObj,
                                                                 @NotNull QuestObjectiveTypeRegistry objectiveTypeRegistry,
                                                                 @NotNull QuestRewardTypeRegistry rewardTypeRegistry,
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
                ? deserializeDistribution(objObj.getAsJsonObject("reward_distribution"),
                        rewardTypeRegistry, questKeyString)
                : null;

        return new QuestObjectiveDefinition(objectiveKey, configuredType, requiredProgress, List.of(), objDist);
    }

    /**
     * Deserializes the {@code rewards} JSON array into a list of configured
     * {@link QuestRewardType} instances by looking up base types in the registry
     * and calling {@link QuestRewardType#fromSerializedConfig}.
     *
     * @param rewardsArray       the JSON array of reward objects
     * @param rewardTypeRegistry registry for looking up reward types
     * @param questKeyString     the quest key string for error context
     * @return the deserialized reward types
     * @throws QuestDeserializationException if a reward type is not registered
     */
    @NotNull
    private static List<QuestRewardType> deserializeRewards(@NotNull JsonArray rewardsArray,
                                                            @NotNull QuestRewardTypeRegistry rewardTypeRegistry,
                                                            @NotNull String questKeyString) {
        List<QuestRewardType> rewards = new ArrayList<>();
        for (JsonElement element : rewardsArray) {
            JsonObject rewardObj = element.getAsJsonObject();
            NamespacedKey typeKey = NamespacedKey.fromString(rewardObj.get("type").getAsString());

            QuestRewardType baseType = rewardTypeRegistry.get(typeKey)
                    .orElseThrow(() -> new QuestDeserializationException(
                            "Unknown reward type '" + typeKey + "' in quest " + questKeyString
                                    + ". Is the type registered?",
                            questKeyString, "reward type " + typeKey));

            Map<String, Object> configMap = jsonObjectToMap(rewardObj.getAsJsonObject("config"));
            rewards.add(baseType.fromSerializedConfig(configMap));
        }
        return rewards;
    }

    @NotNull
    private static RewardDistributionConfig deserializeDistribution(@NotNull JsonObject distObj,
                                                                     @NotNull QuestRewardTypeRegistry rewardTypeRegistry,
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

            List<QuestRewardType> rewards = deserializeRewards(
                    tierObj.getAsJsonArray("rewards"), rewardTypeRegistry, questKeyString);

            NamespacedKey minRarity = tierObj.has("min_rarity")
                    ? NamespacedKey.fromString(tierObj.get("min_rarity").getAsString())
                    : null;
            NamespacedKey requiredRarity = tierObj.has("required_rarity")
                    ? NamespacedKey.fromString(tierObj.get("required_rarity").getAsString())
                    : null;

            tiers.add(new DistributionTierConfig(tierKey, typeKey, splitMode, rewards, typeParameters, minRarity, requiredRarity, true));
        }

        return new RewardDistributionConfig(tiers);
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
    private static QuestObjectiveType createConfiguredType(@NotNull QuestObjectiveType baseType,
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
     * Converts a {@link JsonObject} to a {@link Map}{@code <String, Object>} with
     * Java-native value types. Nested objects become nested maps, arrays become lists.
     *
     * @param jsonObject the JSON object to convert
     * @return an ordered map of string keys to Java-native values
     */
    @NotNull
    private static Map<String, Object> jsonObjectToMap(@NotNull JsonObject jsonObject) {
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
    private static Object jsonElementToJava(@NotNull JsonElement element) {
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

    @NotNull
    private static JsonObject serializeDistribution(@NotNull RewardDistributionConfig config) {
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
            for (QuestRewardType reward : tier.getRewards()) {
                JsonObject rObj = new JsonObject();
                rObj.addProperty("type", reward.getKey().toString());
                rObj.add("config", GSON.toJsonTree(reward.serializeConfig()));
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
    private static JsonElement toJsonElement(@NotNull Object value) {
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
