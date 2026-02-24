package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.quest.QuestObjectiveCompleteEvent;
import us.eunoians.mcrpg.event.quest.QuestStageCompleteEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasFiredEventInstance;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasNotFiredEventInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class QuestObjectiveCompleteListenerTest extends McRPGBaseTest {

    private QuestDefinition definition;
    private QuestManager mockQuestManager;

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();
        server.getPluginManager().registerEvents(new QuestObjectiveCompleteListener(), mcRPG);
        definition = QuestTestHelper.singlePhaseQuest("obj_complete_test");

        mockQuestManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        when(mockQuestManager.getQuestDefinition(any(NamespacedKey.class))).thenReturn(Optional.of(definition));
    }

    @DisplayName("Given a stage with all objectives completed, when objective completes, then stage complete event fires")
    @Test
    public void onObjectiveComplete_firesStageComplete_whenAllObjectivesDone() {
        QuestInstance quest = QuestTestHelper.startedQuestInstance(definition);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        QuestObjectiveInstance objective = stage.getQuestObjectives().get(0);
        objective.markAsComplete();

        server.getPluginManager().callEvent(new QuestObjectiveCompleteEvent(quest, stage, objective));
        assertThat(server.getPluginManager(), hasFiredEventInstance(QuestStageCompleteEvent.class));
    }

    @DisplayName("Given a stage with some objectives incomplete, when objective completes, then stage does NOT complete")
    @Test
    public void onObjectiveComplete_doesNotFireStageComplete_whenSomeObjectivesRemain() {
        QuestDefinition multiObjDef = QuestTestHelper.singlePhaseQuest("multi_obj_test");
        when(mockQuestManager.getQuestDefinition(any(NamespacedKey.class))).thenReturn(Optional.of(multiObjDef));

        QuestInstance quest = QuestTestHelper.startedQuestInstance(multiObjDef);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);

        if (stage.getQuestObjectives().size() == 1) {
            return;
        }

        QuestObjectiveInstance objective = stage.getQuestObjectives().get(0);
        objective.markAsComplete();

        server.getPluginManager().clearEvents();
        server.getPluginManager().callEvent(new QuestObjectiveCompleteEvent(quest, stage, objective));
        assertThat(server.getPluginManager(), hasNotFiredEventInstance(QuestStageCompleteEvent.class));
    }
}
