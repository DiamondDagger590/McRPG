package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.quest.QuestStartEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.MockQuestObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuestStartAutoCompleteListenerTest extends McRPGBaseTest {

    private QuestManager mockQuestManager;

    @BeforeEach
    public void setup() {
        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        mockQuestManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        server.getPluginManager().registerEvents(new QuestStartAutoCompleteListener(mockQuestManager), mcRPG);
    }

    @Test
    @DisplayName("Given a QuestStartEvent with null starterUUID, when fired, then no objectives are progressed")
    public void onQuestStart_doesNothing_whenStarterUUIDIsNull() {
        MockQuestObjectiveType baseType = QuestTestHelper.mockObjectiveType("auto_null_type");
        QuestObjectiveType type = new AutoCompleteWrappingType(baseType, OptionalLong.of(100));

        QuestObjectiveDefinition objDef = QuestTestHelper.objectiveDef("auto_null_obj", type, 10, List.of());
        QuestStageDefinition stageDef = QuestTestHelper.stageDef("auto_null_stage", List.of(objDef), List.of());
        QuestPhaseDefinition phaseDef = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stageDef);
        QuestDefinition def = new QuestDefinition.Builder(
                new NamespacedKey("mcrpg", "auto_null_quest"),
                new NamespacedKey("mcrpg", "single_player"),
                List.of(phaseDef)
        ).build();

        UUID playerUUID = UUID.randomUUID();
        QuestInstance instance = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);

        server.getPluginManager().callEvent(new QuestStartEvent(instance, def, new ManualQuestSource(), null));

        QuestObjectiveInstance obj = instance.getActiveQuestStages().get(0).getQuestObjectives().get(0);
        assertEquals(0, obj.getCurrentProgression());
    }

    @Test
    @DisplayName("Given a QuestStartEvent with starterUUID and checkAutoComplete returning a value meeting requiredProgress, when fired, then the objective is progressed to requiredProgress")
    public void onQuestStart_progressesObjective_whenAutoCompleteValueMeetsRequired() {
        MockQuestObjectiveType baseType = QuestTestHelper.mockObjectiveType("auto_success_type");
        QuestObjectiveType type = new AutoCompleteWrappingType(baseType, OptionalLong.of(10));

        QuestObjectiveDefinition objDef = QuestTestHelper.objectiveDef("auto_success_obj", type, 10, List.of());
        QuestStageDefinition stageDef = QuestTestHelper.stageDef("auto_success_stage", List.of(objDef), List.of());
        QuestPhaseDefinition phaseDef = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stageDef);
        QuestDefinition def = new QuestDefinition.Builder(
                new NamespacedKey("mcrpg", "auto_success_quest"),
                new NamespacedKey("mcrpg", "single_player"),
                List.of(phaseDef)
        ).build();

        UUID playerUUID = UUID.randomUUID();
        QuestInstance instance = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
        QuestObjectiveInstance obj = instance.getActiveQuestStages().get(0).getQuestObjectives().get(0);

        server.getPluginManager().callEvent(new QuestStartEvent(instance, def, new ManualQuestSource(), playerUUID));

        assertEquals(10, obj.getCurrentProgression());
    }

    @Test
    @DisplayName("Given a QuestStartEvent with starterUUID, when objective type returns empty from checkAutoComplete, then no objectives are progressed")
    public void onQuestStart_doesNotAutoComplete_whenObjectiveTypeReturnsEmpty() {
        MockQuestObjectiveType type = QuestTestHelper.mockObjectiveType("auto_empty_type");

        QuestObjectiveDefinition objDef = QuestTestHelper.objectiveDef("auto_empty_obj", type, 10, List.of());
        QuestStageDefinition stageDef = QuestTestHelper.stageDef("auto_empty_stage", List.of(objDef), List.of());
        QuestPhaseDefinition phaseDef = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stageDef);
        QuestDefinition def = new QuestDefinition.Builder(
                new NamespacedKey("mcrpg", "auto_empty_quest"),
                new NamespacedKey("mcrpg", "single_player"),
                List.of(phaseDef)
        ).build();

        UUID playerUUID = UUID.randomUUID();
        QuestInstance instance = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);

        server.getPluginManager().callEvent(new QuestStartEvent(instance, def, new ManualQuestSource(), playerUUID));

        QuestObjectiveInstance obj = instance.getActiveQuestStages().get(0).getQuestObjectives().get(0);
        assertEquals(0, obj.getCurrentProgression());
    }

    /**
     * Delegating wrapper that overrides {@link #checkAutoComplete} to return a fixed value,
     * while delegating all other behavior to an underlying {@link MockQuestObjectiveType}.
     */
    private static final class AutoCompleteWrappingType extends MockQuestObjectiveType {

        private final OptionalLong autoCompleteResult;

        AutoCompleteWrappingType(@NotNull MockQuestObjectiveType delegate, @NotNull OptionalLong autoCompleteResult) {
            super(delegate.getKey(), delegate.getExpansionKey().orElseThrow());
            this.autoCompleteResult = autoCompleteResult;
        }

        @NotNull
        @Override
        public OptionalLong checkAutoComplete(@NotNull UUID playerUUID) {
            return autoCompleteResult;
        }
    }
}
