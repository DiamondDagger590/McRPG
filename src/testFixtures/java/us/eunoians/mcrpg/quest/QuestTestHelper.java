package us.eunoians.mcrpg.quest;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestRepeatMode;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.scope.impl.SinglePlayerQuestScope;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;
import us.eunoians.mcrpg.quest.objective.type.MockQuestObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.quest.reward.MockQuestRewardType;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Factory methods for building valid quest definition and instance object graphs in tests.
 * All keys use the {@code "mcrpg"} namespace.
 */
public final class QuestTestHelper {

    private static final String NAMESPACE = "mcrpg";
    private static final NamespacedKey TEST_EXPANSION_KEY = new NamespacedKey(NAMESPACE, "test_expansion");
    private static final NamespacedKey SINGLE_PLAYER_SCOPE = new NamespacedKey(NAMESPACE, "single_player");

    private QuestTestHelper() {
    }

    /**
     * Creates a mock objective type with the given key.
     *
     * @param key the plain key (will be namespaced under {@code mcrpg})
     * @return a mock objective type
     */
    @NotNull
    public static MockQuestObjectiveType mockObjectiveType(@NotNull String key) {
        return new MockQuestObjectiveType(new NamespacedKey(NAMESPACE, key), TEST_EXPANSION_KEY);
    }

    /**
     * Creates a mock reward type with the given key.
     *
     * @param key the plain key (will be namespaced under {@code mcrpg})
     * @return a mock reward type that tracks grant() calls
     */
    @NotNull
    public static MockQuestRewardType mockRewardType(@NotNull String key) {
        return new MockQuestRewardType(new NamespacedKey(NAMESPACE, key), TEST_EXPANSION_KEY);
    }

    /**
     * Creates a single objective definition with a mock objective type.
     *
     * @param key              the objective key
     * @param requiredProgress the progress required
     * @return an objective definition
     */
    @NotNull
    public static QuestObjectiveDefinition singleObjectiveDef(@NotNull String key, long requiredProgress) {
        return new QuestObjectiveDefinition(
                new NamespacedKey(NAMESPACE, key),
                mockObjectiveType(key + "_type"),
                requiredProgress,
                List.of()
        );
    }

    /**
     * Creates an objective definition with the given objective type and rewards.
     *
     * @param key              the objective key
     * @param objectiveType    the objective type to use
     * @param requiredProgress the progress required
     * @param rewards          the rewards to attach
     * @return an objective definition
     */
    @NotNull
    public static QuestObjectiveDefinition objectiveDef(@NotNull String key,
                                                        @NotNull QuestObjectiveType objectiveType,
                                                        long requiredProgress,
                                                        @NotNull List<QuestRewardType> rewards) {
        return new QuestObjectiveDefinition(
                new NamespacedKey(NAMESPACE, key),
                objectiveType,
                requiredProgress,
                rewards
        );
    }

    /**
     * Creates a stage definition with a single default objective.
     *
     * @param stageKey     the stage key
     * @param objectiveKey the objective key to create inside
     * @return a stage definition
     */
    @NotNull
    public static QuestStageDefinition singleStageDef(@NotNull String stageKey, @NotNull String objectiveKey) {
        return new QuestStageDefinition(
                new NamespacedKey(NAMESPACE, stageKey),
                List.of(singleObjectiveDef(objectiveKey, 10)),
                List.of()
        );
    }

    /**
     * Creates a stage definition with given objectives and rewards.
     *
     * @param stageKey   the stage key
     * @param objectives the objectives for this stage
     * @param rewards    the rewards for this stage
     * @return a stage definition
     */
    @NotNull
    public static QuestStageDefinition stageDef(@NotNull String stageKey,
                                                @NotNull List<QuestObjectiveDefinition> objectives,
                                                @NotNull List<QuestRewardType> rewards) {
        return new QuestStageDefinition(new NamespacedKey(NAMESPACE, stageKey), objectives, rewards);
    }

    /**
     * Creates a phase definition with the given completion mode and stages.
     *
     * @param mode   the phase completion mode
     * @param stages the stages within this phase
     * @return a phase definition at index 0
     */
    @NotNull
    public static QuestPhaseDefinition singlePhaseDef(@NotNull PhaseCompletionMode mode,
                                                      @NotNull QuestStageDefinition... stages) {
        return new QuestPhaseDefinition(0, mode, Arrays.asList(stages));
    }

    /**
     * Creates a phase definition with a specific index.
     *
     * @param phaseIndex the zero-based phase index
     * @param mode       the phase completion mode
     * @param stages     the stages within this phase
     * @return a phase definition
     */
    @NotNull
    public static QuestPhaseDefinition phaseDef(int phaseIndex,
                                                @NotNull PhaseCompletionMode mode,
                                                @NotNull QuestStageDefinition... stages) {
        return new QuestPhaseDefinition(phaseIndex, mode, Arrays.asList(stages));
    }

    /**
     * Creates a minimal single-phase quest definition with one stage and one objective.
     *
     * @param questKey the quest key
     * @return a quest definition
     */
    @NotNull
    public static QuestDefinition singlePhaseQuest(@NotNull String questKey) {
        QuestStageDefinition stage = singleStageDef(questKey + "_stage", questKey + "_obj");
        QuestPhaseDefinition phase = singlePhaseDef(PhaseCompletionMode.ALL, stage);
        return new QuestDefinition(
                new NamespacedKey(NAMESPACE, questKey),
                SINGLE_PLAYER_SCOPE,
                null,
                List.of(phase),
                List.of(),
                QuestRepeatMode.ONCE,
                null,
                -1,
                null
        );
    }

    /**
     * Creates a multi-phase quest definition with the provided phases.
     *
     * @param questKey the quest key
     * @param phases   the phase definitions (must have at least one)
     * @return a quest definition
     */
    @NotNull
    public static QuestDefinition multiPhaseQuest(@NotNull String questKey,
                                                  @NotNull QuestPhaseDefinition... phases) {
        return new QuestDefinition(
                new NamespacedKey(NAMESPACE, questKey),
                SINGLE_PLAYER_SCOPE,
                null,
                Arrays.asList(phases),
                List.of(),
                QuestRepeatMode.ONCE,
                null,
                -1,
                null
        );
    }

    /**
     * Creates a quest definition with full control over all parameters.
     *
     * @param questKey   the quest key
     * @param phases     the phase definitions
     * @param rewards    the quest-level rewards
     * @param repeatMode the repeat mode
     * @return a quest definition
     */
    @NotNull
    public static QuestDefinition questDef(@NotNull String questKey,
                                           @NotNull List<QuestPhaseDefinition> phases,
                                           @NotNull List<QuestRewardType> rewards,
                                           @NotNull QuestRepeatMode repeatMode) {
        return new QuestDefinition(
                new NamespacedKey(NAMESPACE, questKey),
                SINGLE_PLAYER_SCOPE,
                null,
                phases,
                rewards,
                repeatMode,
                null,
                -1,
                null
        );
    }

    /**
     * Creates a new quest instance from the given definition (not yet started).
     *
     * @param definition the quest definition
     * @return a quest instance in {@code NOT_STARTED} state
     */
    @NotNull
    public static QuestInstance newQuestInstance(@NotNull QuestDefinition definition) {
        return new QuestInstance(definition, null, Map.of(), new ManualQuestSource(), null);
    }

    /**
     * Creates a new quest instance from the given definition and starts it immediately.
     *
     * @param definition the quest definition
     * @return a quest instance in {@code IN_PROGRESS} state
     */
    @NotNull
    public static QuestInstance startedQuestInstance(@NotNull QuestDefinition definition) {
        QuestInstance instance = new QuestInstance(definition, null, Map.of(), new ManualQuestSource(), null);
        instance.start(definition);
        return instance;
    }

    /**
     * Creates a started quest instance with a {@link SinglePlayerQuestScope} containing the
     * given player. Useful for tests that need a fully wired quest with scope and player.
     *
     * @param definition the quest definition
     * @param playerUUID the UUID of the player to place in scope
     * @return a started quest instance with scope assigned
     */
    @NotNull
    public static QuestInstance startedQuestWithPlayer(@NotNull QuestDefinition definition,
                                                       @NotNull UUID playerUUID) {
        QuestInstance instance = new QuestInstance(definition, null, Map.of(), new ManualQuestSource(), null);
        SinglePlayerQuestScope scope = new SinglePlayerQuestScope(instance.getQuestUUID());
        scope.setPlayerInScope(playerUUID);
        instance.setQuestScope(scope);
        instance.start(definition);
        return instance;
    }
}
