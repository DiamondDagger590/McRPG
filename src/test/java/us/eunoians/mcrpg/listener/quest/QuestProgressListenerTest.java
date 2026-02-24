package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.quest.QuestObjectiveProgressEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestRepeatMode;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveState;
import us.eunoians.mcrpg.quest.objective.type.MockQuestObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasFiredEventInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link QuestProgressListener#progressQuests} default method using a mocked
 * QuestManager with stubbed lookup methods.
 */
public class QuestProgressListenerTest extends McRPGBaseTest {

    private QuestManager mockQuestManager;
    private UUID playerUUID;
    private final List<QuestInstance> trackedQuests = new ArrayList<>();
    private final List<QuestDefinition> registeredDefs = new ArrayList<>();

    private final QuestProgressListener listener = new QuestProgressListener() {};
    private static final QuestObjectiveProgressContext DUMMY_CONTEXT = new QuestObjectiveProgressContext() {};

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();
        playerUUID = UUID.randomUUID();
        trackedQuests.clear();
        registeredDefs.clear();

        mockQuestManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        when(mockQuestManager.getActiveQuestsForPlayer(any(UUID.class)))
                .thenAnswer(inv -> List.copyOf(trackedQuests));

        when(mockQuestManager.getQuestDefinition(any(NamespacedKey.class)))
                .thenAnswer(inv -> {
                    NamespacedKey key = inv.getArgument(0);
                    return registeredDefs.stream()
                            .filter(d -> d.getQuestKey().equals(key))
                            .findFirst();
                });
    }

    private QuestInstance buildAndTrackQuest(String key, MockQuestObjectiveType objectiveType) {
        QuestObjectiveDefinition objDef = QuestTestHelper.objectiveDef(
                key + "_obj", objectiveType, 10, List.of());
        QuestStageDefinition stageDef = QuestTestHelper.stageDef(key + "_stage", List.of(objDef), List.of());
        QuestPhaseDefinition phaseDef = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stageDef);
        QuestDefinition def = new QuestDefinition(
                new NamespacedKey("mcrpg", key),
                new NamespacedKey("mcrpg", "single_player"),
                null, List.of(phaseDef), List.of(), QuestRepeatMode.ONCE, null, -1, null);

        registeredDefs.add(def);

        QuestInstance instance = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
        trackedQuests.add(instance);
        return instance;
    }

    @DisplayName("Given a matching objective type, when progressQuests is called, then progress is applied")
    @Test
    public void progressQuests_appliesProgressToMatchingObjective() {
        MockQuestObjectiveType type = QuestTestHelper.mockObjectiveType("match")
                .withCanProcess(ctx -> true)
                .withProgressFunction((inst, ctx) -> 1L);
        QuestInstance quest = buildAndTrackQuest("match_test", type);

        listener.progressQuests(playerUUID, DUMMY_CONTEXT);

        QuestObjectiveInstance obj = quest.getActiveQuestStages().get(0).getQuestObjectives().get(0);
        assertTrue(obj.getCurrentProgression() > 0);
    }

    @DisplayName("Given a non-matching objective type, when progressQuests is called, then progress stays at zero")
    @Test
    public void progressQuests_skipsNonMatchingObjectiveType() {
        MockQuestObjectiveType type = QuestTestHelper.mockObjectiveType("nomatch")
                .withCanProcess(ctx -> false);
        QuestInstance quest = buildAndTrackQuest("nomatch_test", type);

        listener.progressQuests(playerUUID, DUMMY_CONTEXT);

        QuestObjectiveInstance obj = quest.getActiveQuestStages().get(0).getQuestObjectives().get(0);
        assertEquals(0, obj.getCurrentProgression());
    }

    @DisplayName("Given an already-completed objective, when progressQuests is called, then no further progress is applied")
    @Test
    public void progressQuests_skipsCompletedObjectives() {
        MockQuestObjectiveType type = QuestTestHelper.mockObjectiveType("completed")
                .withCanProcess(ctx -> true)
                .withProgressFunction((inst, ctx) -> 1L);
        QuestInstance quest = buildAndTrackQuest("completed_test", type);

        QuestObjectiveInstance obj = quest.getActiveQuestStages().get(0).getQuestObjectives().get(0);
        obj.markAsComplete();
        assertEquals(QuestObjectiveState.COMPLETED, obj.getQuestObjectiveState());

        long progBefore = obj.getCurrentProgression();
        listener.progressQuests(playerUUID, DUMMY_CONTEXT);
        assertEquals(progBefore, obj.getCurrentProgression());
    }

    @DisplayName("Given a tracked quest with no registered definition, when progressQuests is called, then no crash")
    @Test
    public void progressQuests_skipsQuestsWithMissingDefinition() {
        MockQuestObjectiveType type = QuestTestHelper.mockObjectiveType("orphan")
                .withCanProcess(ctx -> true);

        QuestObjectiveDefinition objDef = QuestTestHelper.objectiveDef("orphan_obj", type, 10, List.of());
        QuestStageDefinition stageDef = QuestTestHelper.stageDef("orphan_stage", List.of(objDef), List.of());
        QuestPhaseDefinition phaseDef = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stageDef);
        QuestDefinition def = new QuestDefinition(
                new NamespacedKey("mcrpg", "orphan_quest"),
                new NamespacedKey("mcrpg", "single_player"),
                null, List.of(phaseDef), List.of(), QuestRepeatMode.ONCE, null, -1, null);

        QuestInstance instance = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
        trackedQuests.add(instance);
        // NOT adding def to registeredDefs so getQuestDefinition returns empty

        listener.progressQuests(playerUUID, DUMMY_CONTEXT);

        QuestObjectiveInstance obj = instance.getActiveQuestStages().get(0).getQuestObjectives().get(0);
        assertEquals(0, obj.getCurrentProgression());
    }

    @DisplayName("Given two active quests for the same player, when progressQuests is called, then both receive progress")
    @Test
    public void progressQuests_appliesProgressAcrossMultipleQuests() {
        MockQuestObjectiveType type1 = QuestTestHelper.mockObjectiveType("multi1")
                .withCanProcess(ctx -> true)
                .withProgressFunction((inst, ctx) -> 1L);
        MockQuestObjectiveType type2 = QuestTestHelper.mockObjectiveType("multi2")
                .withCanProcess(ctx -> true)
                .withProgressFunction((inst, ctx) -> 1L);

        QuestInstance q1 = buildAndTrackQuest("multi_quest_1", type1);
        QuestInstance q2 = buildAndTrackQuest("multi_quest_2", type2);

        listener.progressQuests(playerUUID, DUMMY_CONTEXT);

        assertTrue(q1.getActiveQuestStages().get(0).getQuestObjectives().get(0).getCurrentProgression() > 0);
        assertTrue(q2.getActiveQuestStages().get(0).getQuestObjectives().get(0).getCurrentProgression() > 0);
    }

    @DisplayName("Given a type returning zero delta, when progressQuests is called, then no progress is applied")
    @Test
    public void progressQuests_zeroDelta_doesNotApplyProgress() {
        MockQuestObjectiveType type = QuestTestHelper.mockObjectiveType("zero")
                .withCanProcess(ctx -> true)
                .withProgressFunction((inst, ctx) -> 0L);
        QuestInstance quest = buildAndTrackQuest("zero_test", type);

        listener.progressQuests(playerUUID, DUMMY_CONTEXT);

        QuestObjectiveInstance obj = quest.getActiveQuestStages().get(0).getQuestObjectives().get(0);
        assertEquals(0, obj.getCurrentProgression());
    }

    @DisplayName("Given a matching objective, when progressQuests is called, then QuestObjectiveProgressEvent fires")
    @Test
    public void progressQuests_firesProgressEvent() {
        MockQuestObjectiveType type = QuestTestHelper.mockObjectiveType("event_fire")
                .withCanProcess(ctx -> true)
                .withProgressFunction((inst, ctx) -> 1L);
        buildAndTrackQuest("event_fire_test", type);

        listener.progressQuests(playerUUID, DUMMY_CONTEXT);

        assertThat(server.getPluginManager(), hasFiredEventInstance(QuestObjectiveProgressEvent.class));
    }

    @DisplayName("Given a listener that cancels QuestObjectiveProgressEvent, then progress stays zero")
    @Test
    public void progressQuests_cancelledEvent_preventsProgress() {
        Listener cancellingListener = new Listener() {
            @EventHandler
            public void onProgress(QuestObjectiveProgressEvent event) {
                event.setCancelled(true);
            }
        };
        server.getPluginManager().registerEvents(cancellingListener, mcRPG);

        try {
            MockQuestObjectiveType type = QuestTestHelper.mockObjectiveType("cancel")
                    .withCanProcess(ctx -> true)
                    .withProgressFunction((inst, ctx) -> 1L);
            QuestInstance quest = buildAndTrackQuest("cancel_test", type);

            listener.progressQuests(playerUUID, DUMMY_CONTEXT);

            QuestObjectiveInstance obj = quest.getActiveQuestStages().get(0).getQuestObjectives().get(0);
            assertEquals(0, obj.getCurrentProgression());
        } finally {
            org.bukkit.event.HandlerList.unregisterAll(cancellingListener);
        }
    }
}
