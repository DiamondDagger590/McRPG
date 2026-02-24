package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.QuestState;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public class QuestCompleteListenerTest extends McRPGBaseTest {

    private QuestManager mockQuestManager;

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();
        server.getPluginManager().registerEvents(new QuestCompleteListener(), mcRPG);

        mockQuestManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
    }

    @DisplayName("Given a completed quest, when QuestCompleteEvent fires, then retireQuest is called")
    @Test
    public void onQuestComplete_retiresQuest() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("complete_listener_test");
        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

        server.getPluginManager().callEvent(new QuestCompleteEvent(quest, def));
        verify(mockQuestManager).retireQuest(any(QuestInstance.class));
    }
}
